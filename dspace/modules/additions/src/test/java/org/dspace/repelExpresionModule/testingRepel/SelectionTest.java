package org.dspace.repelExpresionModule.testingRepel;


import static org.junit.Assert.*;


import org.junit.Test;
import org.dspace.repelExpresionModule.repel.*;

/**	
* Testing Todo list
* Los casos de uso que debería sastisfacer esta solución serían 
* los correspondientes a la validación  
* Selección:  mostrar los items de una coleccion que cumplan con una validación
* 			  listar metadatos sin authority
* 			  proyectar todas las obras de un autor determinado
* 			  proyectar todos libros que no posean ISBN
**/

public class SelectionTest {

	@Test
	public void testCase1() {
		String elText = "item.handle";
		RepelExpressionModule expModule = new RepelExpressionModule();
		Object convertedText = expModule.eval(elText);
		System.out.print(convertedText);
		assertEquals( "123456", convertedText );
		//TODO realizar los testing de los casos de uso
		//TODO definir la clase ELResolver para Dspace
	}
}
