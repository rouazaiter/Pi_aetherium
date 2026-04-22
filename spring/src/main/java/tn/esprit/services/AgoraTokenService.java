package tn.esprit.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class AgoraTokenService {

    @Value("${agora.app.id:}")
    private String appId;

    @Value("${agora.app.certificate:}")
    private String appCertificate;

    @Value("${agora.token.expiry:3600}")
    private int tokenExpiry;

    public String generateToken(String channelName, Long userId, int expireTime) {
        if (appId == null || appId.isEmpty() || appCertificate == null || appCertificate.isEmpty()) {
            return "";
        }

        long issueTs = System.currentTimeMillis() / 1000;
        long expire = issueTs + (expireTime > 0 ? expireTime : tokenExpiry);

        String uidStr = userId != null ? userId.toString() : "0";
        String signature = generateSignature(channelName, uidStr, String.valueOf(issueTs), String.valueOf(expire));

        return String.format("%s:%s:%s:%s:%s",
                appId,
                userId,
                issueTs,
                expire,
                signature);
    }

    public String generateTokenForUser(String channelName, Long userId) {
        return generateToken(channelName, userId, tokenExpiry);
    }

    private String generateSignature(String channelName, String uid, String issueTs, String expire) {
        String signature = "";
        try {
            String rawSignature = appId + channelName + uid + issueTs + expire;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(appCertificate.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(rawSignature.getBytes(StandardCharsets.UTF_8));
            signature = Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return signature;
    }

    public Map<String, String> generateTokenForRoom(String channelName, Long userId) {
        Map<String, String> result = new HashMap<>();
        String token = generateTokenForUser(channelName, userId);
        result.put("token", token);
        result.put("appId", appId);
        result.put("channelName", channelName);
        return result;
    }
}