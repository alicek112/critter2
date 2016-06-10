package critter2.checks;

import critter2.CritterCheck;
import critter2.Utils;

import org.junit.Test;

import cetus.hir.Program;

public class CheckFunctionCommentValidTest {

	@Test
	public void test() {
		// Tests if the check detects a missing comment
		Program program = Utils.getProgram("pragma_hello2.c");
		
		Utils.TestErrorReporter tr = new Utils.TestErrorReporter();
		
		CritterCheck check = new CheckFunctionCommentValid(program, tr);
		check.check();
		
		
		tr.assertNumErrors(5);
		tr.assertErrorEquals(0,"\n   ../test/resources/include3.h: line 9: high priority: \n   "
				+ "A function's comment should refer to each parameter by name;\n   "
				+ "your comment does not refer to 'y'\n");
		tr.assertErrorEquals(1, "\n   ../test/resources/include3.h: line 9: high priority: \n   "
				+ "A function's comment should state explicitly what the function returns\n");
		tr.assertErrorEquals(2, "\n   ../test/resources/include3.h: line 11: high priority: \n   "
				+ "A function declaration should have a comment\n");
		tr.assertErrorEquals(3,  "\n   ../test/resources/include3.h: line 12: high priority: \n   "
				+ "A function declaration should have a comment\n");
		tr.assertErrorEquals(4, "\n   hello.c: line 9: low priority: \n   "
				+ "Parameter name 'int x' in function definition differs from parameter name "
				+ "'int wrongName' in function declaration\n");
		
	}
}
