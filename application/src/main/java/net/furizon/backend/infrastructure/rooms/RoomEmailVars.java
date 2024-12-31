package net.furizon.backend.infrastructure.rooms;

import lombok.Getter;

public enum RoomEmailVars {
    OWNER_FURSONA_NAME(Format.ITALICS),
    FURSONA_NAME(Format.ITALICS),
    ROOM_NAME(Format.ITALICS),
    ROOM_TYPE_NAME(Format.ITALICS),
    EXCHANGE_ACTION_TEXT(Format.BOLD),
    EXCHANGE_LINK(Format.LINK),
    SANITY_CHECK_REASON(Format.BOLD)
    ;

    @Getter
    private Format[] formats;

    RoomEmailVars(Format ... format) {
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
