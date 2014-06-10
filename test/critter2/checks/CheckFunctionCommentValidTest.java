package critter2.checks;

import critter2.CritterCheck;
import critter2.Utils;

import org.junit.Test;

import cetus.hir.Program;

public class CheckFunctionCommentValidTest {

	@Test
	public void test() {
		// Tests if the check detects a missing comment
		Program program = Utils.getProgram("pragma_functionNaming.c");
		
		Utils.TestErrorReporter tr = new Utils.TestErrorReporter();
		
		CritterCheck check = new CheckFunctionCommentValid(program, tr);
		check.check();
		
		tr.assertNumErrors(3);
		tr.assertErrorEquals(0,"\n../../test/resources/functionNaming.c: line 1: high priority: "
				+ "\nA function's comment should refer to each parameter by name;\nyour comment does not refer to 'a'\n");
		tr.assertErrorEquals(1, "\n../../test/resources/functionNaming.c: line 1: high priority: "
				+ "\nA function's comment should state explicitly what the function returns\n");
		tr.assertErrorEquals(2, "\n../../test/resources/functionNaming.c: line 8: high priority: "
				+ "\nA function definition should have a comment\n");
		
	}
}
