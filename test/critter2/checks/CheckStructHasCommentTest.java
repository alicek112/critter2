package critter2.checks;

import critter2.CritterCheck;
import critter2.Utils;

import org.junit.Test;

import cetus.hir.Program;

public class CheckStructHasCommentTest {

	@Test
	public void test() {
		Program program = Utils.getProgram("pragma_fileLength.c");
		
		Utils.TestErrorReporter tr = new Utils.TestErrorReporter();
		
		CritterCheck check = new CheckStructHasComment(program, tr);
		check.check();
		
		tr.assertNumErrors(1);
		tr.assertErrorEquals(0,  "\n   ../test/resources/fileLength.c: line 6: medium priority:"
				+ "\n   A comment should appear above each field in a struct.\n");
	}
}
