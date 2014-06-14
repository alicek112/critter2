/*
 * Critter.java
 * 
 * Created by Alice Kroutikova '15, based on the Driver.java code of 
 * CETUS, under the advising of Dr. Robert Dondero.
 * 
 * May 6, 2014
 * 
 */


package critter2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cetus.hir.AnnotationDeclaration;
import cetus.hir.AnnotationStatement;
import cetus.hir.Case;
import cetus.hir.ClassDeclaration;
import cetus.hir.CompoundStatement;
import cetus.hir.DFIterator;
import cetus.hir.Declaration;
import cetus.hir.DepthFirstIterator;
import cetus.hir.Enumeration;
import cetus.hir.FlatIterator;
import cetus.hir.FloatLiteral;
import cetus.hir.GotoStatement;
import cetus.hir.IDExpression;
import cetus.hir.IfStatement;
import cetus.hir.IntegerLiteral;
import cetus.hir.Loop;
import cetus.hir.PreAnnotation;
import cetus.hir.Procedure;
import cetus.hir.Program;
import cetus.hir.SwitchStatement;
import cetus.hir.Traversable;
import cetus.hir.VariableDeclaration;
import critter2.checks.CheckAsserts;
import critter2.checks.CheckBeginningComment;
import critter2.checks.CheckEmptyCompound;
import critter2.checks.CheckFileLength;
import critter2.checks.CheckFunctionCommentValid;
import critter2.checks.CheckFunctionHasEnoughComments;
import critter2.checks.CheckFunctionLengthByLines;
import critter2.checks.CheckFunctionNaming;
import critter2.checks.CheckFunctionNumber;
import critter2.checks.CheckFunctionParams;
import critter2.checks.CheckGlobalHasComment;
import critter2.checks.CheckGoTos;
import critter2.checks.CheckLoop;
import critter2.checks.CheckMagicNumbers;
import critter2.checks.CheckNesting;
import critter2.checks.CheckStructHasComment;
import critter2.checks.CheckSwitchCases;
import critter2.checks.CheckSwitchHasDefaultCase;
import critter2.checks.CheckVariableName;

/**
* Implements the command line parser and controls pass ordering.
* Users may extend this class by overriding runPasses
* (which provides a default sequence of passes).  The derived
* class should pass an instance of itself to the run method.
* Derived classes have access to a protected {@link Program Program} 
* object.
*/
public class Critter {
    
	private final Program program;
	
    // COS217 maximum function length by statements
    private int MAX_FUNCTION_STATEMENT_LENGTH = 50;
    // COS217 maximum nesting level
    private int MAX_NESTING = 3;
    // COS217 maximum file length in lines.
    private int MAX_FILE_LENGTH = 500;
    // COS217 acceptable variable names that are shorter than 
    // MIN_VAR_NAME_LENGTH
	// the empty "" variable name is there to account for void parameters 
    // in functions that technically have empty variable names.
    private String[] VAR_NAMES = {
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
    
    private List<String> ACCEPTABLE_VAR_NAMES = Arrays.asList(VAR_NAMES);
   
    // COS217 minimum variable name length
    private int MIN_VAR_NAME_LENGTH = 3;
    

    public Critter(Program program) {
    	
        this.program = program;
    }
    
    private long getLineNumber(Traversable element)
    {
        Traversable lastComment = getPrevious(element);
        
        while(!lastComment.toString().startsWith("#pragma critTer") 
        		|| lastComment.toString().contains("Include"))
        	lastComment = getPrevious(lastComment);

        String[] parts = lastComment.toString().split(":");
        return Long.parseLong(parts[1]);
    }
    
    private String getFilename(Traversable element) {
    	Traversable lastComment = getPrevious(element);
        
        while(!lastComment.toString().startsWith("#pragma critTer") 
        		|| lastComment.toString().contains("Include"))
        	lastComment = getPrevious(lastComment);

        String[] parts = lastComment.toString().split(":");
        return parts[2];
    }
    
    
    
    /*
     * Checks that all switch cases have a break or return statement.
     */
    public void checkSwitchCases() {
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
    						System.err.printf("%n%s: line %d: medium priority:" +
    								" %nEach case/default in a switch statement " +
    								"should have a break or return statement, " +
    								"you're missing one here.%n",
    								getFilename(currentCase), 
    								getLineNumber(currentCase));
    					}
    				}
    				
