package net.furizon.backend.feature.pretix.objects.order;

import com.google.common.hash.Hashing;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.event.finder.EventFinder;
import net.furizon.backend.feature.room.dto.RoomData;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.pretix.PretixConst;
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
import java.util.Collections;
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

    @Nullable
    @Builder.Default
    private Long pretixRoomItemId = -1L;
    @Builder.Default
    private short roomCapacity = 0; // 0 = has no room
    @Nullable
    private String hotelInternalName;
    @Nullable
    private String roomInternalName;


    @NotNull
    private String pretixOrderSecret;
    @Nullable
    private String checkinSecret;

    //Manually defining getters/setters because lombok's default names are ugly lol
    @Builder.Default
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private boolean hasMembership = false;

    @Builder.Default
    private long ticketPositionId = -1L;
    @Builder.Default
    private long ticketPositionPosid = -1L;
    @Nullable
    private Long roomPositionId;
    @Nullable
    private Long roomPositionPosid;
    @Nullable
    private Long earlyPositionId;
    @Nullable
    private Long latePositionId;

    @Builder.Default
    private short extraFursuits = 0;

    @Nullable
    private final String buyerEmail;
    @Nullable
    private final String buyerPhone;
    @Nullable
    private final String buyerUser;
    @Nullable
    private final String buyerLocale;

    private final boolean requireAttention;
    @Nullable private final String internalComment;
    @Nullable private final String checkinText;

    private final long orderSerialInEvent;

    @Nullable
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Long orderOwnerUserId = null;
    @Nullable
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private User orderOwner;
    @NotNull
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private UserFinder userFinder;

    private final long eventId; //eventId CANNOT change
    @Nullable
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Event orderEvent;
    @NotNull
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private EventFinder eventFinder;

    @NotNull
    @Setter(AccessLevel.NONE)
    private Map<String, Object> answers;
    @NotNull
    @Setter(AccessLevel.NONE)
    private Map<String, List<Long>> answerIdentifierToOptionId;
    @NotNull
    @Setter(AccessLevel.NONE)
    private Map<String, List<String>> answerIdentifierToOptionIdentifier;


    public String toFullString() {
        return "code=" + code + " orderStatus=" + orderStatus
            + " sponsorship=" + sponsorship + " extraDays=" + extraDays + " dailyDays=" + dailyDays
            + " pretixRoomItemId=" + pretixRoomItemId + " roomCapacity=" + roomCapacity
            + " hotelInternalName=" + hotelInternalName + " roomInternalName=" + roomInternalName
            + " pretixOrderSecret=" + pretixOrderSecret + " hasMembership=" + hasMembership
            + " ticketPositionId=" + ticketPositionId + " ticketPositionPosid=" + ticketPositionPosid
            + " roomPositionId=" + roomPositionId + " roomPositionPosid=" + roomPositionPosid
            + " earlyPositionId=" + earlyPositionId + " latePositionId=" + latePositionId
            + " extraFursuits=" + extraFursuits
            + " orderOwnerUserId=" + orderOwnerUserId + " eventId=" + eventId
            + " buyerEmail=" + buyerEmail + " buyerPhone=" + buyerPhone
            + " buyerUser=" + buyerUser + " buyerLocale=" + buyerLocale;
    }


    public long getId() {
        return Hashing.sha512().hashString(code + String.format("%016x", eventId), StandardCharsets.UTF_8).asLong();
    }

    public boolean isDaily() {
        return !dailyDays.isEmpty();
    }
    public boolean hasDay(int day) {
        return dailyDays.contains(day);
    }
    public long getDailyDaysBitmask() {
        long ret = 0L;
        for (int day : dailyDays) {
            ret |= (1L << day);
        }
        return ret;
    }

    //we can't use roomPositionId to validate, since it IS set even when we have a NO_ROOM item
    public boolean hasRoom() {
        return roomCapacity > 0;
    }

    public boolean hasMembership() {
        return hasMembership;
    }
    public void setMembership(boolean membership) {
        this.hasMembership = membership;
    }

    public void setOrderOwnerUserId(@Nullable Long userId) {
        this.orderOwnerUserId = userId;
        orderOwner = null;
        //update userId answer
        setAnswer(PretixConst.QUESTIONS_ACCOUNT_USERID, userId == null ? null : Float.valueOf(userId));
    }
    public void setOrderOwner(@NotNull User orderOwner) {
        this.orderOwner = orderOwner;
        orderOwnerUserId = orderOwner.getId();
        setAnswer(PretixConst.QUESTIONS_ACCOUNT_USERID, Float.valueOf(orderOwnerUserId)); //update userId answer
    }
    public @Nullable User getOrderOwner() {
        if (orderOwner == null && orderOwnerUserId != null && orderOwnerUserId >= 0L) {
            orderOwner = userFinder.findById(orderOwnerUserId);
        }
        return orderOwner;
    }

    public @NotNull Event getOrderEvent() {
        if (orderEvent == null) {
            orderEvent = eventFinder.findEventById(eventId);
            if (orderEvent == null) {
                throw new RuntimeException("Event " + eventId + " not found");
            }
        }
        return orderEvent;
    }

    public @Nullable RoomData getBoughtRoomData(PretixInformation pretixInformation) {
        return pretixRoomItemId == null ? null : new RoomData(
                roomCapacity,
                pretixRoomItemId,
                roomInternalName,
                pretixInformation.getRoomNamesFromRoomPretixItemId(pretixRoomItemId)
        );
    }


    @NotNull
    public Optional<Object> getAnswer(@NotNull String questionIdentifier) {
        return Optional.ofNullable(answers.get(questionIdentifier));
    }
    public boolean hasAnswer(@NotNull String questionIdentifier) {
        return answers.containsKey(questionIdentifier);
    }
    public boolean deleteAnswer(@NotNull String questionIdentifier) {
        answerIdentifierToOptionId.remove(questionIdentifier);
        answerIdentifierToOptionIdentifier.remove(questionIdentifier);
        return answers.remove(questionIdentifier) != null;
    }
    public void setAnswer(@NotNull String questionIdentifier, @Nullable Object answer) {
        this.setAnswer(questionIdentifier, answer, Collections.emptyList(), Collections.emptyList());
    }
    public void setAnswer(@NotNull String questionIdentifier, @Nullable Object answer,
                          @Nullable List<Long> optionIds, @Nullable List<String> optionIdentifiers) {
        if (answer == null) {
            deleteAnswer(questionIdentifier);
            return;
        }
        if (answer instanceof String
            || answer instanceof Float
            || answer instanceof Boolean
            || answer instanceof String[]
            || answer instanceof LocalDate
            || answer instanceof LocalTime
            || answer instanceof ZonedDateTime) {
            answers.put(questionIdentifier, answer);
            answerIdentifierToOptionId.put(questionIdentifier, optionIds);
            answerIdentifierToOptionIdentifier.put(questionIdentifier, optionIdentifiers);
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
    public List<PretixAnswer> getAllAnswers(@NotNull PretixInformation pretixInformation) {
        final var list = new ArrayList<PretixAnswer>();
        final var answers = getAnswers();
        for (String key : answers.keySet()) {
            final var questionId = pretixInformation.getQuestionIdFromIdentifier(key);
            if (questionId.isEmpty()) {
                continue;
            }

            long id = questionId.get();
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
                List<Long> optionsId = answerIdentifierToOptionId.getOrDefault(key, Collections.emptyList());
                List<String> optionsIdentifiers = answerIdentifierToOptionIdentifier
                        .getOrDefault(key, Collections.emptyList());

                list.add(new PretixAnswer(id, out, optionsId, optionsIdentifiers));
            }
        }

        return list;
    }

    public static @NotNull Set<Integer> parseDailyDays(long dailyDays) {
        Set<Integer> ret = new TreeSet<>();
        for (int i = 0; i < 63; i++) {
            if ((dailyDays & (1L << i)) != 0L) {
                ret.add(i);
            }
        }
        return ret;
    }


    public static class OrderBuilder {
        public OrderBuilder dailyDays(Set<Integer> days) {
            dailyDays = days;
            return this;
        }

        public OrderBuilder dailyDays(long days) {
            dailyDays = parseDailyDays(days);
            return this;
        }

        public OrderBuilder answers(@NotNull List<PretixAnswer> answers, @NotNull PretixInformation pi) {
            this.answers = new HashMap<>();
            this.answerIdentifierToOptionId = new HashMap<>();
            this.answerIdentifierToOptionIdentifier = new HashMap<>();
            for (PretixAnswer answer : answers) {
                long questionId = answer.getQuestionId();
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
                        case FILE -> PretixConst.QUESTIONS_FILE_KEEP;
                        case DATE -> LocalDate.parse(value);
                        case TIME -> LocalTime.parse(value);
                        case DATE_TIME -> ZonedDateTime.parse(value, PretixGenericUtils.PRETIX_DATETIME_FORMAT);
                    };
                    this.answers.put(answerIdentifier, o);
                }

                answerIdentifierToOptionId.put(answerIdentifier, answer.getOptionsId());
                answerIdentifierToOptionIdentifier.put(answerIdentifier, answer.getOptionIdentifiers());
            }
            return this;
        }
    }
}
