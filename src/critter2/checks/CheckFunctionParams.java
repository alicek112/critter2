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

public class CheckFunctionParams extends CritterCheck {
	
	 // COS217 maximum number of parameters per function
    private static final int MAX_PARAMETER_NUMBER = 7;

    /*
     * Constructor used for testing.
     */
	public CheckFunctionParams(Program program, ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/*
	 * General constructor used by Critter.java.
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
    		Traversable t = dfs.next();
    		
    		// skips all included files
    		if (t.toString().startsWith("#pragma critTer:startStdInclude:")) {
    			while (!(t.toString().startsWith("#pragma critTer:endStdInclude:"))) {
    				t = dfs.next();
    			}
    		}
    		
    		else if (t instanceof Procedure) {
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
