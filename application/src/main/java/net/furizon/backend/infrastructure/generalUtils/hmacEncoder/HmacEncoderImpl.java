package net.furizon.backend.infrastructure.generalUtils.hmacEncoder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class HmacEncoderImpl implements HmacEncoder {
    @NotNull
    private final ObjectMapper mapper;

    @Override
    public HmacResponseBytes encode(@NotNull String data, @NotNull String key) {
        byte[] dataBytes = data.getBytes();
        return new HmacResponseBytes(dataBytes, encode(dataBytes, key));
    }

    @Override
    public HmacResponseString encode(@NotNull Object object, @NotNull String key) throws JsonProcessingException {
        String data = mapper.writeValueAsString(object);
        return new HmacResponseString(data, encode(data.getBytes(), key));
    }

    @Override
    public byte[] encode(byte[] dataBytes, @NotNull String key) {
        HMac hmac = new HMac(new SHA256Digest());
        hmac.init(new KeyParameter(key.getBytes()));
        hmac.update(dataBytes, 0, dataBytes.length);
        byte[] hmacOut = new byte[hmac.getMacSize()];
        hmac.doFinal(hmacOut, 0);
        return hmacOut;
    }


    @Override
    public <T> HmacVerifyResultObject<T> verifyAndGetObject(byte[] hmacBytes,
                                                            byte[] dataBytes,
                                                            @NotNull String key,
                                                            @NotNull Class<T> clazz) throws JsonProcessingException {
        var res = verify(hmacBytes, dataBytes, key);
        T object = null;
        if (res.isValid()) {
            object = mapper.readValue(new String(dataBytes), clazz);
        }
        return new HmacVerifyResultObject<>(res.isValid(), res.hmacOut(), object);
    }

    @Override
    public HmacVerifyResult verify(byte[] hmacBytes, byte[] dataBytes, @NotNull String key) {
        HMac hmac = new HMac(new SHA256Digest());
        hmac.init(new KeyParameter(key.getBytes()));

        hmac.update(dataBytes, 0, dataBytes.length);
        byte[] hmacOut = new byte[hmac.getMacSize()];
        hmac.doFinal(hmacOut, 0);

        return new HmacVerifyResult(Arrays.equals(hmacBytes, hmacOut), hmacOut);
    }

}
