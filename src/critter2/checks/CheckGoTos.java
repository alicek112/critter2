package critter2.checks;

import cetus.hir.DepthFirstIterator;
import cetus.hir.GotoStatement;
import cetus.hir.Program;
import cetus.hir.Traversable;
import critter2.CritterCheck;

public class CheckGoTos extends CritterCheck {

	public CheckGoTos(Program program, ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	public CheckGoTos(Program program) {
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
    		
    		else if (t instanceof GotoStatement) {
    			reportErrorPos(t, "high priority: " +
    					"%nNever use GOTO statements%n");
    		}
    	}
	}
}
