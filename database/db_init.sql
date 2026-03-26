-- 创建数据库
CREATE DATABASE IF NOT EXISTS library_system CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE library_system;

-- 创建图书表
CREATE TABLE `book` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图书ID',
    `isbn` VARCHAR(20) NOT NULL COMMENT 'ISBN号',
    `title` VARCHAR(100) NOT NULL COMMENT '书名',
    `author` VARCHAR(150) NOT NULL COMMENT '作者',
    `publisher` VARCHAR(100) NOT NULL COMMENT '出版社',
    `publish_date` VARCHAR(50) COMMENT '出版日期',
    `category` VARCHAR(150) NOT NULL COMMENT '分类',
    `description` TEXT COMMENT '图书描述',
    `cover_url` VARCHAR(255) COMMENT '封面图片URL',
    `stock` INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    `borrowed_count` INT NOT NULL DEFAULT 0 COMMENT '借阅次数',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-在馆，2-借出，3-维修中',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_isbn` (`isbn`),
    KEY `idx_title` (`title`),
    KEY `idx_author` (`author`),
    KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='图书信息表';

-- 创建用户表
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `email` VARCHAR(100) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `role` TINYINT NOT NULL DEFAULT 1 COMMENT '角色：1-普通用户，2-管理员',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常，2-禁用',
    `avatar_url` VARCHAR(255) COMMENT '头像URL',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_real_name` (`real_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户信息表';

-- 插入测试数据
INSERT INTO `book` (`isbn`, `title`, `author`, `publisher`, `publish_date`, `category`, `description`, `stock`, `status`) VALUES
('9787115345632', 'Java编程思想', 'Bruce Eckel', '机械工业出版社', '2013-03-01', '计算机科学', 'Java编程经典教程', 10, 1),
('9787115465448', '深入理解Java虚拟机', '周志明', '机械工业出版社', '2019-09-01', '计算机科学', 'Java虚拟机详解', 5, 1),
('9787115521994', 'Spring实战', ' Craig Walls', '人民邮电出版社', '2021-03-01', '计算机科学', 'Spring框架实战指南', 8, 1),
('9787121232384', 'MySQL必知必会', 'Ben Forta', '电子工业出版社', '2013-08-01', '数据库', 'MySQL入门教程', 12, 1),
('9787115282246', '算法导论', 'Thomas H. Cormen', '机械工业出版社', '2013-01-01', '算法', '计算机算法经典教材', 6, 1);

INSERT INTO `user` (`username`, `password`, `real_name`, `email`, `phone`, `role`, `status`) VALUES
('admin', '0192023a7bbd73250516f069df18b500', '管理员', 'admin@library.com', '13800138000', 2, 1),
('teacher', '0192023a7bbd73250516f069df18b500', '教师用户', 'teacher@library.com', '13800138001', 1, 1),
('student', '0192023a7bbd73250516f069df18b500', '学生用户', 'student@library.com', '13800138002', 1, 1);