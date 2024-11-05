package net.furizon.backend.infrastructure.web;

public enum ApiCommonErrorCode {
    UNKNOWN,
    UNAUTHENTICATED,
    SESSION_NOT_FOUND,
    ;

    @Override
    public String toString() {
        return name();
    }
}
