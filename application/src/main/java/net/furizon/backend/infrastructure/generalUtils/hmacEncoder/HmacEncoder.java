package net.furizon.backend.infrastructure.generalUtils.hmacEncoder;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface HmacEncoder {
    HmacResponseBytes encode(@NotNull String data, @NotNull String key);

    HmacResponseString encode(@NotNull Object object, @NotNull String key) throws JsonProcessingException;

    byte[] encode(byte[] dataBytes, @NotNull String key);

    <T> HmacVerifyResultObject<T> verifyAndGetObject(byte[] hmacBytes,
                                                     byte[] dataBytes,
                                                     @NotNull String key,
                                                     @NotNull Class<T> clazz) throws JsonProcessingException;

    HmacVerifyResult verify(byte[] hmacBytes, byte[] dataBytes, @NotNull String key);

    record HmacResponseBytes(
            byte[] dataBytes,
            byte[] hmacOut
    ) {}

    record HmacResponseString(
            @NotNull String data,
            byte[] hmacOut
    ) {}

    record HmacVerifyResult(
            boolean isValid,
            byte[] hmacOut
    ) {}

    record HmacVerifyResultObject<T>(
            boolean isValid,
            byte[] hmacOut,
            @Nullable T object
    ) {}
}
