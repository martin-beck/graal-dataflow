package org.rosi.ccc;

public class TestClass {
	
	public void call(int i) {
		method1(i);
	}

	public void method1(int i) {
		method2(i);
	}
	
	public void method2(int i) {
		i -= 1;
		System.out.println(i);
	}
}
