package http.filehandler;

import static org.junit.Assert.*;

import org.junit.Test;

public class HashTest {

	@Test
	public void testAStringHashCode() {
		String code = Utility.calcCheckSum("alma".getBytes());		
		assertTrue(code.equals("cf43e029efe6476e1f7f84691f89c876818610c2eaeaeb881103790a48745b82"));
	}

	@Test
	public void testCompareTwoStringsHashCode() {
		String codeAlma = Utility.calcCheckSum("alma".getBytes());
		String codeALMA = Utility.calcCheckSum("ALMA".getBytes());
		System.out.println(codeAlma);
		System.out.println(codeALMA);
		assertTrue(codeAlma.equals("cf43e029efe6476e1f7f84691f89c876818610c2eaeaeb881103790a48745b82"));
		assertTrue(codeALMA.equals("fd65f29e3b357a9df3131cf6ddeb6762517b07b466fae39089a2276a2e5ee8bf"));
		assertFalse(codeAlma.equals(codeALMA));
	}

}
