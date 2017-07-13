package org.rosi.ccc;

public class TestClass {
	
	public void call(int i) {
		i = i + 1;
		if (i > 0) { 
			method1(i);
		} else {
			method2(i);
		}
	}

	public void method1(int i) {
		method2(-i);
	}
	
	public void method2(int i) {
		i -= 1;
		System.out.println(i);
	}
}
