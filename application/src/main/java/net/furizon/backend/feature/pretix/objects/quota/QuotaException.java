package net.furizon.backend.feature.pretix.objects.quota;

public class QuotaException extends RuntimeException {
    public QuotaException(String message) {
        super(message);
    }
}
