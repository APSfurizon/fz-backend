package net.furizon.backend.infrastructure.email;

import lombok.Getter;

public enum EmailVars {
    OWNER_FURSONA_NAME(Format.ITALICS),
    FURSONA_NAME(Format.ITALICS),
    ROOM_NAME(Format.ITALICS),
    ROOM_TYPE_NAME(Format.ITALICS),
    EXCHANGE_ACTION_TEXT(Format.BOLD),
    EXCHANGE_LINK(Format.LINK),
    SANITY_CHECK_REASON(Format.BOLD),
    EVENT_NAME(Format.ITALICS),
    ORDER_CODE(Format.BOLD),
    DUPLICATE_ORDER_CODE(Format.BOLD),
    ;

    @Getter
    private Format[] formats;

    EmailVars(Format ... format) {
        formats = format;
    }

    @Override
    public String toString() {
        return "%" + this.name() + "%";
    }

    public enum Format {
        ITALICS,
        BOLD,
        LINK
    }
}
