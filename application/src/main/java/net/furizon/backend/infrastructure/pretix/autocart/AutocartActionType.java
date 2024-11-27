package net.furizon.backend.infrastructure.pretix.autocart;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AutocartActionType {
    BOOL('b'),
    INPUT('i'),
    TEXT_AREA('t'),
    DROPDOWN('d');

    private final char value;
}
