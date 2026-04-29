package com.education.platform.services.implementations;

import com.education.platform.common.ApiException;
import com.education.platform.dto.message.ConversationResponse;
import com.education.platform.dto.message.ConversationSummaryResponse;
import com.education.platform.dto.message.MessageResponse;
import com.education.platform.dto.message.MessageStreamEvent;
import com.education.platform.entities.DirectConversation;
import com.education.platform.entities.DirectMessage;
import com.education.platform.entities.User;
import com.education.platform.repositories.DirectConversationRepository;
import com.education.platform.repositories.DirectMessageRepository;
import com.education.platform.repositories.UserRepository;
import com.education.platform.services.interfaces.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Duration ACTIVE_WINDOW = Duration.ofMinutes(5);

    private final DirectConversationRepository conversationRepository;
    private final DirectMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageRealtimeService realtimeService;
    private final VoiceMessageStorage voiceMessageStorage;
    private final ConversationSummaryService conversationSummaryService;

    public MessageServiceImpl(
            DirectConversationRepository conversationRepository,
            DirectMessageRepository messageRepository,
            UserRepository userRepository,
            MessageRealtimeService realtimeService,
            VoiceMessageStorage voiceMessageStorage,
            ConversationSummaryService conversationSummaryService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.realtimeService = realtimeService;
        this.voiceMessageStorage = voiceMessageStorage;
        this.conversationSummaryService = conversationSummaryService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> listConversations(User current) {
        return conversationRepository.findByUserA_IdOrUserB_Id(current.getId(), current.getId()).stream()
                .map(conv -> toConversationResponse(current, conv))
                .sorted(Comparator.comparing(
                        (ConversationResponse c) -> c.getLastMessageAt() == null ? Instant.EPOCH : c.getLastMessageAt())
                        .reversed())
                .toList();
    }

    @Override
    @Transactional
    public List<MessageResponse> conversationMessages(User current, Long conversationId) {
        DirectConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Conversation introuvable"));
        ensureParticipant(current, conversation);
        List<DirectMessage> messages = messageRepository.findByConversation_IdOrderBySentAtAsc(conversationId);
        boolean touched = false;
        for (DirectMessage message : messages) {
            if (!isVisibleForUser(message, current, conversation)) {
                continue;
            }
            if (!message.isRead() && !message.getSender().getId().equals(current.getId()) && !message.isDeletedForEveryone()) {
                message.setRead(true);
                touched = true;
            }
        }
        if (touched) {
            messageRepository.saveAll(messages);
        }
        return messages.stream()
                .filter(m -> isVisibleForUser(m, current, conversation))
                .map(m -> toMessageResponse(m, current))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationSummaryResponse summarizeConversation(User current, Long conversationId) {
        User me = userRepository.findById(current.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        DirectConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Conversation introuvable"));
        ensureParticipant(me, conversation);

        User other = conversation.getUserA().getId().equals(me.getId()) ? conversation.getUserB() : conversation.getUserA();
        List<String> textLines = messageRepository.findByConversation_IdOrderBySentAtAsc(conversationId).stream()
                .filter(m -> isVisibleForUser(m, me, conversation))
                .filter(m -> !m.isDeletedForEveryone())
                .filter(m -> m.getVoiceUrl() == null || m.getVoiceUrl().isBlank())
                .filter(m -> m.getContent() != null && !m.getContent().isBlank())
                .filter(m -> !m.getContent().startsWith("[[GIF]]"))
                .map(m -> {
                    String who = m.getSender().getId().equals(me.getId()) ? me.getUsername() : other.getUsername();
                    String text = m.getContent().trim();
                    return who + ": " + (text.length() <= 220 ? text : text.substring(0, 220) + "...");
                })
                .toList();

        String summary = conversationSummaryService.summarize(me.getUsername(), other.getUsername(), textLines);
        return ConversationSummaryResponse.builder()
                .conversationId(conversationId)
                .analyzedTextMessages(textLines.size())
                .summary(summary)
                .build();
    }

    @Override
    @Transactional
    public ConversationResponse ensureConversation(User current, Long recipientId) {
        if (recipientId == null || recipientId.equals(current.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Destinataire invalide");
        }
        User sender = userRepository.findById(current.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Destinataire introuvable"));
        boolean isFriend = sender.getFriends().stream().anyMatch(f -> f.getId().equals(recipientId));
        if (!isFriend) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Vous pouvez discuter uniquement avec vos amis");
        }
        DirectConversation conversation = getOrCreateConversation(sender, recipient);
        return toConversationResponse(sender, conversation);
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(User current, Long recipientId, String content, Long replyToMessageId) {
        if (recipientId == null || recipientId.equals(current.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Destinataire invalide");
        }
        String cleanedContent = content == null ? "" : content.trim();
        if (cleanedContent.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Message vide");
        }
        User sender = userRepository.findById(current.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Destinataire introuvable"));
        boolean isFriend = sender.getFriends().stream().anyMatch(f -> f.getId().equals(recipientId));
        if (!isFriend) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Vous pouvez envoyer des messages uniquement à vos amis");
        }
        DirectConversation conversation = getOrCreateConversation(sender, recipient);
        DirectMessage replyTo = null;
        if (replyToMessageId != null) {
            replyTo = messageRepository.findById(replyToMessageId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Message de reponse introuvable"));
            if (!replyTo.getConversation().getId().equals(conversation.getId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Le message de reponse ne fait pas partie de la conversation");
            }
        }
        DirectMessage message = DirectMessage.builder()
                .conversation(conversation)
                .sender(sender)
                .content(cleanedContent)
                .sentAt(Instant.now())
                .read(false)
                .replyToMessage(replyTo)
                .build();
        messageRepository.save(message);
        MessageStreamEvent event = MessageStreamEvent.builder()
                .type("MESSAGE_SENT")
                .conversationId(conversation.getId())
                .messageId(message.getId())
                .senderId(sender.getId())
                .recipientId(recipient.getId())
                .build();
        realtimeService.publishMessageEvent(sender.getId(), event);
        realtimeService.publishMessageEvent(recipient.getId(), event);
        return toMessageResponse(message, sender);
    }

    @Override
    @Transactional
    public MessageResponse sendVoiceMessage(User current, Long recipientId, MultipartFile file, Long replyToMessageId) {
        if (recipientId == null || recipientId.equals(current.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Destinataire invalide");
        }
        User sender = userRepository.findById(current.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Destinataire introuvable"));
        boolean isFriend = sender.getFriends().stream().anyMatch(f -> f.getId().equals(recipientId));
        if (!isFriend) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Vous pouvez envoyer des messages uniquement à vos amis");
        }
        DirectConversation conversation = getOrCreateConversation(sender, recipient);
        DirectMessage replyTo = null;
        if (replyToMessageId != null) {
            replyTo = messageRepository.findById(replyToMessageId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Message de reponse introuvable"));
            if (!replyTo.getConversation().getId().equals(conversation.getId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Le message de reponse ne fait pas partie de la conversation");
            }
        }
        String voiceUrl;
        try {
            voiceUrl = voiceMessageStorage.store(sender.getId(), file);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Impossible d'enregistrer le message vocal.");
        }

        DirectMessage message = DirectMessage.builder()
                .conversation(conversation)
                .sender(sender)
                .content("[Voice message]")
                .voiceUrl(voiceUrl)
                .sentAt(Instant.now())
                .read(false)
                .replyToMessage(replyTo)
                .build();
        messageRepository.save(message);
        MessageStreamEvent event = MessageStreamEvent.builder()
                .type("MESSAGE_SENT")
                .conversationId(conversation.getId())
                .messageId(message.getId())
                .senderId(sender.getId())
                .recipientId(recipient.getId())
                .build();
        realtimeService.publishMessageEvent(sender.getId(), event);
        realtimeService.publishMessageEvent(recipient.getId(), event);
        return toMessageResponse(message, sender);
    }

    @Override
    @Transactional
    public MessageResponse reactToMessage(User current, Long messageId, String emoji) {
        User me = userRepository.findById(current.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        DirectMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Message introuvable"));
        DirectConversation conversation = message.getConversation();
        ensureParticipant(me, conversation);
        String normalized = emoji == null ? "" : emoji.trim();
        if (normalized.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reaction invalide");
        }
        message.setReactionEmoji(normalized);
        messageRepository.save(message);
        publishMutationEvent(message, "MESSAGE_REACTED");
        return toMessageResponse(message, me);
    }

    @Override
    @Transactional
    public void deleteMessage(User current, Long messageId, String scope) {
        User me = userRepository.findById(current.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        DirectMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Message introuvable"));
        DirectConversation conversation = message.getConversation();
        ensureParticipant(me, conversation);
        String normalizedScope = scope == null ? "" : scope.trim().toUpperCase(Locale.ROOT);
        if ("EVERYONE".equals(normalizedScope)) {
            if (!message.getSender().getId().equals(me.getId())) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Seul l'auteur peut supprimer pour tout le monde");
            }
            message.setDeletedForEveryone(true);
            message.setReactionEmoji(null);
            message.setContent("This message was deleted.");
        } else if ("ME".equals(normalizedScope)) {
            if (message.getSender().getId().equals(me.getId())) {
                message.setDeletedForSender(true);
            } else {
                message.setDeletedForRecipient(true);
            }
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Scope invalide");
        }
        messageRepository.save(message);
        publishMutationEvent(message, "MESSAGE_DELETED");
    }

    private ConversationResponse toConversationResponse(User current, DirectConversation conversation) {
        User other = conversation.getUserA().getId().equals(current.getId()) ? conversation.getUserB() : conversation.getUserA();
        List<DirectMessage> all = messageRepository.findByConversation_IdOrderBySentAtAsc(conversation.getId());
        DirectMessage last = all.stream()
                .filter(m -> isVisibleForUser(m, current, conversation))
                .reduce((first, second) -> second)
                .orElse(null);
        long unread = all.stream()
                .filter(m -> !m.getSender().getId().equals(current.getId()))
                .filter(m -> !m.isRead())
                .filter(m -> !m.isDeletedForEveryone())
                .filter(m -> isVisibleForUser(m, current, conversation))
                .count();
        Boolean activeNow = null;
        Instant lastActive = null;
        if (other.getProfile() != null && other.getProfile().isActiveStatusVisible()) {
            lastActive = other.getLastLogin();
            activeNow = lastActive != null && lastActive.isAfter(Instant.now().minus(ACTIVE_WINDOW));
        }
        return ConversationResponse.builder()
                .id(conversation.getId())
                .otherUserId(other.getId())
                .otherUsername(other.getUsername())
                .otherFirstName(other.getProfile() != null ? other.getProfile().getFirstName() : null)
                .otherLastName(other.getProfile() != null ? other.getProfile().getLastName() : null)
                .otherActiveNow(activeNow)
                .otherLastActiveAt(lastActive)
                .lastMessage(last != null ? last.getContent() : "")
                .lastMessageAt(last != null ? last.getSentAt() : null)
                .unreadCount(unread)
                .build();
    }

    private DirectConversation getOrCreateConversation(User a, User b) {
        long min = Math.min(a.getId(), b.getId());
        long max = Math.max(a.getId(), b.getId());
        return conversationRepository.findByUserA_IdAndUserB_Id(min, max)
                .orElseGet(() -> conversationRepository.save(DirectConversation.builder()
                        .userA(userRepository.getReferenceById(min))
                        .userB(userRepository.getReferenceById(max))
                        .createdAt(Instant.now())
                        .build()));
    }

    private void ensureParticipant(User current, DirectConversation conversation) {
        if (!conversation.getUserA().getId().equals(current.getId()) && !conversation.getUserB().getId().equals(current.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Accès interdit à cette conversation");
        }
    }

    private MessageResponse toMessageResponse(DirectMessage m, User current) {
        String snippet = null;
        Long replyToId = null;
        if (m.getReplyToMessage() != null) {
            replyToId = m.getReplyToMessage().getId();
            snippet = summarizeReplySnippet(m.getReplyToMessage().getContent());
        }
        return MessageResponse.builder()
                .id(m.getId())
                .senderId(m.getSender().getId())
                .senderUsername(m.getSender().getUsername())
                .content(m.getContent())
                .voiceUrl(m.getVoiceUrl())
                .sentAt(m.getSentAt())
                .reactionEmoji(m.getReactionEmoji())
                .replyToMessageId(replyToId)
                .replyToSnippet(snippet)
                .deleted(isDeletedForUser(m, current))
                .build();
    }

    private String summarizeReplySnippet(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String text = raw.trim();
        return text.length() <= 80 ? text : text.substring(0, 80) + "...";
    }

    private boolean isDeletedForUser(DirectMessage message, User current) {
        if (message.isDeletedForEveryone()) {
            return false;
        }
        if (message.getSender().getId().equals(current.getId())) {
            return message.isDeletedForSender();
        }
        return message.isDeletedForRecipient();
    }

    private boolean isVisibleForUser(DirectMessage message, User current, DirectConversation conversation) {
        if (message.isDeletedForEveryone()) {
            return false;
        }
        if (message.getSender().getId().equals(current.getId())) {
            return !message.isDeletedForSender();
        }
        Long otherId = conversation.getUserA().getId().equals(current.getId())
                ? conversation.getUserB().getId()
                : conversation.getUserA().getId();
        if (message.getSender().getId().equals(otherId)) {
            return !message.isDeletedForRecipient();
        }
        return true;
    }

    private void publishMutationEvent(DirectMessage message, String type) {
        Long senderId = message.getConversation().getUserA().getId();
        Long recipientId = message.getConversation().getUserB().getId();
        MessageStreamEvent event = MessageStreamEvent.builder()
                .type(type)
                .conversationId(message.getConversation().getId())
                .messageId(message.getId())
                .senderId(senderId)
                .recipientId(recipientId)
                .build();
        realtimeService.publishMessageEvent(senderId, event);
        realtimeService.publishMessageEvent(recipientId, event);
    }
}
