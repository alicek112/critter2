package critter2.checks;

import critter2.CritterCheck;
import critter2.Utils;

import org.junit.Test;

import cetus.hir.Program;

public class CheckEmptyCompoundTest {

	@Test
	public void test() {
		Program program = Utils.getProgram("pragma_functionComments.c");
		
		Utils.TestErrorReporter tr = new Utils.TestErrorReporter();
		
		CritterCheck check = new CheckEmptyCompound(program, tr);
		check.check();
		
		tr.assertNumErrors(1);
		tr.assertErrorEquals(0,  "\n   ../test/resources/functionCommentParam.c: line 23: "
				+ "medium priority: \n   Do not use empty compound statements.\n");
	}
}
