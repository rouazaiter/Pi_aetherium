package com.education.platform.services.interfaces;

import com.education.platform.dto.message.ConversationResponse;
import com.education.platform.dto.message.ConversationSummaryResponse;
import com.education.platform.dto.message.MessageResponse;
import com.education.platform.entities.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MessageService {

    List<ConversationResponse> listConversations(User current);

    List<MessageResponse> conversationMessages(User current, Long conversationId);

    ConversationSummaryResponse summarizeConversation(User current, Long conversationId);

    ConversationResponse ensureConversation(User current, Long recipientId);

    MessageResponse sendMessage(User current, Long recipientId, String content, Long replyToMessageId);

    MessageResponse sendVoiceMessage(User current, Long recipientId, MultipartFile file, Long replyToMessageId);

    MessageResponse reactToMessage(User current, Long messageId, String emoji);

    void deleteMessage(User current, Long messageId, String scope);
}
