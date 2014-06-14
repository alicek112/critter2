package critter2.checks;

import cetus.hir.DepthFirstIterator;
import cetus.hir.Loop;
import cetus.hir.Program;
import cetus.hir.Statement;
import cetus.hir.Traversable;
import critter2.CritterCheck;

/**
 * Warn if loop length exceeds a maximum line length (MAX_LOOP_LENGTH).
 * 
 * @author Alice Kroutikova '15.
 *
 */
public class CheckLoop extends CritterCheck {
	
	// COS217 maximum loop length
    private static final int MAX_LOOP_LENGTH = 35;
	
    /**
     * Constructor used for testing.
     * 
     * @param program the root node of the parse tree
     * @param errorReporter testing class
     */
	public CheckLoop(Program program, CritterCheck.ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/**
	 * Main constructor used in Critter.java
	 * 
	 * @param program the root node of the parse tree
	 */
	public CheckLoop(Program program) {
		super(program);
	}

	@Override
	public void check() {
    	DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	
    	// Traverse tree looking for flaws
    	while (dfs.hasNext()) {
    		Traversable t = nextNoInclude(dfs); // skip all included header files
    		
    		if (t instanceof Loop) {
    			Statement s = ((Loop) t).getBody();
    			int looplinecount = linecount(s);
    			
    			if (looplinecount > MAX_LOOP_LENGTH) {
    				reportErrorPos(t, "low priority: " +
    						"\nA loop should consist of fewer than %d " +
    						"lines;\n " +
    						"this loop consists of %d lines; consider " +
    						"refactoring\n", 
    						MAX_LOOP_LENGTH, looplinecount);
    			}
    		}
    	}
	}
}
