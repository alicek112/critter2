package critter2.checks;

import critter2.CritterCheck;
import critter2.Utils;

import org.junit.Test;

import cetus.hir.Program;

public class CheckSwitchCasesTest {

	@Test
	public void test() {
		Program program = Utils.getProgram("pragma_switch.c");
		
		Utils.TestErrorReporter tr = new Utils.TestErrorReporter();
		
		CritterCheck check = new CheckSwitchCases(program, tr);
		check.check();
		
		tr.assertNumErrors(1);
		tr.assertErrorEquals(0,  "\n../test/resources/switch.c: line 8: medium priority: \nEach case/default in a "
				+ "switch statement should have a break or return statement, you're missing one here.\n");
	}
}
