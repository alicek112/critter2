/*
 * Critter.java
 * 
 * Created by Alice Kroutikova '15, based on the Driver.java code of 
 * CETUS.
 * 
 * May 6, 2014
 * 
 */


package critter2;

import cetus.hir.DepthFirstIterator;
import cetus.hir.Program;
import cetus.hir.Traversable;

/**
* TODOTODOTODO
*/
public abstract class CritterCheck {
    
	public interface ErrorReporter {
		public void reportError(String message, Object... args);
	}
	
	protected final Program program;
	
	private final ErrorReporter errorReporter;

    public CritterCheck(Program program, ErrorReporter errorReporter) {
    	
        this.program = program;
        this.errorReporter = errorReporter;
    }
    
    public CritterCheck(Program program) {
    	this(program, new StandardErrorReporter());
    }
    
    public abstract void check();
    
    public void reportError(String message, Object... args) {
    	errorReporter.reportError(message, args);
    }
    
    public void reportErrorPos(Traversable t, String message, Object... args) {
    	String error = String.format("%n%s: line %d: ", getFilename(t), getLineNumber(t)) + String.format(message, args);
    	reportError(error);
    }
    
    public long getLineNumber(Traversable element) {
        Traversable lastComment = getPrevious(element);
        
        while(!lastComment.toString().startsWith("#pragma critTer") 
        		|| lastComment.toString().contains("Include"))
        	lastComment = getPrevious(lastComment);

        String[] parts = lastComment.toString().split(":");
        return Long.parseLong(parts[1]);
    }
    
    public String getFilename(Traversable element) {
    	Traversable lastComment = getPrevious(element);
        
        while(!lastComment.toString().startsWith("#pragma critTer") 
        		|| lastComment.toString().contains("Include"))
        	lastComment = getPrevious(lastComment);

        String[] parts = lastComment.toString().split(":");
        return parts[2];
    }
    
    
    /*
     * Returns the previous node in the parse tree that is not a pragma
     * annotation
     */
    public Traversable getPreviousNonPragma(Traversable current) {
    	Traversable nonPragmaPrev = getPrevious(current);
    	while (nonPragmaPrev.toString().startsWith("#pragma")) {
    		// skip over .h files
    		if (nonPragmaPrev.toString().startsWith("#pragma critTer:end")) {
    			while(!nonPragmaPrev.toString().startsWith("#pragma critTer:start"))
    				nonPragmaPrev = getPrevious(nonPragmaPrev);
    		}
    		nonPragmaPrev = getPrevious(nonPragmaPrev);
    	}
    	
    	return nonPragmaPrev;
    }
    
    /*
     * Returns previous node in parse tree that is a pragma annotation
     */
    public Traversable getPreviousPragma(Traversable current) {
    	Traversable pragmaPrev = getPrevious(current);
    	while (!pragmaPrev.toString().startsWith("#pragma"))
    		pragmaPrev = getPrevious(pragmaPrev);
    	return pragmaPrev;
    }
    
    /*
     * Returns the previous node in parse tree.
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
}

class StandardErrorReporter implements CritterCheck.ErrorReporter {
	public void reportError(String message, Object... args) {
    	System.err.printf(message, args);
    }
}