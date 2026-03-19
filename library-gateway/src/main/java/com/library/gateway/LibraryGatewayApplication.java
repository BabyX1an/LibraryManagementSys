package com.library.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 图书管理系统API网关
 */
@SpringBootApplication
public class LibraryGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(LibraryGatewayApplication.class, args);
    }
}

/**
 * 在登陆页面添加注册功能，点击注册后当前输密码和账号的窗口变为注册窗口，注册窗口需要填写数据库中user的全部字段，账号密码是必填，真实姓名也是必填，身份信息给两个选项：老师、学生，电话邮箱选填。检查后端是否有新增用户的逻辑，若有就直接调用，注意密码使用MD5加密
 *
 *
 */