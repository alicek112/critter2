/*
 * Warns if functions don’t have enough local comments.
 * 
 * Assumes that if, while, for, do while and switch elements should all have comments.
 * Checks the number of local comments with the number of those 
 * elements, and throws a warning if the discrepancy between the two 
 * is greater than MAX_LOCAL_COMMENT_DISCREPANCY.
 * 
 * Created by Alice Kroutikova '15.
 */

package critter2.checks;

import cetus.hir.AnnotationStatement;
import cetus.hir.DepthFirstIterator;
import cetus.hir.IfStatement;
import cetus.hir.Loop;
import cetus.hir.Procedure;
import cetus.hir.Program;
import cetus.hir.SwitchStatement;
import cetus.hir.Traversable;
import critter2.CritterCheck;

public class CheckFunctionHasEnoughComments extends CritterCheck {

	// COS217 maximum discrepancy between number of local comments
    // and the number of elements that should have comments
    private final static int MAX_LOCAL_COMMENT_DISCREPANCY = 5;
	
    /*
     * Constructor used in testing.
     */
	public CheckFunctionHasEnoughComments(Program program,
			ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/*
	 * General constructor used in Critter.java.
	 */
	public CheckFunctionHasEnoughComments(Program program) {
		super(program);
	}

	@Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	
		// Traverse parse tree looking for functions (Procedure nodes).
    	while (dfs.hasNext()) {
    		Traversable t = dfs.next();
    		
    		// skips all the standard included files
    		if (t.toString().startsWith("#pragma critTer:startStdInclude:")) {
    			while (!(t.toString().startsWith("#pragma critTer:endStdInclude:"))) {
    				t = dfs.next();
    			}
    		}
    		
    		else if (t instanceof Procedure) {
    			DepthFirstIterator<Traversable> functiondfs = 
    					new DepthFirstIterator<Traversable>(t);
    			
    			// Count if, while, for, do while and switch elements
    			int countElements = 0;
    			// Count comments
    			int countComments = 0;
    			
    			// Traverse tree rooted at the function for elements and comments.
    			while (functiondfs.hasNext()) {
    				Traversable functiont = functiondfs.next();
    				if (functiont instanceof Loop)
    					countElements++;
    				else if (functiont instanceof IfStatement)
    					countElements++;
    				else if (functiont instanceof SwitchStatement)
    					countElements++;
    				
    				else if (functiont instanceof AnnotationStatement) {
    					if (!functiont.toString().startsWith("#pragma"))
    						countComments++;	
    				}
    			}
    			
    			if ((countElements - countComments) 
    					> MAX_LOCAL_COMMENT_DISCREPANCY) {
    				reportErrorPos(t, "low priority: " +
    						"%nThis function definition probably needs" +
    						" more local comments%n");
    			}
    		}
    	}
	}
}
