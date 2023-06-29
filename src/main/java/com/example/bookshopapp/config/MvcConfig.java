package com.example.bookshopapp.config;

import com.example.bookshopapp.interceptor.HeaderInterceptor;
import com.example.bookshopapp.interceptor.UserInterceptor;
import com.example.bookshopapp.service.AuthService;
import com.example.bookshopapp.service.BookStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    private final BookShopConfig config;
    private final BookStatusService bookStatusService;
    private final AuthService authService;

    @Autowired
    public MvcConfig(BookStatusService bookStatusService, BookShopConfig config, AuthService authService) {
        this.bookStatusService = bookStatusService;
        this.config = config;
        this.authService = authService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/book-covers/**")
                .addResourceLocations("file:" + config.getUploadPath() + "/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HeaderInterceptor(bookStatusService, authService, config));
        registry.addInterceptor(new UserInterceptor(authService));
    }
}
