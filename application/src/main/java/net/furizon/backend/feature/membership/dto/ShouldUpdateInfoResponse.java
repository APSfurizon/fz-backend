package net.furizon.backend.feature.membership.dto;

import lombok.Data;

@Data
public class ShouldUpdateInfoResponse {
    public static final ShouldUpdateInfoResponse YES = new ShouldUpdateInfoResponse(true);
    public static final ShouldUpdateInfoResponse NO = new ShouldUpdateInfoResponse(false);


    private final boolean shouldUpdatePersonalInformation;
}
