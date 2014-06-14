package critter2;

import java.io.File;

import cetus.base.grammars.CetusCParser;
import cetus.exec.CetusParser;
import cetus.exec.CommandLineOptionSet;
import cetus.exec.Driver;
import cetus.hir.Program;
import cetus.hir.TranslationUnit;

/**
 * Encapsulates important parts of CETUS code that build parse tree.
 * 
 * @author Alice Kroutikova '15
 *
 */
public class CritterDriver {
	static {
		Driver.registerOptions();
	}
	
	/**
	 * Parses a c file and returns the root node of the parse tree.
	 * 
	 * All header files are included by CETUS in the parse tree.
	 * 
	 * @param filename name of the .c file
	 * @return root node of the parse tree
	 */
	protected Program parseProgram(String filename) {
        Program program = new Program();
        CommandLineOptionSet options = new CommandLineOptionSet();
        options.add(options.UTILITY,
                "preprocessor",
                "gcc -E -C -dD",
                "command",
                "Set the preprocessor command to use");
        
        String dir = (new File(filename)).getParent();
        CetusParser cparser = new CetusCParser(dir);
        TranslationUnit tu = cparser.parseFile(filename, options);
        program.addTranslationUnit(tu);
        
        return program;
        
    }
}
