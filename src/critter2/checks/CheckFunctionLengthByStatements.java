package critter2.checks;

import cetus.hir.AnnotationDeclaration;
import cetus.hir.AnnotationStatement;
import cetus.hir.CompoundStatement;
import cetus.hir.DepthFirstIterator;
import cetus.hir.FlatIterator;
import cetus.hir.IfStatement;
import cetus.hir.Loop;
import cetus.hir.Procedure;
import cetus.hir.Program;
import cetus.hir.SwitchStatement;
import cetus.hir.Traversable;
import critter2.CritterCheck;

public class CheckFunctionLengthByStatements extends CritterCheck {
	
	// COS217 maximum function length by statements
    private final static int MAX_FUNCTION_STATEMENT_LENGTH = 50;
    
	public CheckFunctionLengthByStatements(Program program,
			ErrorReporter errorReporter) {
		super(program, errorReporter);
	}
	
	public CheckFunctionLengthByStatements(Program program) {
		super(program);
	}
	
	private int countStatements(Traversable body) {
    	FlatIterator<Traversable> flat = new FlatIterator<Traversable>(body);
		
		int statementcount = 0;
		
		while (flat.hasNext()) {
			Traversable s = flat.next();
			statementcount++;
			
			if (s instanceof Loop) {
				statementcount += countStatements(((Loop) s).getBody());
			}
			
			else if (s instanceof IfStatement) {
				if (((IfStatement) s).getElseStatement() != null)
					statementcount += countStatements(((IfStatement) s).getElseStatement());
				if (((IfStatement) s).getThenStatement() != null)
					statementcount += countStatements(((IfStatement) s).getThenStatement());
			}
			
			else if (s instanceof CompoundStatement) {
				statementcount += countStatements(s);
				statementcount--;
			}
			
			else if (s instanceof SwitchStatement) {
				statementcount += countStatements(((SwitchStatement) s).getBody());
			}
			
			else if (s instanceof AnnotationStatement || s instanceof AnnotationDeclaration)
				statementcount--;
		}
		return statementcount;
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
    		
    		else if (t instanceof Procedure) {
    			CompoundStatement body = ((Procedure) t).getBody();
    			
    			int statementcount = countStatements(body);
    			
    			if (statementcount > MAX_FUNCTION_STATEMENT_LENGTH) {
    				System.err.printf("%n%s: line %d: low priority: " +
    						"%nA function definition should consist of " +
    						"fewer than %d statements;%nthis function " +
    						"definition consists of %d statements%n",
    						getFilename(t), getLineNumber(t), 
    						MAX_FUNCTION_STATEMENT_LENGTH, statementcount);
    			}
    		}
    	}
	}
}
