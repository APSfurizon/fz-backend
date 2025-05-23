package net.furizon.backend.feature.admin.usecase.export;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.admin.dto.HotelExportRow;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
public class ExportHotelUseCase implements UseCase<PretixInformation, String> {
    @NotNull private final ObjectWriter writer;
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomFinder roomFinder;

    public ExportHotelUseCase(
            @NotNull final RoomFinder roomFinder,
            @NotNull final RoomLogic roomLogic,
            @NotNull final CsvMapper mapper
    ) {
        CsvSchema schema = mapper.schemaFor(HotelExportRow.class)
                                 .withHeader();
        this.writer = mapper.writer(schema);
        this.roomFinder = roomFinder;
        this.roomLogic = roomLogic;
    }

    @Override
    public @NotNull String executor(@NotNull PretixInformation pretixInformation) {
        List<HotelExportRow> rows = roomFinder.exportHotel(
                pretixInformation.getCurrentEvent().getId(),
                roomLogic,
                pretixInformation
        );
        log.debug("Export hotel row length: {}", rows.size());
        String out;
        try {
            out = writer.writeValueAsString(rows);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return out;
    }
}
