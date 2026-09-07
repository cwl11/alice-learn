package com.alice.learn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("reading_attempt")
public class ReadingAttempt {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long passageId;
    private Integer total;
    private Integer correctCount;
    private Integer timeSpentSec;
    private LocalDateTime createdAt;
}
