package critter2;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

import cetus.hir.Program;

public class Utils {
	public static String res(String name) {
		URL url = Thread.currentThread().getContextClassLoader().getResource("");
		
		// gradle and eclipse disagree on resource location base
		if ((new File(url.getPath() + "../../../test/resources/" + name)).isFile())
			return url.getPath() + "../../../test/resources/" + name;
		else
			return url.getPath() + "../test/resources/" + name;
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
			
			if (model.compareTo(test) != 0) {
				System.out.println("\"" + test.replace("\n", "\\n") + "\"");
			}
			
			assertEquals(model, test);
			
		}
		
	}
}
