package cetus.exec;

import java.io.File;

import cetus.base.grammars.CetusCParser;
import cetus.hir.Program;
import cetus.hir.TranslationUnit;

public class CritterDriver {
	static {
		Driver.registerOptions();
	}
	
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
