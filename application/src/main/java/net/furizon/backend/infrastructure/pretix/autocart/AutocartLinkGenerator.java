package net.furizon.backend.infrastructure.pretix.autocart;

import ch.qos.logback.core.CoreConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.util.*;

@Slf4j
@Data
@Component
@RequiredArgsConstructor
public class AutocartLinkGenerator {
    @NotNull
    private final ObjectMapper objectMapper;

    @NotNull
    @Value("${pretix.shop-url}")
    private String shopUrl = "";

    @NotNull
    private final PrivateKey privateKey;

    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    @PostConstruct
    public void init() {
        List<AutocartAction<?>> actions = new ArrayList<>();
        var s = generateUrl(actions);
        System.out.println(s);
    }

    @NotNull
    public Optional<String> generateUrl(@NotNull List<AutocartAction<?>> actions) {

        Map<String, String> data = new LinkedHashMap<>();
        for (AutocartAction<?> action : actions) {
            AutocartActionType type = action.getType();

            if (type == AutocartActionType.VALUE) {
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


        return Optional.of(shopUrl + "#a=" + encodedData + "&s=" + encodedSignature);
    }
}
