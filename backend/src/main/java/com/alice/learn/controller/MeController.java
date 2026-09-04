package com.alice.learn.controller;

import com.alice.learn.common.PageResult;
import com.alice.learn.common.Result;
import com.alice.learn.common.SecurityUtils;
import com.alice.learn.entity.Essay;
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

    public MeController(WritingService writingService) {
        this.writingService = writingService;
    }

    @Operation(summary = "我的作文记录")
    @GetMapping("/essays")
    public Result<PageResult<Essay>> essays(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(writingService.myEssays(SecurityUtils.requireUserId(), page, size));
    }
}
