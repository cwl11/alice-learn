package com.alice.learn.controller;

import com.alice.learn.common.PageResult;
import com.alice.learn.common.Result;
import com.alice.learn.common.SecurityUtils;
import com.alice.learn.dto.ReadingPassageResponse;
import com.alice.learn.dto.SubmitAnswersRequest;
import com.alice.learn.dto.SubmitAnswersResponse;
import com.alice.learn.entity.ReadingPassage;
import com.alice.learn.service.ReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "阅读")
@RestController
@RequestMapping("/api/reading")
public class ReadingController {

    private final ReadingService readingService;

    public ReadingController(ReadingService readingService) {
        this.readingService = readingService;
    }

    @Operation(summary = "阅读文章列表")
    @GetMapping("/passages")
    public Result<PageResult<ReadingPassage>> passages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(readingService.listPassages(page, size));
    }

    @Operation(summary = "文章详情与题目")
    @GetMapping("/passages/{id}")
    public Result<ReadingPassageResponse> passage(@PathVariable Long id) {
        return Result.ok(readingService.getPassage(id, false));
    }

    @Operation(summary = "提交阅读答案")
    @PostMapping("/answers")
    public Result<SubmitAnswersResponse> submit(@Valid @RequestBody SubmitAnswersRequest request) {
        return Result.ok(readingService.submit(SecurityUtils.requireUserId(), request));
    }
}
