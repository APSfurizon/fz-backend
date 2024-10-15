package net.furizon.backend.security.permissions;

public enum Permission {
    /** Allows the user to change user's group of permissions */
    UPGRADE_USERS("UPGRADE_USERS"),
    /** Allows the user to ban/disable login of a user */
    BAN_USERS("BAN_USERS"),
    /** Allows the user to access the admin area */
    ACCESS_ADMIN_AREA("ACCESS_ADMIN_AREA"),
    /** Allows the user to download badges */
    DOWNLOAD_BADGES("DOWNLOAD_BADGES"),
    /** Allows the user to review and flag uploads */
    REVIEW_UPLOADS("REVIEW_UPLOADS");

    private String value;

    Permission(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}