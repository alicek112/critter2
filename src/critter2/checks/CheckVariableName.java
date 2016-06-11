package critter2.checks;

import java.util.Arrays;
import java.util.List;

import cetus.hir.DepthFirstIterator;
import cetus.hir.Enumeration;
import cetus.hir.IDExpression;
import cetus.hir.Program;
import cetus.hir.Traversable;
import cetus.hir.VariableDeclaration;
import critter2.CritterCheck;

/**
 * Checks that variable names are longer than the MIN_VAR_NAME_LENGTH, 
 * with the exception of ACCEPTABLE_VAR_NAMES.
 * 
 * @author Alice Kroutikova '15
 *
 */
public class CheckVariableName extends CritterCheck {
	
	// COS217 acceptable variable names that are shorter than 
    // MIN_VAR_NAME_LENGTH
	// the empty "" variable name is there to account for void parameters 
    // in functions that technically have empty variable names.
    private final static String[] VAR_NAMES = {
    	      "c", "pc", "c1", "c2", "uc", "ac",
    	      "s", "ps", "s1", "s2", "us", "as",
    	      "i", "pi", "i1", "i2", "ui", "ai",
    	      "l", "pl", "l1", "l2", "ul", "al",
    	      "f", "pf", "f1", "f2", "af",
    	      "d", "pd", "d1", "d2", "ad",
    	      "pv", "u", "u1", "u2",
    	      "o", "po", "ao",
    	      "j", "k", "n", "m", "", 
    	   }; 
    private final static List<String> ACCEPTABLE_VAR_NAMES = Arrays.asList(VAR_NAMES);
    
    // COS217 minimum variable name length
    private final static int MIN_VAR_NAME_LENGTH = 3;

    /**
     * Constructor used for testing.
     * 
     * @param program the root node of the parse tree
     * @param errorReporter testing class
     */
	public CheckVariableName(Program program, CritterCheck.ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/**
	 * Main constructor used in Critter.java
	 * 
	 * @param program the root node of the parse tree
	 */
	public CheckVariableName(Program program) {
		super(program);
	}

	@Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    
    	while (dfs.hasNext()) {
    		Traversable t = nextNoStdInclude(dfs);
    		
    		// Examines variable declarations (that are not inserted into 
    		// code during preprocessing)
    		if (t instanceof VariableDeclaration 
    				&& !t.getParent().toString().startsWith("__")) {

    			
    			List<IDExpression> vars = ((VariableDeclaration) t).getDeclaredIDs();
    			for (IDExpression x : vars) {
    				String xName = x.toString();
    				if (!ACCEPTABLE_VAR_NAMES.contains(xName)) {
    					if (xName.length() < MIN_VAR_NAME_LENGTH) {
    						reportErrorPos(t, "medium priority:" +
    								"%n   Variable/function name '%s' " +
    								"is too short%n", xName);
    					}
    				}
    			}
    		}
    		
    		// Examines enums for variable name lengths.
    		else if (t instanceof Enumeration) {
    			List<IDExpression> vars = ((Enumeration) t).getDeclaredIDs();
    			for (IDExpression x : vars) {
    				String xName = x.toString();
    				if (!ACCEPTABLE_VAR_NAMES.contains(xName)) {
    					if (xName.length() < MIN_VAR_NAME_LENGTH) {
    						reportErrorPos(t, "medium priority:" +
    								"%n   Variable/function name '%s' " +
    								"is too short%n", xName);
    					}
    				}
    			}
    		}
    	}
	}
}
