package net.furizon.backend.infrastructure.web.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@Builder
public class MethodArgumentNotValidError {
    public final String message;
}
