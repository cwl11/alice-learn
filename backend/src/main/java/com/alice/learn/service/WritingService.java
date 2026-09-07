package com.alice.learn.service;

import com.alice.learn.common.BusinessException;
import com.alice.learn.common.PageResult;
import com.alice.learn.common.WordCountUtils;
import com.alice.learn.dto.EssayDetailResponse;
import com.alice.learn.dto.EssayReviewResponse;
import com.alice.learn.dto.EssaySaveRequest;
import com.alice.learn.dto.EssaySummaryResponse;
import com.alice.learn.dto.EssayUpdateRequest;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        int wordCount = checkWordCount(task, request.getContent(), request.isSubmit());
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

    /** 更新同一篇作文；已批改的作文冻结，不允许再改。 */
    public Essay updateEssay(Long userId, Long essayId, EssayUpdateRequest request) {
        Essay essay = requireOwnedEssay(userId, essayId);
        if ("REVIEWED".equals(essay.getStatus())) {
            throw new BusinessException("该作文已批改，不能再修改；请重新开始一篇");
        }
        WritingTask task = getTask(essay.getTaskId());
        int wordCount = checkWordCount(task, request.getContent(), request.isSubmit());
        essay.setContent(request.getContent());
        essay.setWordCount(wordCount);
        if (request.isSubmit()) {
            essay.setStatus("SUBMITTED");
        }
        essay.setUpdatedAt(LocalDateTime.now());
        essayMapper.updateById(essay);
        return essay;
    }

    /** 该题目下用户最近一篇未批改的作文（草稿或已提交），用于编辑页回填。 */
    public Essay latestEditableEssay(Long userId, Long taskId) {
        getTask(taskId);
        return essayMapper.selectOne(new LambdaQueryWrapper<Essay>()
                .eq(Essay::getUserId, userId)
                .eq(Essay::getTaskId, taskId)
                .ne(Essay::getStatus, "REVIEWED")
                .orderByDesc(Essay::getUpdatedAt)
                .orderByDesc(Essay::getId)
                .last("LIMIT 1"));
    }

    public void deleteEssay(Long userId, Long essayId) {
        requireOwnedEssay(userId, essayId);
        essayReviewMapper.delete(new LambdaQueryWrapper<EssayReview>().eq(EssayReview::getEssayId, essayId));
        essayMapper.deleteById(essayId);
    }

    public void saveSampleEssay(Long essayId, String sample) {
        Essay essay = essayMapper.selectById(essayId);
        if (essay == null) {
            return;
        }
        essay.setSampleEssay(sample);
        essay.setUpdatedAt(LocalDateTime.now());
        essayMapper.updateById(essay);
    }

    private int checkWordCount(WritingTask task, String content, boolean submit) {
        int wordCount = WordCountUtils.count(content);
        if (submit) {
            int min = "TASK_1".equals(task.getTaskType()) ? 150 : 250;
            if (wordCount < min) {
                throw new BusinessException("作文字数不足，" + task.getTaskType() + " 至少 " + min + " 词");
            }
        }
        return wordCount;
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
        response.setSampleEssay(essay.getSampleEssay());
        response.setCreatedAt(essay.getCreatedAt());
        response.setUpdatedAt(essay.getUpdatedAt());
        EssayReview review = essayReviewMapper.selectOne(new LambdaQueryWrapper<EssayReview>()
                .eq(EssayReview::getEssayId, essayId)
                .orderByDesc(EssayReview::getId)
                .last("LIMIT 1"));
        if (review != null) {
            response.setReview(toReviewResponse(review));
        }
        return response;
    }

    public PageResult<EssaySummaryResponse> myEssays(Long userId, int page, int size, String status) {
        Page<Essay> pager = new Page<>(page, size);
        Page<Essay> result = essayMapper.selectPage(pager, new LambdaQueryWrapper<Essay>()
                .eq(Essay::getUserId, userId)
                .eq(status != null && !status.isBlank(), Essay::getStatus, status)
                .orderByDesc(Essay::getUpdatedAt)
                .orderByDesc(Essay::getId));
        List<Essay> essays = result.getRecords();
        if (essays.isEmpty()) {
            return new PageResult<>(result.getTotal(), List.of());
        }

        Set<Long> taskIds = essays.stream().map(Essay::getTaskId).collect(Collectors.toSet());
        Map<Long, WritingTask> taskMap = writingTaskMapper.selectBatchIds(taskIds).stream()
                .collect(Collectors.toMap(WritingTask::getId, Function.identity()));

        List<Long> essayIds = essays.stream().map(Essay::getId).toList();
        Map<Long, BigDecimal> latestScore = new HashMap<>();
        essayReviewMapper.selectList(new LambdaQueryWrapper<EssayReview>()
                        .select(EssayReview::getEssayId, EssayReview::getOverallScore)
                        .in(EssayReview::getEssayId, essayIds)
                        .orderByDesc(EssayReview::getId))
                .forEach(review -> latestScore.putIfAbsent(review.getEssayId(), review.getOverallScore()));

        List<EssaySummaryResponse> records = essays.stream().map(essay -> {
            EssaySummaryResponse item = new EssaySummaryResponse();
            item.setId(essay.getId());
            item.setTaskId(essay.getTaskId());
            WritingTask task = taskMap.get(essay.getTaskId());
            item.setTaskTitle(task == null ? "题目已删除" : task.getTitle());
            item.setTaskType(task == null ? null : task.getTaskType());
            item.setWordCount(essay.getWordCount());
            item.setStatus(essay.getStatus());
            item.setOverallScore(latestScore.get(essay.getId()));
            item.setCreatedAt(essay.getCreatedAt());
            item.setUpdatedAt(essay.getUpdatedAt());
            return item;
        }).toList();
        return new PageResult<>(result.getTotal(), records);
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
