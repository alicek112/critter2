package org.critter2;

import static org.junit.Assert.*;

import java.net.URL;

import cetus.hir.Program;

public class Utils {
	public static String res(String name) {
		URL url = Thread.currentThread().getContextClassLoader().getResource("");
		
		return url.getPath() + "../src/test/resources/" + name;
	}
	
	public static Program getProgram(String name) {
		CritterDriver cd = new CritterDriver();
		
		return cd.parseProgram(res(name));
	}
	
	public static class TestErrorReporter implements CritterCheck.ErrorReporter {
		
		private final String model;
		
		public TestErrorReporter(String model) {
			this.model = model;
		}

		@Override
		public void reportError(String message, Object... args) {
			String test = String.format(message, args);
			
			assertEquals(model, test);
			
		}
		
	}
}
