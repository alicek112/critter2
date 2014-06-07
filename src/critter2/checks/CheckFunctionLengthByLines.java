package critter2.checks;

import critter2.CritterCheck;

import cetus.hir.DepthFirstIterator;
import cetus.hir.Procedure;
import cetus.hir.Program;
import cetus.hir.Statement;
import cetus.hir.Traversable;

public class CheckFunctionLengthByLines extends CritterCheck {
	
	// COS217 maximum function length
    private int MAX_FUNCTION_LENGTH = 140;

	public CheckFunctionLengthByLines(Program program,
			ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	public CheckFunctionLengthByLines(Program program) {
		super(program);
	}

	@Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	
		while (dfs.hasNext()) {
    		Traversable t = dfs.next();
    		
    		// skips all standard included files
    		if (t.toString().startsWith("#pragma critTer:startStdInclude:")) {
    			while (!(t.toString().startsWith("#pragma critTer:endStdInclude:"))) {
    				t = dfs.next();
    			}
    		}
    		
    		else if (t instanceof Procedure) {
    			Statement s = ((Procedure) t).getBody();
    			DepthFirstIterator<Traversable> ldfs = 
    					new DepthFirstIterator<Traversable>(s);
    			
    			int looplinecount = 0;
    			while (ldfs.hasNext()) {
    				Traversable st = ldfs.next();
    				
    				if (st.toString().startsWith("#pragma critTer")) {
    					looplinecount++;
    				}
    			}
    			
    			if (looplinecount > MAX_FUNCTION_LENGTH) {
    				reportErrorPos(t, "low priority: " +
    						"\nA function should consist of fewer than " +
    						"%d lines;\n " +
    						"this function consists of %d lines; " +
    						"consider refactoring\n", 
    						MAX_FUNCTION_LENGTH, looplinecount);
    			}
    		}
    	}
	}
}
