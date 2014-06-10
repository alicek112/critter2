package critter2.checks;

import critter2.CritterCheck;
import critter2.Utils;

import org.junit.Test;

import cetus.hir.Program;

public class CheckFunctionNamingTest {
	@Test
	public void test() {
		Program program = Utils.getProgram("pragma_functionNaming.c");

		Utils.TestErrorReporter tr = new Utils.TestErrorReporter();
		
		CritterCheck check = new CheckFunctionNaming(program, tr);
		check.check();
		
		tr.assertNumErrors(1);
		tr.assertErrorEquals(0,"\n../../test/resources/functionNaming.c: line 8: medium priority: " +
				"\nA function's prefix should match the module name; prefix and badprefix do not match\n");
		
	}
}
