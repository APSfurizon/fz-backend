package net.furizon.backend.feature.pretix.objects.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderWebhookRequest {
    @JsonProperty("notification_id")
    private final long notificationId;

    @NotNull
    @NotEmpty
    private final String organizer;

    @NotNull
    @NotEmpty
    private final String event;

    @NotNull
    @NotEmpty
    private final String code;

    @NotNull
    @NotEmpty
    private final String action;
}
