/*
 * Warns if function comment fails to mention each parameter 
 * by name or explain what the function returns.
 * Also warns if a function is missing a comment.
 * 
 * Created by Alice Kroutikova '15.
 */
package critter2.checks;

import java.util.ArrayList;
import java.util.List;

import critter2.CritterCheck;

import cetus.hir.DepthFirstIterator;
import cetus.hir.PreAnnotation;
import cetus.hir.Procedure;
import cetus.hir.Program;
import cetus.hir.Traversable;

public class CheckFunctionCommentValid extends CritterCheck {

	/*
	 * Constructor used in testing.
	 */
	public CheckFunctionCommentValid(Program program,
			ErrorReporter errorReporter) {
		super(program, errorReporter);
	}

	/* 
	 * General constructor used in Critter.java
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
    		Traversable t = dfs.next();
    		
    		// skips all the standard included files
    		if (t.toString().startsWith("#pragma critTer:startStdInclude:")) {
    			while (!(t.toString().startsWith("#pragma critTer:endStdInclude:"))) {
    				t = dfs.next();
    			}
    		}
    		
    		else if (t instanceof Procedure) {
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
		   					
		   					if (!comment.toString().contains(" " + paramName)) {
		   						reportErrorPos(comment, "high priority: " +
		   								"\nA function's comment should refer to " +
		   								"each parameter by name;\nyour comment " +
		   								"does not refer to '%s'\n", paramName);
		   					}
		    			}
		    				
		    			// Checks for explicitly stated return, only 
		   				// for non-void function.
		    			if (!stringList.contains("void")) {
			    			if (!comment.toString().contains("return") && 
			    					!comment.toString().contains("Return")) {
			   					reportErrorPos(comment, "high priority: " +
			   							"\nA function's comment should state " +
			   	                        "explicitly what the function returns\n");
		    				}
	    				}
	    			}
	   			}
	   			
	   			// All functions, including main functions, should have comments
	   			// preceding them.
	   			if (!(p instanceof PreAnnotation)) {
		    		reportErrorPos(function, "high priority: " +
		    				"\nA function definition should have a comment\n");		
	    		}
    		}
    	}
	}
}
