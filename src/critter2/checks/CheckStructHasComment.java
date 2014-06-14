package critter2.checks;

import cetus.hir.ClassDeclaration;
import cetus.hir.DepthFirstIterator;
import cetus.hir.Enumeration;
import cetus.hir.PreAnnotation;
import cetus.hir.Program;
import cetus.hir.Traversable;
import cetus.hir.VariableDeclaration;
import critter2.CritterCheck;

/**
 * Warns if a field in a struct lacks comments.
 * 
 * @author Alice Kroutikova '15.
 *
 */
public class CheckStructHasComment extends CritterCheck {

	/**
     * Constructor used for testing.
     * 
     * @param program the root node of the parse tree
     * @param errorReporter testing class
     */
	public CheckStructHasComment(Program program, CritterCheck.ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/**
	 * Main constructor used in Critter.java
	 * 
	 * @param program the root node of the parse tree
	 */
	public CheckStructHasComment(Program program) {
		super(program);
	}
	
	/**
	 * Implements check and reports warnings.
	 */
	@Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	
		// Traverse parse tree looking for structs (ClassDeclaration nodes)
    	while (dfs.hasNext()) {
    		Traversable t = nextNoStdInclude(dfs);
    		
    		if (t instanceof ClassDeclaration) {
    			DepthFirstIterator<Traversable> cdfs = 
    					new DepthFirstIterator<Traversable>(t);
    			
    			// Traverse tree rooted at struct node to find fields of struct
    			while (cdfs.hasNext()) {
    				Traversable c = cdfs.next();
    			
	    			if (c instanceof VariableDeclaration 
	    					|| t instanceof Enumeration) {
	    				
	        			Traversable p = getPreviousNonPragma(c.getParent());
	        			if (!(p instanceof PreAnnotation)) {
	        				reportErrorPos(c, "medium priority:" +
	        						" %nA comment should appear above each " +
	        						"field in a struct.%n");
	        			}
	        			
	    			}
    			}
        	}
    	}
	}
}
