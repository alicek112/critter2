/*
 * Critter.java
 * 
 * Created by Alice Kroutikova '15, based on the Driver.java code of 
 * CETUS.
 * 
 * May 6, 2014
 * 
 */


package cetus.exec;

import cetus.analysis.*;
import cetus.base.grammars.CetusCParser;
import cetus.codegen.CodeGenPass;
import cetus.codegen.ompGen;
import cetus.hir.Annotatable;
import cetus.hir.Annotation;
import cetus.hir.AnnotationDeclaration;
import cetus.hir.AnnotationStatement;
import cetus.hir.BreadthFirstIterator;
import cetus.hir.Case;
import cetus.hir.ClassDeclaration;
import cetus.hir.CodeAnnotation;
import cetus.hir.CommentAnnotation;
import cetus.hir.CompoundStatement;
import cetus.hir.DFIterator;
import cetus.hir.Declaration;
import cetus.hir.Declarator;
import cetus.hir.Default;
import cetus.hir.DepthFirstIterator;
import cetus.hir.Enumeration;
import cetus.hir.Expression;
import cetus.hir.FlatIterator;
import cetus.hir.FloatLiteral;
import cetus.hir.ForLoop;
import cetus.hir.GotoStatement;
import cetus.hir.IDExpression;
import cetus.hir.IfStatement;
import cetus.hir.InlineAnnotation;
import cetus.hir.IntegerLiteral;
import cetus.hir.Literal;
import cetus.hir.Loop;
import cetus.hir.NestedDeclarator;
import cetus.hir.PragmaAnnotation;
import cetus.hir.PreAnnotation;
import cetus.hir.PrintTools;
import cetus.hir.Procedure;
import cetus.hir.ProcedureDeclarator;
import cetus.hir.Program;
import cetus.hir.SimpleExpression;
import cetus.hir.Statement;
import cetus.hir.StatementExpression;
import cetus.hir.SwitchStatement;
import cetus.hir.SymbolTools;
import cetus.hir.Tools;
import cetus.hir.TranslationUnit;
import cetus.hir.Traversable;
import cetus.hir.VariableDeclaration;
import cetus.hir.VariableDeclarator;
import cetus.hir.WhileLoop;
import cetus.transforms.*;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/**
* Implements the command line parser and controls pass ordering.
* Users may extend this class by overriding runPasses
* (which provides a default sequence of passes).  The derived
* class should pass an instance of itself to the run method.
* Derived classes have access to a protected {@link Program Program} 
* object.
*/
public class Critter {

    /**
    * A mapping from option names to option values.
    */
    protected static CommandLineOptionSet options = 
    		new CommandLineOptionSet();

    /**
    * Override runPasses to do something with this object.
    * It will contain a valid program when runPasses is called.
    */
    protected Program program;

    /** The filenames supplied on the command line. */
    protected List<String> filenames;
    
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
    
    private String currentFilename;

    /**
    * Constructor used by derived classes.
    */
    protected Critter() {
    	Driver.registerOptions();
        options.add(options.UTILITY, "parser",
                "cetus.base.grammars.CetusCParser", "parsername",
                "Name of parser to be used for parsing source file");
        options.add(options.UTILITY, "outdir", "cetus_output", "dirname",
                "Set the output directory name (default is cetus_output)");
        options.add(options.UTILITY, "preprocessor", "cpp -C -I.", 
        		"command",
                "Set the preprocessor command to use");
        options.add(options.UTILITY, "verbosity", "0", "N",
                "Degree of status messages (0-4) that you wish to see " +
                "(default is 0)");
        options.add(options.UTILITY, "expand-user-header",
                "Expand user (non-standard) header file #includes into " +
                "code");
        options.add(options.UTILITY, "expand-all-header", null,
                "Expand all header file #includes into code");
        Driver.setOptionValue("expand-all-header", null);
    }

    /**
    * Returns the value of the given key or null * if the value is not 
    * set.
    * Key values are set on the command line as <b>-option_name=value</b>.
    *
    * @param key The key to search
    * @return the value of the given key or null if the value is not set.
    */
    public static String getOptionValue(String key) {
        return options.getValue(key);
    }

    /**
    * Returns the set of  procedure names that should be excluded from
    * transformations. These procedure names are specified with the
    * skip-procedures command line option by providing a comma-separated list
    * of names.
    * @return set of procedure names that should be excluded from
    *         transformations
    */
    public static HashSet getSkipProcedureSet() {
        HashSet<String> proc_skip_set = new HashSet<String>();
        String s = getOptionValue("skip-procedures");
        if (s != null) {
            String[] proc_names = s.split(",");
            proc_skip_set.addAll(Arrays.asList(proc_names));
        }
        return proc_skip_set;
    }

