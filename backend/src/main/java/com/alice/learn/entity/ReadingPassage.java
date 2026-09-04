package com.alice.learn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("reading_passage")
public class ReadingPassage {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String difficulty;
}
