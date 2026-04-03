package net.furizon.backend.infrastructure.email;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum NotificationType {
    ADMIN_ORDER_OWNER_CHANGED_BUT_CARD_REGISTERED(0),
    ADMIN_ORDER_CARD_REMOVED_BUT_CARD_REGISTERED(1);

    private final int id;

    private static final Map<Integer, NotificationType> MAP = new HashMap<>();

    public static NotificationType get(int id) {
        return MAP.get(id);
    }

    static {
        for (var type : NotificationType.values()) {
            if (MAP.containsKey(type.getId())) {
                throw new IllegalStateException("Duplicate id for " + type);
            }
            MAP.put(type.getId(), type);
        }
    }
}
