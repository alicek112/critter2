package critter2.checks;

import critter2.CritterCheck;
import critter2.Utils;

import org.junit.Test;

import cetus.hir.Program;

public class CheckVariableNameTest {

	@Test
	public void test() {
		Program program = Utils.getProgram("pragma_functionComments.c");
		
		Utils.TestErrorReporter tr = new Utils.TestErrorReporter();
		
		CritterCheck check = new CheckVariableName(program, tr);
		check.check();
		
		tr.assertNumErrors(1);
		tr.assertErrorEquals(0,  "\n   ../test/resources/functionCommentParam.c: "
				+ "line 5: medium priority:\n   Variable/function name 'x' is too short\n");
	}
}
