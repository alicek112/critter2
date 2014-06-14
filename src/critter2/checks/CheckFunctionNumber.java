package critter2.checks;

import critter2.CritterCheck;

import cetus.hir.DepthFirstIterator;
import cetus.hir.Procedure;
import cetus.hir.Program;
import cetus.hir.Traversable;

/**
 * Warns if number of functions in a file exceeds maximum number (MAX_FUNCTION_NUMBER).
 * 
 * @author Alice Kroutikova '15.
 *
 */
public class CheckFunctionNumber extends CritterCheck {
	
	// COS217 maximum function number per file
    private static final int MAX_FUNCTION_NUMBER = 15;

    /**
     * Constructor used for testing.
     * 
     * @param program the root node of the parse tree
     * @param errorReporter testing class
     */
	public CheckFunctionNumber(Program program, CritterCheck.ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/**
	 * Main constructor used in Critter.java
	 * 
	 * @param program the root node of the parse tree
	 */
	public CheckFunctionNumber(Program program) {
		super(program);
	}

	@Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	
		int functioncount = 0;
		Traversable firstNode = null; // used to determine location of the stylistic flaw
		
		// Traverse parse tree counting functions (Procedures)
    	while (dfs.hasNext()) {
    		Traversable t = nextNoInclude(dfs);
    		
    		if (firstNode == null)
    			firstNode = t;
    		
    		if (t instanceof Procedure) {
    			functioncount++;
    		}
    	}
    	
    	if (functioncount > MAX_FUNCTION_NUMBER) {
			reportErrorPos(firstNode, "low priority: \nA file should " +
					"contain no more than %d functions;\n " +
					"this file contains %d functions\n", 
					MAX_FUNCTION_NUMBER, functioncount);
		}
	}
}
