package org.critter2.checks;

import org.critter2.CritterCheck;
import org.critter2.Utils;
import org.junit.Test;

import cetus.hir.Program;

public class CheckLoopTest {

	@Test
	public void test() {
		Program program = Utils.getProgram("pragma_forLoop.c");
		
		CritterCheck check = new CheckLoop(program, new Utils.TestErrorReporter("\n" +
				"../../test/resources/pragma_forLoop.c: line 6: low priority: " +
				"\nA loop should consist of fewer than 35 lines;" +
				"\n this loop consists of 49 lines; consider refactoring\n"));
		check.check();
		
	}
}
