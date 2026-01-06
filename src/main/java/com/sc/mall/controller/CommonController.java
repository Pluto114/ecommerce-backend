package com.sc.mall.controller;

import com.sc.mall.common.Result;
import com.sc.mall.common.utils.AliOssUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "0. 通用模块", description = "文件上传接口")
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Resource
    private AliOssUtil aliOssUtil;

    @Operation(summary = "文件上传 (OSS)")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> upload(@RequestPart("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return Result.error("未接收到文件(file)");
        }
        long maxBytes = 100L * 1024 * 1024; // 100MB
        if (file.getSize() > maxBytes) {
            return Result.error("文件过大：最大允许 100MB");
        }


        log.info("接收文件: name={}, size={} bytes, contentType={}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        try {
            // 自动按类型分目录：images/ 或 models/
            String url = aliOssUtil.uploadAutoDir(file);
            return Result.success(url);

        } catch (com.aliyun.oss.OSSException oe) {
            log.error("OSS服务端异常: Code={}, Msg={}", oe.getErrorCode(), oe.getErrorMessage());
            return Result.error("OSS配置错误: " + oe.getErrorCode() + " - " + oe.getErrorMessage());

        } catch (com.aliyun.oss.ClientException ce) {
            log.error("OSS客户端异常: {}", ce.getMessage());
            return Result.error("OSS连接失败: " + ce.getMessage());

        } catch (Exception e) {
            log.error("上传系统异常", e);
            return Result.error("系统异常: " + e.getMessage());
        }
    }
}
