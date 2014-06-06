package cetus.exec;

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Test;

import cetus.hir.Program;

public class CritterDriverTest {

	public static String res(String name) {
		URL url = Thread.currentThread().getContextClassLoader().getResource("");
		
		return url.getPath() + "../src/test/resources/" + name;
	}
	
	@Test
	public void testSmoke() {
		CritterDriver cd = new CritterDriver();
		
		Program program = cd.parseProgram(res("pragma_hello.c"));
		
		assertNotNull(program);
		
	}
}
