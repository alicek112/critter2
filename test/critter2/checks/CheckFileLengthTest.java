package critter2.checks;

import critter2.CritterCheck;
import critter2.Utils;

import org.junit.Test;

import cetus.hir.Program;

public class CheckFileLengthTest {

	@Test
	public void test() {
		Program program = Utils.getProgram("pragma_fileLength.c");
		
		Utils.TestErrorReporter tr = new Utils.TestErrorReporter();
		
		CritterCheck check = new CheckFileLength(program, tr);
		check.check();
		
		tr.assertNumErrors(1);
		tr.assertErrorEquals(0,  "\n   ../test/resources/fileLength.c: line 621: low priority: "
				+ "\n   A source code file should contain fewer than 500 lines;\n   this file contains 622 lines\n");
	}
}
