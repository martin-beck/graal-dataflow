package org.prettycat.examples.test;

public class TestClass {
	
	int k;
	
	public void call(int i) {
		i = i + k;
		if (i > 0) { 
			method1(i);
		} else {
			method2(i);
		}
	}

	public void method1(int i) {
		method2(-i % k);
	}
	
	public void method2(int i) {
		i -= k;
		System.out.println(i);
	}
}
