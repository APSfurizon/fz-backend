package net.furizon.backend.feature.pretix.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.event.Event;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.infrastructure.pretix.Const;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Slf4j
public class Order {

    @NotNull
    @Setter(AccessLevel.NONE)
    private String code;

    @NotNull
    private OrderStatus orderStatus;

    @NotNull
    private Sponsorship sponsorship = Sponsorship.NONE;

    @NotNull
    private ExtraDays extraDays = ExtraDays.NONE;

    @NotNull
    private final Set<Integer> dailyDays;

    private short roomCapacity = 0; // 0 = has no room

    @Nullable
    private String hotelLocation;

    @NotNull
    private String pretixOrderSecret;

    //Manually defining getters/setters because lombok's default names are ugly lol
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private boolean hasMembership = false;

    private int answersMainPositionId = -1;

    @Nullable
    private User orderOwner; //TODO load from db! (lazy load?)

    @NotNull
    private Event orderEvent; //TODO load from db! (lazy load?)

    @NotNull
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Map<String, Object> answers;

    public boolean isDaily() {
        return !dailyDays.isEmpty();
    }

    public boolean hasDay(int day) {
        return dailyDays.contains(day);
    }

    public boolean hasMembership() {
        return hasMembership;
    }
    public void setMembership(boolean membership) {
        this.hasMembership = membership;
    }

    @NotNull
    public Optional<Object> getAnswer(String questionIdentifier) {
        return Optional.ofNullable(answers.get(questionIdentifier));
    }
    public boolean hasAnswer(String questionIdentifier) {
        return answers.containsKey(questionIdentifier);
    }
    public boolean deleteAnswer(String questionIdentifier) {
        return answers.remove(questionIdentifier) != null;
    }
    public void setAnswer(String questionIdentifier, Object answer) {
        if (answer instanceof String
                || answer instanceof Float
                || answer instanceof Boolean
                || answer instanceof String[]
                || answer instanceof LocalDate
                || answer instanceof LocalTime
                || answer instanceof ZonedDateTime) {
            answers.put(questionIdentifier, answer);
        } else {
            throw new IllegalArgumentException("answer must be of one of the following types: "
                    + "String, "
                    + "Float, "
                    + "Boolean, "
                    + "String[], "
                    + "LocalDate, "
                    + "LocalTime, "
                    + "ZonedDateTime");
        }
    }

    @NotNull
    public String getAnswersJson(@NotNull PretixInformation pi) {
        List<PretixAnswer> answers = new ArrayList<>();
        for (String key : this.answers.keySet()) {
            Object o = this.answers.get(key);
            var identifierOpt = pi.getQuestionIdFromIdentifier(key);
            if (identifierOpt.isPresent()) {
                int id = identifierOpt.get();
                var type = pi.getQuestionTypeFromId(id);
                if (type.isPresent()) {
                    String out = switch (type.get()) {
                        case NUMBER -> Float.toString((float) o);
                        case STRING_ONE_LINE -> (String) o;
                        case STRING_MULTI_LINE -> (String) o;
                        case BOOLEAN -> ((boolean) o) ? "True" : "False"; //fuck python
                        case LIST_SINGLE_CHOICE -> (String) o;
                        case LIST_MULTIPLE_CHOICE -> String.join(", ", (String[]) o);
                        case FILE -> (String) o;
                        case DATE -> o.toString();
                        case TIME -> o.toString();
                        case DATE_TIME -> ((ZonedDateTime) o).format(PretixGenericUtils.PRETIX_DATETIME_FORMAT);
                        case COUNTRY_CODE -> (String) o;
                        case PHONE_NUMBER -> (String) o;
                    };

                    if (out != null && !(out = out.strip()).isEmpty()) {
                        answers.add(new PretixAnswer(id, out));
                    }
                }
            }
        }

        ObjectMapper om = new ObjectMapper();
        try {
            return om.writeValueAsString(answers);
        } catch (JsonProcessingException e) {
            log.error("Error while serializing answers for order {}", getCode(), e);
            return "[]";
        }
    }

    public long getDailyDaysBitmask() {
        long ret = 0L;
        for (int day : dailyDays) {
            ret |= (1L << day);
        }
        return ret;
    }

    public static class OrderBuilder {

        public OrderBuilder dailyDays(Set<Integer> days) {
            dailyDays = days;
            return this;
        }
        public OrderBuilder dailyDays(long days) {
            dailyDays = new TreeSet<>();
            for (int i = 0; i < 63; i++) {
                if ((days & (1L << i)) != 0L) {
                    dailyDays.add(i);
                }
            }
            return this;
        }

        public OrderBuilder answers(@NotNull List<PretixAnswer> answers, @NotNull PretixInformation pi) {
            this.answers = new HashMap<String, Object>();
            for (PretixAnswer answer : answers) {
                int questionId = answer.getQuestionId();
                var identifier = pi.getQuestionIdentifierFromId(questionId);
                if (identifier.isPresent()) {
                    String answerIdentifier = identifier.get();
                    String value = answer.getAnswer();
                    if (value != null) {
                        var type = pi.getQuestionTypeFromId(questionId);
                        if (type.isPresent()) {
                            Object o = switch (type.get()) {
                                case NUMBER -> Float.parseFloat(value);
                                case STRING_ONE_LINE -> value;
                                case STRING_MULTI_LINE -> value;
                                case BOOLEAN -> value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes");
                                case LIST_SINGLE_CHOICE -> value;
                                case LIST_MULTIPLE_CHOICE -> value.split(", ");
                                case FILE -> Const.QUESTIONS_FILE_KEEP;
                                case DATE -> LocalDate.parse(value);
                                case TIME -> LocalTime.parse(value);
                                case DATE_TIME -> ZonedDateTime.parse(value, PretixGenericUtils.PRETIX_DATETIME_FORMAT);
                                case COUNTRY_CODE -> value;
                                case PHONE_NUMBER -> value;
                            };
                            this.answers.put(answerIdentifier, o);
                        }
                    }
                }
            }
            return this;
        }
        public OrderBuilder answers(@NotNull String answersStr, @NotNull PretixInformation pi) {
            try {
                ObjectMapper om = new ObjectMapper();
                List<PretixAnswer> answers = om.readValue(answersStr, new TypeReference<List<PretixAnswer>>() {});
                return answers(answers, pi);
            } catch (JsonProcessingException e) {
                log.error("Unable to parse answers json", e);
            }
            return this;
        }
    }
}
