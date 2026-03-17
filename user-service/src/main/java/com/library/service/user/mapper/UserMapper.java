package com.library.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.service.user.entity.User;
import org.apache.ibatis.annotations.Param;

/**
 * 用户信息Mapper接口
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 分页查询用户
     * @param page 分页对象
     * @param username 用户名
     * @param realName 真实姓名
     * @return 分页结果
     */
    Page<User> pageQuery(Page<User> page,
                         @Param("username") String username,
                         @Param("realName") String realName);
}