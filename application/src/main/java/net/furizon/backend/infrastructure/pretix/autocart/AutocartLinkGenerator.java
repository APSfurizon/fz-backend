package net.furizon.backend.infrastructure.pretix.autocart;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Data
@Component
@RequiredArgsConstructor
public class AutocartLinkGenerator {
    @NotNull
    private final ObjectMapper objectMapper;

    @NotNull
    private final PretixConfig pretixConfig;

    @NotNull
    private final PrivateKey privateKey;

    @NotNull
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    @NotNull
    public Optional<String> generateUrl(@NotNull List<AutocartAction<?>> actions) {

        Map<String, String> data = new LinkedHashMap<>();
        for (AutocartAction<?> action : actions) {
            AutocartActionType type = action.type();

            if (type != AutocartActionType.BOOL) {
                data.put(action.targetId(), type.getValue() + action.value().toString());
            } else {
                boolean val = (boolean) action.value();
                data.put(action.targetId(), type.getValue() + (val ? "1" : "0"));
            }
        }

        String json;
        try {
            json = objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("Error while generating json string for autocart data. Map: {}", data);
            return Optional.empty();
        }

        byte[] encodedDataBytes = encoder.encode(json.getBytes(StandardCharsets.UTF_8));
        String encodedData = new String(encodedDataBytes, StandardCharsets.UTF_8);


        String encodedSignature;
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(privateKey);
            sig.update(encodedDataBytes);
            byte[] signedData = sig.sign();
            encodedSignature = encoder.encodeToString(signedData);

        } catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException e) {
            log.error("Error while generating signature for autocart data. Error: {}. Map: {}", e.getMessage(), data);
            return Optional.empty();
        }


        return Optional.of(pretixConfig.getShop().getUrl() + "#a=" + encodedData + "&s=" + encodedSignature);
    }
}
