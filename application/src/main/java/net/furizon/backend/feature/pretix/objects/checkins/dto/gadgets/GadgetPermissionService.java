package net.furizon.backend.feature.pretix.objects.checkins.dto.gadgets;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GadgetPermissionService {
    @NotNull
    private final Map<Permission, List<Gadget>> gadgets = new EnumMap<>(Permission.class);

    @NotNull
    private final PretixConfig pretixConfig;

    @NotNull
    private final ObjectMapper objectMapper;

    public @NotNull List<Gadget> getGadgetsForPermission(@NotNull Permission permission) {
        return gadgets.getOrDefault(permission, Collections.emptyList());
    }


    @PostConstruct
    public void init() {
        log.info("Loading gadget permissions");
        String filename = pretixConfig.getGadgetPermissionFile();
        if (filename == null || filename.isBlank()) {
            log.warn("No gadget permission file configured");
            return;
        }
        Path path = Paths.get(filename);

        if (Files.notExists(path)) {
            log.warn("Gadget permission file {} does not exist", filename);
            return;
        }

        try {
            byte[] data = Files.readAllBytes(path);
            List<GadgetPermission> l = objectMapper.readValue(data, new TypeReference<List<GadgetPermission>>() {});

            Map<Permission, GadgetManager> gadgets = new EnumMap<>(Permission.class);
            for (GadgetPermission gadgetPermission : l) {
                Permission p = Permission.get(gadgetPermission.getPermission());
                GadgetManager gadget = gadgets.computeIfAbsent(p, k -> new GadgetManager());
                gadget.addAll(gadgetPermission.getGadgets());
            }

            for (Map.Entry<Permission, GadgetManager> entry : gadgets.entrySet()) {
                this.gadgets.put(entry.getKey(), entry.getValue().getGadgets());
            }
        } catch (IOException e) {
            log.error("Error reading gadget permission file {}", filename, e);
        }
    }
}
