/*
 * Checks if a function length exceeds a maximum length (MAX_FUNCTION_LENGTH).
 * 
 * Created by Alice Kroutikova '15.
 */

package critter2.checks;

import critter2.CritterCheck;
import cetus.hir.DepthFirstIterator;
import cetus.hir.PreAnnotation;
import cetus.hir.Procedure;
import cetus.hir.Program;
import cetus.hir.Statement;
import cetus.hir.Traversable;

public class CheckFunctionLengthByLines extends CritterCheck {
	
	// COS217 maximum function length
    private int MAX_FUNCTION_LENGTH = 140;

    /*
     * Constructor used for testing.
     */
	public CheckFunctionLengthByLines(Program program,
			ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/*
	 * General constructor used by Critter.java.
	 */
	public CheckFunctionLengthByLines(Program program) {
		super(program);
	}

	@Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	
		// Traverse parse tree
		while (dfs.hasNext()) {
    		Traversable t = dfs.next();
    		
    		// skips all standard included files
    		if (t.toString().startsWith("#pragma critTer:startStdInclude:")) {
    			while (!(t.toString().startsWith("#pragma critTer:endStdInclude:"))) {
    				t = dfs.next();
    			}
    		}
    		
    		// if node is a function (Procedure), determine its linecount
    		else if (t instanceof Procedure) {
    			Statement s = ((Procedure) t).getBody();
    			int looplinecount = 0;
    			
    			// Traverse the children of the function to count its lines
    			DepthFirstIterator<Traversable> ldfs = 
    					new DepthFirstIterator<Traversable>(s);
    			
    			while (ldfs.hasNext()) {
    				Traversable st = ldfs.next();
    				
    				// Count pragmas inserted in the annotating script that indicate line numbers
    				if (st instanceof PreAnnotation && st.toString().startsWith("#pragma critTer")) {
    					System.err.println(st.toString());
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
