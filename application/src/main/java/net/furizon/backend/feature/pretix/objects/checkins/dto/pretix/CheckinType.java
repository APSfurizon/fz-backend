package net.furizon.backend.feature.pretix.objects.checkins.dto.pretix;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CheckinType {
    @JsonProperty("entry")
    ENTRY,
    @JsonProperty("exit")
    EXIT
}