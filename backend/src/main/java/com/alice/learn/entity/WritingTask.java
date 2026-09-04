package com.alice.learn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("writing_task")
public class WritingTask {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskType;
    private String title;
    private String description;
    private String imageUrl;
    private String difficulty;
    private LocalDateTime createdAt;
}
