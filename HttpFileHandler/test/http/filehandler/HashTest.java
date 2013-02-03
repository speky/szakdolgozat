package http.filehandler;

import static org.junit.Assert.*;

import org.junit.Test;

public class HashTest {

	@Test
	public void testAStringHashCode() {
		String code = Utility.calcSHA1("alma");		
		assertTrue(code.equals("5f5ea3800d9a62bc5a008759dbbece9cad5db58f"));
	}

	@Test
	public void testCompareTwoStringsHashCode() {
		String codeAlma = Utility.calcSHA1("alma");
		String codeALMA = Utility.calcSHA1("ALMA");
		System.out.println(codeAlma);
		System.out.println(codeALMA);
		assertTrue(codeAlma.equals("5f5ea3800d9a62bc5a008759dbbece9cad5db58f"));
		assertTrue(codeALMA.equals("17a146f32cf25696f1d5d1a26567d816eff32c6a"));
		assertFalse(codeAlma.equals(codeALMA));
	}

}
