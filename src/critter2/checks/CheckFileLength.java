package critter2.checks;

import cetus.hir.DepthFirstIterator;
import cetus.hir.Program;
import cetus.hir.Traversable;
import critter2.CritterCheck;

/**
 * Warns if file exceeds a maximum length (MAX_FILE_LENGTH),
 * measured by lines.
 * 
 * @author Alice Kroutikova '15.
 *
 */
public class CheckFileLength extends CritterCheck {
	
	// COS217 maximum file length in lines.
    private final static int MAX_FILE_LENGTH = 500;

    /**
     * Constructor used for testing.
     * 
     * @param program the root node of the parse tree
     * @param errorReporter testing class
     */
	public CheckFileLength(Program program, CritterCheck.ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/**
	 * Main constructor used in Critter.java
	 * 
	 * @param program the root node of the parse tree
	 */
	public CheckFileLength(Program program) {
		super(program);
	}

	/**
	 * Implements check and reports warnings.
	 */
	@Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	
    	long linecount = 0;
    	Traversable currentNode = null;
    	
    	// Traverse parse tree counting lines in files.
    	while (dfs.hasNext()) {
    		Traversable t = nextNoStdInclude(dfs);
    		
    		// deals with student's included files
    		if (t.toString().startsWith("#pragma critTer:endStudentInclude:")) {
    			// Finds the last Critter pragma in included file to find last line number.
    			if (getLineNumber(t) > MAX_FILE_LENGTH) {
    	    		reportErrorPos(t, "low priority: %nA source " +
    	    				"code file should contain fewer than %d " +
    	    				"lines;%nthis file contains %d lines%n", MAX_FILE_LENGTH, getLineNumber(t));
    	    	}
    		}
    		
    		// Finds the maximum line number in the main .c file.
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
