/*
 * A sample class to write new checks.
 * 
 * Created by Alice Kroutikova '15
 */

package critter2.checks;

import cetus.hir.Program;
import critter2.CritterCheck;

public class SampleCheck extends CritterCheck {

	/*
	 * Constructor used in testing to verify stylistic flaws are being reported.
	 */
	public SampleCheck(Program program, ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/*
	 * The default constructor used by Critter.java
	 */
	public SampleCheck(Program program) {
		super(program);
	}

	@Override
	public void check() {
		// The check itself occurs here.
	}
}
