package net.furizon.backend.infrastructure.pretix;

public class PretixGenericUtils {
    public static String buildOrgEventSlug(String eventSlug, String organizerSlug) {
        return eventSlug + "/" + organizerSlug;
    }
}
