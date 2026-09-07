package com.alice.learn.controller;

import com.alice.learn.common.PageResult;
import com.alice.learn.common.Result;
import com.alice.learn.common.SecurityUtils;
import com.alice.learn.dto.EssaySummaryResponse;
import com.alice.learn.dto.ReadingAttemptResponse;
import com.alice.learn.service.ReadingService;
import com.alice.learn.service.WritingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "个人中心")
@RestController
@RequestMapping("/api/me")
public class MeController {

    private final WritingService writingService;
    private final ReadingService readingService;

    public MeController(WritingService writingService, ReadingService readingService) {
        this.writingService = writingService;
        this.readingService = readingService;
    }

    @Operation(summary = "我的作文记录（含题目、最新总分）")
    @GetMapping("/essays")
    public Result<PageResult<EssaySummaryResponse>> essays(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        return Result.ok(writingService.myEssays(SecurityUtils.requireUserId(), page, size, status));
    }

    @Operation(summary = "我的阅读练习记录")
    @GetMapping("/reading")
    public Result<PageResult<ReadingAttemptResponse>> reading(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(readingService.myAttempts(SecurityUtils.requireUserId(), page, size));
    }
}
