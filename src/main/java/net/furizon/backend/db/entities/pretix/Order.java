package net.furizon.backend.db.entities.pretix;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.furizon.backend.db.entities.users.User;
import net.furizon.backend.pretix.*;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.naming.Name;
import java.util.*;

/*
- Che prodotti ha
	- SE ha un biglietto per l'evento
	- Tipo di sponsor
	- Se daily: che giorni ha
	- SE ha una stanza; tipo della stanza
- Secret per collegare l'ordine all'utente
 */
@Entity
@Table(name = "orders")
public class Order {

	@Id
	@Getter
	private String code;

	private long dailyDays = 0L; //bitmask of days

	@Getter
	private Sponsorship sponsorship = Sponsorship.NONE;

	@Getter
	private ExtraDays extraDays = ExtraDays.NONE;

	@Getter
	private int roomCapacity = 0; // 0 = has no room

	@Getter
	private String hotelLocation = "ITALY";

	@Getter
	private String pretixOrderSecret = "GABIBBO";

	@Getter
	private boolean hasMembership = false;

	@Getter
	private int answersMainPositionId = -1;

	private String answers;

	@ManyToOne
	@JoinColumn(name = "user_id")
	@Getter
	private User orderOwner;

	@ManyToOne
	@JoinColumn(name = "event_id")
	@Getter
	private Event orderEvent;

	@Transient
	private Map<String, Object> answersData = null;
	private void loadAnswers() {
		JSONArray jsonArray = new JSONArray(answers);
		Map<String, Object> answersData = new HashMap<String, Object>();
		for(int i = 0; i < jsonArray.length(); i++){
			JSONObject obj = jsonArray.getJSONObject(i);
			int questionId = obj.getInt("question");
			String answerIdentifier = PretixInteraction.translateQuestionId(questionId);
			String value = obj.getString("answer");
			Object o = switch(PretixInteraction.translateQuestionType(questionId)){
				case NUMBER -> Float.parseFloat(value);
				case STRING_ONE_LINE -> value;
				case STRING_MULTI_LINE -> value;
				case BOOLEAN -> value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes");
				case LIST_SINGLE_CHOICE -> value;
				case LIST_MULTIPLE_CHOICE -> value;
				case FILE -> Constants.QUESTIONS_FILE_KEEP;
				case DATE -> value; //TODO convert
				case TIME -> value; //TODO convert
				case DATE_TIME -> value; //TODO convert
				case COUNTRY_CODE -> value;
				case PHONE_NUMBER -> value;
			};
			answersData.put(answerIdentifier, o);
		}
		this.answersData = answersData;
	}
	private void saveAnswers() {
		if(answersData == null) loadAnswers();
		JSONArray jsonArray  = new JSONArray();
		for(String key : answersData.keySet()){
			Object o = answersData.get(key);
			String out = switch(PretixInteraction.translateQuestionType(key)){
				case QuestionType.NUMBER -> String.valueOf(o);
				case STRING_ONE_LINE -> (String) o;
				case STRING_MULTI_LINE -> (String) o;
				case BOOLEAN -> ((boolean) o) ? "true" : "false";
				case LIST_SINGLE_CHOICE -> (String) o;
				case LIST_MULTIPLE_CHOICE -> (String) o;
				case FILE -> Constants.QUESTIONS_FILE_KEEP;
				case DATE -> o.toString(); //TODO convert
				case TIME -> o.toString(); //TODO convert
				case DATE_TIME -> o.toString(); //TODO convert
				case COUNTRY_CODE -> (String) o;
				case PHONE_NUMBER -> (String) o;
			};
			jsonArray.put(out);
		}
		answers = jsonArray.toString();
	}

	public Object getAnswerValue(String answer){
		if(answersData == null) loadAnswers();
		return answersData.get(answer);

	}
	public void setAnswerValue(String answer, Object value){
		if(answersData == null) loadAnswers();
		answersData.put(answer, value);
		saveAnswers();
	}
	public String getAnswersRaw(){
		return answers;
	}

	public boolean isDaily(){
		return dailyDays != 0L;
	}

	public boolean hasDay(int day){
		return (dailyDays & (1L << day)) != 0L;
	}

	@Transient
	private Set<Integer> dailyDaysSet = null;
	public Set<Integer> getDays(){
		if(dailyDaysSet == null){
			Set<Integer> s = new HashSet<>();
			for(int i = 0; i < 64; i++)
				if(hasDay(i))
					s.add(i);
			dailyDaysSet = s;
		}
		return dailyDaysSet;
	}

	public boolean ownsRoom(){
		return roomCapacity > 0;
	}

	public void update(String code, String pretixOrderSecret, int positionId, Set<Integer> days, Sponsorship sponsorship, ExtraDays extraDays, int roomCapacity, String hotelLocation, boolean hasMembership, User orderOwner, Event orderEvent, JSONArray answersJson){
		this.code = code;
		this.extraDays = extraDays;
		this.sponsorship = sponsorship;
		this.roomCapacity = roomCapacity;
		this.hasMembership = hasMembership;
		this.hotelLocation  = hotelLocation;
		this.pretixOrderSecret = pretixOrderSecret;
		this.answersMainPositionId = positionId;
		this.answers = answersJson.toString();
		this.answersData = null;
		this.dailyDaysSet = null;

		this.dailyDays = 0L;
		for(Integer day : days)
			dailyDays |= 1L << day;
	}
}
