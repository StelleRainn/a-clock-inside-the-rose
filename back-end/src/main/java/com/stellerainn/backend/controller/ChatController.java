package com.stellerainn.backend.controller;

import com.stellerainn.backend.common.Result;
import com.stellerainn.backend.entity.ChatSession;
import com.stellerainn.backend.entity.ChatMessage;
import com.stellerainn.backend.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 智能对话控制器 (ChatController)
 * 提供对 AI 聊天会话 (ChatSession) 和聊天消息 (ChatMessage) 的管理接口。
 * 注意：这里的接口仅负责“会话历史的持久化”和“读取”，
 * 真正的 AI 对话流式请求是在前端直接调用大模型 API 完成的，后端只负责归档聊天记录。
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    // ==========================================
    // 会话 (Sessions) 管理接口
    // ==========================================

    /**
     * 获取指定用户的所有聊天会话列表
     */
    @GetMapping("/sessions")
    public Result<List<ChatSession>> getSessions(@RequestParam Long userId) {
        List<ChatSession> sessions = chatService.getSessionsByUserId(userId);
        return Result.success(sessions);
    }

    /**
     * 创建新的聊天会话
     * @param request 包含 userId 和初始 title 的 JSON
     */
    @PostMapping("/sessions")
    public Result<ChatSession> createSession(@RequestBody ChatSession request) {
        ChatSession session = chatService.createSession(request.getUserId(), request.getTitle());
        return Result.success(session);
    }

    /**
     * 更新会话标题 (通常在 AI 回复第一句话后，前端会截取片段请求此接口更新标题)
     */
    @PutMapping("/sessions/{id}")
    public Result<Void> updateSessionTitle(@PathVariable Long id, @RequestBody ChatSession request) {
        chatService.updateSessionTitle(id, request.getTitle());
        return Result.success(null);
    }

    /**
     * 删除指定会话 (会级联删除其下的所有消息)
     */
    @DeleteMapping("/sessions/{id}")
    public Result<Void> deleteSession(@PathVariable Long id) {
        chatService.deleteSession(id);
        return Result.success(null);
    }

    // ==========================================
    // 消息 (Messages) 管理接口
    // ==========================================

    /**
     * 获取指定会话下的所有历史消息
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<ChatMessage>> getMessages(@PathVariable Long sessionId) {
        List<ChatMessage> messages = chatService.getMessagesBySessionId(sessionId);
        return Result.success(messages);
    }

    /**
     * 保存单条聊天消息
     * @param sessionId URL 中的会话 ID
     * @param request 包含 role (user/model) 和 content 的 JSON 对象
     */
    @PostMapping("/sessions/{sessionId}/messages")
    public Result<ChatMessage> saveMessage(@PathVariable Long sessionId, @RequestBody ChatMessage request) {
        ChatMessage message = chatService.saveMessage(sessionId, request.getRole(), request.getContent());
        return Result.success(message);
    }
}
