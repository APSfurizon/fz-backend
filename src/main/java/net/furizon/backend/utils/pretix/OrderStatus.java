package net.furizon.backend.utils.pretix;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public enum OrderStatus {
	PENDING("n"), PAID("p"), EXPIRED("e"), CANCELED("c");

	@Getter
	private final String code;

	private static final Map<String, OrderStatus> MAP = new HashMap<>();

	static {
		for (OrderStatus type : OrderStatus.values())
			MAP.put(type.code, type);
	}

	public static OrderStatus fromCode(String code) {
		return MAP.get(code);
	}
}
