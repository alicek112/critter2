package critter2.checks;

import java.util.ArrayList;
import java.util.List;

import cetus.hir.Declaration;
import cetus.hir.DepthFirstIterator;
import cetus.hir.IDExpression;
import cetus.hir.Procedure;
import cetus.hir.Program;
import cetus.hir.Traversable;
import critter2.CritterCheck;

/**
 * Warns if pointer parameters not validated by an assert
 * 
 * @author Alice Kroutikova '15
 *
 */
public class CheckAsserts extends CritterCheck {

	/**
     * Constructor used for testing.
     * 
     * @param program the root node of the parse tree
     * @param errorReporter testing class
     */
	public CheckAsserts(Program program, CritterCheck.ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/**
	 * Main constructor used in Critter.java
	 * 
	 * @param program the root node of the parse tree
	 */
	public CheckAsserts(Program program) {
		super(program);
	}

	@Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = new DepthFirstIterator<Traversable>(program);
    	
		// Traverse parse tree looking for function nodes (Procedure)
    	while (dfs.hasNext()) {
    		Traversable t = nextNoStdInclude(dfs);
    		
    		if (t instanceof Procedure) {
    			
	    		// Keep list of parameters for the function
	    		@SuppressWarnings("unchecked")
				List<Declaration> params = ((Procedure) t).getParameters();
	    		List<String> paramNames = new ArrayList<String>();
	    		
	    		for (Declaration p : params) {
		    		List<IDExpression> declaredIDs = p.getDeclaredIDs();
		    		for (IDExpression parameter : declaredIDs) {
		    			// parameters formated as arrays
		    			if (parameter.getParent().toString().contains("[]")) {
		    				paramNames.add(parameter.toString());
		    			}
		    			// parameters formatted as pointers
		    			else if (parameter.getParent().toString().contains("*")) {
		    				paramNames.add(parameter.toString());
		    			}
		    		}
		    	
	    		}	
		    	boolean[] hasAssert = new boolean[paramNames.size()];
		    	
		    	// Traverse parse tree rooted at function to determine if asserts
		    	// are being called
		    	DepthFirstIterator<Traversable> functiondfs = 
	    				new DepthFirstIterator<Traversable>(t);
		    	while (functiondfs.hasNext()) {
		    		Traversable t2 = functiondfs.next();
		    		
		    		if (t2.toString().startsWith("__assert")) {
		   				for (int i = 0; i < paramNames.size(); i++) {
		   					if (t2.toString().contains(paramNames.get(i)))
		   						hasAssert[i] = true;
		   				}
	    			}	
		    	}
		    	
		    	// no need for asserts for argv
		    	for (int i = 0; i < hasAssert.length; i++) {
		    		if (paramNames.get(i).compareTo("argv") != 0) {
			    		if (!hasAssert[i]) {
			   				reportErrorPos(t, "medium priority:" +
			   						"\n   Do you want to validate '%s' through an assert?\n", paramNames.get(i));
			   			}
		    		}
	    		}
    		}
    	}
	}
}
