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
    	      "pv",
    	      "o", "po", "ao",
    	      "j", "k", "n", "m", "", 
    	   }; 
    private final static List<String> ACCEPTABLE_VAR_NAMES = Arrays.asList(VAR_NAMES);
    
    // COS217 minimum variable name length
    private final static int MIN_VAR_NAME_LENGTH = 3;

	public CheckVariableName(Program program, ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	public CheckVariableName(Program program) {
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
    		
    		else if (t instanceof VariableDeclaration 
    				&& !t.getParent().toString().startsWith("__")) {

    			
    			List<IDExpression> vars = ((VariableDeclaration) t).getDeclaredIDs();
    			for (IDExpression x : vars) {
    				String xName = x.toString();
    				if (!ACCEPTABLE_VAR_NAMES.contains(xName)) {
    					if (xName.length() < MIN_VAR_NAME_LENGTH) {
    						reportErrorPos(t, "medium priority:" +
    								" %nVariable/function name '%s' " +
    								"is too short%n", xName);
    					}
    				}
    			}
    		}
    		
    		else if (t instanceof Enumeration) {
    			List<IDExpression> vars = ((Enumeration) t).getDeclaredIDs();
    			for (IDExpression x : vars) {
    				String xName = x.toString();
    				if (!ACCEPTABLE_VAR_NAMES.contains(xName)) {
    					if (xName.length() < MIN_VAR_NAME_LENGTH) {
    						reportErrorPos(t, "medium priority:" +
    								" %nVariable/function name '%s' " +
    								"is too short%n", xName);
    					}
    				}
    			}
    		}
    	}
	}
}
