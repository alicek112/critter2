package org.critter2.checks;

import org.critter2.CritterCheck;

import cetus.hir.AnnotationStatement;
import cetus.hir.DepthFirstIterator;
import cetus.hir.Loop;
import cetus.hir.PreAnnotation;
import cetus.hir.Program;
import cetus.hir.Statement;
import cetus.hir.Traversable;

public class CheckLoop extends CritterCheck {
	
	public CheckLoop(Program program, CritterCheck.ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	public CheckLoop(Program program) {
		super(program);
	}

	// COS217 maximum loop length
    private int MAX_LOOP_LENGTH = 35;

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
    		
    		else if (t instanceof Loop) {
    			Statement s = ((Loop) t).getBody();
    			DepthFirstIterator<Traversable> ldfs = 
    					new DepthFirstIterator<Traversable>(s);
    			
    			int looplinecount = 0;
    			while (ldfs.hasNext()) {
    				Traversable st = ldfs.next();
    				
    				if (st instanceof AnnotationStatement || st instanceof PreAnnotation) {
    					looplinecount++;
    				}
    			}
    			
    			if (looplinecount > MAX_LOOP_LENGTH) {
    				reportErrorPos(t, "low priority: " +
    						"\nA loop should consist of fewer than %d " +
    						"lines;\n " +
    						"this loop consists of %d lines; consider " +
    						"refactoring\n", 
    						MAX_LOOP_LENGTH, looplinecount);
    			}
    		}
    	}
	}
}
