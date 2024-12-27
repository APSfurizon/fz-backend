package net.furizon.backend.feature.room.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ExchangeAction {
    @JsonProperty("room")
    TRASFER_EXCHANGE_ROOM,
    @JsonProperty("order")
    TRASFER_FULL_ORDER
}
