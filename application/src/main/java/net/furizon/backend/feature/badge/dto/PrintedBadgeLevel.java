package net.furizon.backend.feature.badge.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public enum PrintedBadgeLevel {
    //For your own convention you may want to change this
    MAIN_STAFF("badge-level-staff-main"),
    JUNIOR_STAFF("badge-level-staff-junior"),
    SUPER_SPONSOR("badge-level-sponsor-normal"),
    NORMAL_SPONSOR("badge-level-sponsor-super"),
    NORMAL_BADGE("badge-level-normal"),
    ;

    @Getter
    private final @NotNull String name;

    @Override
    public String toString() {
        return name;
    }

}
