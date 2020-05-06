package com.atguigu.gmall.user.mapper;

import com.atguigu.gmall.bean.UmsMember;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author leizi
 * @create 2020-05-03 18:01
 */
@Repository
public interface UserMapper extends Mapper<UmsMember> {

}
