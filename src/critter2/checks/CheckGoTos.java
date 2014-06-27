package critter2.checks;

import cetus.hir.DepthFirstIterator;
import cetus.hir.GotoStatement;
import cetus.hir.Program;
import cetus.hir.Traversable;
import critter2.CritterCheck;

/**
 * Warns against using goto statements. 
 * 
 * @author Alice Kroutikova '15.
 *
 */
public class CheckGoTos extends CritterCheck {

	/**
     * Constructor used for testing.
     * 
     * @param program the root node of the parse tree
     * @param errorReporter testing class
     */
	public CheckGoTos(Program program, CritterCheck.ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/**
	 * Main constructor used in Critter.java
	 * 
	 * @param program the root node of the parse tree
	 */
	public CheckGoTos(Program program) {
		super(program);
	}

	@Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	
		// Traverse parse tree looking for goto statements.
    	while (dfs.hasNext()) {
    		Traversable t = nextNoInclude(dfs);
    		
    		if (t instanceof GotoStatement) {
    			reportErrorPos(t, "high priority: " +
    					"%n   Never use GOTO statements%n");
    		}
    	}
	}
}
