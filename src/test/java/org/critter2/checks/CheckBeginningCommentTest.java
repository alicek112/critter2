package org.critter2.checks;

import org.critter2.CritterCheck;
import org.critter2.Utils;
import org.junit.Test;

import cetus.hir.Program;

public class CheckBeginningCommentTest {

	@Test
	public void test() {
		Program program = Utils.getProgram("pragma_forLoop.c");
		
		CritterCheck check = new CheckBeginningComment(program, new Utils.TestErrorReporter(
				"\n../../test/resources/pragma_forLoop.c: line 1: high priority: \nA file should begin with a comment.\n"));
		check.check();
		
	}
}