    				if (i.toString().startsWith("break") 
    						|| i.toString().startsWith("return"))
    					caseHasBreak = true;
    			}
    		}
    	}
    }
    
    
    /*
     * Checks if the file is longer than MAX_FILELENGTH.
     */
    public void checkFileLength() {
    	DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	
    	long linecount = 0;
    	String currentFilename = null;
    	
    	while (dfs.hasNext()) {
    		Traversable t = dfs.next();
    		
    		// skips all the standard included files
    		if (t.toString().startsWith("#pragma critTer:startStdInclude:")) {
    			while (!(t.toString().startsWith("#pragma critTer:endStdInclude:"))) {
    				t = dfs.next();
    			}
    		}
    		
    		// deals with student's included files
    		else if (t.toString().startsWith("#pragma critTer:endStudentInclude:")) {
    			if (getLineNumber(t) > MAX_FILE_LENGTH) {
    	    		System.err.printf("%n%s: low priority: %nA source " +
    	    				"code file should contain fewer than %d " +
    	    				"lines;%nthis file contains %d lines%n",
    						getFilename(t), MAX_FILE_LENGTH, getLineNumber(t));
    	    	}
    			
    		}
    		
    		else if (t.toString().startsWith("#pragma critTer") 
    				&& !t.toString().contains("Include")) {
    			String[] parts = t.toString().split(":");
    		    long currentline = Long.parseLong(parts[1]);
    		    currentFilename = parts[2];
    		    if (currentline > linecount)
    		       	linecount = currentline;
    		}
    	}
    	
    	if (linecount > MAX_FILE_LENGTH) {
    		System.err.printf("%n%s: low priority: %nA source code " +
    				"file should contain fewer than %d " +
    				"lines;%nthis file contains %d lines%n",
					currentFilename, MAX_FILE_LENGTH, linecount);
    	}
    }
    
    /*
     * Checks if all fields in a struct have comments.
     */
    public void checkStructHasComment() {
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
    		
    		else if (t instanceof ClassDeclaration) {
    			DepthFirstIterator<Traversable> cdfs = 
    					new DepthFirstIterator<Traversable>(t);
    			
    			while (cdfs.hasNext()) {
    				Traversable c = cdfs.next();
    			
	    			if (c instanceof VariableDeclaration 
	    					|| t instanceof Enumeration) {
	    				
	        			Traversable p = getPreviousNonPragma(c.getParent());
	        			if (!(p instanceof PreAnnotation)) {
	        				
	        				System.err.printf("%n%s: line %d: medium priority:" +
	        						" %nA comment should appear above each " +
	        						"field in a struct.%n",
	        						getFilename(c), getLineNumber(c));
	        			}
	        			
	    			}
    			}
        	}
    	}
    }
    
    /*
     * Warn against using GOTOs.
     */
    public void checkGoTos() {
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
    			System.err.printf("%n%s: line %d: high priority: " +
    					"%nNever use GOTO statements%n",
						getFilename(t), getLineNumber(t));
    		}
    	}
    }
    
    /*
     * Warn against using magic numbers outside of a declaration 
     * (except for 0, 1 and 2, except in case statements).
     */
    public void checkMagicNumbers() {
    	DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	dfs.pruneOn(VariableDeclaration.class); // don't check declarations
    	dfs.pruneOn(Enumeration.class); // don't check magic numbers in enums
    	
    	while (dfs.hasNext()) {
    		Traversable t = dfs.next();
    		
    		// skips all the standard included files
    		if (t.toString().startsWith("#pragma critTer:startStdInclude:")) {
    			while (!(t.toString().startsWith("#pragma critTer:endStdInclude:"))) {
    				t = dfs.next();
    			}
    		}
    		
    		else {
	    		// handle cases here (no magic numbers whatsoever 
    			// inside case)
	    		if (t instanceof Case) {
	    			String c = ((Case) t).getExpression().toString();
	    			
	    			if (isNumeric(c)) {
	    				System.err.printf("%n%s: line %d: high priority: " +
	    						"%nUse of magic number (%s), which should " +
	    						"be given a meaningful name, " +
	    						"or a #define, which should be replaced " +
	    						"with an enum (unless it's the result of " +
	    						"a #define in a standard C header file)%n",
	    						getFilename(t), getLineNumber(t), c);
	    			}
	    			
	    		}
	    		
	    		if (t instanceof FloatLiteral) {
	    			FloatLiteral number = (FloatLiteral) t;
	    			if (!t.getParent().toString().startsWith("__")) {
	    				if (number.getValue() != 0 && number.getValue() != 1 
	    						&& number.getValue() != 2) {
	    					System.err.printf("%n%s: line %d: high priority:" +
	    							" %nUse of magic number (%s), which should" +
	    							" be given a meaningful name, " +
	    						    "or a #define, which should be replaced " +
	    						    "with an enum (unless it's the result of " +
	    						    "a #define in a standard C header file)%n",
	        						getFilename(t), getLineNumber(t), 
	        						t.toString());
	    				}	
	    			}
	    			
	    		}
	    		
	    		if (t instanceof IntegerLiteral) {
	    			IntegerLiteral number = (IntegerLiteral) t;
	    			if (!t.getParent().toString().startsWith("__")) {
	    				if (number.getValue() != 0 && number.getValue() != 1 
	    						&& number.getValue() != 2) {
	    					System.err.printf("%n%s: line %d: high priority: " +
	    							"%nUse of magic number (%s), which should " +
	    							"be given a meaningful name, " +
	    						    "or a #define, which should be replaced with " +
	    						    "an enum (unless it's the result of a #define " +
	    						    "in a standard C header file)%n",
	        						getFilename(t), getLineNumber(t), 
	        						t.toString());
	    				}	
	    			}
	    		}
    		}
    	}
    }
    
    /*
     * Checks that variable names are longer than the MIN_VAR_NAME_LENGTH, 
     * with the exception of ACCEPTABLE_VAR_NAMES.
     */
    public void checkVariableName() {
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
    						System.err.printf("%n%s: line %d: medium priority:" +
    								" %nVariable/function name '%s' " +
    								"is too short%n",
            						getFilename(t), getLineNumber(t), xName);
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
    						System.err.printf("%n%s: line %d: medium priority:" +
    								" %nVariable/function name '%s' " +
    								"is too short%n",
            						getFilename(t), getLineNumber(t), xName);
    					}
    				}
    			}
    		}
    	}
    }
    
    /*
     * Check if a function exceeds a maximum statement count.
     */
    public void checkFunctionLengthByStatement() {
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
    
    /*
     * Check if nesting is too deep.
     */
    public void checkNesting() {
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
    		
    		else {
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
	    				System.err.printf("%n%s: line %d: low priority: " +
	    						"%nThis area is deeply nested at level %d," +
	    						" consider refactoring%n",
	    						getFilename(body), getLineNumber(body), 
	    						nesting);
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
	    				System.err.printf("%n%s: line %d: low priority: " +
	    						"%nThis area is deeply nested at level %d," +
	    						" consider refactoring%n",
	    						getFilename(t), getLineNumber(t), nesting);
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
	    				System.err.printf("%n%s: line %d: low priority: " +
	    						"%nThis area is deeply nested at level %d," +
	    						" consider refactoring%n",
	    						getFilename(body), getLineNumber(body), 
	    						nesting);
	    			}
	    		}
    		}
    		
    	}
    }
    
    /*
     * Checks if compound statement is empty.
     */
    public void checkEmptyCompound() {
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
    		
    		else if (t.toString().compareTo("\n\n}") == 0) {
    			System.err.printf("%n%s: line %d: medium priority: " +
    					"%nDo not use empty compound statements.%n",
						getFilename(t), getLineNumber(t));
    		}
    	}
    }
    
    /*
     * Check if pointer parameters are checked by asserts.
     */
    public void checkAsserts() {
    DFIterator<Traversable> dfs = new DFIterator<Traversable>(program);
    	
    	while (dfs.hasNext()) {
    		Traversable t = dfs.next();
    		
    		// skips all standard included files
    		if (t.toString().startsWith("#pragma critTer:startStdInclude:")) {
    			while (!(t.toString().startsWith("#pragma critTer:endStdInclude:"))) {
    				t = dfs.next();
    			}
    		}
    		
    		if (t instanceof Procedure) {
    			
	    		DepthFirstIterator<Traversable> functiondfs = 
	    				new DepthFirstIterator<Traversable>(t);
	    		
	    		@SuppressWarnings("unchecked")
				List<Declaration> params = ((Procedure) t).getParameters();
	    		List<String> paramNames = new ArrayList<String>();
	    		
	    		for (Declaration p : params) {
		    		List<IDExpression> declaredIDs = p.getDeclaredIDs();
		    		for (IDExpression parameter : declaredIDs) {
		    			if (parameter.getParent().toString().contains("[]")) {
		    				paramNames.add(parameter.toString());
		    			}
		    			// parameters formatted as pointers
		    			else if (parameter.getParent().toString().contains("*")) {
		    				paramNames.add(parameter.toString());
		    			}
		    		}
		    	
	    		}	
		    	boolean[] hasAssert = new boolean[paramNames.size()];
		    		
		   		
		    	while (functiondfs.hasNext()) {
		    		Traversable t2 = functiondfs.next();
		    		
		    		if (t2.toString().startsWith("__assert")) {
		   				for (int i = 0; i < paramNames.size(); i++) {
		   					if (t2.toString().contains(paramNames.get(i)))
		   						hasAssert[i] = true;
		   				}
	    				
	    			}	
		    	}
		    	
		    	// no need for asserts for argv
		    	for (int i = 0; i < hasAssert.length; i++) {
		    		if (paramNames.get(i).compareTo("argv") != 0) {
			    		if (!hasAssert[i]) {
			   				System.err.printf("%n%s: line %d: medium priority:" +
			   						" %nDo you want to validate '%s' " +
			   						"through an assert?%n",
			   						getFilename(t), getLineNumber(t), 
			   						paramNames.get(i));
			   			}
		    		}
	    		}
	    	
    		}
    	}
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
    
    /*
     * Determines if a string input is numeric.
     */
    private boolean isNumeric(String input) {
        try {
            Double.parseDouble(input);
            return true;
        }
        catch( Exception e ) {
            return false;
        }
    }
    
    /*
     * Returns the previous node in the parse tree that is not a pragma
     * annotation
     */
    private Traversable getPreviousNonPragma(Traversable current) {
    	Traversable nonPragmaPrev = getPrevious(current);
    	while (nonPragmaPrev.toString().startsWith("#pragma")) {
    		// skip over .h files
    		if (nonPragmaPrev.toString().startsWith("#pragma critTer:end")) {
    			while(!nonPragmaPrev.toString().startsWith("#pragma critTer:start"))
    				nonPragmaPrev = getPrevious(nonPragmaPrev);
    		}
    		nonPragmaPrev = getPrevious(nonPragmaPrev);
    	}
    	
    	return nonPragmaPrev;
    }
    
    
    /*
     * Returns the previous node in parse tree.
     */
    private Traversable getPrevious(Traversable current) {
    	DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	DepthFirstIterator<Traversable> dfs2 = 
    			new DepthFirstIterator<Traversable>(program);
    	
    	// dfs2 is always walking one ahead of dfs
    	dfs2.next();
    	Traversable t = dfs2.next(); 
    	
    	Traversable prev = dfs.next();
    	
    	while (dfs2.hasNext()) {
    		if (t == current)
    			break;
    		t = dfs2.next();
    		prev = dfs.next();
    	}
    	
    	return prev;
    }
   
    public static int usage() {
    	System.out.println("USAGE: Critter filename");
    	return -1;
    }
   
    /**
    * Entry point for Cetus; creates a new Driver object,
    * and calls run on it with args.
    *
    * @param args Command line options.
    */
    public static void main(String[] args) {
    	
    	if (args.length != 1) {
    		System.exit(usage());
    	}
    	
    	Program program = (new CritterDriver()).parseProgram(args[0]);
        
        System.err.println("critTer2 warnings start here");
        System.err.println("----------------------------");
        System.err.println();
        
        // Checks begin here.
        
        CritterCheck[] checks = {
        		new CheckLoop(program),
        		new CheckBeginningComment(program),
        		new CheckFunctionNaming(program),
        		new CheckFunctionLengthByLines(program),
        		new CheckFunctionNumber(program), 
        		new CheckFunctionCommentValid(program),
        		new CheckAsserts(program),
        		new CheckGoTos(program), 
        		new CheckFunctionHasEnoughComments(program), 
        		new CheckGlobalHasComment(program), 
        		new CheckFunctionParams(program),
        		new CheckFileLength(program),
        		new CheckSwitchHasDefaultCase(program),
        		new CheckSwitchCases(program),
        		new CheckStructHasComment(program),
        		new CheckMagicNumbers(program),
        		new CheckVariableName(program),
        		new CheckEmptyCompound(program),
        		new CheckNesting(program)
        };
        
        for (CritterCheck check : checks)
        	check.check();

        System.err.println();
        System.err.println("----------------------------");
        System.err.println("critTer2 warnings end here");
        
    }
}