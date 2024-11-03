package net.furizon.backend.feature.pretix.order;

import com.google.common.hash.Hashing;
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

import java.nio.charset.StandardCharsets;
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
    private final String code;

    @NotNull
    private OrderStatus orderStatus;

    @Builder.Default
    @NotNull
    private Sponsorship sponsorship = Sponsorship.NONE;

    @Builder.Default
    @NotNull
    private ExtraDays extraDays = ExtraDays.NONE;

    @NotNull
    private final Set<Integer> dailyDays;

    @Builder.Default
    private short roomCapacity = 0; // 0 = has no room

    @Nullable
    private String hotelLocation;

    @NotNull
    private String pretixOrderSecret;

    //Manually defining getters/setters because lombok's default names are ugly lol
    @Builder.Default
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private boolean hasMembership = false;

    @Builder.Default
    private int answersMainPositionId = -1;

    @Nullable
    private User orderOwner;

    @NotNull
    private Event orderEvent;

    @NotNull
    @Setter(AccessLevel.NONE)
    private Map<String, Object> answers;

    public int getId() {
        return Hashing.sha512().hashString(code, StandardCharsets.UTF_8).asInt();
    }

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

    public long getDailyDaysBitmask() {
        long ret = 0L;
        for (int day : dailyDays) {
            ret |= (1L << day);
        }
        return ret;
    }

    public List<PretixAnswer> getAllAnswers(@NotNull PretixInformation pretixInformation) {
        final var list = new ArrayList<PretixAnswer>();
        final var answers = getAnswers();
        for (String key : answers.keySet()) {
            final var questionId = pretixInformation.getQuestionIdFromIdentifier(key);
            if (questionId.isEmpty()) {
                continue;
            }

            int id = questionId.get();
            var type = pretixInformation.getQuestionTypeFromId(id);
            if (type.isEmpty()) {
                continue;
            }
            Object o = answers.get(key);
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

        return list;
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
            this.answers = new HashMap<>();
            for (PretixAnswer answer : answers) {
                int questionId = answer.getQuestionId();
                var identifier = pi.getQuestionIdentifierFromId(questionId);
                if (identifier.isEmpty()) {
                    throw new IllegalArgumentException("Answer identifier is empty");
                }

                String answerIdentifier = identifier.get();
                String value = answer.getAnswer();
                if (value == null) {
                    throw new IllegalArgumentException("Answer " + answerIdentifier + " has null value");
                }

                var type = pi.getQuestionTypeFromId(questionId);
                if (type.isPresent()) {
                    Object o = switch (type.get()) {
                        case NUMBER -> Float.parseFloat(value);
                        case STRING_ONE_LINE, STRING_MULTI_LINE, COUNTRY_CODE, PHONE_NUMBER,
                             LIST_SINGLE_CHOICE -> value;
                        case BOOLEAN -> value.equalsIgnoreCase("true")
                            || value.equalsIgnoreCase("yes");
                        case LIST_MULTIPLE_CHOICE -> value.split(", ");
                        case FILE -> Const.QUESTIONS_FILE_KEEP;
                        case DATE -> LocalDate.parse(value);
                        case TIME -> LocalTime.parse(value);
                        case DATE_TIME -> ZonedDateTime.parse(value, PretixGenericUtils.PRETIX_DATETIME_FORMAT);
                    };
                    this.answers.put(answerIdentifier, o);
                }
            }
            return this;
        }
    }
}
