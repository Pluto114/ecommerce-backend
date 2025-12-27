package com.sc.mall.common.utils;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * MD5 加密工具类
 */
public class Md5Util {

    // 为了增加安全性，我们可以给所有密码加一个固定的“盐”值，或者配合数据库的随机盐
    // 这里简单演示，也可以作为混淆字符串
    private static final String STATIC_SALT = "sc_mall_salt_2025";

    /**
     * MD5加密方法
     * @param password 明文密码
     * @param salt 盐值 (可以是用户专属的随机盐)
     * @return 加密后的字符串
     */
    public static String encrypt(String password, String salt) {
        if (password == null) {
            return null;
        }
        if (salt == null) {
            salt = "";
        }
        // 拼接：密码 + 静态盐 + 动态盐
        String str = password + STATIC_SALT + salt;
        // 使用 Spring 自带的工具进行 MD5 加密
        return DigestUtils.md5DigestAsHex(str.getBytes(StandardCharsets.UTF_8));
    }

    public static void main(String[] args) {
        // 测试一下
        System.out.println(encrypt("123456", "random_salt"));
    }
}