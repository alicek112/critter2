package critter2.checks;

import cetus.hir.DepthFirstIterator;
import cetus.hir.Procedure;
import cetus.hir.Program;
import cetus.hir.Traversable;
import critter2.CritterCheck;

public class CheckFunctionParams extends CritterCheck {
	
	 // COS217 maximum number of parameters per function
    private static final int MAX_PARAMETER_NUMBER = 7;

	public CheckFunctionParams(Program program, ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	public CheckFunctionParams(Program program) {
		super(program);
	}

	@Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	
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
