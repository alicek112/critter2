package critter2;

import java.util.Iterator;

import cetus.hir.DepthFirstIterator;
import cetus.hir.PreAnnotation;
import cetus.hir.Program;
import cetus.hir.Traversable;

/**
 * Base class for all checks.
 * 
 * @author Alice Kroutikova '15.
 *
 */
public abstract class CritterCheck {
 
	/**
	 * Interface for error reporting.
	 *
	 */
	public interface ErrorReporter {
		public void reportError(String message, Object... args);
	}
	
	/**
	 * Root node of the parse tree.
	 */
	protected final Program program;
	
	private final ErrorReporter errorReporter;

	/**
	 * Constructor used for testing.
	 * 
	 * @param program the root node of the parse tree
     * @param errorReporter testing class
	 */
    public CritterCheck(Program program, ErrorReporter errorReporter) {
    	
        this.program = program;
        this.errorReporter = errorReporter;
    }
    
    /**
	 * Main constructor used in Critter.java
	 * 
	 * @param program the root node of the parse tree
	 */
    public CritterCheck(Program program) {
    	this(program, new StandardErrorReporter());
    }
    
    /**
     * Subclass must override this and implement checks and report warnings, if any.
     * 
     */
    public abstract void check();
    
    /**
     * Use this to report warnings without locations.
     * 
     * @param message the format string of the warning message to report
     * @param args formatting arguments
     */
    public void reportError(String message, Object... args) {
    	errorReporter.reportError(message, args);
    }
    
    /**
     * Use this to report warnings and their locations.
     * 
     * @param t the node in the parse tree where the stylistic flaw occurred
     * @param message the format string of the warning message to report
     * @param args formatting arguments
     */
    public void reportErrorPos(Traversable t, String message, Object... args) {
    	String error = String.format("%n%s: line %d: ", getFilename(t), getLineNumber(t)) + String.format(message, args);
    	reportError(error);
    }
    
    /**
     * Returns the line number (based on the pragmas inserted during the 
     * annotating Critter script)
     * 
     * @param element the node whose starting location you need
     * @return the line number
     */
    public long getLineNumber(Traversable element) {
        Traversable lastComment = getPrevious(element);
        
        while(!lastComment.toString().startsWith("#pragma critTer") 
        		|| lastComment.toString().contains("Include"))
        	lastComment = getPrevious(lastComment);

        String[] parts = lastComment.toString().split(":");
        return Long.parseLong(parts[1]);
    }
    
    /**
     * Returns the filename of the file in which a parse tree node
     * occurs (based on the pragmas inserted during the annotating Critter script)
     * 
     * @param element the node whose filename you need
     * @return the filename of the node's location
     */
    public String getFilename(Traversable element) {
    	Traversable lastComment = getPrevious(element);
        
        while(!lastComment.toString().startsWith("#pragma critTer") 
        		|| lastComment.toString().contains("Include"))
        	lastComment = getPrevious(lastComment);

        String[] parts = lastComment.toString().split(":");
        return parts[2];
    }
    
    
    /**
     * Returns the previous node in the parse tree that is not a pragma
     * annotation
     * 
     * @param current the current node of the parse tree
     * @return the previous node that is not a pragma annotation
     */
    public Traversable getPreviousNonPragma(Traversable current) {
    	Traversable nonPragmaPrev = getPrevious(current);
    	while (nonPragmaPrev.toString().startsWith("#pragma")) {
    		// skip over .h files
    		if (nonPragmaPrev.toString().startsWith("#pragma critTer:endStudentInclude"))
    			return null;
    		if (nonPragmaPrev.toString().startsWith("#pragma critTer:end")) {
    			while(!nonPragmaPrev.toString().startsWith("#pragma critTer:start"))
    				nonPragmaPrev = getPrevious(nonPragmaPrev);
    		}
    		nonPragmaPrev = getPrevious(nonPragmaPrev);
    	}
    	
    	return nonPragmaPrev;
    }
    
    /**
     * Returns the previous node in the parse tree that is a pragma
     * annotation
     * 
     * @param current the current node of the parse tree
     * @return the previous node that is a pragma annotation
     */
    public Traversable getPreviousPragma(Traversable current) {
    	Traversable pragmaPrev = getPrevious(current);
    	while (!pragmaPrev.toString().startsWith("#pragma"))
    		pragmaPrev = getPrevious(pragmaPrev);
    	return pragmaPrev;
    }
    
    /**
     * Returns the previous node in the parse tree
     * 
     * @param current the current node of the parse tree
     * @return the previous node
     */
    public Traversable getPrevious(Traversable current) {
    	DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	DepthFirstIterator<Traversable> dfs2 = 
    			new DepthFirstIterator<Traversable>(program);
    	
    	// dfs2 is always walking one ahead of dfs
    	dfs2.next();
    	Traversable t = dfs2.next(); 
    	
    	Traversable prev = dfs.next();
    	
    	while (dfs2.hasNext()) {
    		if (t == current)
    			break;
    		t = dfs2.next();
    		prev = dfs.next();
    	}
    	
    	return prev;
    }
    
    /**
     * Returns the number of lines contained in the parse tree node node.
     * 
     * @param node the parse tree element whose lines are being counted
     * @return the number of lines in the node
     */
    public int linecount(Traversable node) {
    	DepthFirstIterator<Traversable> ldfs = 
				new DepthFirstIterator<Traversable>(node);
		
		int linecount = 0;
		while (ldfs.hasNext()) {
			Traversable t = ldfs.next();
			
			// counts the pragmas (from annotating script) indicating line numbers in the loop
			if (t instanceof PreAnnotation 
					&& t.toString().startsWith("#pragma critTer")
					&& !t.toString().contains("Include")) {
				linecount++;
			}
		}
		
		return linecount;
    }
    
    /**
     * Filters any iterator but skipping all the standard included header files.
     * 
     * @param i the iterator used
     * @return the next node, skipping standard included header files
     */
    public Traversable nextNoStdInclude(Iterator<Traversable> i) {
    	if (!i.hasNext())
    		return null;
    	Traversable next = i.next();
    	
    	// Uses the pragmas inserted in the annotate critter script to
    	// skip over standard header files
    	if (next.toString().startsWith("#pragma critTer:startStdInclude:")) {
			while (!(next.toString().startsWith("#pragma critTer:endStdInclude:"))) {
				next = i.next();
			}
		}
    	
    	return next;
    }
    
    /**
     * Filters any iterator, skipping all included header files (standard and user-created).
     * 
     * @param i the iterator used
     * @return the next node, skipping all included header files
     */
    public Traversable nextNoInclude(Iterator<Traversable> i) {
    	if (!i.hasNext())
    		return null;
    	Traversable next = i.next();
    	
    	// Uses the pragmas inserted in the annotate critter script to
    	// skip over standard header files
    	if (next.toString().startsWith("#pragma critTer:startStdInclude:")) {
			while (!(next.toString().startsWith("#pragma critTer:endStdInclude:"))) {
				next = i.next();
			}
		}
    	
    	// Uses the pragmas inserted in the annotate critter script to
    	// skip over user header files
    	if (next.toString().startsWith("#pragma critTer:startStudentInclude:")) {
			while (!(next.toString().startsWith("#pragma critTer:endStudentInclude:"))) {
				next = i.next();
			}
		}
    	
    	return next;
    }
}

/**
 * Standard error reporter that prints the reported error to standard error.
 *
 */
class StandardErrorReporter implements CritterCheck.ErrorReporter {
	public void reportError(String message, Object... args) {
    	System.err.printf(message, args);
    }
}