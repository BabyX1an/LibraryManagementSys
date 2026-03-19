package com.library.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.service.user.entity.User;
import com.library.service.user.mapper.UserMapper;
import com.library.service.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

/**
 * 用户信息服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;

    @Override
    public Page<User> pageQueryUsers(Page<User> page, String username, String realName) {
        return userMapper.pageQuery(page, username, realName);
    }

    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username)
                   .eq(User::getDeleted, 0);
        return getOne(queryWrapper);
    }

    @Override
    public boolean addUser(User user) {
        user.setPassword(md5Encrypt(user.getPassword()));
        user.setDeleted(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return save(user);
    }

    /**
     * MD5加密
     */
    private String md5Encrypt(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }

    @Override
    public boolean updateUser(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return updateById(user);
    }

    @Override
    public boolean deleteUser(Long id) {
        return removeById(id);
    }

    @Override
    public User login(String username, String password) {
        User user = getUserByUsername(username);
        if (user != null && md5Encrypt(password).equals(user.getPassword())) {
            user.setLastLoginTime(LocalDateTime.now());
            updateById(user);
            // 清除密码字段后再返回
            user.setPassword(null);
            return user;
        }
        return null;
    }

    @Override
    public User getUserById(Long userId) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, userId)
                   .eq(User::getDeleted, 0);
        return getOne(queryWrapper);
    }

    @Override
    public boolean updateCurrentUser(User user) {
        // 只允许更新特定字段
        User existingUser = getUserById(user.getId());
        if (existingUser == null) {
            return false;
        }

        // 只更新允许修改的字段
        if (user.getRealName() != null) {
            existingUser.setRealName(user.getRealName());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getPhone() != null) {
            existingUser.setPhone(user.getPhone());
        }
        if (user.getAvatarUrl() != null) {
            existingUser.setAvatarUrl(user.getAvatarUrl());
        }

        existingUser.setUpdatedAt(LocalDateTime.now());
        return updateById(existingUser);
    }
}