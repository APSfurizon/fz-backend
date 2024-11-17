package net.furizon.backend.infrastructure.pretix.autocart;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.service.annotation.GetExchange;

@Getter
@RequiredArgsConstructor
public enum AutocartActionType {
    BOOL('b'), VALUE('v'), DROPDOWN('d');
    private final char value;
}
