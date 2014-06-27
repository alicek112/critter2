package critter2.checks;

import cetus.hir.Default;
import cetus.hir.DepthFirstIterator;
import cetus.hir.Program;
import cetus.hir.SwitchStatement;
import cetus.hir.Traversable;
import critter2.CritterCheck;

/**
 * Warns if a switch is missing a default case.
 * 
 * @author alicek112
 *
 */
public class CheckSwitchHasDefaultCase extends CritterCheck {

	/**
     * Constructor used for testing.
     * 
     * @param program the root node of the parse tree
     * @param errorReporter testing class
     */
	public CheckSwitchHasDefaultCase(Program program, CritterCheck.ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/**
	 * Main constructor used in Critter.java
	 * 
	 * @param program the root node of the parse tree
	 */
	public CheckSwitchHasDefaultCase(Program program) {
		super(program);
	}

	@Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	
		// Traverse parse tree looking for switch statements (SwitchStatement node).
    	while (dfs.hasNext()) {
    		Traversable t = nextNoInclude(dfs);
    		
    		if (t instanceof SwitchStatement) {
    			DepthFirstIterator<Traversable> sdfs = 
    					new DepthFirstIterator<Traversable>(t);
    			boolean hasDefault = false;
    			
    			// Traverses tree rooted at switch statement, looking for default.
    			while (sdfs.hasNext()) {
    				Traversable s = sdfs.next();
    				if (s instanceof Default)
    					hasDefault = true;
    			}
    			
    			if (!hasDefault) {
    				reportErrorPos(t, "low priority: " +
    						"%n   A switch statement should have a default " +
    						"case%n");
    			}
    		}
    	}
	}
}
