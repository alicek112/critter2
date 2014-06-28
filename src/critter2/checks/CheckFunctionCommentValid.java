package critter2.checks;

import java.util.ArrayList;
import java.util.List;

import critter2.CritterCheck;

import cetus.hir.DepthFirstIterator;
import cetus.hir.PreAnnotation;
import cetus.hir.Procedure;
import cetus.hir.Program;
import cetus.hir.Traversable;

/**
 * Warns if function comment fails to mention each parameter 
 * by name or explain what the function returns.
 * Also warns if a function is missing a comment.
 * 
 * @author Alice Kroutikova '15
 *
 */
public class CheckFunctionCommentValid extends CritterCheck {

	/**
     * Constructor used for testing.
     * 
     * @param program the root node of the parse tree
     * @param errorReporter testing class
     */
	public CheckFunctionCommentValid(Program program, CritterCheck.ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/**
	 * Main constructor used in Critter.java
	 * 
	 * @param program the root node of the parse tree
	 */
	public CheckFunctionCommentValid(Program program) {
		super(program);
	}

	@Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	
    	// Traverse parse tree looking for functions (Procedure nodes).
    	while (dfs.hasNext()) {
    		Traversable t = nextNoStdInclude(dfs);
    		
    		if (t instanceof Procedure) {
    			Procedure function = (Procedure) t;
    			
	    		List<?> list = function.getReturnType();
	    		List<String> stringList = new ArrayList<String>();
	    			
	   			for (Object x : list) {
	   				stringList.add(x.toString());
	   			}
	   			
	   			Traversable p = getPreviousNonPragma(function);
	   			
	   			// main function doesn't need to be checked for parameter
	   			// or return mentions in comment
	   			if (function.getName().toString().compareTo("main") != 0) {
		    			
		    		if (p instanceof PreAnnotation) {
		    			Traversable comment = (PreAnnotation) p;
		    				
		    			// Checks if the function's comment refers to 
		    			// parameters.
		   				for (int i = 0; 
		   						i < function.getNumParameters(); 
		   						i++) {
		    					
		   					String paramName = 
		   						function.getParameter(i).getDeclaredIDs().get(0).toString();
		   					
		   					if (!comment.toString().contains(paramName)) {
		   						reportErrorPos(comment, "high priority: " +
		   								"\n   A function's comment should refer to each parameter by name;"
		   								+ "\n   your comment does not refer to '%s'\n", paramName);
		   					}
		    			}
		    				
		    			// Checks for explicitly stated return, only 
		   				// for non-void function.
		    			if (!stringList.contains("void")) {
			    			if (!comment.toString().contains("return") && 
			    					!comment.toString().contains("Return")) {
			   					reportErrorPos(comment, "high priority: " +
			   							"\n   A function's comment should state " +
			   	                        "explicitly what the function returns\n");
		    				}
	    				}
	    			}
	   			}
	   			
	   			// All functions, including main functions, should have comments
	   			// preceding them.
	   			if (!(p instanceof PreAnnotation)) {
		    		reportErrorPos(function, "high priority: " +
		    				"\n   A function definition should have a comment\n");		
	    		}
    		}
    	}
	}
}
