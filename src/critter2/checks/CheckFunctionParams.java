/*
 * Warns if a function has too many parameters (MAX_PARAMETER_NUMBER).
 * 
 * Created by Alice Kroutikova '15.
 */
package critter2.checks;

import cetus.hir.DepthFirstIterator;
import cetus.hir.Procedure;
import cetus.hir.Program;
import cetus.hir.Traversable;
import critter2.CritterCheck;

/**
 * Warns if a function has too many parameters (MAX_PARAMETER_NUMBER).
 * 
 * @author alicek112
 *
 */
public class CheckFunctionParams extends CritterCheck {
	
	 // COS217 maximum number of parameters per function
    private static final int MAX_PARAMETER_NUMBER = 7;

    /**
     * Constructor used for testing.
     * 
     * @param program the root node of the parse tree
     * @param errorReporter testing class
     */
	public CheckFunctionParams(Program program, CritterCheck.ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/**
	 * Main constructor used in Critter.java
	 * 
	 * @param program the root node of the parse tree
	 */
	public CheckFunctionParams(Program program) {
		super(program);
	}

	@Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	
		// Traverse parse tree looking for functions (Procedure nodes)
		// to examine parameters
		while (dfs.hasNext()) {
    		Traversable t = nextNoInclude(dfs);
    		
    		if (t instanceof Procedure) {
    			int paramNum = ((Procedure) t).getNumParameters();
    			
    			if (paramNum > MAX_PARAMETER_NUMBER) {
    				reportErrorPos(t, "medium priority: " +
    						"%nA function should have no more than %d " +
    						"parameters; this function has %d%n",
    						MAX_PARAMETER_NUMBER, paramNum);
    			}	
    		}
		}
	}
}
