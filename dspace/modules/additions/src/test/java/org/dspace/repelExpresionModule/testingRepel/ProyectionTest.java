package org.dspace.repelExpresionModule.testingRepel;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.dspace.repelExpresionModule.repel.RepelExpressionModule;

public class ProyectionTest {
	@Test
	public void testCase1() {
		String elText = "item.handle";
		RepelExpressionModule expModule = new RepelExpressionModule(null);
		Object convertedText = expModule.eval(elText);
		System.out.print(convertedText);
		assertEquals( "123456", convertedText );
	}
}
