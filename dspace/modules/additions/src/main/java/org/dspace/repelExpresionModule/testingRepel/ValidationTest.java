package org.dspace.repelExpresionModule.testingRepel;
import static org.junit.Assert.*;
import org.junit.Test;


/**
 * Testing Todo list
 * Los casos de uso que debería sastisfacer esta solución serían 
 * los correspondientes a la validación 
 * Estos serían:
 * Validacion: verificar que los articulos posean un metadato determinado
 * 			   validar que un Bundle de un item posea  al menos un Bitstream
 * 			   validar la clase de un Dspaceobject
 * 			   validar la existencia de un metadato en un item
 * 
 * 
 *
 *TODO mostrarle a lira lo q paso con getIdentifiers
 *
 *@author terru
 *
 */

public class ValidationTest extends RepelTesT {
	
	/**
	 * @test test del caso de uso para validación número 1
	 * verificar que los objetos tengan un atributo determinado
	 * En este test se proceden a mostrar los atributos de un elemento, para que se pueda implementar
	 * la comparación con null que requiere saber si lo tienen o no
	 * @assert true si el item tiene handle
	 * @assert true si el item no tiene ID 
	 */
	
	@Override
	protected void setMetamodel() {
		//Item item = new Item();
		//item.setHandle("123456");
		//item.addMetadata(null, "dc", "type", "es", "article");
		//Bundle bundle = new Bundle();
		//bundle.setName("original");
		//this.getExpModule().defineObject("item", item);
		//this.getExpModule().defineObject("bundle",bundle);
	}

	@Test
	public void testCase1() {
		this.initConfig("Proyectar el Handle de un item");
		String elText = "item.handle";
		Object convertedText = this.getExpModule().eval(elText);
		System.out.print(convertedText);
		System.out.print("\n\n");
		assertEquals( "123456", convertedText );
	}
	
	@Test
	public void testCase2(){
		this.initConfig("Proyectar los metadatos que coinciden con un valor determinado");
		String elText = "item.getMetadata('algo').stream().filter(m->m.name == 'algo2')";
		Object convertedText = this.getExpModule().eval(elText);
		System.out.print(convertedText);
		System.out.print("\n\n");
		assertEquals( null, convertedText );
	}
	
	@Test
	public void testCase3(){
		this.initConfig("Recuperar si un item tiene archivos o no");
		String elText = "item.hasUploadedFiles()";
		Object convertedText = this.getExpModule().eval(elText);
		System.out.print(convertedText);
		System.out.print("\n\n");
		assertEquals( true, convertedText );
	}
	
	@Test
	public void testCase4(){
		this.initConfig("Verificar que un item sea un artículo \n\n");
		String elText = "item.getMetadata('dc.type').stream().anyMatch(s->s.getValue == 'article')";
		Object convertedText = this.getExpModule().eval(elText);
		System.out.print(convertedText);
		System.out.print("\n\n");
		assertEquals( false, convertedText );
	}
	
	@Test
	public void testCase5(){
		this.initConfig("Verificar que un bundle sea el original \n\n");
		String elText = "bundle.name == 'original'";
		Object convertedText = this.getExpModule().eval(elText);
		System.out.print(convertedText);
		System.out.print("\n\n");
		assertEquals( true, convertedText );
	}
	
	@Test
	public void testCase6(){
		this.initConfig("Verificar que un item tenga publication date \n\n");
		String elText = "!empty(item.getMetadata('date.publication'))";
		Object convertedText = this.getExpModule().eval(elText);
		System.out.print(convertedText);
		System.out.print("\n\n");
		assertEquals( false, convertedText );
	}


}
