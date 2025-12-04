package net.furizon.backend.infrastructure.pretix.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.TreeMap;

@Slf4j
@AllArgsConstructor
public enum Sponsorship {
    //These MUST be in "importance" order
    NONE((short)          0),
    SPONSOR((short)       1),
    SUPER_SPONSOR((short) 2),
    ULTRA_SPONSOR((short) 3);

    @Getter
    private final short dbId;

    private static final Map<Short, Sponsorship> DB_TABLE = new TreeMap<>();

    static {
        for (Sponsorship sponsor : Sponsorship.values()) {
            if (DB_TABLE.containsKey(sponsor.getDbId())) {
                throw new RuntimeException("DUPLICATE SPONSORSHIP DATABASE ID: " + sponsor.getDbId());
            }
            DB_TABLE.put(sponsor.getDbId(), sponsor);
        }
    }

    @NotNull
    public static Sponsorship getFromDbId(short dbId) {
        Sponsorship s = DB_TABLE.get(dbId);
        return s == null ? Sponsorship.NONE : s;
    }
}
