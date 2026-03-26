package com.library.service.book.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源配置
 * 将本地目录映射为可访问的URL路径
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.image.location:/Users/mortis/LibraryImages}")
    private String imageLocation;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射 /images/** 到本地目录
        // 访问 http://localhost:8107/images/xxx.jpg
        // 实际读取 imageLocation 目录下的文件
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + imageLocation + "/");
    }
}
