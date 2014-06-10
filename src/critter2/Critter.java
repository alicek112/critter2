/*
 * Critter.java
 * 
 * Created by Alice Kroutikova '15, based on the Driver.java code of 
 * CETUS.
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
import cetus.hir.Default;
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
import cetus.hir.Statement;
import cetus.hir.SwitchStatement;
import cetus.hir.Traversable;
import cetus.hir.VariableDeclaration;
import critter2.checks.CheckAsserts;
import critter2.checks.CheckBeginningComment;
import critter2.checks.CheckFunctionCommentValid;
import critter2.checks.CheckFunctionLengthByLines;
import critter2.checks.CheckFunctionNaming;
import critter2.checks.CheckFunctionNumber;
import critter2.checks.CheckGoTos;
import critter2.checks.CheckLoop;

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
	
    // COS217 maximum loop length
    private int MAX_LOOP_LENGTH = 35;
    // COS217 maximum function length
    private int MAX_FUNCTION_LENGTH = 140;
    // COS217 maximum function length by statements
    private int MAX_FUNCTION_STATEMENT_LENGTH = 50;
    // COS217 maximum nesting level
    private int MAX_NESTING = 3;
    // COS217 maximum function number per file
    private int MAX_FUNCTION_NUMBER = 15;
    // COS217 maximum number of parameters per function
    private int MAX_PARAMETER_NUMBER = 7;
    // COS217 maximum discrepancy between number of local comments
    // and the number of elements that should have comments
    private int MAX_LOCAL_COMMENT_DISCREPANCY = 5;
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
     * Check if loop length exceeds a maximum length (max_loop_length).
     */
    public void checkLoop() {
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
    				
    				if (st instanceof AnnotationStatement) {
    					looplinecount++;
    				}
    			}
    			
    			if (looplinecount > MAX_LOOP_LENGTH) {
    				System.err.printf("%n%s: line %d: low priority: " +
    						"%nA loop should consist of fewer than %d " +
    						"lines;%n " +
    						"this loop consists of %d lines; consider " +
    						"refactoring%n", 
    						getFilename(t), getLineNumber(t),
    						MAX_LOOP_LENGTH, looplinecount);
    			}
    		}
    	}
    	
    }
    
    /*
     * Check if all functions in non-main modules have the same prefix.
     */
    public void checkFunctionNaming() {
    	DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);

		String commonPrefix = null;
		
		// check if test needs to be done (check if there is a main 
		// function definition)
		boolean hasMain = false;
		while (dfs.hasNext()) {
			Traversable t = dfs.next();
			
			if (t instanceof Procedure) {
				IDExpression n = ((Procedure) t).getName();
    			String name = n.getName();
    			if (name.compareTo("main") == 0)
    				hasMain = true;
			}
		}
		
		if (!hasMain) {
			dfs = new DepthFirstIterator<Traversable>(program);
		
			while (dfs.hasNext()) {
	    		Traversable t = dfs.next();
	    		
	    		// skips all the standard included files
	    		if (t.toString().startsWith("#pragma critTer:startStdInclude:")) {
	    			while (!(t.toString().startsWith("#pragma critTer:endStdInclude:"))) {
	    				t = dfs.next();
	    			}
	    		}
	    		
	    		
	    		if (t instanceof Procedure) {
	    			
	    			IDExpression n = ((Procedure) t).getName();
	    			String name = n.getName();
	    			String prefix = name.split("_")[0];
	    			if (commonPrefix == null && 
	    					prefix.compareTo("main") != 0)
	    				commonPrefix = prefix;
	    			else if (prefix.compareTo("main") != 0) {
	    				if (commonPrefix.compareTo(prefix) != 0) {
	    					System.err.printf("%n%s: line %d: medium priority: " +
	    							"%nA function's prefix should match the " +
	    							"module name; %s and %s do not match%n", 
	    							getFilename(t), getLineNumber(t),
	        						commonPrefix, prefix);
	    					
	    				}
	    			}
	    		}
			}
    	}
    }
    
    /*
     * Checks if a function length exceeds a maximum length 
     * (MAX_FUNCTION_LENGTH)
     */
    void checkFunctionLengthByLines() {
    	DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	
		while (dfs.hasNext()) {
    		Traversable t = dfs.next();
    		
    		// skips all standard included files
    		if (t.toString().startsWith("#pragma critTer:startStdInclude:")) {
    			while (!(t.toString().startsWith("#pragma critTer:endStdInclude:"))) {
    				t = dfs.next();
    			}
    		}
    		
    		else if (t instanceof Procedure) {
    			Statement s = ((Procedure) t).getBody();
    			DepthFirstIterator<Traversable> ldfs = 
    					new DepthFirstIterator<Traversable>(s);
    			
    			int looplinecount = 0;
    			while (ldfs.hasNext()) {
    				Traversable st = ldfs.next();
    				
    				if (st.toString().startsWith("#pragma critTer")) {
    					looplinecount++;
    				}
    			}
    			
    			if (looplinecount > MAX_FUNCTION_LENGTH) {
    				System.err.printf("%n%s: line %d: low priority: " +
    						"%nA function should consist of fewer than " +
    						"%d lines;%n " +
    						"this function consists of %d lines; " +
    						"consider refactoring%n", 
    						getFilename(t), getLineNumber(t),
    						MAX_FUNCTION_LENGTH, looplinecount);
    			}
    			
    		}
    		
    	}
    }
    
    /*
     * Check if there are too many functions in a file 
     * (MAX_FUNCTION_NUMBER).
     */
    public void checkFunctionNumber() {
    	DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	
		int functioncount = 0;
		String currentFilename = null;
    	while (dfs.hasNext()) {
    		Traversable t = dfs.next();
    		
    		if (currentFilename == null)
    			currentFilename = getFilename(t);
    		
    		// skips all the included files
    		if (t.toString().startsWith("#pragma critTer:startStdInclude:")) {
    			while (!(t.toString().startsWith("#pragma critTer:endStdInclude:"))) {
    				t = dfs.next();
    			}
    		}
    		
    		if (t instanceof Procedure) {
    			functioncount++;
    		}
    		
    	}
    	
    	if (functioncount > MAX_FUNCTION_NUMBER) {
			System.err.printf("%n%s: low priority: %nA file should " +
					"contain no more than %d functions;%n " +
					"this file contains %d functions%n", 
					currentFilename, MAX_FUNCTION_NUMBER, functioncount);
		}
    }
    
    /*
     * Check if there are too many parameters in a function 
     * (max_parameter_number).
     */
    public void checkFunctionParams() {
    	DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	
		while (dfs.hasNext()) {
    		Traversable t = dfs.next();
    		
    		// skips all included files
    		if (t.toString().startsWith("#pragma critTer:startStdInclude:")) {
    			while (!(t.toString().startsWith("#pragma critTer:endStdInclude:"))) {
    				t = dfs.next();
    			}
    		}
    		
    		else if (t instanceof Procedure) {
    			int paramNum = ((Procedure) t).getNumParameters();
    			
    			if (paramNum > MAX_PARAMETER_NUMBER) {
    				System.err.printf("%n%s: line %d: medium priority: " +
    						"%nA function should have no more than %d " +
    						"parameters; this function has %d%n", 
    						getFilename(t), getLineNumber(t),
    						MAX_PARAMETER_NUMBER, paramNum);
    			}	
    		}	
    	}
    }
    
    /* Check if all functions have comments, and if the comment mentions
     * each parameter by name and what the function returns.
     */
    public void checkFunctionCommentValid() {
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
    			Procedure function = (Procedure) t;
    			
	    		List<?> list = function.getReturnType();
	    		List<String> stringList = new ArrayList<String>();
	    			
	   			for (Object x : list) {
	   				stringList.add(x.toString());
	   			}
	   			
	   			Traversable p = getPreviousNonPragma(function);
	   			// main function doesn't need to be checked for parameter
	   			// or return mentions in comment
	   			if (function.getName().toString().compareTo("main") != 0) {
		    			
		    		if (p instanceof AnnotationDeclaration) {
		    			Traversable comment = (AnnotationDeclaration) p;
		    				
		    			// Checks if the function's comment refers to 
		    			// parameters.
		   				for (int i = 0; 
		   						i < function.getNumParameters(); 
		   						i++) {
		    					
		   					String paramName = 
		   						function.getParameter(i).getDeclaredIDs().get(0).toString();
		   					if (!comment.toString().contains(paramName)) {
		   						System.err.printf("%n%s: line %d: high priority: " +
		   								"%nA function's comment should refer to " +
		   								"each parameter by name;%nyour comment " +
		   								"does not refer to '%s'%n",
	    								getFilename(comment), getLineNumber(comment), paramName);
		   					}
		    			}
		    				
		    			// Checks for explicitly stated return, only 
		   				// for non-void function.
		    			if (!stringList.contains("void")) {
			    			if (!comment.toString().contains("return") && 
			    					!comment.toString().contains("Return")) {
			   					System.err.printf("%n%s: line %d: high priority: " +
			   							"%nA function's comment should state " +
			   	                        "explicitly what the function returns%n",
										getFilename(comment), getLineNumber(comment));
		    				}
	    				}
	    			}
	   			}
	   			
	   			if (!(p instanceof AnnotationDeclaration)) {
		    		System.err.printf("%n%s: line %d: high priority: " +
		    				"%nA function definition should have a comment%n",
		    					getFilename(function), getLineNumber(function));		
	    		}
    		}
    	}
    		
    }
    
    
    /* Assumes that if, while, for, do while and switch elements should 
     * all have comments.
     * Checks the number of local comments with the number of those 
     * elements, and throws a warning if the discrepancy between the two 
     * is greater than maxLocalCommentDiscrepancy.
     */
    public void checkFunctionHasEnoughComments() {
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
    			DepthFirstIterator<Traversable> functiondfs = 
    					new DepthFirstIterator<Traversable>(t);
    			int countElements = 0;
    			int countComments = 0;
    			
    			while (functiondfs.hasNext()) {
    				Traversable functiont = functiondfs.next();
    				if (functiont instanceof Loop)
    					countElements++;
    				else if (functiont instanceof IfStatement)
    					countElements++;
    				else if (functiont instanceof SwitchStatement)
    					countElements++;
    				
    				else if (functiont instanceof AnnotationStatement) {
    					if (!functiont.toString().startsWith("#pragma"))
    						countComments++;	
    				}
    			}
    			
    			if ((countElements - countComments) 
    					> MAX_LOCAL_COMMENT_DISCREPANCY) {
    				System.err.printf("%n%s: line %d: low priority: " +
    						"%nThis function definition probably needs" +
    						" more local comments%n",
    						getFilename(t), getLineNumber(t));
    			}
    			
    		}
    	}
    }
    
    /* Checks if all global variables have comments. Comments must be 
     * either on the line immediately previous the global variable, or 
     * with at most one blank line between the comment and the global 
     * variable.
     */
    public void checkGlobalHasComment() {
    	DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	dfs.pruneOn(Procedure.class); // skips all the functions
    	dfs.pruneOn(ClassDeclaration.class); // skips all the structs

    	while (dfs.hasNext()) {
    		Traversable t = dfs.next();
    		
    		// skips all the standard included files
    		if (t.toString().startsWith("#pragma critTer:startStdInclude:")) {
    			while (!(t.toString().startsWith("#pragma critTer:endStdInclude:"))) {
    				t = dfs.next();
    			}
    		}
    		
    		else if (t instanceof VariableDeclaration 
    				|| t instanceof Enumeration 
    				|| t instanceof ClassDeclaration) {
    			if (!t.toString().startsWith("typedef int * __")) {
	    			Traversable p = getPreviousNonPragma(t);
	    			if (!(p instanceof AnnotationStatement) 
	    					&& !(p instanceof AnnotationDeclaration)) {
	    				if (t.getParent().getParent() != null) {
	    					if (!(t.getParent().getParent() instanceof VariableDeclaration)) {
			    				System.err.printf("%n%s: line %d: high priority: " +
			    						"%nA comment should appear above each " +
			    						"global variable.%n",
			    						getFilename(t), getLineNumber(t));
	    					}
	    				}
	    			}
    			}
    		}
    	}
    }
    
    /*
     * Checks if the file begins with a comment.
     */
    public void checkBeginningComment() {
    	DepthFirstIterator<Traversable> dfs = 
    			new DepthFirstIterator<Traversable>(program);
    	
    	Traversable t = dfs.next();
    	
    	while (!(t.toString().startsWith("#pragma critTer"))) {
    		t = dfs.next();
    	}
    	Traversable first = dfs.next();
    	
    	if (first.toString().startsWith("#pragma")) {
    		System.err.printf("%n%s: line %d: high priority: " +
    				"%nA file should begin with a comment.%n",
					getFilename(first), getLineNumber(first));
    	}
    	
    	if (!(first instanceof AnnotationDeclaration)) {
    		System.err.printf("%n%s: line %d: high priority: " +
    				"%nA file should begin with a comment.%n",
					getFilename(first), getLineNumber(first));
    	}
    	
    	dfs = new DepthFirstIterator<Traversable>(program);
    	
    	// check all student's .h files
    	while (dfs.hasNext()) {
    		t = dfs.next();
    		
    		if (t.toString().startsWith("#pragma critTer:startStudentInclude")) {
    			while (!(t.toString().startsWith("#pragma critTer:1:")))
    	    		t = dfs.next();
    			Traversable n = dfs.next();
    			if (n.toString().startsWith("#pragma critTer")) {
    				System.err.printf("%n%s: line %d: high priority: " +
    						"%nA file should begin with a comment.%n",
    						getFilename(n), getLineNumber(n));
    			}
    			if (!(n instanceof AnnotationDeclaration)) {
    				System.err.printf("%n%s: line %d: high priority: " +
    						"%nA file should begin with a comment.%n",
    						getFilename(n), getLineNumber(n));
    			}
    		}
    		
    	}
    }
    
    /*
     * Checks that all switch statements have default cases.
     */
    public void checkSwitchHasDefaultCase() {
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
    			DepthFirstIterator<Traversable> sdfs = 
    					new DepthFirstIterator<Traversable>(t);
    			boolean hasDefault = false;
    			
    			while (sdfs.hasNext()) {
    				
    				Traversable s = sdfs.next();
    				if (s instanceof Default)
    					hasDefault = true;
    			}
    			
    			if (!hasDefault) {
    				System.err.printf("%n%s: line %d: low priority: " +
    						"%nA switch statement should have a default " +
    						"case%n",
    						getFilename(t), getLineNumber(t));
    			}
    		}
    		
    	}
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
     * Returns previous node in parse tree that is a pragma annotation
     */
    private Traversable getPreviousPragma(Traversable current) {
    	Traversable pragmaPrev = getPrevious(current);
    	while (!pragmaPrev.toString().startsWith("#pragma"))
    		pragmaPrev = getPrevious(pragmaPrev);
    	return pragmaPrev;
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
    	
    	Critter dt = new Critter(program);
        
        System.err.println("critTer2 warnings start here");
        System.err.println("----------------------------");
        System.err.println();
        
        // Checks begin here.
        dt.checkFunctionHasEnoughComments();
        dt.checkGlobalHasComment();
        dt.checkFunctionParams();
        dt.checkFileLength();
        dt.checkSwitchHasDefaultCase();
        
        dt.checkSwitchCases();
        dt.checkStructHasComment();
        dt.checkMagicNumbers();
        dt.checkVariableName();
        //dt.checkFunctionLengthByStatement();
        dt.checkNesting();
        dt.checkEmptyCompound();
        
        CritterCheck[] checks = {
        		new CheckLoop(program),
        		new CheckBeginningComment(program),
        		new CheckFunctionNaming(program),
        		new CheckFunctionLengthByLines(program),
        		new CheckFunctionNumber(program), 
        		new CheckFunctionCommentValid(program),
        		new CheckAsserts(program),
        		new CheckGoTos(program)
        };
        
        for (CritterCheck check : checks)
        	check.check();

        System.err.println();
        System.err.println("----------------------------");
        System.err.println("critTer2 warnings end here");
        
    }
}