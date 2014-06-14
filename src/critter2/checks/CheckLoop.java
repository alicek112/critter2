/*
 * Check if loop length exceeds a maximum line length (MAX_LOOP_LENGTH).
 * 
 * Created by Alice Kroutikova '15
 */

package critter2.checks;

import critter2.CritterCheck;

import cetus.hir.AnnotationStatement;
import cetus.hir.DepthFirstIterator;
import cetus.hir.Loop;
import cetus.hir.PreAnnotation;
import cetus.hir.Program;
import cetus.hir.Statement;
import cetus.hir.Traversable;

public class CheckLoop extends CritterCheck {
	
	// COS217 maximum loop length
    private static final int MAX_LOOP_LENGTH = 35;
	
    /*
     * Constructor used for testing.
     */
	public CheckLoop(Program program, CritterCheck.ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/*
	 * Main constructor used in Critter.java
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
    		Traversable t = dfs.next();
    		
    		// skips all the standard included files
    		if (t.toString().startsWith("#pragma critTer:startStdInclude:")) {
    			while (!(t.toString().startsWith("#pragma critTer:endStdInclude:"))) {
    				t = dfs.next();
    			}
    		}
    		
    		else if (t instanceof Loop) {
    			Statement s = ((Loop) t).getBody();
    			DepthFirstIterator<Traversable> ldfs = 
    					new DepthFirstIterator<Traversable>(s);
    			
    			int looplinecount = 0;
    			while (ldfs.hasNext()) {
    				Traversable st = ldfs.next();
    				
    				// counts the pragmas (from annotating script) indicating line numbers in the loop
    				if (st instanceof PreAnnotation && st.toString().startsWith("#pragma critTer")) {
    					looplinecount++;
    				}
    			}
    			
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