    protected void parseOption(String opt) {
        opt = opt.trim();
        // empty line
        if (opt.length() < 2) {
            return;
        }
        int eq = opt.indexOf('=');
        if (eq == -1) { // if value is not set
            // registered option
            if (options.contains(opt)) {
                // no value on the option line, so set it to null
                setOptionValue(opt, null);
            } else {
                System.err.println("ignoring unrecognized option " + opt);
            }
        } else { // if value is set
            String option_name = opt.substring(0, eq);
            if (options.contains(option_name)) {
                if (option_name.equals("preprocessor")) {
                    setOptionValue(option_name,
                               opt.substring(eq + 1).replace("\"", ""));
                } else {
                    // use the value from the command line
                    setOptionValue(option_name, opt.substring(eq + 1));
                }
            } else {
                System.err.println("ignoring unrecognized option "
                                   + option_name);
            }
        }
    }

    /**
    * Parses command line options to Cetus.
    *
    * @param args The String array passed to main by the system.
    */
    protected void parseCommandLine(String[] args) {
        /* print a useful message if there are no arguments */
        if (args.length == 0) {
            printUsage();
            Tools.exit(1);
        }
        // keeps track of dangling preprocessor values
        // e.g., args[n] = -preprocessor="cpp
        //       args[n+1] = -EP"
        boolean preprocessor = false;
        int i; /* used after loop; don't put inside for loop */
        for (i = 0; i < args.length; ++i) {
            String opt = args[i];
            // options start with "-"
            if (opt.charAt(0) != '-') {
                /* not an option -- skip to handling options and 
                 * filenames */
                break;
            }
            int eq = opt.indexOf('=');
            if (eq == -1) { // if value is not set
                String option_name = opt.substring(1);
                if (options.contains(option_name)) { // registered option
                    preprocessor = false;
                    // no value on the command line, so just set it to "1"
                    // setValue(name) will search for predefined value
                    // --> see setValue(String) for more information.
                    options.setValue(option_name);
                } else if (preprocessor) {
                    // found dangling preprocessor option
                    setOptionValue("preprocessor",
                                   getOptionValue("preprocessor")
                                   + " " + opt.replace("\"",""));
                } else {
                    System.err.println("ignoring unrecognized option " +
                                       option_name);
                }
            } else { // if value is set
                String option_name = opt.substring(1, eq);
                if (options.contains(option_name)) {
                    if (option_name.equals("preprocessor")) {
                        preprocessor = true;
                        setOptionValue(option_name,
                                  opt.substring(eq + 1).replace("\"",""));
                    } else {
                        preprocessor = false;
                        // use the value from the command line
                        setOptionValue(option_name, opt.substring(eq + 1));
                    }
                } else if (preprocessor) {
                    setOptionValue("preprocessor",
                                   getOptionValue("preprocessor")
                                   + " " + opt.replace("\"",""));
                } else {
                    System.err.println("ignoring unrecognized option " +
                                       option_name);
                }
            }
            if (getOptionValue("help") != null ||
                getOptionValue("usage") != null) {
                printUsage();
                Tools.exit(0);
            }
            if (getOptionValue("dump-options") != null) {
                setOptionValue("dump-options", null);
                dumpOptionsFile();
                Tools.exit(0);
            }
            if (getOptionValue("dump-system-options") != null) {
                setOptionValue("dump-system-options", null);
                dumpSystemOptionsFile();
                Tools.exit(0);
            }
            // load options file and then proceed with rest of command line
            // options
            if (getOptionValue("load-options") != null) {
                // load options should not be set in options file
                setOptionValue("load-options", null);
                loadOptionsFile();
                // prevent reentering this handler
                setOptionValue("load-options", null);
            }
        }
        // end of arguments without a file name
        if (i >= args.length) {
            System.err.println("No input files!");
            Tools.exit(1);
        }
        // The purpose of this wildcard expansion is to ease the use of 
        // IDE environment which usually doesn't handle wildcards.
        int num_file_args = args.length-i;
        filenames = new ArrayList<String>(num_file_args);
        for (int j = 0; j < num_file_args; ++j, ++i) {
            if (args[i].contains("*") || args[i].contains("?")) {
                File parent =
                        (new File(args[i])).getAbsoluteFile().getParentFile();
                for (File file : parent.listFiles(new RegexFilter(args[i]))) {
                    filenames.add(file.getAbsolutePath());
                }
            } else {
                filenames.add(args[i]);
            }
        }
        if (filenames.isEmpty()) {
            System.err.println("No input files!");
            Tools.exit(1);
        }
    }

