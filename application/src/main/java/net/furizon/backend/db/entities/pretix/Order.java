package net.furizon.backend.db.entities.pretix;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import net.furizon.backend.db.entities.users.User;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.model.QuestionType;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import net.furizon.backend.service.pretix.PretixService;
import net.furizon.backend.utils.pretix.Constants;
import org.apache.http.entity.ContentType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

// TODO -> Replace on JOOQ

@Entity
@Table(name = "orders")
@Getter
public class Order {
    @Id
    private String code;

    private OrderStatus orderStatus;

    @Getter(AccessLevel.NONE)
    private long dailyDays = 0L; //bitmask of days

    private Sponsorship sponsorship = Sponsorship.NONE;

    private ExtraDays extraDays = ExtraDays.NONE;

    private int roomCapacity = 0; // 0 = has no room

    private String hotelLocation = "ITALY";

    private String pretixOrderSecret = "GABIBBO";

    private boolean hasMembership = false;

    private int answersMainPositionId = -1;

    @Getter(AccessLevel.NONE)
    private String answers;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User orderOwner;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event orderEvent;


    @Transient
    @Getter(AccessLevel.NONE)
    private Map<String, Object> answersData = null;

    private void loadAnswers(PretixService ps) {
        JSONArray jsonArray = new JSONArray(answers);
        Map<String, Object> answersData = new HashMap<String, Object>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            int questionId = obj.getInt("question");
            String answerIdentifier = ps.translateQuestionId(questionId);
            String value = obj.getString("answer");
            Object o = switch (ps.translateQuestionType(questionId)) {
                case NUMBER -> Float.parseFloat(value);
                case STRING_ONE_LINE -> value;
                case STRING_MULTI_LINE -> value;
                case BOOLEAN -> value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes");
                case LIST_SINGLE_CHOICE -> value;
                case LIST_MULTIPLE_CHOICE -> value;
                case FILE -> Constants.QUESTIONS_FILE_KEEP;
                case DATE -> LocalDate.parse(value);
                case TIME -> LocalTime.parse(value);
                case DATE_TIME -> ZonedDateTime.parse(value);
                case COUNTRY_CODE -> value;
                case PHONE_NUMBER -> value;
            };
            answersData.put(answerIdentifier, o);
        }
        this.answersData = answersData;
    }

    private void saveAnswers(PretixService ps) {
        if (answersData == null) {
            loadAnswers(ps);
        }
        JSONArray jsonArray = new JSONArray();
        for (String key : answersData.keySet()) {
            Object o = answersData.get(key);
            String out = switch (ps.translateQuestionType(key)) {
                case QuestionType.NUMBER -> String.valueOf(o);
                case STRING_ONE_LINE -> (String) o;
                case STRING_MULTI_LINE -> (String) o;
                case BOOLEAN -> ((boolean) o) ? "true" : "false";
                case LIST_SINGLE_CHOICE -> (String) o;
                case LIST_MULTIPLE_CHOICE -> (String) o;
                case FILE -> (String) o;
                case DATE -> o.toString();
                case TIME -> o.toString();
                case DATE_TIME -> o.toString();
                case COUNTRY_CODE -> (String) o;
                case PHONE_NUMBER -> (String) o;
            };
            out = out.strip();
            if (!out.isEmpty()) {
                jsonArray.put(out);
            }
        }
        answers = jsonArray.toString();
    }

    public Object getAnswerValue(String answer, PretixService ps) {
        if (answersData == null) {
            loadAnswers(ps);
        }
        return answersData.get(answer);

    }

    public void setAnswerFile(
        String answer,
        ContentType mimeType,
        String fileName,
        byte[] bytes,
        PretixService ps
    ) throws TimeoutException {
        String newAns = ps.uploadFile(mimeType, fileName, bytes);
        this.setAnswerValue(answer, newAns, ps);
    }

    public void setAnswerValue(String answer, Object value, PretixService ps) {
        if (answersData == null) {
            loadAnswers(ps);
        }
        answersData.put(answer, value);
        saveAnswers(ps);
    }

    public void removeAnswer(String answer, PretixService ps) {
        if (answersData == null) {
            loadAnswers(ps);
        }
        answersData.remove(answer);
        saveAnswers(ps);
    }

    public String getAnswersRaw() {
        return answers;
    }

    public void resetFileUploadAnswers(PretixService ps) {
        //After uploading a file, we need to change it's value to "file:keep"
        for (String key : answersData.keySet()) {
            if (ps.translateQuestionType(key) == QuestionType.FILE) {
                answersData.put(key, Constants.QUESTIONS_FILE_KEEP);
            }
        }
        saveAnswers(ps);
    }

    public boolean isDaily() {
        return dailyDays != 0L;
    }

    public boolean hasDay(int day) {
        return (dailyDays & (1L << day)) != 0L;
    }

    @Transient
    @Getter(AccessLevel.NONE)
    private Set<Integer> dailyDaysSet = null;

    public Set<Integer> getDays() {
        if (dailyDaysSet == null) {
            Set<Integer> s = new HashSet<>();
            for (int i = 0; i < 64; i++) {
                if (hasDay(i)) {
                    s.add(i);
                }
            }
            dailyDaysSet = s;
        }
        return dailyDaysSet;
    }

    public boolean ownsRoom() {
        return roomCapacity > 0;
    }

    public void update(
        String code,
        OrderStatus status,
        String pretixOrderSecret,
        int positionId,
        Set<Integer> days,
        Sponsorship sponsorship,
        ExtraDays extraDays,
        int roomCapacity,
        String hotelLocation,
        boolean hasMembership,
        User orderOwner,
        Event orderEvent,
        JSONArray answersJson
    ) {
        this.code = code;
        this.orderStatus = status;
        this.extraDays = extraDays;
        this.sponsorship = sponsorship;
        this.roomCapacity = roomCapacity;
        this.hasMembership = hasMembership;
        this.hotelLocation = hotelLocation;
        this.pretixOrderSecret = pretixOrderSecret;
        this.answersMainPositionId = positionId;
        this.answers = answersJson.toString();
        this.answersData = null;
        this.dailyDaysSet = null;

        this.dailyDays = 0L;
        for (Integer day : days) {
            dailyDays |= 1L << day;
        }
    }
}
