package com.alice.learn.service;

import com.alice.learn.common.BusinessException;
import com.alice.learn.common.PageResult;
import com.alice.learn.dto.ReadingPassageResponse;
import com.alice.learn.dto.ReadingQuestionResponse;
import com.alice.learn.dto.SubmitAnswersRequest;
import com.alice.learn.dto.SubmitAnswersResponse;
import com.alice.learn.entity.ReadingPassage;
import com.alice.learn.entity.ReadingQuestion;
import com.alice.learn.entity.UserAnswer;
import com.alice.learn.mapper.ReadingPassageMapper;
import com.alice.learn.mapper.ReadingQuestionMapper;
import com.alice.learn.mapper.UserAnswerMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReadingService {

    private final ReadingPassageMapper passageMapper;
    private final ReadingQuestionMapper questionMapper;
    private final UserAnswerMapper userAnswerMapper;
    private final ObjectMapper objectMapper;

    public ReadingService(
            ReadingPassageMapper passageMapper,
            ReadingQuestionMapper questionMapper,
            UserAnswerMapper userAnswerMapper,
            ObjectMapper objectMapper) {
        this.passageMapper = passageMapper;
        this.questionMapper = questionMapper;
        this.userAnswerMapper = userAnswerMapper;
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

    public SubmitAnswersResponse submit(Long userId, SubmitAnswersRequest request) {
        List<Long> ids = request.getAnswers().stream()
                .map(SubmitAnswersRequest.AnswerItem::getQuestionId)
                .toList();
        Map<Long, ReadingQuestion> questionMap = questionMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(ReadingQuestion::getId, Function.identity()));

        SubmitAnswersResponse response = new SubmitAnswersResponse();
        List<SubmitAnswersResponse.Item> items = new ArrayList<>();
        int correctCount = 0;
        for (SubmitAnswersRequest.AnswerItem answerItem : request.getAnswers()) {
            ReadingQuestion question = questionMap.get(answerItem.getQuestionId());
            if (question == null) {
                throw new BusinessException(404, "题目不存在: " + answerItem.getQuestionId());
            }
            boolean correct = isCorrect(question.getAnswer(), answerItem.getAnswer());
            if (correct) {
                correctCount++;
            }
            UserAnswer userAnswer = new UserAnswer();
            userAnswer.setUserId(userId);
            userAnswer.setQuestionId(question.getId());
            userAnswer.setAnswer(answerItem.getAnswer());
            userAnswer.setIsCorrect(correct);
            userAnswer.setCreatedAt(LocalDateTime.now());
            userAnswerMapper.insert(userAnswer);

            SubmitAnswersResponse.Item item = new SubmitAnswersResponse.Item();
            item.setQuestionId(question.getId());
            item.setUserAnswer(answerItem.getAnswer());
            item.setCorrectAnswer(question.getAnswer());
            item.setCorrect(correct);
            item.setExplanation(question.getExplanation());
            items.add(item);
        }
        response.setTotal(items.size());
        response.setCorrectCount(correctCount);
        response.setItems(items);
        return response;
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
