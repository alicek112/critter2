/*
 * Warns if a switch is missing a default case.
 * 
 * Created by Alice Kroutikova '15.
 */

package critter2.checks;

import cetus.hir.Default;
import cetus.hir.DepthFirstIterator;
import cetus.hir.Program;
import cetus.hir.SwitchStatement;
import cetus.hir.Traversable;
import critter2.CritterCheck;

public class CheckSwitchHasDefaultCase extends CritterCheck {

	/*
	 * Constructor used in testing.
	 */
	public CheckSwitchHasDefaultCase(Program program,
			ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/*
	 * General constructor used in Critter.java.
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
    		Traversable t = dfs.next();
    		
    		// skips all the standard included files
    		if (t.toString().startsWith("#pragma critTer:startStdInclude:")) {
    			while (!(t.toString().startsWith("#pragma critTer:endStdInclude:"))) {
    				t = dfs.next();
    			}
    		}
    		
    		else if (t instanceof SwitchStatement) {
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
    						"%nA switch statement should have a default " +
    						"case%n");
    			}
    		}
    	}
	}
}
