package com.alice.learn.service;

import com.alice.learn.common.BusinessException;
import com.alice.learn.common.PageResult;
import com.alice.learn.dto.ReadingAttemptResponse;
import com.alice.learn.dto.ReadingPassageResponse;
import com.alice.learn.dto.ReadingQuestionResponse;
import com.alice.learn.dto.SubmitAnswersRequest;
import com.alice.learn.dto.SubmitAnswersResponse;
import com.alice.learn.entity.ReadingAttempt;
import com.alice.learn.entity.ReadingPassage;
import com.alice.learn.entity.ReadingQuestion;
import com.alice.learn.entity.UserAnswer;
import com.alice.learn.mapper.ReadingAttemptMapper;
import com.alice.learn.mapper.ReadingPassageMapper;
import com.alice.learn.mapper.ReadingQuestionMapper;
import com.alice.learn.mapper.UserAnswerMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReadingService {

    private final ReadingPassageMapper passageMapper;
    private final ReadingQuestionMapper questionMapper;
    private final UserAnswerMapper userAnswerMapper;
    private final ReadingAttemptMapper attemptMapper;
    private final ObjectMapper objectMapper;

    public ReadingService(
            ReadingPassageMapper passageMapper,
            ReadingQuestionMapper questionMapper,
            UserAnswerMapper userAnswerMapper,
            ReadingAttemptMapper attemptMapper,
            ObjectMapper objectMapper) {
        this.passageMapper = passageMapper;
        this.questionMapper = questionMapper;
        this.userAnswerMapper = userAnswerMapper;
        this.attemptMapper = attemptMapper;
        this.objectMapper = objectMapper;
    }

    public PageResult<ReadingPassage> listPassages(int page, int size) {
        Page<ReadingPassage> pager = new Page<>(page, size);
        Page<ReadingPassage> result = passageMapper.selectPage(pager, new LambdaQueryWrapper<ReadingPassage>()
                .select(ReadingPassage::getId, ReadingPassage::getTitle, ReadingPassage::getDifficulty)
                .orderByAsc(ReadingPassage::getId));
        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    public ReadingPassageResponse getPassage(Long id, boolean includeAnswers) {
        ReadingPassage passage = passageMapper.selectById(id);
        if (passage == null) {
            throw new BusinessException(404, "阅读文章不存在");
        }
        List<ReadingQuestion> questions = questionMapper.selectList(new LambdaQueryWrapper<ReadingQuestion>()
                .eq(ReadingQuestion::getPassageId, id)
                .orderByAsc(ReadingQuestion::getId));
        ReadingPassageResponse response = new ReadingPassageResponse();
        response.setId(passage.getId());
        response.setTitle(passage.getTitle());
        response.setContent(passage.getContent());
        response.setDifficulty(passage.getDifficulty());
        response.setQuestions(questions.stream()
                .map(question -> toQuestion(question, includeAnswers))
                .toList());
        return response;
    }

    @Transactional
    public SubmitAnswersResponse submit(Long userId, SubmitAnswersRequest request) {
        ReadingPassage passage = passageMapper.selectById(request.getPassageId());
        if (passage == null) {
            throw new BusinessException(404, "阅读文章不存在");
        }
        Map<Long, ReadingQuestion> questionMap = questionMapper.selectList(new LambdaQueryWrapper<ReadingQuestion>()
                        .eq(ReadingQuestion::getPassageId, passage.getId()))
                .stream()
                .collect(Collectors.toMap(ReadingQuestion::getId, Function.identity()));

        Set<Long> seen = new HashSet<>();
        for (SubmitAnswersRequest.AnswerItem answerItem : request.getAnswers()) {
            if (!questionMap.containsKey(answerItem.getQuestionId())) {
                throw new BusinessException(400, "题目 " + answerItem.getQuestionId() + " 不属于该文章");
            }
            if (!seen.add(answerItem.getQuestionId())) {
                throw new BusinessException(400, "题目 " + answerItem.getQuestionId() + " 重复提交");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        List<SubmitAnswersResponse.Item> items = new ArrayList<>();
        int correctCount = 0;
        for (SubmitAnswersRequest.AnswerItem answerItem : request.getAnswers()) {
            ReadingQuestion question = questionMap.get(answerItem.getQuestionId());
            boolean correct = isCorrect(question.getAnswer(), answerItem.getAnswer());
            if (correct) {
                correctCount++;
            }
            items.add(toItem(question, answerItem.getAnswer(), correct));
        }

        ReadingAttempt attempt = new ReadingAttempt();
        attempt.setUserId(userId);
        attempt.setPassageId(passage.getId());
        attempt.setTotal(items.size());
        attempt.setCorrectCount(correctCount);
        attempt.setTimeSpentSec(request.getTimeSpentSec());
        attempt.setCreatedAt(now);
        attemptMapper.insert(attempt);

        for (SubmitAnswersResponse.Item item : items) {
            UserAnswer userAnswer = new UserAnswer();
            userAnswer.setUserId(userId);
            userAnswer.setQuestionId(item.getQuestionId());
            userAnswer.setAttemptId(attempt.getId());
            userAnswer.setAnswer(item.getUserAnswer());
            userAnswer.setIsCorrect(item.isCorrect());
            userAnswer.setCreatedAt(now);
            userAnswerMapper.insert(userAnswer);
        }

        SubmitAnswersResponse response = new SubmitAnswersResponse();
        response.setAttemptId(attempt.getId());
        response.setTotal(items.size());
        response.setCorrectCount(correctCount);
        response.setItems(items);
        return response;
    }

    public PageResult<ReadingAttemptResponse> myAttempts(Long userId, int page, int size) {
        Page<ReadingAttempt> pager = new Page<>(page, size);
        Page<ReadingAttempt> result = attemptMapper.selectPage(pager, new LambdaQueryWrapper<ReadingAttempt>()
                .eq(ReadingAttempt::getUserId, userId)
                .orderByDesc(ReadingAttempt::getId));
        List<ReadingAttempt> attempts = result.getRecords();
        if (attempts.isEmpty()) {
            return new PageResult<>(result.getTotal(), List.of());
        }
        Set<Long> passageIds = attempts.stream().map(ReadingAttempt::getPassageId).collect(Collectors.toSet());
        Map<Long, ReadingPassage> passageMap = passageMapper.selectList(new LambdaQueryWrapper<ReadingPassage>()
                        .select(ReadingPassage::getId, ReadingPassage::getTitle, ReadingPassage::getDifficulty)
                        .in(ReadingPassage::getId, passageIds))
                .stream()
                .collect(Collectors.toMap(ReadingPassage::getId, Function.identity()));
        List<ReadingAttemptResponse> records = attempts.stream()
                .map(attempt -> toAttemptResponse(attempt, passageMap.get(attempt.getPassageId())))
                .toList();
        return new PageResult<>(result.getTotal(), records);
    }

    public ReadingAttemptResponse getAttempt(Long userId, Long attemptId) {
        ReadingAttempt attempt = attemptMapper.selectById(attemptId);
        if (attempt == null) {
            throw new BusinessException(404, "练习记录不存在");
        }
        if (!attempt.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权访问该练习记录");
        }
        ReadingPassage passage = passageMapper.selectById(attempt.getPassageId());
        ReadingAttemptResponse response = toAttemptResponse(attempt, passage);

        List<UserAnswer> answers = userAnswerMapper.selectList(new LambdaQueryWrapper<UserAnswer>()
                .eq(UserAnswer::getAttemptId, attemptId)
                .orderByAsc(UserAnswer::getId));
        Map<Long, ReadingQuestion> questionMap = questionMapper.selectList(new LambdaQueryWrapper<ReadingQuestion>()
                        .eq(ReadingQuestion::getPassageId, attempt.getPassageId()))
                .stream()
                .collect(Collectors.toMap(ReadingQuestion::getId, Function.identity()));
        response.setItems(answers.stream()
                .filter(answer -> questionMap.containsKey(answer.getQuestionId()))
                .map(answer -> toItem(questionMap.get(answer.getQuestionId()), answer.getAnswer(),
                        Boolean.TRUE.equals(answer.getIsCorrect())))
                .toList());
        return response;
    }

    private ReadingAttemptResponse toAttemptResponse(ReadingAttempt attempt, ReadingPassage passage) {
        ReadingAttemptResponse response = new ReadingAttemptResponse();
        response.setId(attempt.getId());
        response.setPassageId(attempt.getPassageId());
        response.setPassageTitle(passage == null ? "文章已删除" : passage.getTitle());
        response.setDifficulty(passage == null ? null : passage.getDifficulty());
        response.setTotal(attempt.getTotal());
        response.setCorrectCount(attempt.getCorrectCount());
        response.setTimeSpentSec(attempt.getTimeSpentSec());
        response.setCreatedAt(attempt.getCreatedAt());
        return response;
    }

    private SubmitAnswersResponse.Item toItem(ReadingQuestion question, String userAnswer, boolean correct) {
        SubmitAnswersResponse.Item item = new SubmitAnswersResponse.Item();
        item.setQuestionId(question.getId());
        item.setUserAnswer(userAnswer);
        item.setCorrectAnswer(question.getAnswer());
        item.setCorrect(correct);
        item.setExplanation(question.getExplanation());
        return item;
    }

    static boolean isCorrect(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return expected.trim().equalsIgnoreCase(actual.trim());
    }

    private ReadingQuestionResponse toQuestion(ReadingQuestion question, boolean includeAnswers) {
        ReadingQuestionResponse response = new ReadingQuestionResponse();
        response.setId(question.getId());
        response.setQuestionType(question.getQuestionType());
        response.setQuestion(question.getQuestion());
        response.setOptions(parseOptions(question.getOptionsJson()));
        if (includeAnswers) {
            response.setAnswer(question.getAnswer());
            response.setExplanation(question.getExplanation());
        }
        return response;
    }

    private List<String> parseOptions(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return List.of();
        }
    }
}
