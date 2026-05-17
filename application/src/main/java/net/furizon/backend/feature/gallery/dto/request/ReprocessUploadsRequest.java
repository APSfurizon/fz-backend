package net.furizon.backend.feature.gallery.dto.request;

import lombok.Data;

import java.util.Set;

@Data
public class ReprocessUploadsRequest {
    private final Set<Long> uploadIds;
}
