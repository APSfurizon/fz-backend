package net.furizon.backend.infrastructure.web;

public enum ApiCommonErrorCode {
    UNKNOWN;

    @Override
    public String toString() {
        return name();
    }
}
