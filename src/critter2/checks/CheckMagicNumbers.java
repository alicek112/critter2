package critter2.checks;

import cetus.hir.Case;
import cetus.hir.DepthFirstIterator;
import cetus.hir.Enumeration;
import cetus.hir.FloatLiteral;
import cetus.hir.IntegerLiteral;
import cetus.hir.Program;
import cetus.hir.Traversable;
import cetus.hir.VariableDeclaration;
import critter2.CritterCheck;

/**
 * Warns against using magic numbers outside of 
 * declarations (except 0, 1, and 2 in case statements.)
 * 
 * Additionally warns against using #define because
 * all #defines are replaced during the preprocessing done by
 * CritterDriver. If #defines were used in standard header files,
 * this causes unnecessary warnings thrown when Critter
 * mistakes those for magic numbers.
 * 
 * @author Alice Kroutikova '15
 *
 */
public class CheckMagicNumbers extends CritterCheck {

	/**
     * Constructor used for testing.
     * 
     * @param program the root node of the parse tree
     * @param errorReporter testing class
     */
	public CheckMagicNumbers(Program program, CritterCheck.ErrorReporter errorReporter, String path) {
		super(program, errorReporter, path);
	}
	
	/**
	 * Main constructor used in Critter.java
	 * 
	 * @param program the root node of the parse tree
	 */
	public CheckMagicNumbers(Program program, String path) {
		super(program, path);
	}

	/*
     * Determines if a string input is numeric.
     */
    private boolean isNumeric(String input) {
        try {
            Double.parseDouble(input);
            return true;
        }
        catch( Exception e ) {
            return false;
        }
    }
    
    /*
     * Determines if a node corresponds to a magic number in the original student file.
     */
    private boolean isRealMagicNumber(Traversable t, String num) {
    	int firstLineNum = getBeginningLineNumber(t);
    	int lastLineNum = getLineNumber(t);
    	int numLines = lastLineNum - firstLineNum + 1;
    	
    	boolean containsNum = false;
    	
    	for (int i = 0; i < numLines; i++) {
    		if(getLine(getFilename(t), firstLineNum+i).contains(num))
    			containsNum = true;
    	}
    	
    	return containsNum;
    }
	
    @Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	dfs.pruneOn(VariableDeclaration.class);    // don't check declarations
    	dfs.pruneOn(Enumeration.class);            // don't check magic numbers in enums
    	
    	while (dfs.hasNext()) {
    		Traversable t = nextNoStdInclude(dfs);
    		
    		// handle cases here (no magic numbers whatsoever 
			// inside case)
    		if (t instanceof Case) {
    			String c = ((Case) t).getExpression().toString();
    			
    			if (isNumeric(c)) {
    				if (isRealMagicNumber(t, c)) {
	    				reportErrorPos(t, "high priority: " +
	    						"%n   Use of magic number (%s), which should be given a meaningful name%n", c);
    				}
    			}	
    		}
    		
    		if (t instanceof FloatLiteral) {
    			FloatLiteral number = (FloatLiteral) t;
    			if (!t.getParent().toString().startsWith("__")) {
    				if (number.getValue() != 0 && number.getValue() != 1 
    						&& number.getValue() != 2) {
    					if (isRealMagicNumber(t, t.toString())) {
	    					reportErrorPos(t, "high priority: " +
	        						"%n   Use of magic number (%s), which should be given a meaningful name%n", 
	        						t.toString());
    					}
    				}	
    			}
    		}
    		
    		if (t instanceof IntegerLiteral) {
    			IntegerLiteral number = (IntegerLiteral) t;
    			if (!t.getParent().toString().startsWith("__")) {
    				if (number.getValue() != 0 && number.getValue() != 1 
    						&& number.getValue() != 2) {
    					if (isRealMagicNumber(t, t.toString())) {
	    					reportErrorPos(t, "high priority: " +
	        						"%n   Use of magic number (%s), which should be given a meaningful name%n", 
	        						t.toString());
    					}
    				}	
    			}
    		}
    	}
	}
}
