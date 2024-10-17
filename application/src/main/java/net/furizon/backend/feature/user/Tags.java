package net.furizon.backend.feature.user;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Tags {
    FURSUIT("fursuit"),
    UPLOAD_AREA("upload_area"),
    BADGE("badge"),
    BADGE_MAIN("badge_main");

    public final String label;
}
