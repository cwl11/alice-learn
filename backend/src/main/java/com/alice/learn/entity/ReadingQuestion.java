package com.alice.learn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("reading_question")
public class ReadingQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long passageId;
    private String questionType;
    private String question;
    private String optionsJson;
    private String answer;
    private String explanation;
}
