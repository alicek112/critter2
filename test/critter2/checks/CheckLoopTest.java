package critter2.checks;

import critter2.CritterCheck;
import critter2.Utils;

import org.junit.Test;

import cetus.hir.Program;

public class CheckLoopTest {

	@Test
	public void test() {
		Program program = Utils.getProgram("pragma_forLoop.c");
		
		Utils.TestErrorReporter tr = new Utils.TestErrorReporter();
		
		CritterCheck check = new CheckLoop(program, tr);
		check.check();
		
		tr.assertNumErrors(1);
		tr.assertErrorEquals(0,"\n   ../../test/resources/pragma_forLoop.c: line 6: low priority: "
				+ "\n   A loop should consist of fewer than 35 lines;\n   this loop consists of 49 lines; "
				+ "consider refactoring\n");
	}
}
