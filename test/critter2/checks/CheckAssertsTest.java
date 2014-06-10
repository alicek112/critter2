package critter2.checks;

import critter2.CritterCheck;
import critter2.Utils;
import org.junit.Test;

import cetus.hir.Program;

public class CheckAssertsTest {

	@Test
	public void test() {
		Program program = Utils.getProgram("pragma_functionCommentReturn.c");
		
		Utils.TestErrorReporter tr = new Utils.TestErrorReporter();
		CritterCheck check = new CheckAsserts(program, tr);
		check.check();
		
		tr.assertNumErrors(1);
		tr.assertErrorEquals(0, "\n../../test/resources/functionCommentReturn.c: line 2: medium priority: " +
				"\nDo you want to validate 'x' through an assert?\n");
		
	}
}
