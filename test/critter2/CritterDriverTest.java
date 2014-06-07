package critter2;

import static org.junit.Assert.*;

import critter2.CritterDriver;
import org.junit.Test;

import cetus.hir.Program;

public class CritterDriverTest {

	@Test
	public void testSmoke() {
		CritterDriver cd = new CritterDriver();
		
		Program program = cd.parseProgram(Utils.res("pragma_hello.c"));
		
		assertNotNull(program);
		
	}
}
