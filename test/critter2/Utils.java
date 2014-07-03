package critter2;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cetus.hir.Program;

public class Utils {
	public static String res(String name) {
		return path(name) + name;
	}
	
	public static String path(String name) {
		URL url = Thread.currentThread().getContextClassLoader().getResource("");
		
		// gradle and eclipse disagree on resource location base
		if ((new File(url.getPath() + "../../../test/resources/" + name)).isFile())
			return url.getPath() + "../../../test/resources/";
		else
			return url.getPath() + "../test/resources/";
	}
	
	public static Program getProgram(String name) {
		CritterDriver cd = new CritterDriver();
		
		return cd.parseProgram(res(name));
	}
	
	public static class TestErrorReporter implements CritterCheck.ErrorReporter {
		
		public final List<String> reportedErrors = new ArrayList<String>();
		
		@Override
		public void reportError(String message, Object... args) {
			String test = String.format(message, args);
			
			reportedErrors.add(test);
		}
		
		public void assertErrorEquals(int index, String model) {
			String test = reportedErrors.get(index);
			if (model.compareTo(test) != 0) {
				System.out.println("model: \"" + model.replace("\n", "\\n") + "\"");
				System.out.println("test:  \"" + test.replace("\n", "\\n") + "\"");
			}
			
			assertEquals(model, test);
		}
		
		public void assertNumErrors(int numErrors) {
			assertEquals(numErrors, reportedErrors.size());
		}
	}
}
