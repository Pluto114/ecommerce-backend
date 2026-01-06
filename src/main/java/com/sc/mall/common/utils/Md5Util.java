package com.sc.mall.common.utils;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

public class Md5Util {

    public static String encrypt(String password, String salt) {
        String s = (salt == null) ? "" : salt;
        return DigestUtils.md5DigestAsHex((password + s).getBytes(StandardCharsets.UTF_8));
    }
}
