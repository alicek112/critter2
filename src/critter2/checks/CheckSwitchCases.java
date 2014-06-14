package critter2.checks;

import java.util.List;

import cetus.hir.DepthFirstIterator;
import cetus.hir.Program;
import cetus.hir.SwitchStatement;
import cetus.hir.Traversable;
import critter2.CritterCheck;

/**
 * Warns if a switch case is missing a break (or return) statement.
 * 
 * @author Alice Kroutikova '15.
 *
 */
public class CheckSwitchCases extends CritterCheck {

	/**
     * Constructor used for testing.
     * 
     * @param program the root node of the parse tree
     * @param errorReporter testing class
     */
	public CheckSwitchCases(Program program, CritterCheck.ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/**
	 * Main constructor used in Critter.java
	 * 
	 * @param program the root node of the parse tree
	 */
	public CheckSwitchCases(Program program) {
		super(program);
	}

	/**
	 * Implements check and reports warnings.
	 */
	@Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	
		// Traverse parse tree looking for Switch Statement nodes.
    	while (dfs.hasNext()) {
    		Traversable t = nextNoInclude(dfs);
    		
    		if (t instanceof SwitchStatement) {
    			
    			Traversable body = ((SwitchStatement) t).getBody();
    			List<Traversable> list = body.getChildren();
    			
    			boolean caseHasBreak = true;
    			Traversable currentCase = null;
    			
    			// Searches the children of the switch statement
    			for (Traversable i : list) {
    				
    				// if child begins with "case" or "default", previous case
    				// has ended, so can evaluate if it had a break statement or not.
    				if (i.toString().startsWith("case") 
    						|| i.toString().startsWith("default")) {
    					if (caseHasBreak) {
    						caseHasBreak = false;
    						currentCase = i;
    					}
    					else {
    						reportErrorPos(currentCase, "medium priority:" +
    								" %nEach case/default in a switch statement " +
    								"should have a break or return statement, " +
    								"you're missing one here.%n");
    					}
    				}
    				
    				// current case has a break or return statement
    				if (i.toString().startsWith("break") 
    						|| i.toString().startsWith("return"))
    					caseHasBreak = true;
    			}
    		}
    	}
	}
}
