package net.furizon.backend.feature.pretix.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class OrderWebhookRequest {
    @JsonProperty("notification_id")
    private final long notificationId;

    @NotNull
    private final String organizer;

    @NotNull
    private final String event;

    @NotNull
    private final String code;

    @NotNull
    private final String action;
}
