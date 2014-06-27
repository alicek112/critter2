package critter2.checks;

import cetus.hir.AnnotationDeclaration;
import cetus.hir.AnnotationStatement;
import cetus.hir.ClassDeclaration;
import cetus.hir.DepthFirstIterator;
import cetus.hir.Enumeration;
import cetus.hir.PreAnnotation;
import cetus.hir.Procedure;
import cetus.hir.Program;
import cetus.hir.Traversable;
import cetus.hir.VariableDeclaration;
import critter2.CritterCheck;

/**
 * Warns if global variables are missing comments.
 * 
 * Comments must be either on the line immediately preceding the global 
 * variable, or with at most one blank line between the comment and 
 * the global variable.
 * 
 * @author Alice Kroutikova '15
 *
 */
public class CheckGlobalHasComment extends CritterCheck {

	/**
     * Constructor used for testing.
     * 
     * @param program the root node of the parse tree
     * @param errorReporter testing class
     */
	public CheckGlobalHasComment(Program program, CritterCheck.ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/**
	 * Main constructor used in Critter.java
	 * 
	 * @param program the root node of the parse tree
	 */
	public CheckGlobalHasComment(Program program) {
		super(program);
	}

	@Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
		
    	dfs.pruneOn(Procedure.class); 		 // skips all the functions (Procedure nodes)
    	dfs.pruneOn(ClassDeclaration.class); // skips all the structs

    	// Traverse parse tree (skipping functions and their contents).
    	while (dfs.hasNext()) {
    		Traversable t = nextNoStdInclude(dfs);
    		
    		if (t instanceof VariableDeclaration 
    				|| t instanceof Enumeration ) {
    			
    			// typedef int * __ are the variables declared through C preprocessing,
    			// not in student code, and are therefore ignored.
    			if (!t.toString().startsWith("typedef int * __")) {
    				
	    			Traversable p = getPreviousNonPragma(t);
	    			
	    			if (!(p instanceof AnnotationStatement) 
	    					&& !(p instanceof AnnotationDeclaration)
	    					&& !(p instanceof PreAnnotation)) {
	    				
	    				if (t.getParent().getParent() != null) {
	    					if (!(t.getParent().getParent() instanceof VariableDeclaration)) {
	    						
			    				reportErrorPos(t, "high priority: " +
			    						"%n   A comment should appear above each " +
			    						"global variable.%n");
	    					}
	    				}
	    			}
    			}
    		}
    		
    		// CETUS considers fields of a struct that are also structs as
    		// separate class declarations, this filters those nodes out
    		if (t instanceof ClassDeclaration && t.getChildren() != null) {
    			
    			// typedef int * __ are the variables declared through C preprocessing,
    			// not in student code, and are therefore ignored.
    			if (!t.toString().startsWith("typedef int * __")) {
    				
	    			Traversable p = getPreviousNonPragma(t);
	    			
	    			if (!(p instanceof AnnotationStatement) 
	    					&& !(p instanceof AnnotationDeclaration)
	    					&& !(p instanceof PreAnnotation)) {
	    				if (t.getParent().getParent() != null) {
	    					if (!(t.getParent().getParent() instanceof VariableDeclaration)) {
	    						
			    				reportErrorPos(t, "high priority: " +
			    						"%n   A comment should appear above each " +
			    						"global variable.%n");
	    					}
	    				}
	    			}
    			}
    		}
    	}
	}
}
