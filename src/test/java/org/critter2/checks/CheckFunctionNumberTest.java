package org.critter2.checks;

import org.critter2.CritterCheck;
import org.critter2.Utils;
import org.junit.Test;

import cetus.hir.Program;

public class CheckFunctionNumberTest {

	@Test
	public void test() {
		Program program = Utils.getProgram("pragma_functionNumber.c");
		
		CritterCheck check = new CheckFunctionNumber(program, new Utils.TestErrorReporter(
				"\n../../test/resources/functionNumber.c: line 63: low priority: " +
				"\nA file should contain no more than 15 functions;\n this file contains 16 functions\n"));
		check.check();
		
	}
}
