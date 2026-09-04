package com.alice.learn.service;

import com.alice.learn.common.BusinessException;
import com.alice.learn.common.PageResult;
import com.alice.learn.common.VocabReviewPolicy;
import com.alice.learn.dto.WordResponse;
import com.alice.learn.entity.UserWord;
import com.alice.learn.entity.Word;
import com.alice.learn.mapper.UserWordMapper;
import com.alice.learn.mapper.WordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VocabService {

    private final WordMapper wordMapper;
    private final UserWordMapper userWordMapper;

    public VocabService(WordMapper wordMapper, UserWordMapper userWordMapper) {
        this.wordMapper = wordMapper;
        this.userWordMapper = userWordMapper;
    }

    public PageResult<WordResponse> listWords(Long userId, int page, int size, String category, String keyword) {
        Page<Word> pager = new Page<>(page, size);
        LambdaQueryWrapper<Word> wrapper = new LambdaQueryWrapper<Word>()
                .eq(category != null && !category.isBlank(), Word::getCategory, category)
                .like(keyword != null && !keyword.isBlank(), Word::getWord, keyword)
                .orderByAsc(Word::getId);
        Page<Word> result = wordMapper.selectPage(pager, wrapper);
        Map<Long, UserWord> notebook = notebookMap(userId, result.getRecords().stream().map(Word::getId).toList());
        List<WordResponse> records = result.getRecords().stream()
                .map(word -> toResponse(word, notebook.get(word.getId())))
                .toList();
        return new PageResult<>(result.getTotal(), records);
    }

    public PageResult<WordResponse> notebook(Long userId, int page, int size) {
        Page<UserWord> pager = new Page<>(page, size);
        Page<UserWord> result = userWordMapper.selectPage(pager, new LambdaQueryWrapper<UserWord>()
                .eq(UserWord::getUserId, userId)
                .orderByDesc(UserWord::getId));
        if (result.getRecords().isEmpty()) {
            return new PageResult<>(result.getTotal(), List.of());
        }
        Set<Long> ids = result.getRecords().stream().map(UserWord::getWordId).collect(Collectors.toSet());
        Map<Long, Word> words = wordMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Word::getId, Function.identity()));
        List<WordResponse> records = result.getRecords().stream()
                .map(item -> toResponse(words.get(item.getWordId()), item))
                .filter(item -> item.getId() != null)
                .toList();
        return new PageResult<>(result.getTotal(), records);
    }

    public void addToNotebook(Long userId, Long wordId) {
        Word word = wordMapper.selectById(wordId);
        if (word == null) {
            throw new BusinessException(404, "单词不存在");
        }
        UserWord existing = userWordMapper.selectOne(new LambdaQueryWrapper<UserWord>()
                .eq(UserWord::getUserId, userId)
                .eq(UserWord::getWordId, wordId));
        if (existing != null) {
            return;
        }
        UserWord userWord = new UserWord();
        userWord.setUserId(userId);
        userWord.setWordId(wordId);
        userWord.setFamiliarity(0);
        userWord.setNextReviewAt(LocalDateTime.now());
        userWord.setCreatedAt(LocalDateTime.now());
        userWordMapper.insert(userWord);
    }

    public void removeFromNotebook(Long userId, Long wordId) {
        userWordMapper.delete(new LambdaQueryWrapper<UserWord>()
                .eq(UserWord::getUserId, userId)
                .eq(UserWord::getWordId, wordId));
    }

    public List<WordResponse> reviewCards(Long userId) {
        List<UserWord> due = userWordMapper.selectList(new LambdaQueryWrapper<UserWord>()
                .eq(UserWord::getUserId, userId)
                .le(UserWord::getNextReviewAt, LocalDateTime.now())
                .orderByAsc(UserWord::getNextReviewAt)
                .last("LIMIT 20"));
        if (due.isEmpty()) {
            return List.of();
        }
        Set<Long> ids = due.stream().map(UserWord::getWordId).collect(Collectors.toSet());
        Map<Long, Word> words = wordMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Word::getId, Function.identity()));
        return due.stream()
                .map(item -> toResponse(words.get(item.getWordId()), item))
                .filter(item -> item.getId() != null)
                .toList();
    }

    public WordResponse review(Long userId, Long wordId, boolean remembered) {
        UserWord userWord = userWordMapper.selectOne(new LambdaQueryWrapper<UserWord>()
                .eq(UserWord::getUserId, userId)
                .eq(UserWord::getWordId, wordId));
        if (userWord == null) {
            throw new BusinessException(404, "该词不在生词本中");
        }
        int next = VocabReviewPolicy.nextFamiliarity(userWord.getFamiliarity(), remembered);
        userWord.setFamiliarity(next);
        userWord.setNextReviewAt(VocabReviewPolicy.nextReviewAt(next, LocalDateTime.now()));
        userWordMapper.updateById(userWord);
        return toResponse(wordMapper.selectById(wordId), userWord);
    }

    private Map<Long, UserWord> notebookMap(Long userId, List<Long> wordIds) {
        if (wordIds == null || wordIds.isEmpty()) {
            return Map.of();
        }
        return userWordMapper.selectList(new LambdaQueryWrapper<UserWord>()
                        .eq(UserWord::getUserId, userId)
                        .in(UserWord::getWordId, wordIds))
                .stream()
                .collect(Collectors.toMap(UserWord::getWordId, Function.identity(), (a, b) -> a));
    }

    private WordResponse toResponse(Word word, UserWord userWord) {
        WordResponse response = new WordResponse();
        if (word != null) {
            response.setId(word.getId());
            response.setWord(word.getWord());
            response.setPhonetic(word.getPhonetic());
            response.setMeaning(word.getMeaning());
            response.setExample(word.getExample());
            response.setCategory(word.getCategory());
        }
        response.setInNotebook(userWord != null);
        response.setFamiliarity(userWord == null ? null : userWord.getFamiliarity());
        return response;
    }
}
