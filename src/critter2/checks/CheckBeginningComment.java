package critter2.checks;

import critter2.CritterCheck;

import cetus.hir.DepthFirstIterator;
import cetus.hir.PreAnnotation;
import cetus.hir.Program;
import cetus.hir.Traversable;

/**
 * Warns if there is no comment in the beginning of each file.
 * 
 * @author alicek112
 *
 */
public class CheckBeginningComment extends CritterCheck {
	
	/**
     * Constructor used for testing.
     * 
     * @param program the root node of the parse tree
     * @param errorReporter testing class
     */
	public CheckBeginningComment(Program program, CritterCheck.ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/**
	 * Main constructor used in Critter.java
	 * 
	 * @param program the root node of the parse tree
	 */
	public CheckBeginningComment(Program program) {
		super(program);
	}

	@Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	
    	Traversable t = dfs.next();
    	
    	// Skips until the first physical line of code written by the student.
    	while (!(t.toString().startsWith("#pragma critTer"))) {
    		t = dfs.next();
    	}
    	Traversable first = dfs.next();
    	
    	// If two lines in a row begin with #pragma, then the first line 
    	// of student code is either blank or a pragma.
    	if (first.toString().startsWith("#pragma")) {
    		reportErrorPos(first, "high priority: " +
    				"\n   A file should begin with a comment.\n");
    	}
    	
    	if (!(first instanceof PreAnnotation)) {
    		reportErrorPos(first, "high priority: " +
    				"\n   A file should begin with a comment.\n");
    	}
    	
    	dfs = new DepthFirstIterator<Traversable>(program);
    	
    	// Check if all student's .h files begin with comments.
    	while (dfs.hasNext()) {
    		t = dfs.next();
    		
    		if (t.toString().startsWith("#pragma critTer:startStudentInclude")) {
    			// If two lines in a row begin with #pragma, then the first line 
    	    	// of student code is either blank or a pragma.
    			while (!(t.toString().startsWith("#pragma critTer:1:")))
    	    		t = dfs.next();
    			Traversable n = dfs.next();
    			if (n.toString().startsWith("#pragma critTer")) {
    				reportErrorPos(n, "high priority: " +
    						"\n   A file should begin with a comment.\n");
    			}
    			if (!(n instanceof PreAnnotation)) {
    				reportErrorPos(n, "high priority: " +
    						"\n   A file should begin with a comment.\n");
    			}
    		}
    	}
	}
}
