/*
 * Warns if a switch case is missing a break (or return) statement.
 * 
 * Created by Alice Kroutikova '15.
 */

package critter2.checks;

import java.util.List;

import cetus.hir.DepthFirstIterator;
import cetus.hir.Program;
import cetus.hir.SwitchStatement;
import cetus.hir.Traversable;
import critter2.CritterCheck;

public class CheckSwitchCases extends CritterCheck {

	/*
	 * Constructor used in testing.
	 */
	public CheckSwitchCases(Program program, ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/*
	 * General constructor used in Critter.java.
	 */
	public CheckSwitchCases(Program program) {
		super(program);
	}

	@Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	
		// Traverse parse tree looking for Switch Statement nodes.
    	while (dfs.hasNext()) {
    		Traversable t = dfs.next();
    		
    		// skips all the standard included files
    		if (t.toString().startsWith("#pragma critTer:startStdInclude:")) {
    			while (!(t.toString().startsWith("#pragma critTer:endStdInclude:"))) {
    				t = dfs.next();
    			}
    		}
    		
    		else if (t instanceof SwitchStatement) {
    			
    			Traversable body = ((SwitchStatement) t).getBody();
    			List<Traversable> list = body.getChildren();
    			
    			boolean caseHasBreak = true;
    			Traversable currentCase = null;
    			
    			for (Traversable i : list) {
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
    				
    				if (i.toString().startsWith("break") 
    						|| i.toString().startsWith("return"))
    					caseHasBreak = true;
    			}
    		}
    	}
	}
}
