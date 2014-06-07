package org.critter2.checks;

import org.critter2.CritterCheck;
import org.critter2.Utils;
import org.junit.Test;

import cetus.hir.Program;

public class CheckFunctionCommentValidTest {

	@Test
	public void test() {
		// Tests if the check detects a missing comment
		Program program1 = Utils.getProgram("pragma_functionNaming.c");
		CritterCheck check1 = new CheckFunctionCommentValid(program1, new Utils.TestErrorReporter(
				"\n../../test/resources/functionNaming.c: line 8: high priority: " +
				"\nA function definition should have a comment\n"
));
		check1.check();
		
		// Tests if the check detects failure to describe what a function returns
		Program program2 = Utils.getProgram("pragma_functionCommentReturn.c");
		CritterCheck check2 = new CheckFunctionCommentValid(program2, new Utils.TestErrorReporter(
				"\n../../test/resources/functionCommentReturn.c: line 1: high priority: " +
				"\nA function's comment should state explicitly what the function returns\n"
));
		check2.check();
		
		// Tests if the check detects failure to describe parameter
		Program program3 = Utils.getProgram("pragma_functionCommentParam.c");
		CritterCheck check3 = new CheckFunctionCommentValid(program3, new Utils.TestErrorReporter(
				"\n../../test/resources/functionCommentParam.c: line 1: high priority: " +
				"\nA function's comment should refer to each parameter by name;\nyour comment " +
				"does not refer to 'x'\n"
));
		check3.check();
		
	}
}
