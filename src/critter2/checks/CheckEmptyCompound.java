package critter2.checks;

import cetus.hir.DepthFirstIterator;
import cetus.hir.Program;
import cetus.hir.Traversable;
import critter2.CritterCheck;

/**
 * Warns against empty compound statements
 * 
 * @author Alice Kroutikova '15
 *
 */
public class CheckEmptyCompound extends CritterCheck {

	/**
     * Constructor used for testing.
     * 
     * @param program the root node of the parse tree
     * @param errorReporter testing class
     */
	public CheckEmptyCompound(Program program, CritterCheck.ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/**
	 * Main constructor used in Critter.java
	 * 
	 * @param program the root node of the parse tree
	 */
	public CheckEmptyCompound(Program program) {
		super(program);
	}

	/**
	 * Implements check and reports warnings.
	 */
	@Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	
		// Traverses parse tree looking for empty compound statements.
    	while (dfs.hasNext()) {
    		Traversable t = nextNoStdInclude(dfs);
    		
    		if (t.toString().compareTo("{\n\n}") == 0) {
    			reportErrorPos(t, "medium priority: " +
    					"%nDo not use empty compound statements.%n");
    		}
    	}
	}
}
