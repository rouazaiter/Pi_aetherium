package com.education.platform.services.interfaces;

import java.util.Map;

public interface IAgoraTokenService {
    String generateToken(String channelName, Long userId, int expireTime);

    String generateTokenForUser(String channelName, Long userId);

    Map<String, String> generateTokenForRoom(String channelName, Long userId);
}
