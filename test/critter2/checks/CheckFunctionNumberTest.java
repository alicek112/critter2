package critter2.checks;

import critter2.CritterCheck;
import critter2.Utils;

import org.junit.Test;

import cetus.hir.Program;

public class CheckFunctionNumberTest {

	@Test
	public void test() {
		Program program = Utils.getProgram("pragma_functionNumber.c");
		
		Utils.TestErrorReporter tr = new Utils.TestErrorReporter();
		
		CritterCheck check = new CheckFunctionNumber(program, tr);
		check.check();
		
		tr.assertNumErrors(1);
		tr.assertErrorEquals(0,"\n   ../../test/resources/functionNumber.c: line 63: low priority: " +
				"\n   A file should contain no more than 15 functions;\n   this file contains 16 functions\n");

	}
}