    /**
    * Parses all of the files listed in <var>filenames</var>
    * and creates a {@link Program Program} object.
    */
    @SuppressWarnings({"unchecked", "cast"})
    protected void parseFiles() {
        program = new Program();
        Class class_parser;
        try {
            class_parser = getClass().getClassLoader().loadClass(
                           getOptionValue("parser"));
            //CetusParser cparser =
             //       (CetusParser)class_parser.getConstructor().newInstance();
            String dir = (new File(filenames.get(0))).getParent();
            CetusParser cparser = new CetusCParser(dir);
            for (String file : filenames) {
            	TranslationUnit tu = cparser.parseFile(file, options);
                program.addTranslationUnit(tu);
                String[] f = file.split("/");
                currentFilename = f[f.length - 1];
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to load parser: " +
                               getOptionValue("parser"));
            Tools.exit(1);
        } catch(Exception e) {
            System.err.println("Failed to initialize parser");
            Tools.exit(1);
        }
        
        // It is more natural to include these two steps in this method.
        // Link IDExpression => Symbol object for faster future access.
        SymbolTools.linkSymbol(program);
        // Convert the IR to a new one with improved annotation support
        TransformPass.run(new AnnotationParser(program));
    }

    /**
    * Prints the list of options that Cetus accepts.
    */
    public void printUsage() {
        String usage = "\ncetus.exec.Critter [option]... [file]...\n";
        usage += options.getUsage();
        System.err.println(usage);
    }

    /**
    * dump default options to file options.cetus in working directory
    * do not overwrite if file already exists.
    */
    public void dumpOptionsFile()  {
        // check for options.cetus in working directory
        // registerOptions();
        File optionsFile = new File("options.cetus");
        // create file options.cetus
        try {
            if (optionsFile.createNewFile()) {
                // populate options.cetus
                FileOutputStream fo = new FileOutputStream(optionsFile);
                PrintStream ps = new PrintStream(fo);
                ps.println(options.dumpOptions().trim());
                ps.close();
                fo.close();
            }
        } catch (IOException e) {
            System.err.println("Error: Failed to dump options.cetus");
        }
    }

    public void dumpSystemOptionsFile()  {
        // check for options.cetus in working directory
        // registerOptions();
        String homePath = System.getProperty("user.home");
        File optionsFile = new File(homePath,"options.cetus");
        // create file options.cetus
        try {
            if (optionsFile.createNewFile()) {
                // populate options.cetus
                FileOutputStream fo = new FileOutputStream(optionsFile);
                PrintStream ps = new PrintStream(fo);
                ps.println(options.dumpOptions().trim());
                ps.close();
                fo.close();
            }
        } catch (IOException e) {
            System.err.println(
                    "Error: Failed to dump system wide options.cetus");
        }
    }

