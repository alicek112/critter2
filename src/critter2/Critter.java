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

import cetus.hir.DepthFirstIterator;
import cetus.hir.Program;
import cetus.hir.Traversable;
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

    public Critter(Program program) {
    	
        this.program = program;
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