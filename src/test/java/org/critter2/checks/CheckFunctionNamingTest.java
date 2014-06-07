package org.critter2.checks;

import org.critter2.CritterCheck;
import org.critter2.Utils;
import org.junit.Test;

import cetus.hir.Program;

public class CheckFunctionNamingTest {
	@Test
	public void test() {
		Program program = Utils.getProgram("pragma_functionNaming.c");
		
		CritterCheck check = new CheckFunctionNaming(program, new Utils.TestErrorReporter(
				"\n../../test/resources/functionNaming.c: line 7: medium priority: \nA function's " +
				"prefix should match the module name; prefix and badprefix do not match\n"));
		check.check();
		
	}
}
