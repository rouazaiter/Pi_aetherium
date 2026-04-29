package com.education.platform.agora;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

final class AccessToken2 {

    static final short SERVICE_TYPE_RTC = 1;
    static final String VERSION = "007";

    enum PrivilegeRtc {
        PRIVILEGE_JOIN_CHANNEL(1),
        PRIVILEGE_PUBLISH_AUDIO_STREAM(2),
        PRIVILEGE_PUBLISH_VIDEO_STREAM(3),
        PRIVILEGE_PUBLISH_DATA_STREAM(4);

        final short value;

        PrivilegeRtc(int value) {
            this.value = (short) value;
        }
    }

    static class Service {
        final short type;
        final TreeMap<Short, Integer> privileges = new TreeMap<>();

        Service(short type) {
            this.type = type;
        }

        void addPrivilegeRtc(PrivilegeRtc privilege, int expire) {
            this.privileges.put(privilege.value, expire);
        }

        TokenByteBuf pack(TokenByteBuf buf) {
            return buf.put(type).putIntMap(privileges);
        }
    }

    static class ServiceRtc extends Service {
        private final String channelName;
        private final String uid;

        ServiceRtc(String channelName, String uid) {
            super(SERVICE_TYPE_RTC);
            this.channelName = channelName;
            this.uid = uid;
        }

        @Override
        TokenByteBuf pack(TokenByteBuf buf) {
            return super.pack(buf).put(channelName).put(uid);
        }
    }

    private final String appId;
    private final String appCertificate;
    private final int expire;
    private final int issueTs;
    private final int salt;
    private final TreeMap<Short, Service> services = new TreeMap<>();

    AccessToken2(String appId, String appCertificate, int expire) {
        this.appId = appId;
        this.appCertificate = appCertificate;
        this.expire = expire;
        this.issueTs = TokenUtils.nowSeconds();
        this.salt = TokenUtils.randomInt();
    }

    void addService(Service service) {
        services.put(service.type, service);
    }

    String build() throws Exception {
        if (!TokenUtils.isAgoraUuid(appId) || !TokenUtils.isAgoraUuid(appCertificate)) {
            return "";
        }

        TokenByteBuf tokenBody = new TokenByteBuf()
                .put(appId)
                .put(issueTs)
                .put(expire)
                .put(salt)
                .put((short) services.size());

        for (Map.Entry<Short, Service> entry : services.entrySet()) {
            entry.getValue().pack(tokenBody);
        }

        byte[] signingKey = createSigningKey();
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(signingKey, "HmacSHA256"));
        byte[] signature = mac.doFinal(tokenBody.asBytes());

        TokenByteBuf finalBuffer = new TokenByteBuf();
        finalBuffer.put(signature);
        finalBuffer.put(tokenBody.asBytes());
        return VERSION + TokenUtils.base64Encode(TokenUtils.compress(finalBuffer.asBytes()));
    }

    private byte[] createSigningKey() throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(new TokenByteBuf().put(issueTs).asBytes(), "HmacSHA256"));
        byte[] signing = mac.doFinal(appCertificate.getBytes(StandardCharsets.UTF_8));
        mac.init(new SecretKeySpec(new TokenByteBuf().put(salt).asBytes(), "HmacSHA256"));
        return mac.doFinal(signing);
    }

    static String uidToString(int uid) {
        if (uid == 0) {
            return "";
        }
        return String.valueOf(uid & 0xFFFFFFFFL);
    }
}
