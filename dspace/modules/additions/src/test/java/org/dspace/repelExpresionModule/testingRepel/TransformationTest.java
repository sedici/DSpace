package org.dspace.repelExpresionModule.testingRepel;

import static org.junit.Assert.assertEquals;

import org.dspace.repelExpresionModule.repel.RepelExpressionModule;
import org.junit.Test;

public class TransformationTest {
	@Test
	public void testCase1() {
		String elText = "item.handle";
		RepelExpressionModule expModule = new RepelExpressionModule(null);
		Object convertedText = expModule.eval(elText);
		System.out.print(convertedText);
		assertEquals( "123456", convertedText );
		//TODO realizar los testing de los casos de uso
		//TODO definir la clase ELResolver para Dspace
	}
}
