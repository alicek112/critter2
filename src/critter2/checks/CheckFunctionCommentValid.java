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
	   			
	   			PreAnnotation[] prevComments = getPreviousComments(function);
	   			
	   			// All functions, including main functions, should have comments
	   			// preceding them.
	   			if (prevComments.length == 0) {
		    		reportErrorPos(function, "high priority: " +
		    				"\n   A function definition should have a comment\n");		
	    		}
	   			
	   			// main function doesn't need to be checked for parameter
	   			// or return mentions in comment
	   			else if (function.getName().toString().compareTo("main") != 0) {
	   				PreAnnotation comment = prevComments[0]; // gives location for all comments
		    				
	    			// Checks if the function's comment refers to parameters.
	   				for (int i = 0; 
	   						i < function.getNumParameters(); 
	   						i++) {
	    					
	   					String paramName = 
	   						function.getParameter(i).getDeclaredIDs().get(0).toString();
	   					
	   					boolean containsParam = false;
	   					
	   					for (int j = 0; j < prevComments.length; j++) {
	   						if (prevComments[j].toString().contains(paramName))
	   							containsParam = true;
	   					}
	   					
	   					if (!containsParam) {
	   						reportErrorPos(comment, "high priority: " +
	   								"\n   A function's comment should refer to each parameter by name;"
	   								+ "\n   your comment does not refer to '%s'\n", paramName);
	   					}
	    			}
	    			// Checks for explicitly stated return, only 
	   				// for non-void function.
	    			if (!stringList.contains("void")) {
	    				
	    				boolean containsReturn = false;
	    				
	    				for (int i = 0; i < prevComments.length; i++) {
	   						if (prevComments[i].toString().contains("return") || 
			    					prevComments[i].toString().contains("Return"))
	   							containsReturn = true;
	   					}
	    				
		    			if (!containsReturn) {
		   					reportErrorPos(comment, "high priority: " +
		   							"\n   A function's comment should state " +
		   	                        "explicitly what the function returns\n");
	    				}
    				}
	   			}
    		}
    	}
	}
}
