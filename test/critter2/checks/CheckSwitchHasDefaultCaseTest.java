package critter2.checks;

import critter2.CritterCheck;
import critter2.Utils;

import org.junit.Test;

import cetus.hir.Program;

public class CheckSwitchHasDefaultCaseTest {

	@Test
	public void test() {
		Program program = Utils.getProgram("pragma_switch.c");
		
		Utils.TestErrorReporter tr = new Utils.TestErrorReporter();
		
		CritterCheck check = new CheckSwitchHasDefaultCase(program, tr);
		check.check();
		
		tr.assertNumErrors(1);
		tr.assertErrorEquals(0,  "\n   ../test/resources/switch.c: line 6: low priority: "
				+ "\n   A switch statement should have a default case\n");
	}
}
