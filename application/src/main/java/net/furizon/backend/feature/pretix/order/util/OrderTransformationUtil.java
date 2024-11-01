package net.furizon.backend.feature.pretix.order.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.order.Order;
import net.furizon.backend.feature.pretix.order.PretixAnswer;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderTransformationUtil {
    private final ObjectMapper objectMapper;

    // @Lazy annotation will help here to avoid
    //circular-injection, but let's avoid to reuse it
    @Lazy
    private final PretixInformation pretixInformation;

    @NotNull
    public String getAnswersAsJson(@NotNull Order order) {
        final var list = new ArrayList<PretixAnswer>();
        final var answers = order.getAnswers();

        for (String key : answers.keySet()) {
            Object o = answers.get(key);
            var identifierOpt = pretixInformation.getQuestionIdFromIdentifier(key);
            if (identifierOpt.isPresent()) {
                int id = identifierOpt.get();
                var type = pretixInformation.getQuestionTypeFromId(id);
                if (type.isPresent()) {
                    String out = switch (type.get()) {
                        case NUMBER -> Float.toString((float) o);
                        case STRING_ONE_LINE, FILE, COUNTRY_CODE, PHONE_NUMBER, STRING_MULTI_LINE, LIST_SINGLE_CHOICE ->
                            (String) o;
                        case BOOLEAN -> ((boolean) o) ? "True" : "False"; //fuck python
                        case LIST_MULTIPLE_CHOICE -> String.join(", ", (String[]) o);
                        case DATE, TIME -> o.toString();
                        case DATE_TIME -> ((ZonedDateTime) o).format(PretixGenericUtils.PRETIX_DATETIME_FORMAT);
                    };

                    if (out != null && !(out = out.strip()).isEmpty()) {
                        list.add(new PretixAnswer(id, out));
                    }
                }
            }
        }

        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.error("Error while serializing answers for order {}", order.getCode(), e);
            return "[]";
        }
    }
}
