package critter2.checks;

import critter2.CritterCheck;
import critter2.Utils;

import org.junit.Test;

import cetus.hir.Program;

public class CheckFunctionNamingTest {
	@Test
	public void test() {
		Program program = Utils.getProgram("pragmaFunctionName.c");

		Utils.TestErrorReporter tr = new Utils.TestErrorReporter();
		
		CritterCheck check = new CheckFunctionNaming(program, tr);
		check.check();
		
		tr.assertNumErrors(2);
		tr.assertErrorEquals(0,"\n   resources/functionName.c: line 5: medium priority:"
				+ "\n   Function names should be prefixed with module names;"
				+ "\n   function name bad_func does not match module name resources/functionName.c\n");
		tr.assertErrorEquals(1,"\n   resources/functionName.c: line 9: medium priority:"
				+ "\n   Function names should be prefixed with module names;"
				+ "\n   function name reallybad does not match module name resources/functionName.c\n");
		
	}
}
