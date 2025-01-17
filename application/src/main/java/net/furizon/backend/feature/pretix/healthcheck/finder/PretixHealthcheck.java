package net.furizon.backend.feature.pretix.healthcheck.finder;

public interface PretixHealthcheck {
    boolean runHealthcheck();

    void waitForPretix();
}
