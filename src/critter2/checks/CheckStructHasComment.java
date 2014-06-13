package critter2.checks;

import cetus.hir.ClassDeclaration;
import cetus.hir.DepthFirstIterator;
import cetus.hir.Enumeration;
import cetus.hir.PreAnnotation;
import cetus.hir.Program;
import cetus.hir.Traversable;
import cetus.hir.VariableDeclaration;
import critter2.CritterCheck;

public class CheckStructHasComment extends CritterCheck {

	public CheckStructHasComment(Program program, ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	public CheckStructHasComment(Program program) {
		super(program);
	}

	@Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	
    	while (dfs.hasNext()) {
    		Traversable t = dfs.next();
    		
    		// skips all the standard included files
    		if (t.toString().startsWith("#pragma critTer:startStdInclude:")) {
    			while (!(t.toString().startsWith("#pragma critTer:endStdInclude:"))) {
    				t = dfs.next();
    			}
    		}
    		
    		else if (t instanceof ClassDeclaration) {
    			DepthFirstIterator<Traversable> cdfs = 
    					new DepthFirstIterator<Traversable>(t);
    			
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
