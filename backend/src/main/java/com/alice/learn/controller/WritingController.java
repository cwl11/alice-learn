package com.alice.learn.controller;

import com.alice.learn.common.PageResult;
import com.alice.learn.common.Result;
import com.alice.learn.common.SecurityUtils;
import com.alice.learn.dto.EssayDetailResponse;
import com.alice.learn.dto.EssayReviewResponse;
import com.alice.learn.dto.EssaySaveRequest;
import com.alice.learn.entity.Essay;
import com.alice.learn.entity.WritingTask;
import com.alice.learn.service.AiWritingService;
import com.alice.learn.service.WritingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "写作")
@RestController
@RequestMapping("/api/writing")
public class WritingController {

    private final WritingService writingService;
    private final AiWritingService aiWritingService;

    public WritingController(WritingService writingService, AiWritingService aiWritingService) {
        this.writingService = writingService;
        this.aiWritingService = aiWritingService;
    }

    @Operation(summary = "写作题库")
    @GetMapping("/tasks")
    public Result<PageResult<WritingTask>> tasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String taskType) {
        return Result.ok(writingService.listTasks(page, size, taskType));
    }

    @Operation(summary = "题目详情")
    @GetMapping("/tasks/{id}")
    public Result<WritingTask> task(@PathVariable Long id) {
        return Result.ok(writingService.getTask(id));
    }

    @Operation(summary = "保存或提交作文")
    @PostMapping("/essays")
    public Result<Essay> save(@Valid @RequestBody EssaySaveRequest request) {
        return Result.ok(writingService.saveEssay(SecurityUtils.requireUserId(), request));
    }

    @Operation(summary = "作文详情")
    @GetMapping("/essays/{id}")
    public Result<EssayDetailResponse> essay(@PathVariable Long id) {
        return Result.ok(writingService.getEssayDetail(SecurityUtils.requireUserId(), id));
    }

    @Operation(summary = "AI 生成范文（SSE）")
    @PostMapping(value = "/essays/{id}/sample", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sample(@PathVariable Long id) {
        return aiWritingService.streamSample(SecurityUtils.requireUserId(), id);
    }

    @Operation(summary = "AI 批改作文")
    @PostMapping("/essays/{id}/review")
    public Result<EssayReviewResponse> review(@PathVariable Long id) {
        return Result.ok(aiWritingService.reviewEssay(SecurityUtils.requireUserId(), id));
    }
}
