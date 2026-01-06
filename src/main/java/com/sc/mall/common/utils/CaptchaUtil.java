package com.sc.mall.common.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

public class CaptchaUtil {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final Random R = new Random();

    public static String randomCode(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(CHARS.charAt(R.nextInt(CHARS.length())));
        return sb.toString();
    }

    // ✅ 纯 SVG，无 AWT / ImageIO / 字体库依赖
    public static String renderBase64Svg(String code, int width, int height) {
        StringBuilder sb = new StringBuilder();
        sb.append("<svg xmlns='http://www.w3.org/2000/svg' width='").append(width)
                .append("' height='").append(height).append("' viewBox='0 0 ").append(width).append(" ").append(height).append("'>");

        // 背景
        sb.append("<rect width='100%' height='100%' fill='#f5f7fa'/>");

        // 干扰线
        for (int i = 0; i < 8; i++) {
            int x1 = R.nextInt(width), y1 = R.nextInt(height);
            int x2 = R.nextInt(width), y2 = R.nextInt(height);
            String color = String.format("#%02x%02x%02x", R.nextInt(180), R.nextInt(180), R.nextInt(180));
            sb.append("<line x1='").append(x1).append("' y1='").append(y1)
                    .append("' x2='").append(x2).append("' y2='").append(y2)
                    .append("' stroke='").append(color).append("' stroke-width='1' opacity='0.8'/>");
        }

        // 字符
        int charWidth = width / code.length();
        for (int i = 0; i < code.length(); i++) {
            String color = String.format("#%02x%02x%02x", R.nextInt(120), R.nextInt(120), R.nextInt(120));
            int x = i * charWidth + 12;
            int y = (int) (height * 0.7);
            int rotate = R.nextInt(21) - 10; // -10~10 度
            sb.append("<text x='").append(x).append("' y='").append(y).append("' fill='").append(color)
                    .append("' font-size='26' font-family='Arial, sans-serif' font-weight='700'")
                    .append(" transform='rotate(").append(rotate).append(" ").append(x).append(" ").append(y).append(")'>")
                    .append(code.charAt(i))
                    .append("</text>");
        }

        sb.append("</svg>");

        String svg = sb.toString();
        String b64 = Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
        return "data:image/svg+xml;base64," + b64;
    }
}
