package com.education.platform.controllers;

import com.education.platform.dto.message.ConversationResponse;
import com.education.platform.dto.message.ConversationSummaryResponse;
import com.education.platform.dto.message.EnsureConversationRequest;
import com.education.platform.dto.message.MessageDeleteRequest;
import com.education.platform.dto.message.MessageReactionRequest;
import com.education.platform.dto.message.MessageResponse;
import com.education.platform.dto.message.SendMessageRequest;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.MessageService;
import com.education.platform.services.implementations.MessageRealtimeService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final CurrentUserService currentUserService;
    private final MessageService messageService;
    private final MessageRealtimeService realtimeService;

    public MessageController(CurrentUserService currentUserService, MessageService messageService, MessageRealtimeService realtimeService) {
        this.currentUserService = currentUserService;
        this.messageService = messageService;
        this.realtimeService = realtimeService;
    }

    @GetMapping("/conversations")
    public List<ConversationResponse> conversations() {
        return messageService.listConversations(currentUserService.getCurrentUser());
    }

    @GetMapping("/conversations/{id}")
    public List<MessageResponse> messages(@PathVariable Long id) {
        return messageService.conversationMessages(currentUserService.getCurrentUser(), id);
    }

    @GetMapping("/conversations/{id}/summary")
    public ConversationSummaryResponse summary(@PathVariable Long id) {
        return messageService.summarizeConversation(currentUserService.getCurrentUser(), id);
    }

    @PostMapping("/conversations/ensure")
    public ConversationResponse ensureConversation(@Valid @RequestBody EnsureConversationRequest request) {
        return messageService.ensureConversation(currentUserService.getCurrentUser(), request.getRecipientId());
    }

    @PostMapping("/send")
    public MessageResponse send(@Valid @RequestBody SendMessageRequest request) {
        return messageService.sendMessage(
                currentUserService.getCurrentUser(),
                request.getRecipientId(),
                request.getContent(),
                request.getReplyToMessageId());
    }

    @PostMapping(value = "/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MessageResponse sendVoice(
            @RequestPart("file") MultipartFile file,
            @RequestParam("recipientId") Long recipientId,
            @RequestParam(value = "replyToMessageId", required = false) Long replyToMessageId) {
        return messageService.sendVoiceMessage(currentUserService.getCurrentUser(), recipientId, file, replyToMessageId);
    }

    @PostMapping("/{messageId}/reaction")
    public MessageResponse react(
            @PathVariable Long messageId,
            @Valid @RequestBody MessageReactionRequest request) {
        return messageService.reactToMessage(currentUserService.getCurrentUser(), messageId, request.getEmoji());
    }

    @PostMapping("/{messageId}/delete")
    public void delete(
            @PathVariable Long messageId,
            @Valid @RequestBody MessageDeleteRequest request) {
        messageService.deleteMessage(currentUserService.getCurrentUser(), messageId, request.getScope());
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return realtimeService.subscribe(currentUserService.getCurrentUser().getId());
    }
}
