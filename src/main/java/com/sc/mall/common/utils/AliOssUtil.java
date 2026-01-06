package com.sc.mall.common.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Data
@Component
@Slf4j
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    // 可选：如果你绑了 OSS 自定义域名（CDN/域名），填这个会更漂亮
    // 例如：https://static.yourdomain.com
    private String publicDomain;

    private OSS ossClient;

    @PostConstruct
    public void init() {
        System.out.println("================= OSS 配置检查 =================");
        System.out.println("Endpoint: " + endpoint);
        System.out.println("BucketName: " + bucketName);
        System.out.println("AccessKeyId: " + (accessKeyId == null ? "NULL" : accessKeyId));
        System.out.println("Secret 长度: " + (accessKeySecret == null ? 0 : accessKeySecret.length()));
        System.out.println("PublicDomain: " + publicDomain);
        System.out.println("===============================================");

        // 基础校验，避免空配置跑起来但上传必炸
        if (isBlank(endpoint) || isBlank(accessKeyId) || isBlank(accessKeySecret) || isBlank(bucketName)) {
            throw new IllegalStateException("OSS 配置缺失：请检查 aliyun.oss.endpoint/accessKeyId/accessKeySecret/bucketName");
        }

        // 复用客户端（更企业级，不用每次 build）
        this.ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    /**
     * 自动目录：image/* -> images/；.glb/.gltf -> models/；其他 -> files/
     */
    public String uploadAutoDir(MultipartFile file) throws Exception {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        String lowerName = originalFilename == null ? "" : originalFilename.toLowerCase(Locale.ROOT);

        String dir;
        if (contentType != null && contentType.startsWith("image/")) {
            dir = "images";
        } else if (lowerName.endsWith(".glb") || lowerName.endsWith(".gltf")) {
            dir = "models";
        } else {
            dir = "files";
        }
        return upload(file, dir);
    }

    /**
     * 上传文件到 OSS
     * @param file 文件
     * @param dir  目录（如 images / models / files），可为空
     * @return 公网访问 URL
     */
    public String upload(MultipartFile file, String dir) throws Exception {

        String originalFilename = file.getOriginalFilename();
        String suffix = getSuffix(originalFilename);

        // 目录 + 日期分桶（企业常见做法，便于运维）
        String datePath = LocalDate.now().toString(); // 2025-12-30
        String safeDir = (dir == null || dir.isBlank()) ? "uploads" : dir.trim();

        String objectKey = safeDir + "/" + datePath + "/" + UUID.randomUUID().toString().replace("-", "") + suffix;

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(file.getSize());
        if (file.getContentType() != null) {
            meta.setContentType(file.getContentType());
        }

        try (InputStream inputStream = file.getInputStream()) {
            ossClient.putObject(bucketName, objectKey, inputStream, meta);
        }

        String url = buildPublicUrl(objectKey);
        log.info("OSS 上传成功: {}", url);
        return url;
    }

    private String buildPublicUrl(String objectKey) {
        // 如果配置了自定义域名（推荐），优先用它
        if (!isBlank(publicDomain)) {
            String domain = publicDomain.replaceAll("/+$", ""); // 去掉尾部 /
            return domain + "/" + objectKey;
        }

        // 否则使用 https://bucket.endpoint/objectKey
        String clean = endpoint.replaceFirst("^https?://", "");

        // 关键修复：防止你把 endpoint 配成 bucket.xxx 造成 bucket.bucket.xxx
        if (clean.startsWith(bucketName + ".")) {
            clean = clean.substring(bucketName.length() + 1);
        }

        return "https://" + bucketName + "." + clean + "/" + objectKey;
    }

    private String getSuffix(String filename) {
        if (filename == null) return "";
        int i = filename.lastIndexOf('.');
        if (i < 0) return "";
        return filename.substring(i);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }



}
