package critter2.checks;

import critter2.CritterCheck;
import critter2.Utils;

import org.junit.Test;

import cetus.hir.Program;

public class CheckGoTosTest {

	@Test
	public void test() {
		Program program = Utils.getProgram("pragma_forLoop.c");
		
		Utils.TestErrorReporter tr = new Utils.TestErrorReporter();
		
		CritterCheck check = new CheckGoTos(program, tr);
		
		check.check();
		
		tr.assertNumErrors(1);
		tr.assertErrorEquals(0, "\n../../test/resources/pragma_forLoop.c: line 14: high priority: \nNever use GOTO statements\n");
	}
}
