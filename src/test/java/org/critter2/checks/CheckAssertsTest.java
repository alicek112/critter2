package org.critter2.checks;

import org.critter2.CritterCheck;
import org.critter2.Utils;
import org.junit.Test;

import cetus.hir.Program;

public class CheckAssertsTest {

	@Test
	public void test() {
		// Tests if the check detects a missing comment
		Program program = Utils.getProgram("pragma_functionCommentReturn.c");
		CritterCheck check = new CheckAsserts(program, new Utils.TestErrorReporter(
				"\n../../test/resources/functionCommentReturn.c: line 2: medium priority: " +
				"\nDo you want to validate 'x' through an assert?\n"));
		check.check();
		
		
	}
}
