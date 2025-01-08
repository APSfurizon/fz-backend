package net.furizon.backend.feature.pretix.objects.product;

import org.jetbrains.annotations.NotNull;

public record HotelCapacityPair(@NotNull String hotelInternalName, short capacity) {}
