package net.furizon.backend.infrastructure.web;

public enum ApiCommonError {
    UNKNOWN;

    @Override
    public String toString() {
        return name();
    }
}
