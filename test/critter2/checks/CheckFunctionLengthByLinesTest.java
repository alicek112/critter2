package critter2.checks;

import critter2.CritterCheck;
import critter2.Utils;
import org.junit.Test;

import cetus.hir.Program;

public class CheckFunctionLengthByLinesTest {

	@Test
	public void test() {
		Program program = Utils.getProgram("pragma_functionLength.c");
		
		CritterCheck check = new CheckFunctionLengthByLines(program, new Utils.TestErrorReporter(
				"\n../../test/resources/functionLength.c: line 1: low priority: \nA function " +
				"should consist of fewer than 140 lines;\n this function consists of 294 lines; " +
				"consider refactoring\n"));
		check.check();
		
	}
}
