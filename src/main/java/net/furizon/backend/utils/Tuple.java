package net.furizon.backend.utils;

import lombok.Data;

@Data
public class Tuple<A, B> {
	private A a;
	private B b;

	public Tuple(A a, B b) {
		this.a = a;
		this.b = b;
	}

	public Object get(int x){
		if(x == 0) return a;
		else if(x == 1) return b;
		else throw new IndexOutOfBoundsException();
	}
}
