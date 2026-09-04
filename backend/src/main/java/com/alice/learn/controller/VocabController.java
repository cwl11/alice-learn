package com.alice.learn.controller;

import com.alice.learn.common.PageResult;
import com.alice.learn.common.Result;
import com.alice.learn.common.SecurityUtils;
import com.alice.learn.dto.NotebookRequest;
import com.alice.learn.dto.ReviewCardRequest;
import com.alice.learn.dto.WordResponse;
import com.alice.learn.service.VocabService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "词汇")
@RestController
@RequestMapping("/api/vocab")
public class VocabController {

    private final VocabService vocabService;

    public VocabController(VocabService vocabService) {
        this.vocabService = vocabService;
    }

    @Operation(summary = "词库分页")
    @GetMapping("/words")
    public Result<PageResult<WordResponse>> words(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        return Result.ok(vocabService.listWords(SecurityUtils.requireUserId(), page, size, category, keyword));
    }

    @Operation(summary = "生词本")
    @GetMapping("/notebook")
    public Result<PageResult<WordResponse>> notebook(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(vocabService.notebook(SecurityUtils.requireUserId(), page, size));
    }

    @Operation(summary = "加入生词本")
    @PostMapping("/notebook")
    public Result<Void> add(@Valid @RequestBody NotebookRequest request) {
        vocabService.addToNotebook(SecurityUtils.requireUserId(), request.getWordId());
        return Result.ok();
    }

    @Operation(summary = "移出生词本")
    @DeleteMapping("/notebook/{wordId}")
    public Result<Void> remove(@PathVariable Long wordId) {
        vocabService.removeFromNotebook(SecurityUtils.requireUserId(), wordId);
        return Result.ok();
    }

    @Operation(summary = "待复习卡片")
    @GetMapping("/review")
    public Result<List<WordResponse>> reviewCards() {
        return Result.ok(vocabService.reviewCards(SecurityUtils.requireUserId()));
    }

    @Operation(summary = "提交记忆卡片结果")
    @PostMapping("/review")
    public Result<WordResponse> review(@Valid @RequestBody ReviewCardRequest request) {
        return Result.ok(vocabService.review(
                SecurityUtils.requireUserId(),
                request.getWordId(),
                Boolean.TRUE.equals(request.getRemembered())));
    }
}
