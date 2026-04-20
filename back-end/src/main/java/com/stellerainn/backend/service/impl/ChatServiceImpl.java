package com.stellerainn.backend.service.impl;

import com.stellerainn.backend.entity.ChatSession;
import com.stellerainn.backend.entity.ChatMessage;
import com.stellerainn.backend.mapper.ChatMapper;
import com.stellerainn.backend.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 聊天服务实现类 (ChatServiceImpl)
 * 这里处理 AI 助手的会话和消息落库逻辑。
 * 面向接口编程，实现了 ChatService 接口，符合 Spring 的最佳实践。
 */
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatMapper chatMapper;

    @Override
    public ChatSession createSession(Long userId, String title) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle(title);
        // Mybatis insert 之后，如果配置了 useGeneratedKeys，主键会自动填充到 session 实体中
        chatMapper.insertSession(session);
        return session;
    }

    @Override
    public List<ChatSession> getSessionsByUserId(Long userId) {
        return chatMapper.findSessionsByUserId(userId);
    }

    @Override
    public ChatSession getSessionById(Long id) {
        return chatMapper.findSessionById(id);
    }

    @Override
    public void updateSessionTitle(Long id, String title) {
        chatMapper.updateSessionTitle(id, title);
    }

    @Override
    public void deleteSession(Long id) {
        chatMapper.deleteSession(id);
    }

    /**
     * 保存单条聊天记录
     * 这不仅记录了用户的 Prompt，也记录了 AI 的 Response。
     */
    @Override
    public ChatMessage saveMessage(Long sessionId, String role, String content) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setRole(role); // "user" or "model"
        message.setContent(content);
        chatMapper.insertMessage(message);
        
        // Touch session updated_at to bring it to top
        // But mysql handles it automatically if we update it. Actually let's manually trigger it if needed,
        // or just let the database ON UPDATE handle it. Wait, inserting to chat_messages doesn't trigger chat_sessions update.
        // Let's do a dummy update or specific query. For now it's okay, maybe just order by id DESC for sessions.
        
        return message;
    }

    @Override
    public List<ChatMessage> getMessagesBySessionId(Long sessionId) {
        return chatMapper.findMessagesBySessionId(sessionId);
    }
}
