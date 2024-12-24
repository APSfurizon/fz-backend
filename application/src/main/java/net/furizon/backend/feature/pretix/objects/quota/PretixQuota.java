package net.furizon.backend.feature.pretix.objects.quota;

import lombok.Data;

import java.util.List;

@Data
public class PretixQuota {
    private final long id;
    private final String name;
    private final long size;
    private final List<Long> items;
}
