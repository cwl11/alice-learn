package com.alice.learn;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.alice.learn.mapper")
public class AliceLearnApplication {

    public static void main(String[] args) {
        SpringApplication.run(AliceLearnApplication.class, args);
    }
}
