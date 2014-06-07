package critter2.checks;

import critter2.CritterCheck;

import cetus.hir.AnnotationDeclaration;
import cetus.hir.DepthFirstIterator;
import cetus.hir.PreAnnotation;
import cetus.hir.Program;
import cetus.hir.Traversable;

public class CheckBeginningComment extends CritterCheck {
	public CheckBeginningComment(Program program, CritterCheck.ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	public CheckBeginningComment(Program program) {
		super(program);
	}

	@Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	
    	Traversable t = dfs.next();
    	
    	while (!(t.toString().startsWith("#pragma critTer"))) {
    		t = dfs.next();
    	}
    	Traversable first = dfs.next();
    	
    	if (first.toString().startsWith("#pragma")) {
    		reportErrorPos(first, "high priority: " +
    				"\nA file should begin with a comment.\n");
    	}
    	
    	if (!(first instanceof PreAnnotation)) {
    		reportErrorPos(first, "high priority: " +
    				"\nA file should begin with a comment.\n");
    	}
    	
    	dfs = new DepthFirstIterator<Traversable>(program);
    	
    	// check all student's .h files
    	while (dfs.hasNext()) {
    		t = dfs.next();
    		
    		if (t.toString().startsWith("#pragma critTer:startStudentInclude")) {
    			while (!(t.toString().startsWith("#pragma critTer:1:")))
    	    		t = dfs.next();
    			Traversable n = dfs.next();
    			if (n.toString().startsWith("#pragma critTer")) {
    				reportErrorPos(n, "high priority: " +
    						"\nA file should begin with a comment.\n");
    			}
    			if (!(n instanceof PreAnnotation)) {
    				reportErrorPos(n, "high priority: " +
    						"\nA file should begin with a comment.\n");
    			}
    		}
    	}
	}
}
