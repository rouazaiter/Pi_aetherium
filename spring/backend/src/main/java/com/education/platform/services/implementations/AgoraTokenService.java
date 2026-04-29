package com.education.platform.services.implementations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.education.platform.agora.RtcTokenBuilder2;
import com.education.platform.services.interfaces.IAgoraTokenService;

import java.util.HashMap;
import java.util.Map;

@Service
public class AgoraTokenService implements IAgoraTokenService {

    @Value("${agora.app.id:}")
    private String appId;

    @Value("${agora.app.certificate:}")
    private String appCertificate;

    @Value("${agora.token.expiry:3600}")
    private int tokenExpiry;

    @Override
    public String generateToken(String channelName, Long userId, int expireTime) {
        String normalizedAppId = normalize(appId);
        String normalizedCertificate = normalize(appCertificate);
        if (!isValidAgoraHex(normalizedAppId) || !isValidAgoraHex(normalizedCertificate)) {
            return "";
        }
        String normalizedChannel = normalize(channelName);
        if (normalizedChannel.isEmpty()) {
            return "";
        }
        int uid = userId != null && userId > 0 ? (int) (userId & 0xFFFFFFFFL) : 0;
        int ttl = expireTime > 0 ? expireTime : tokenExpiry;
        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();
        return tokenBuilder.buildTokenWithUid(
                normalizedAppId,
                normalizedCertificate,
                normalizedChannel,
                uid,
                RtcTokenBuilder2.Role.ROLE_PUBLISHER,
                ttl,
                ttl
        );
    }

    @Override
    public String generateTokenForUser(String channelName, Long userId) {
        return generateToken(channelName, userId, tokenExpiry);
    }

    @Override
    public Map<String, String> generateTokenForRoom(String channelName, Long userId) {
        Map<String, String> result = new HashMap<>();
        String token = generateTokenForUser(channelName, userId);
        result.put("token", token);
        result.put("appId", normalize(appId));
        result.put("channelName", channelName);
        return result;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isValidAgoraHex(String value) {
        return value != null && value.matches("^[A-Fa-f0-9]{32}$");
    }
}