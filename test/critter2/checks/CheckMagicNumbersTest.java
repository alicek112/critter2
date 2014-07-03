package critter2.checks;

import critter2.CritterCheck;
import critter2.Utils;

import org.junit.Test;

import cetus.hir.Program;

public class CheckMagicNumbersTest {

	@Test
	public void test() {
		Program program = Utils.getProgram("pragma_magicNums.c");
		
		Utils.TestErrorReporter tr = new Utils.TestErrorReporter();
		
		CritterCheck check = new CheckMagicNumbers(program, tr, Utils.path("pragma_magicNums.c"));
		check.check();
		
		tr.assertNumErrors(3);
		tr.assertErrorEquals(0,  "\n   magicNums.c: line 7: high priority: \n   Use of magic number (1), "
				+ "which should be given a meaningful name\n");
		tr.assertErrorEquals(1, "\n   magicNums.c: line 16: high priority: \n   Use of magic number (15.2), "
				+ "which should be given a meaningful name\n");
		tr.assertErrorEquals(2, "\n   magicNums.c: line 16: high priority: \n   Use of magic number (22), "
				+ "which should be given a meaningful name\n");
	}
}
