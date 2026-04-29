package com.education.platform.agora;

public class RtcTokenBuilder2 {

    public enum Role {
        ROLE_PUBLISHER,
        ROLE_SUBSCRIBER
    }

    public String buildTokenWithUid(
            String appId,
            String appCertificate,
            String channelName,
            int uid,
            Role role,
            int tokenExpire,
            int privilegeExpire
    ) {
        AccessToken2 accessToken = new AccessToken2(appId, appCertificate, tokenExpire);
        AccessToken2.ServiceRtc rtcService = new AccessToken2.ServiceRtc(channelName, AccessToken2.uidToString(uid));

        rtcService.addPrivilegeRtc(AccessToken2.PrivilegeRtc.PRIVILEGE_JOIN_CHANNEL, privilegeExpire);
        if (role == Role.ROLE_PUBLISHER) {
            rtcService.addPrivilegeRtc(AccessToken2.PrivilegeRtc.PRIVILEGE_PUBLISH_AUDIO_STREAM, privilegeExpire);
            rtcService.addPrivilegeRtc(AccessToken2.PrivilegeRtc.PRIVILEGE_PUBLISH_VIDEO_STREAM, privilegeExpire);
            rtcService.addPrivilegeRtc(AccessToken2.PrivilegeRtc.PRIVILEGE_PUBLISH_DATA_STREAM, privilegeExpire);
        }
        accessToken.addService(rtcService);

        try {
            return accessToken.build();
        } catch (Exception e) {
            return "";
        }
    }
}
