package com.alice.learn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("word")
public class Word {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String word;
    private String phonetic;
    private String meaning;
    private String example;
    private String category;
}
