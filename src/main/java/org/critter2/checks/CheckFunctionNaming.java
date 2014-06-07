package org.critter2.checks;

import org.critter2.CritterCheck;

import cetus.hir.DepthFirstIterator;
import cetus.hir.IDExpression;
import cetus.hir.Procedure;
import cetus.hir.Program;
import cetus.hir.Traversable;

public class CheckFunctionNaming extends CritterCheck {

	public CheckFunctionNaming(Program program, CritterCheck.ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	public CheckFunctionNaming(Program program) {
		super(program);
	}

	@Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);

		String commonPrefix = null;
		
		// check if test needs to be done (check if there is a main 
		// function definition)
		boolean hasMain = false;
		while (dfs.hasNext()) {
			Traversable t = dfs.next();
			
			if (t instanceof Procedure) {
				IDExpression n = ((Procedure) t).getName();
    			String name = n.getName();
    			if (name.compareTo("main") == 0)
    				hasMain = true;
			}
		}
		
		if (!hasMain) {
			dfs = new DepthFirstIterator<Traversable>(program);
		
			while (dfs.hasNext()) {
	    		Traversable t = dfs.next();
	    		
	    		// skips all the standard included files
	    		if (t.toString().startsWith("#pragma critTer:startStdInclude:")) {
	    			while (!(t.toString().startsWith("#pragma critTer:endStdInclude:"))) {
	    				t = dfs.next();
	    			}
	    		}
	    		
	    		if (t instanceof Procedure) {
	    			IDExpression n = ((Procedure) t).getName();
	    			String name = n.getName();
	    			String prefix = name.split("_")[0];
	    			if (commonPrefix == null && 
	    					prefix.compareTo("main") != 0)
	    				commonPrefix = prefix;
	    			else if (prefix.compareTo("main") != 0) {
	    				if (commonPrefix.compareTo(prefix) != 0) {
	    					reportErrorPos(t, "medium priority: " +
	    							"\nA function's prefix should match the " +
	    							"module name; %s and %s do not match\n", 
	        						commonPrefix, prefix);
	    					
	    				}
	    			}
	    		}
			}
    	}
	}
}
