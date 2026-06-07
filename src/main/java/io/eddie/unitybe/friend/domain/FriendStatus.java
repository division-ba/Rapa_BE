package io.eddie.unitybe.friend.domain;

public enum FriendStatus {
    PENDING, ACCEPTED, DECLINED, CANCELLED, DELETED;
    public static FriendStatus fromString (String status) {
        for (FriendStatus s : FriendStatus.values()) {
            if (s.name().equalsIgnoreCase(status)) {
                return s;
            }
        }
        return null;
    }
}
