package critter2.checks;

import critter2.CritterCheck;
import critter2.Utils;

import org.junit.Test;

import cetus.hir.Program;

public class CheckNestingTest {

	@Test
	public void test() {
		Program program = Utils.getProgram("pragma_nesting.c");
		
		Utils.TestErrorReporter tr = new Utils.TestErrorReporter();
		
		CritterCheck check = new CheckNesting(program, tr);
		check.check();
		
		tr.assertNumErrors(1);
		tr.assertErrorEquals(0,  "\n   ../test/resources/nesting.c: line 11: low priority: "
				+ "\n   This area is deeply nested at level 4, consider refactoring\n");
	}
}
