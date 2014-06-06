package org.critter2;

import static org.junit.Assert.*;

import java.net.URL;

import org.critter2.Critter;
import org.junit.Test;

public class CritterTest {

	public static String res(String name) {
		URL url = Thread.currentThread().getContextClassLoader().getResource("");
		
		return url.getPath() + "../src/test/resources/" + name;
	}
	
	@Test
	public void test() {
		String[] args = {"-preprocessor=\"gcc -E -C -dD\"", res("pragma_hello.c")};
		Critter.main(args);
	}
}
