package com.sc.mall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// 关键：扫描 mapper 包下的接口，让 MyBatis-Plus 能找到它们
@MapperScan("com.sc.mall.mapper")
public class MallBackendApplication {

    public static void main(String[] args) {
        // 错误写法: MallBackendApplication.java
        // 正确写法: MallBackendApplication.class
        SpringApplication.run(MallBackendApplication.class, args);
    }

}