package critter2.checks;

import java.util.ArrayList;
import java.util.List;

import critter2.CritterCheck;
import cetus.hir.Declaration;
import cetus.hir.DepthFirstIterator;
import cetus.hir.PreAnnotation;
import cetus.hir.Procedure;
import cetus.hir.ProcedureDeclarator;
import cetus.hir.Program;
import cetus.hir.Specifier;
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
		
		// declaredFunctions keeps track of functions declared in the .h files.
		List<ProcedureDeclarator> declaredFunctions = new ArrayList<ProcedureDeclarator>();
    	
    	// Traverse parse tree looking for functions (Procedure nodes).
    	while (dfs.hasNext()) {
    		Traversable t = nextNoStdInclude(dfs);
    		
    		
    		if (t instanceof ProcedureDeclarator) {
    			ProcedureDeclarator declarator = (ProcedureDeclarator) t;
    			//declaredFunctions.add(declarator.toString());
    			declaredFunctions.add(declarator);
    			
    			PreAnnotation[] prevComments = getPreviousComments(declarator.getParent());
    			
    			if (prevComments.length == 0) {
		    		reportErrorPos(declarator, "high priority: " +
		    				"\n   A function declaration should have a comment\n");		
	    		}
	   			
    			else {
	   				PreAnnotation comment = prevComments[0]; // gives location for all comments
		    				
	   				List<Declaration> params = declarator.getParameters();
	   				
	    			// Checks if the function's comment refers to parameters.
	   				for (int i = 0; 
	   						i < params.size(); 
	   						i++) {
	   					
	   					String paramName = params.get(i).getDeclaredIDs().get(0).toString();
	   					
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
	    			if (!declarator.getTypeSpecifiers().contains(Specifier.VOID)) {
	    				
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
    		
    		if (t instanceof Procedure) {
    			Procedure function = (Procedure) t;
    			
    			// Static functions (and main functions) should have comments in the .c file.
    			if (function.getTypeSpecifiers().contains(Specifier.STATIC) || function.getName().toString().compareTo("main") == 0) {
    			
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
			    				"\n   A static function definition should have a comment\n");		
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
		    			if (!function.getTypeSpecifiers().contains(Specifier.VOID)) {
		    				
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
    			
    			// Check if non-static functions have the same variable names in declaration and definition
    			else {
    				
    				String functionName = function.getDeclarator().getID().toString();
					
    				for (int i = 0; i < declaredFunctions.size(); i++) {
    					String currName = declaredFunctions.get(i).getID().toString();
    					if (currName.equals(functionName)) {
    						List<?> functionParams = function.getParameters();
    						List<?> currParams = declaredFunctions.get(i).getParameters();
    						
    						if (functionParams.size() == currParams.size()) {
    						
	    						for (int j = 0; j < functionParams.size(); j++) {
	    							
	    							if (!functionParams.get(j).toString().split(" ")[0].equals(currParams.get(j).toString().split(" ")[0])) {
	    								break;
	    							}
	    							if (!functionParams.get(j).toString().equals(currParams.get(j).toString())) {
	    								reportErrorPos(function, "low priority: " +
	    			   							"\n   Parameter name \'%s\' in function definition differs "
	    			   							+ "from parameter name \'%s\' in function declaration\n",
	    			   							functionParams.get(j).toString(), currParams.get(j).toString());
	    							}
	    							
	    						}
    						}
    						
    					}
    					
    				}
    			}
    		}
    	}
	}
}
