package com.stellerainn.backend.controller;

import com.stellerainn.backend.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;

/**
 * 导出控制器 (ExportController)
 * 负责处理用户个人数据的导出请求。
 * 这里不返回常见的 JSON 格式 (Result<T>)，而是直接下发二进制文件流，
 * 配合前端（如 ProfileSettings.vue 中通过 Blob 和 a 标签）实现纯前端触发的文件下载体验。
 */
@RestController
@RequestMapping("/api/export")
public class ExportController {

    @Autowired
    private ExportService exportService;

    /**
     * 导出任务数据为 CSV
     * @param userId 用户ID
     * @return 包含 CSV 字节流的 HTTP 响应，并设置了强制下载的 Header
     */
    @GetMapping("/tasks")
    public ResponseEntity<InputStreamResource> exportTasks(@RequestParam Long userId) {
        // 调用 Service 层查询数据库，并在内存中将 List<Task> 拼接成 CSV 格式的字节流
        ByteArrayInputStream in = exportService.exportTasksToCsv(userId);
        
        // 设置 HTTP Header:
        // Content-Disposition: attachment 告诉浏览器这是一个附件，应该触发下载行为而不是尝试预览
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=tasks.csv");
        
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv")) // 明确声明 MIME 类型为 CSV
                .body(new InputStreamResource(in)); // 将字节流包装为 Spring 可识别的资源体
    }

    /**
     * 导出番茄钟/专注记录数据为 CSV
     * 逻辑同上，只换了具体的 Service 方法和下载文件名
     */
    @GetMapping("/pomodoro")
    public ResponseEntity<InputStreamResource> exportPomodoro(@RequestParam Long userId) {
        ByteArrayInputStream in = exportService.exportPomodoroToCsv(userId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=focus_records.csv");
        
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(in));
    }
}
