/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.workflow;

import org.junit.*;

import static org.junit.Assert.*;

public class InChIEqualsConditionTest {

	@Test
	public void doubleBondLayer(){
		assertTrue(equals(BUTENE, BUTENE));
		assertTrue(equals(BUTENE, SUU_BUTENE));

		assertTrue(equals(CIS_BUTENE, SUU_BUTENE));
		assertTrue(equals(TRANS_BUTENE, SUU_BUTENE));
		assertTrue(equals(SUU_BUTENE, SUU_BUTENE));

		assertFalse(equals(CIS_BUTENE, TRANS_BUTENE));
	}

	@Test
	public void tetrahedralLayer(){
		assertTrue(equals(ALANINE, ALANINE));
		assertTrue(equals(ALANINE, SUU_ALANINE));

		assertTrue(equals(D_ALANINE, SUU_ALANINE));
		assertTrue(equals(L_ALANINE, SUU_ALANINE));
		assertTrue(equals(SUU_ALANINE, SUU_ALANINE));

		assertFalse(equals(D_ALANINE, L_ALANINE));
	}

	@Test
	public void firstFragment(){
		assertTrue(equals(PHLOROGLUCINOL, PHLOROGLUCINOL_DIHYDRATE));
	}

	static
	private boolean equals(String left, String right){
		InChIEqualsCondition condition = new InChIEqualsCondition();
		condition.setLeft(left);
		condition.setRight(right);

		return condition.eval();
	}

	private static final String BUTENE = "InChI=1S/C4H8/c1-3-4-2/h3-4H,1-2H3";

	private static final String CIS_BUTENE = BUTENE + "/b4-3-";
	private static final String TRANS_BUTENE = BUTENE + "/b4-3+";
	private static final String SUU_BUTENE = BUTENE + "/b4-3?";

	private static final String ALANINE = "InChI=1S/C3H7NO2/c1-2(4)3(5)6/h2H,4H2,1H3,(H,5,6)";

	private static final String D_ALANINE = ALANINE + "/t2-/m1/s1";
	private static final String L_ALANINE = ALANINE + "/t2-/m0/s1";
	private static final String SUU_ALANINE = ALANINE + "/t2?";

	private static final String PHLOROGLUCINOL = "InChI=1S/C6H6O3/c7-4-1-5(8)3-6(9)2-4/h1-3,7-9H";
	private static final String PHLOROGLUCINOL_DIHYDRATE = "InChI=1S/C6H6O3.2H2O/c7-4-1-5(8)3-6(9)2-4;/h1-3,7-9H;2*1H2";
}