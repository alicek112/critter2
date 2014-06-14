package critter2.checks;

import cetus.hir.DepthFirstIterator;
import cetus.hir.Procedure;
import cetus.hir.Program;
import cetus.hir.Statement;
import cetus.hir.Traversable;
import critter2.CritterCheck;

/**
 * Warns if a function length exceeds a maximum length (MAX_FUNCTION_LENGTH).
 * 
 * @author Alice Krotikova '15
 *
 */
public class CheckFunctionLengthByLines extends CritterCheck {
	
	// COS217 maximum function length
    private int MAX_FUNCTION_LENGTH = 140;

    /**
     * Constructor used for testing.
     * 
     * @param program the root node of the parse tree
     * @param errorReporter testing class
     */
	public CheckFunctionLengthByLines(Program program, CritterCheck.ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/**
	 * Main constructor used in Critter.java
	 * 
	 * @param program the root node of the parse tree
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
    		Traversable t = nextNoInclude(dfs);
    		
    		// if node is a function (Procedure), determine its linecount
    		if (t instanceof Procedure) {
    			Statement s = ((Procedure) t).getBody();
    			int functionlinecount = linecount(s);
    			
    			if (functionlinecount > MAX_FUNCTION_LENGTH) {
    				reportErrorPos(t, "low priority: " +
    						"\nA function should consist of fewer than " +
    						"%d lines;\n " +
    						"this function consists of %d lines; " +
    						"consider refactoring\n", 
    						MAX_FUNCTION_LENGTH, functionlinecount);
    			}
    		}
    	}
	}
}