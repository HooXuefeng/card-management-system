package com.example.card.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 关闭CSRF（前端未做适配时必须关，否则会拦截POST请求）
                .csrf(csrf -> csrf.disable())

                // 2. 授权规则：精准放行静态资源+前端页面，适配context-path=/api
                .authorizeHttpRequests(auth -> auth
                        // 放行所有/api开头的请求（包含前端页面+后端接口）
                        .requestMatchers("/api/**").permitAll()
                        // 兜底：放行静态资源的默认映射路径（双重保障）
                        .requestMatchers("/static/**", "/index.html").permitAll()
                        // 所有请求都允许匿名访问（开发阶段简化配置）
                        .anyRequest().permitAll()
                )

                // 3. 关闭默认的表单登录/HTTP Basic认证（避免Security跳转登录页）
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}