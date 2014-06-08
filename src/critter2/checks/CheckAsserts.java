package critter2.checks;

import java.util.ArrayList;
import java.util.List;

import critter2.CritterCheck;

import cetus.hir.DFIterator;
import cetus.hir.Declaration;
import cetus.hir.DepthFirstIterator;
import cetus.hir.IDExpression;
import cetus.hir.Procedure;
import cetus.hir.Program;
import cetus.hir.Traversable;

public class CheckAsserts extends CritterCheck {

	public CheckAsserts(Program program, ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	public CheckAsserts(Program program) {
		super(program);
	}

	@Override
	public void check() {
		DFIterator<Traversable> dfs = new DFIterator<Traversable>(program);
    	
    	while (dfs.hasNext()) {
    		Traversable t = dfs.next();
    		
    		// skips all standard included files
    		if (t.toString().startsWith("#pragma critTer:startStdInclude:")) {
    			while (!(t.toString().startsWith("#pragma critTer:endStdInclude:"))) {
    				t = dfs.next();
    			}
    		}
    		
    		if (t instanceof Procedure) {
    			
	    		DepthFirstIterator<Traversable> functiondfs = 
	    				new DepthFirstIterator<Traversable>(t);
	    		
	    		@SuppressWarnings("unchecked")
				List<Declaration> params = ((Procedure) t).getParameters();
	    		List<String> paramNames = new ArrayList<String>();
	    		
	    		for (Declaration p : params) {
		    		List<IDExpression> declaredIDs = p.getDeclaredIDs();
		    		for (IDExpression parameter : declaredIDs) {
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
			   						" \nDo you want to validate '%s' " +
			   						"through an assert?\n", paramNames.get(i));
			   			}
		    		}
	    		}
    		}
    	}
	}
}
