package net.furizon.backend.infrastructure.pretix.autocart;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AutocartActionType {
    BOOL('b'), VALUE('v'), DROPDOWN('d');
    private final char value;
}
