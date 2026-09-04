package com.alice.learn.service;

import com.alice.learn.common.BusinessException;
import com.alice.learn.common.PageResult;
import com.alice.learn.common.WordCountUtils;
import com.alice.learn.dto.EssayDetailResponse;
import com.alice.learn.dto.EssayReviewResponse;
import com.alice.learn.dto.EssaySaveRequest;
import com.alice.learn.entity.Essay;
import com.alice.learn.entity.EssayReview;
import com.alice.learn.entity.WritingTask;
import com.alice.learn.mapper.EssayMapper;
import com.alice.learn.mapper.EssayReviewMapper;
import com.alice.learn.mapper.WritingTaskMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class WritingService {

    private final WritingTaskMapper writingTaskMapper;
    private final EssayMapper essayMapper;
    private final EssayReviewMapper essayReviewMapper;
    private final ObjectMapper objectMapper;

    public WritingService(
            WritingTaskMapper writingTaskMapper,
            EssayMapper essayMapper,
            EssayReviewMapper essayReviewMapper,
            ObjectMapper objectMapper) {
        this.writingTaskMapper = writingTaskMapper;
        this.essayMapper = essayMapper;
        this.essayReviewMapper = essayReviewMapper;
        this.objectMapper = objectMapper;
    }

    public PageResult<WritingTask> listTasks(int page, int size, String taskType) {
        Page<WritingTask> pager = new Page<>(page, size);
        LambdaQueryWrapper<WritingTask> wrapper = new LambdaQueryWrapper<WritingTask>()
                .eq(taskType != null && !taskType.isBlank(), WritingTask::getTaskType, taskType)
                .orderByAsc(WritingTask::getId);
        Page<WritingTask> result = writingTaskMapper.selectPage(pager, wrapper);
        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    public WritingTask getTask(Long id) {
        WritingTask task = writingTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(404, "题目不存在");
        }
        return task;
    }

    public Essay saveEssay(Long userId, EssaySaveRequest request) {
        WritingTask task = getTask(request.getTaskId());
        int wordCount = WordCountUtils.count(request.getContent());
        if (request.isSubmit()) {
            int min = "TASK_1".equals(task.getTaskType()) ? 150 : 250;
            if (wordCount < min) {
                throw new BusinessException("作文字数不足，" + task.getTaskType() + " 至少 " + min + " 词");
            }
        }
        Essay essay = new Essay();
        essay.setUserId(userId);
        essay.setTaskId(request.getTaskId());
        essay.setContent(request.getContent());
        essay.setWordCount(wordCount);
        essay.setStatus(request.isSubmit() ? "SUBMITTED" : "DRAFT");
        essay.setCreatedAt(LocalDateTime.now());
        essay.setUpdatedAt(LocalDateTime.now());
        essayMapper.insert(essay);
        return essay;
    }

    public Essay requireOwnedEssay(Long userId, Long essayId) {
        Essay essay = essayMapper.selectById(essayId);
        if (essay == null) {
            throw new BusinessException(404, "作文不存在");
        }
        if (!essay.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权访问该作文");
        }
        return essay;
    }

    public EssayDetailResponse getEssayDetail(Long userId, Long essayId) {
        Essay essay = requireOwnedEssay(userId, essayId);
        WritingTask task = getTask(essay.getTaskId());
        EssayDetailResponse response = new EssayDetailResponse();
        response.setId(essay.getId());
        response.setTaskId(task.getId());
        response.setTaskTitle(task.getTitle());
        response.setTaskType(task.getTaskType());
        response.setTaskDescription(task.getDescription());
        response.setContent(essay.getContent());
        response.setWordCount(essay.getWordCount());
        response.setStatus(essay.getStatus());
        response.setCreatedAt(essay.getCreatedAt());
        EssayReview review = essayReviewMapper.selectOne(new LambdaQueryWrapper<EssayReview>()
                .eq(EssayReview::getEssayId, essayId)
                .orderByDesc(EssayReview::getId)
                .last("LIMIT 1"));
        if (review != null) {
            response.setReview(toReviewResponse(review));
        }
        return response;
    }

    public PageResult<Essay> myEssays(Long userId, int page, int size) {
        Page<Essay> pager = new Page<>(page, size);
        Page<Essay> result = essayMapper.selectPage(pager, new LambdaQueryWrapper<Essay>()
                .eq(Essay::getUserId, userId)
                .orderByDesc(Essay::getId));
        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    public EssayReviewResponse toReviewResponse(EssayReview review) {
        EssayReviewResponse response = new EssayReviewResponse();
        response.setId(review.getId());
        response.setOverallScore(review.getOverallScore());
        response.setTaScore(review.getTaScore());
        response.setCcScore(review.getCcScore());
        response.setLrScore(review.getLrScore());
        response.setGraScore(review.getGraScore());
        response.setComment(review.getComment());
        response.setSampleEssay(review.getSampleEssay());
        response.setCreatedAt(review.getCreatedAt());
        response.setFeedback(parseFeedback(review.getFeedbackJson()));
        return response;
    }

    private List<EssayReviewResponse.FeedbackItem> parseFeedback(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }
}
