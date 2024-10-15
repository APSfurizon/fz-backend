package net.furizon.backend.utils.pretix;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum QuestionType {
	NUMBER("N"), STRING_ONE_LINE("S"), STRING_MULTI_LINE("T"), BOOLEAN("B"), LIST_SINGLE_CHOICE("C"), LIST_MULTIPLE_CHOICE("M"), FILE("F"), DATE("D"), TIME("H"), DATE_TIME("W"), COUNTRY_CODE("CC"), PHONE_NUMBER("TEL");

	@Getter
	private final String code;

	QuestionType(String code) {
		this.code = code;
	}

	private static final Map<String, QuestionType> MAP = new HashMap<>();

	static {
		for (QuestionType type : QuestionType.values())
			MAP.put(type.code, type);
	}

	public static QuestionType fromCode(String code) {
		return MAP.get(code);
	}

}
