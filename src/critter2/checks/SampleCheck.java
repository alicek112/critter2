package critter2.checks;

import cetus.hir.Program;
import critter2.CritterCheck;

/**
 * A sample class to write new checks.
 * 
 * @author Alice Kroutikova '15
 *
 */
public class SampleCheck extends CritterCheck {

	/**
     * Constructor used for testing.
     * 
     * @param program the root node of the parse tree
     * @param errorReporter testing class
     */
	public SampleCheck(Program program, CritterCheck.ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/**
	 * Main constructor used in Critter.java
	 * 
	 * @param program the root node of the parse tree
	 */
	public SampleCheck(Program program) {
		super(program);
	}

	@Override
	public void check() {
		// The check itself occurs here.
	}
}
