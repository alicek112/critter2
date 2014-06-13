package critter2.checks;

import cetus.hir.DepthFirstIterator;
import cetus.hir.Program;
import cetus.hir.Traversable;
import critter2.CritterCheck;

public class CheckFileLength extends CritterCheck {
	
	// COS217 maximum file length in lines.
    private final static int MAX_FILE_LENGTH = 500;

	public CheckFileLength(Program program, ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	public CheckFileLength(Program program) {
		super(program);
	}

	@Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	
    	long linecount = 0;
    	Traversable currentNode = null;
    	
    	while (dfs.hasNext()) {
    		Traversable t = dfs.next();
    		
    		// skips all the standard included files
    		if (t.toString().startsWith("#pragma critTer:startStdInclude:")) {
    			while (!(t.toString().startsWith("#pragma critTer:endStdInclude:"))) {
    				t = dfs.next();
    			}
    		}
    		
    		// deals with student's included files
    		else if (t.toString().startsWith("#pragma critTer:endStudentInclude:")) {
    			if (getLineNumber(t) > MAX_FILE_LENGTH) {
    	    		reportErrorPos(t, "low priority: %nA source " +
    	    				"code file should contain fewer than %d " +
    	    				"lines;%nthis file contains %d lines%n", MAX_FILE_LENGTH, getLineNumber(t));
    	    	}
    		}
    		
    		else if (t.toString().startsWith("#pragma critTer") 
    				&& !t.toString().contains("Include")) {
    			String[] parts = t.toString().split(":");
    		    long currentline = Long.parseLong(parts[1]);
    		    if (currentline > linecount) {
    		       	linecount = currentline;
    		       	currentNode = t;
    		    }
    		}
    	}
    	
    	if (linecount > MAX_FILE_LENGTH) {
    		reportErrorPos(currentNode, "low priority: %nA source code " +
    				"file should contain fewer than %d " +
    				"lines;%nthis file contains %d lines%n", MAX_FILE_LENGTH, linecount);
    	}
	}
}
