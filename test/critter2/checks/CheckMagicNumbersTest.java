package critter2.checks;

import critter2.CritterCheck;
import critter2.Utils;

import org.junit.Test;

import cetus.hir.Program;

public class CheckMagicNumbersTest {

	@Test
	public void test() {
		Program program = Utils.getProgram("pragma_forLoop.c");
		
		Utils.TestErrorReporter tr = new Utils.TestErrorReporter();
		
		CritterCheck check = new CheckMagicNumbers(program, tr);
		check.check();
		
		tr.assertNumErrors(1);
		tr.assertErrorEquals(0,  "\n../../test/resources/pragma_forLoop.c: line 6: high priority: "
				+ "\nUse of magic number (10), which should be given a meaningful name, or a #define, "
				+ "which should be replaced with an enum (unless it's the result of a #define in a standard C header file)\n");
	}
}
