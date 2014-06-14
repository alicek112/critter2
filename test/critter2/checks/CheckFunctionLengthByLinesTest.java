package critter2.checks;

import critter2.CritterCheck;
import critter2.Utils;

import org.junit.Test;

import cetus.hir.Program;

public class CheckFunctionLengthByLinesTest {

	@Test
	public void test() {
		Program program = Utils.getProgram("pragma_functionLength.c");
		
		Utils.TestErrorReporter tr = new Utils.TestErrorReporter();
		
		CritterCheck check = new CheckFunctionLengthByLines(program, tr);
		check.check();
		
		tr.assertNumErrors(1);
		tr.assertErrorEquals(0,"\n../../test/resources/functionLength.c: line 1: low priority: "
				+ "\nA function should consist of fewer than 140 lines;"
				+ "\n this function consists of 147 lines; consider refactoring\n");
		
	}
}
