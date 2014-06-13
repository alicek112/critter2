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

	public CheckGlobalHasComment(Program program, ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	public CheckGlobalHasComment(Program program) {
		super(program);
	}

	@Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	dfs.pruneOn(Procedure.class); // skips all the functions
    	dfs.pruneOn(ClassDeclaration.class); // skips all the structs

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
