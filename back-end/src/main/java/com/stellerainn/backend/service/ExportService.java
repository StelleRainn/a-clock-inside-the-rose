package com.stellerainn.backend.service;

import com.stellerainn.backend.entity.PomodoroRecord;
import com.stellerainn.backend.entity.Task;
import com.stellerainn.backend.entity.Tag;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据导出服务层 (ExportService)
 * 负责将数据库中的业务数据（Task、PomodoroRecord）序列化为标准的 CSV 二进制流，
 * 供 ExportController 作为文件附件返回给前端。
 * 采用了 Apache Commons CSV 库来处理 CSV 格式的转义和组装。
 */
@Service
public class ExportService {

    @Autowired
    private TaskService taskService;

    @Autowired
    private PomodoroService pomodoroService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 将用户的全量任务导出为 CSV 流
     */
    public ByteArrayInputStream exportTasksToCsv(Long userId) {
        List<Task> tasks = taskService.getTasksByUserId(userId);
        
        // 使用 try-with-resources 语法，确保流对象在使用完毕后被自动关闭，防止内存泄漏
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
            CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(out, StandardCharsets.UTF_8), 
            CSVFormat.DEFAULT.builder().setHeader("ID", "Title", "Description", "Status", "Priority", "Tags", "Due Date", "Created At").build())) {

            for (Task task : tasks) {
                // 利用 Java 8 Stream API 将标签列表转换为逗号分隔的字符串
                String tags = task.getTags() != null 
                    ? task.getTags().stream().map(Tag::getName).collect(Collectors.joining(",")) 
                    : "";
                
                csvPrinter.printRecord(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getStatus(),
                    task.getPriority(),
                    tags,
                    task.getDueDate() != null ? task.getDueDate().format(DATE_FORMATTER) : "",
                    task.getCreatedAt() != null ? task.getCreatedAt().format(DATE_FORMATTER) : ""
                );
            }
            // 确保缓冲区内的数据全部写入输出流
            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to export tasks to CSV: " + e.getMessage());
        }
    }

    /**
     * 将用户的番茄钟记录导出为 CSV 流
     */
    public ByteArrayInputStream exportPomodoroToCsv(Long userId) {
        List<PomodoroRecord> records = pomodoroService.getRecordsByUserId(userId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(out, StandardCharsets.UTF_8), 
                CSVFormat.DEFAULT.builder().setHeader("ID", "Task ID", "Start Time", "End Time", "Duration (sec)", "Status").build())) {

            for (PomodoroRecord record : records) {
                csvPrinter.printRecord(
                    record.getId(),
                    record.getTaskId() != null ? record.getTaskId() : "N/A",
                    record.getStartTime() != null ? record.getStartTime().format(DATE_FORMATTER) : "",
                    record.getEndTime() != null ? record.getEndTime().format(DATE_FORMATTER) : "",
                    record.getDurationSeconds(),
                    record.getStatus()
                );
            }
            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to export pomodoro records to CSV: " + e.getMessage());
        }
    }
}
