package net.furizon.backend.infrastructure.pretix.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.pretix.PretixConst;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.TreeMap;

@Slf4j
@AllArgsConstructor
public enum Board {
    NONE((short) 0),
    HALF((short) 1),
    FULL((short) 2);

    @Getter
    private final short dbId;

    private static final Map<Short, Board> DB_TABLE = new TreeMap<>();

    static {
        for (Board board : Board.values()) {
            if (DB_TABLE.containsKey(board.getDbId())) {
                throw new RuntimeException("DUPLICATE BOARD DATABASE ID: " + board.getDbId());
            }
            DB_TABLE.put(board.getDbId(), board);
        }
    }

    @NotNull
    public static Board getFromDbId(short dbId) {
        Board b = DB_TABLE.get(dbId);
        return b == null ? Board.NONE : b;
    }

    @NotNull
    public static Board fromPretixString(@NotNull String s) {
        if (s.equalsIgnoreCase(PretixConst.METADATA_BOARD_FULL_VARIATIONS_TAG)) {
            return FULL;
        } else if (s.equalsIgnoreCase(PretixConst.METADATA_BOARD_HALF_VARIATIONS_TAG)) {
            return HALF;
        }
        log.error("Unable to parse pretix board string: {}", s);
        return NONE;
    }
}
