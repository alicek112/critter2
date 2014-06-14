/*
 * Warns if global variables are missing comments.
 * 
 * Comments must be either on the line immediately preceding the global 
 * variable, or with at most one blank line between the comment and 
 * the global variable.
 * 
 * Created by Alice Kroutikova '15.
 */

package critter2.checks;

import cetus.hir.AnnotationDeclaration;
import cetus.hir.AnnotationStatement;
import cetus.hir.ClassDeclaration;
import cetus.hir.DepthFirstIterator;
import cetus.hir.Enumeration;
import cetus.hir.Procedure;
import cetus.hir.Program;
import cetus.hir.Traversable;
import cetus.hir.VariableDeclaration;
import critter2.CritterCheck;

public class CheckGlobalHasComment extends CritterCheck {

	/*
	 * Constructor used in testing.
	 */
	public CheckGlobalHasComment(Program program, ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/*
	 * General constructor used in Critter.java.
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
    		Traversable t = dfs.next();
    		
    		// skips all the standard included files
    		if (t.toString().startsWith("#pragma critTer:startStdInclude:")) {
    			while (!(t.toString().startsWith("#pragma critTer:endStdInclude:"))) {
    				t = dfs.next();
    			}
    		}
    		
    		else if (t instanceof VariableDeclaration 
    				|| t instanceof Enumeration 
    				|| t instanceof ClassDeclaration) {
    			
    			// typedef int * __ are the variables declared through C preprocessing,
    			// not in student code, and are therefore ignored.
    			if (!t.toString().startsWith("typedef int * __")) {
    				
	    			Traversable p = getPreviousNonPragma(t);
	    			
	    			if (!(p instanceof AnnotationStatement) 
	    					&& !(p instanceof AnnotationDeclaration)) {
	    				if (t.getParent().getParent() != null) {
	    					if (!(t.getParent().getParent() instanceof VariableDeclaration)) {
			    				reportErrorPos(t, "high priority: " +
			    						"%nA comment should appear above each " +
			    						"global variable.%n");
	    					}
	    				}
	    			}
    			}
    		}
    	}
	}
}
