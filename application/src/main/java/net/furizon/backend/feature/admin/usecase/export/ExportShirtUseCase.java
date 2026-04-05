package net.furizon.backend.feature.admin.usecase.export;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.admin.dto.ShirtExportRow;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ExportShirtUseCase implements UseCase<PretixInformation, String> {
    @NotNull private final ObjectWriter writer;
    @NotNull private final UserFinder userFinder;

    public ExportShirtUseCase(
            @NotNull final UserFinder userFinder,
            @NotNull final CsvMapper mapper
    ) {
        CsvSchema schema = mapper.schemaFor(ShirtExportRow.class)
                                 .withHeader();
        this.writer = mapper.writer(schema);
        this.userFinder = userFinder;
    }

    @Override
    public @NotNull String executor(@NotNull PretixInformation pretixInformation) {
        long eventId = pretixInformation.getCurrentEvent().getId();
        List<ShirtExportRow> rows = userFinder.exportShirts(eventId);
        log.debug("Export shirts row length: {}", rows.size());
        String out;
        try {
            out = writer.writeValueAsString(rows);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return out;
    }
}
