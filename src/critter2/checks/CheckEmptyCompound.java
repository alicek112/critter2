package critter2.checks;

import cetus.hir.DepthFirstIterator;
import cetus.hir.Program;
import cetus.hir.Traversable;
import critter2.CritterCheck;

public class CheckEmptyCompound extends CritterCheck {

	public CheckEmptyCompound(Program program, ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	public CheckEmptyCompound(Program program) {
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
    		
    		else if (t.toString().compareTo("{\n\n}") == 0) {
    			reportErrorPos(t, "medium priority: " +
    					"%nDo not use empty compound statements.%n");
    		}
    	}
	}
}