    /**
    * load options.cetus
    * search order is working directory and then home directory
    */
    public void loadOptionsFile() {
        // check working directory for options.cetus
        // check home directory for options.cetus
        File optionsFile = new File("options.cetus");
        //dumpOptionsFile();
        if (!optionsFile.exists()) {
            String homePath = System.getProperty("user.home");
            optionsFile = new File(homePath,"options.cetus");
        }
        if (!optionsFile.exists()) {
            System.err.println("Error: Failed to load options.cetus");
            System.err.println(
                "Use option -dump-options or -dump-system-options"
                + " to create options.cetus with default values");
            Tools.exit(1);
        }
        // load file contents
        try {
            FileReader fr = new FileReader(optionsFile);
            BufferedReader br = new BufferedReader(fr);
            String line;
            // Read lines
            while ((line=br.readLine()) != null) {
                // Remove comments
                if (line.startsWith("#"))
                    continue;
                // load option
                parseOption(line);
            }
        } catch (Exception e) {
            System.err.println("Error while loading options file");
            Tools.exit(1);
        }
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
    				System.err.printf("\n%s: line %d: low priority: " +
    						"\nA loop should consist of fewer than %d " +
    						"lines;\n " +
    						"this loop consists of %d lines; consider " +
    						"refactoring\n", 
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
	    					System.err.printf("\n%s: line %d: medium priority: " +
	    							"\nA function's prefix should match the " +
	    							"module name; %s and %s do not match\n", 
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
    				System.err.printf("\n%s: line %d: low priority: " +
    						"\nA function should consist of fewer than " +
    						"%d lines;\n " +
    						"this function consists of %d lines; " +
    						"consider refactoring\n", 
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
    	while (dfs.hasNext()) {
    		Traversable t = dfs.next();
    		
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
			System.err.printf("\n%s: low priority: \nA file should " +
					"contain no more than %d functions;\n " +
					"this file contains %d functions\n", 
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
    				System.err.printf("\n%s: line %d: medium priority: " +
    						"\nA function should have no more than %d " +
    						"parameters; this function has %d\n", 
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
    			
	    		List list = function.getReturnType();
	    		List stringList = new ArrayList();
	    			
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
		   						System.err.printf("\n%s: line %d: high priority: " +
		   								"\nA function's comment should refer to " +
		   								"each parameter by name;\nyour comment " +
		   								"does not refer to '%s'\n",
	    								getFilename(comment), getLineNumber(comment), paramName);
		   					}
		    			}
		    				
		    			// Checks for explicitly stated return, only 
		   				// for non-void function.
		    			if (!stringList.contains("void")) {
			    			if (!comment.toString().contains("return") && 
			    					!comment.toString().contains("Return")) {
			   					System.err.printf("\n%s: line %d: high priority: " +
			   							"\nA function's comment should state " +
			   	                        "explicitly what the function returns\n",
										getFilename(comment), getLineNumber(comment));
		    				}
	    				}
	    			}
	   			}
	   			
	   			if (!(p instanceof AnnotationDeclaration)) {
		    		System.err.printf("\n%s: line %d: high priority: " +
		    				"\nA function definition should have a comment\n",
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
    				System.err.printf("\n%s: line %d: low priority: " +
    						"\nThis function definition probably needs" +
    						" more local comments\n",
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
			    				System.err.printf("\n%s: line %d: high priority: " +
			    						"\nA comment should appear above each " +
			    						"global variable.\n",
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
    		System.err.printf("\n%s: line %d: high priority: " +
    				"\nA file should begin with a comment.\n",
					currentFilename, getLineNumber(first));
    	}
    	
    	if (!(first instanceof AnnotationDeclaration)) {
    		System.err.printf("\n%s: line %d: high priority: " +
    				"\nA file should begin with a comment.\n",
					currentFilename, getLineNumber(first));
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
    				System.err.printf("\n%s: line %d: high priority: " +
    						"\nA file should begin with a comment.\n",
    						getFilename(n), getLineNumber(n));
    			}
    			if (!(n instanceof AnnotationDeclaration)) {
    				System.err.printf("\n%s: line %d: high priority: " +
    						"\nA file should begin with a comment.\n",
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
    				System.err.printf("\n%s: line %d: low priority: " +
    						"\nA switch statement should have a default " +
    						"case\n",
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
    						System.err.printf("\n%s: line %d: medium priority:" +
    								" \nEach case/default in a switch statement " +
    								"should have a break or return statement, " +
    								"you're missing one here.\n",
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
    	    		System.err.printf("\n%s: low priority: \nA source " +
    	    				"code file should contain fewer than %d " +
    	    				"lines;\nthis file contains %d lines\n",
    						getFilename(t), MAX_FILE_LENGTH, getLineNumber(t));
    	    	}
    			
    		}
    		
    		else if (t.toString().startsWith("#pragma critTer") 
    				&& !t.toString().contains("Include")) {
    			String[] parts = t.toString().split(":");
    		    long currentline = Long.parseLong(parts[1]);
    		    if (currentline > linecount)
    		       	linecount = currentline;
    		}
    	}
    	
    	if (linecount > MAX_FILE_LENGTH) {
    		System.err.printf("\n%s: low priority: \nA source code " +
    				"file should contain fewer than %d " +
    				"lines;\nthis file contains %d lines\n",
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
	        				
	        				System.err.printf("\n%s: line %d: medium priority:" +
	        						" \nA comment should appear above each " +
	        						"field in a struct.\n",
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
    			System.err.printf("\n%s: line %d: high priority: " +
    					"\nNever use GOTO statements\n",
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
	    				System.err.printf("\n%s: line %d: high priority: " +
	    						"\nUse of magic number (%s), which should " +
	    						"be given a meaningful name, " +
	    						"or a #define, which should be replaced " +
	    						"with an enum (unless it's the result of " +
	    						"a #define in a standard C header file)\n",
	    						getFilename(t), getLineNumber(t), c);
	    			}
	    			
	    		}
	    		
	    		if (t instanceof FloatLiteral) {
	    			FloatLiteral number = (FloatLiteral) t;
	    			if (!t.getParent().toString().startsWith("__")) {
	    				if (number.getValue() != 0 && number.getValue() != 1 
	    						&& number.getValue() != 2) {
	    					System.err.printf("\n%s: line %d: high priority:" +
	    							" \nUse of magic number (%s), which should" +
	    							" be given a meaningful name, " +
	    						    "or a #define, which should be replaced " +
	    						    "with an enum (unless it's the result of " +
	    						    "a #define in a standard C header file)\n",
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
	    					System.err.printf("\n%s: line %d: high priority: " +
	    							"\nUse of magic number (%s), which should " +
	    							"be given a meaningful name, " +
	    						    "or a #define, which should be replaced with " +
	    						    "an enum (unless it's the result of a #define " +
	    						    "in a standard C header file)\n",
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
    						System.err.printf("\n%s: line %d: medium priority:" +
    								" \nVariable/function name '%s' " +
    								"is too short\n",
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
    						System.err.printf("\n%s: line %d: medium priority:" +
    								" \nVariable/function name '%s' " +
    								"is too short\n",
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
    				System.err.printf("\n%s: line %d: low priority: " +
    						"\nA function definition should consist of " +
    						"fewer than %d statements;\nthis function " +
    						"definition consists of %d statements\n",
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
	    				System.err.printf("\n%s: line %d: low priority: " +
	    						"\nThis area is deeply nested at level %d," +
	    						" consider refactoring\n",
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
	    				System.err.printf("\n%s: line %d: low priority: " +
	    						"\nThis area is deeply nested at level %d," +
	    						" consider refactoring\n",
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
	    				System.err.printf("\n%s: line %d: low priority: " +
	    						"\nThis area is deeply nested at level %d," +
	    						" consider refactoring\n",
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
    		
    		else if (t.toString().compareTo("{\n\n}") == 0) {
    			System.err.printf("\n%s: line %d: medium priority: " +
    					"\nDo not use empty compound statements.\n",
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
			   				System.err.printf("\n%s: line %d: medium priority:" +
			   						" \nDo you want to validate '%s' " +
			   						"through an assert?\n",
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
   
    
    /**
    * Sets the value of the option represented by <i>key</i> to
    * <i>value</i>.
    *
    * @param key The option name.
    * @param value The option value.
    */
    public static void setOptionValue(String key, String value) {
        options.setValue(key, value);
    }

    public static boolean isIncluded(
            String name, String hir_type, String hir_name) {
        return options.isIncluded(name, hir_type, hir_name);
    }

    /**
    * Entry point for Cetus; creates a new Driver object,
    * and calls run on it with args.
    *
    * @param args Command line options.
    */
    public static void main(String[] args) {
    	Critter dt = new Critter();
        dt.parseCommandLine(args);
        dt.parseFiles();
        
        System.err.println("critTer2 warnings start here");
        System.err.println("----------------------------");
        System.err.println();
        
        // Checks begin here.
        dt.checkBeginningComment();
        dt.checkFunctionCommentValid();
        dt.checkFunctionHasEnoughComments();
        dt.checkGlobalHasComment();
        dt.checkLoop();
        dt.checkFunctionParams();
        dt.checkFunctionLengthByLines();
        dt.checkFunctionNumber();
        dt.checkFunctionNaming();
        dt.checkFileLength();
        dt.checkSwitchHasDefaultCase();
        dt.checkSwitchCases();
        dt.checkStructHasComment();
        dt.checkGoTos();
        dt.checkMagicNumbers();
        dt.checkVariableName();
        //dt.checkFunctionLengthByStatement();
        dt.checkNesting();
        dt.checkEmptyCompound();
        dt.checkAsserts();

        System.err.println();
        System.err.println("----------------------------");
        System.err.println("critTer2 warnings end here");
        
    }

    /**
    * Implementation of file filter for handling wild card character and other
    * special characters to generate regular expressions out of a string.
    */
    private static class RegexFilter implements FileFilter {
        /** Regular expression */
        private String regex;

        /**
        * Constructs a new filter with the given input string
        * @param str String to construct regular expression out of
        */
        public RegexFilter(String str) {
            regex = str.replaceAll("\\.", "\\\\.") // . => \.
            .replaceAll("\\?", ".")                // ? => .
            .replaceAll("\\*", ".*");              // * => .*
        }

        @Override
        public boolean accept(File f) {
            return f.getName().matches(regex);
        }
    }

}
