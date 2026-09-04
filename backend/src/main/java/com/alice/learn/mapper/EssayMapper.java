package com.alice.learn.mapper;

import com.alice.learn.entity.Essay;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EssayMapper extends BaseMapper<Essay> {
}
