package critter2.checks;

import cetus.hir.DepthFirstIterator;
import cetus.hir.IfStatement;
import cetus.hir.Loop;
import cetus.hir.Program;
import cetus.hir.SwitchStatement;
import cetus.hir.Traversable;
import critter2.CritterCheck;

/**
 * Warn if area of code is too deeply nested.
 * 
 * @author Alice Kroutikova '15.
 *
 */
public class CheckNesting extends CritterCheck {
	
	// COS217 maximum nesting level
    private final static int MAX_NESTING = 3;

    /**
     * Constructor used for testing.
     * 
     * @param program the root node of the parse tree
     * @param errorReporter testing class
     */
	public CheckNesting(Program program, CritterCheck.ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	/**
	 * Main constructor used in Critter.java
	 * 
	 * @param program the root node of the parse tree
	 */
	public CheckNesting(Program program) {
		super(program);
	}

	@Override
	public void check() {
		DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	
    	while (dfs.hasNext()) {
    		Traversable t = nextNoInclude(dfs);
    		
    		if (t instanceof Loop) {
    			Traversable body = ((Loop) t).getBody();
    			int nesting = 0;
    			
    			Traversable parent = body.getParent();
    			while (parent != null) {
    				if (parent instanceof Loop 
    						|| parent instanceof IfStatement 
    						|| parent instanceof SwitchStatement)
    					nesting++;
    				parent = parent.getParent();
    			}
    			
    			if (nesting > MAX_NESTING) {
    				reportErrorPos(body, "low priority: " +
    						"%n   This area is deeply nested at level %d," +
    						" consider refactoring%n", nesting);
    			}
    		}
    		else if (t instanceof IfStatement) {
    			int nesting = 1;
    			
    			Traversable parent = t.getParent();
    			Traversable parentsGrandChild = t.getChildren().get(0);
    			Traversable current = t;
    			while (parent != null) {
    				if (parent instanceof Loop 
    						|| parent instanceof SwitchStatement)
    					nesting++;
    				if (parent instanceof IfStatement) {
    					if (parentsGrandChild instanceof IfStatement) {
    						Traversable thenStatement = 
    								((IfStatement) parent).getThenStatement();
    						
        					if (thenStatement.toString().compareTo(current.toString()) == 0) {
        						nesting++;
        					}
    					}
    					else
    						nesting++;
    					
    				}
    				
    				parentsGrandChild = parentsGrandChild.getParent();
    				current = parent;
    				parent = parent.getParent();
    			}
    			if (nesting > MAX_NESTING) {
    				reportErrorPos(t, "low priority: " +
    						"%n   This area is deeply nested at level %d," +
    						" consider refactoring%n", nesting);
    			}
    		}
    		else if (t instanceof SwitchStatement) {
    			Traversable body = ((SwitchStatement) t).getBody();
    			int nesting = 0;
    			
    			Traversable parent = body.getParent();
    			while (parent != null) {
    				if (parent instanceof Loop 
    						|| parent instanceof IfStatement 
    						|| parent instanceof SwitchStatement)
    					nesting++;
    				parent = parent.getParent();
    			}
    			
    			if (nesting > MAX_NESTING) {
    				reportErrorPos(body, "low priority: " +
    						"%n   This area is deeply nested at level %d," +
    						" consider refactoring%n", nesting);
    			}
    		}	
    	}
	}
}
