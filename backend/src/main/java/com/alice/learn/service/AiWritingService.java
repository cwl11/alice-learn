package com.alice.learn.service;

import com.alice.learn.common.BusinessException;
import com.alice.learn.common.IeltsScoreUtils;
import com.alice.learn.dto.AiReviewResult;
import com.alice.learn.dto.EssayReviewResponse;
import com.alice.learn.entity.AiCallLog;
import com.alice.learn.entity.Essay;
import com.alice.learn.entity.EssayReview;
import com.alice.learn.entity.WritingTask;
import com.alice.learn.mapper.AiCallLogMapper;
import com.alice.learn.mapper.EssayMapper;
import com.alice.learn.mapper.EssayReviewMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

@Service
public class AiWritingService {

    private final ChatClient chatClient;
    private final WritingService writingService;
    private final EssayMapper essayMapper;
    private final EssayReviewMapper essayReviewMapper;
    private final AiCallLogMapper aiCallLogMapper;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public AiWritingService(
            ChatClient chatClient,
            WritingService writingService,
            EssayMapper essayMapper,
            EssayReviewMapper essayReviewMapper,
            AiCallLogMapper aiCallLogMapper,
            ObjectMapper objectMapper,
            @Value("${spring.ai.openai.api-key}") String apiKey) {
        this.chatClient = chatClient;
        this.writingService = writingService;
        this.essayMapper = essayMapper;
        this.essayReviewMapper = essayReviewMapper;
        this.aiCallLogMapper = aiCallLogMapper;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
    }

    public SseEmitter streamSample(Long userId, Long essayId) {
        ensureConfigured();
        Essay essay = writingService.requireOwnedEssay(userId, essayId);
        WritingTask task = writingService.getTask(essay.getTaskId());
        String prompt = """
                You are an IELTS writing examiner and teacher.
                Write a band 7.5 model essay for the following IELTS %s question.
                Use academic English, a clear structure, and about %s words.
                Output the essay only, no commentary.

                Question title: %s
                Question: %s
                """.formatted(
                task.getTaskType(),
                "TASK_1".equals(task.getTaskType()) ? "170-190" : "270-300",
                task.getTitle(),
                task.getDescription());

        SseEmitter emitter = new SseEmitter(180_000L);
        chatClient.prompt()
                .user(prompt)
                .stream()
                .content()
                .publishOn(Schedulers.boundedElastic())
                .subscribe(
                        chunk -> {
                            try {
                                emitter.send(SseEmitter.event().data(chunk, MediaType.TEXT_PLAIN));
                            } catch (Exception ex) {
                                emitter.completeWithError(ex);
                            }
                        },
                        error -> {
                            try {
                                emitter.send(SseEmitter.event().name("error").data(error.getMessage()));
                            } catch (Exception ignored) {
                                // ignore
                            }
                            emitter.completeWithError(error);
                        },
                        () -> {
                            logCall(userId, "GENERATE_SAMPLE", null, null);
                            emitter.complete();
                        });
        return emitter;
    }

    public EssayReviewResponse reviewEssay(Long userId, Long essayId) {
        ensureConfigured();
        Essay essay = writingService.requireOwnedEssay(userId, essayId);
        WritingTask task = writingService.getTask(essay.getTaskId());
        String prompt = """
                You are a strict IELTS writing examiner.
                Score the essay using IELTS bands 0-9 in 0.5 increments for TA, CC, LR, GRA.
                Return JSON only, matching this schema:
                {
                  "taScore": 6.5,
                  "ccScore": 6.5,
                  "lrScore": 6.0,
                  "graScore": 6.5,
                  "comment": "overall comment in Chinese",
                  "sampleEssay": "a short improved paragraph",
                  "feedback": [
                    {"original": "weak sentence", "suggestion": "improved sentence", "reason": "why in Chinese"}
                  ]
                }
                Give 3 to 6 feedback items.

                Task type: %s
                Question: %s
                Essay: %s
                """.formatted(task.getTaskType(), task.getDescription(), essay.getContent());

        ChatResponse response = chatClient.prompt()
                .user(prompt)
                .call()
                .chatResponse();
        String content = response == null || response.getResult() == null
                ? null
                : response.getResult().getOutput().getText();
        if (content == null || content.isBlank()) {
            throw new BusinessException(502, "AI 未返回批改结果，请稍后重试");
        }
        AiReviewResult result;
        try {
            result = objectMapper.readValue(stripMarkdown(content), AiReviewResult.class);
        } catch (Exception ex) {
            throw new BusinessException(502, "AI 批改结果解析失败，请重试");
        }
        if (result.getTaScore() == null || result.getCcScore() == null
                || result.getLrScore() == null || result.getGraScore() == null) {
            throw new BusinessException(502, "AI 批改缺少分数，请重试");
        }

        EssayReview review = new EssayReview();
        review.setEssayId(essayId);
        review.setTaScore(IeltsScoreUtils.roundToHalfBand(result.getTaScore().doubleValue()));
        review.setCcScore(IeltsScoreUtils.roundToHalfBand(result.getCcScore().doubleValue()));
        review.setLrScore(IeltsScoreUtils.roundToHalfBand(result.getLrScore().doubleValue()));
        review.setGraScore(IeltsScoreUtils.roundToHalfBand(result.getGraScore().doubleValue()));
        review.setOverallScore(IeltsScoreUtils.overall(
                review.getTaScore(), review.getCcScore(), review.getLrScore(), review.getGraScore()));
        review.setComment(result.getComment());
        review.setSampleEssay(result.getSampleEssay());
        review.setCreatedAt(LocalDateTime.now());
        try {
            review.setFeedbackJson(objectMapper.writeValueAsString(
                    result.getFeedback() == null ? java.util.List.of() : result.getFeedback()));
        } catch (Exception ex) {
            review.setFeedbackJson("[]");
        }
        essayReviewMapper.insert(review);

        essay.setStatus("REVIEWED");
        essay.setUpdatedAt(LocalDateTime.now());
        essayMapper.updateById(essay);
        logCall(userId, "REVIEW_ESSAY", null, null);
        return writingService.toReviewResponse(review);
    }

    private void ensureConfigured() {
        if (apiKey == null || apiKey.isBlank() || "sk-not-configured".equals(apiKey)) {
            throw new BusinessException(503, "尚未配置 DEEPSEEK_API_KEY，无法调用 AI 功能");
        }
    }

    private void logCall(Long userId, String scene, Integer promptTokens, Integer completionTokens) {
        AiCallLog log = new AiCallLog();
        log.setUserId(userId);
        log.setScene(scene);
        log.setPromptTokens(promptTokens);
        log.setCompletionTokens(completionTokens);
        log.setCreatedAt(LocalDateTime.now());
        aiCallLogMapper.insert(log);
    }

    static String stripMarkdown(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf('\n');
            int end = trimmed.lastIndexOf("```");
            if (start > 0 && end > start) {
                return trimmed.substring(start + 1, end).trim();
            }
        }
        int firstBrace = trimmed.indexOf('{');
        int lastBrace = trimmed.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1);
        }
        return trimmed;
    }
}
