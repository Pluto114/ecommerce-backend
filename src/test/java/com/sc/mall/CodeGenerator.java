package com.sc.mall;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.sql.Types;
import java.util.Collections;

/**
 * MyBatis-Plus 代码生成器
 * 运行 main 方法即可生成 Entity, Mapper, Service, Controller 代码
 */
public class CodeGenerator {

    public static void main(String[] args) {
        // 1. 数据库配置
        String url = "jdbc:mysql://localhost:3306/mall_db?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&useSSL=false";
        String username = "root";
        String password = "Zhaojerry331!"; // TODO: !!!这里替换成你的数据库密码!!!

        // 2. 开始生成
        FastAutoGenerator.create(url, username, password)
                .globalConfig(builder -> {
                    builder.author("斯聪") // 设置作者
                            .enableSpringdoc() // 开启 SpringDoc (Swagger3) 模式，生成 @Schema 注解
                            .outputDir(System.getProperty("user.dir") + "/src/main/java"); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent("com.sc.mall") // 设置父包名
                            // .moduleName("system") // 如果需要模块名可以打开
                            .entity("entity")
                            .service("service")
                            .serviceImpl("service.impl")
                            .mapper("mapper")
                            .xml("mapper.xml") // XML 文件的目录
                            .controller("controller")
                            .pathInfo(Collections.singletonMap(OutputFile.xml, System.getProperty("user.dir") + "/src/main/resources/mapper")); // 设置 Mapper XML 输出到 resources 目录
                })
                .strategyConfig(builder -> {
                    builder.addInclude("tb_user", "tb_shop", "tb_product", "tb_order", "tb_order_item", "tb_cart", "tb_product_comment") // 设置需要生成的表名
                            .addTablePrefix("tb_") // 设置过滤表前缀 (例如 tb_user -> User)

                            // 实体类配置
                            .entityBuilder()
                            .enableLombok() // 开启 Lombok
                            .enableTableFieldAnnotation() // 开启字段注解

                            // Controller 配置
                            .controllerBuilder()
                            .enableRestStyle(); // 开启 @RestController
                })
                .templateEngine(new FreemarkerTemplateEngine()) // 使用 Freemarker 引擎模板
                .execute();

        System.out.println("-------------------------------------");
        System.out.println(" 代码生成成功！请检查 com.sc.mall 包结构 ");
        System.out.println("-------------------------------------");
    }
}