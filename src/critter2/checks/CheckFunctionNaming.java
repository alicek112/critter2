package critter2.checks;

import critter2.CritterCheck;

import cetus.hir.DepthFirstIterator;
import cetus.hir.IDExpression;
import cetus.hir.Procedure;
import cetus.hir.Program;
import cetus.hir.Traversable;

/**
 * Warns if not all functions in non-main modules have the same prefix.
 * Princeton University's Introduction to Programming Systems
 * requires all functions to have the same prefix, separated by
 * an underscore (ie. prefix_Func1, prefix_Func2).
 * 
 * @author Alice Kroutikova '15
 *
 */
public class CheckFunctionNaming extends CritterCheck {

	/**
     * Constructor used for testing.
     * 
     * @param program the root node of the parse tree
     * @param errorReporter testing class
     */
	public CheckFunctionNaming(Program program, CritterCheck.ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/**
	 * Main constructor used in Critter.java
	 * 
	 * @param program the root node of the parse tree
	 */
	public CheckFunctionNaming(Program program) {
		super(program);
	}

	@Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
		
		// check if test needs to be done (check if there is a main 
		// function definition)
		while (dfs.hasNext()) {
			Traversable t = dfs.next();
			
			if (t instanceof Procedure) {
				IDExpression n = ((Procedure) t).getName();
    			String name = n.getName();
    			if (name.compareTo("main") == 0)
    				return;
			}
		}
		
		dfs = new DepthFirstIterator<Traversable>(program);
		
		while (dfs.hasNext()) {
    		Traversable t = nextNoStdInclude(dfs);
    		
    		// if node is a function (Procedure), test if its name has correct prefix
    		if (t instanceof Procedure) {
    			IDExpression n = ((Procedure) t).getName();
                String fnName = n.getName();
                String fileName = getFilename(t);

                String fnNamePrefix = fnName.split("_")[0];
                String fileNamePrefix = fileName.split("\\.")[0];

                String lowerCaseFileNamePrefix = fileNamePrefix.toLowerCase();
                String lowerCaseFnNamePrefix = fnNamePrefix.toLowerCase();

                if ((lowerCaseFnNamePrefix.indexOf(lowerCaseFileNamePrefix) == -1) &&
                    (lowerCaseFileNamePrefix.indexOf(lowerCaseFnNamePrefix) == -1))

                	reportErrorPos(t, "medium priority:" +
                		    "\n   Function names should be prefixed with module names;" +
                		    "\n   function name %s does not match module name %s" +
                		    "\n", fnName, fileName);
    		}
		}
	}
}