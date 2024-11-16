package net.furizon.backend.infrastructure.pretix.autocart;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AutocartActionType {
    BOOL('b'), VALUE('v');
    @Getter
    private final char value;
}
