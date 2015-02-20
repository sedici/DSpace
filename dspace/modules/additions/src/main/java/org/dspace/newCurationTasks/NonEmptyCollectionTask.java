/**
 * 
 */
package org.dspace.newCurationTasks;

import org.dspace.content.DSpaceObject;
import org.dspace.core.Constants;

/**
 * Clase que extiende la tarea que representa el caso de uso de validación 
 * Chequea que una colección determinada no esté vacía
 * @usa REPEL EXPRESION Module
 * @see  org.dspace.repelExpresionModule
 * @author terru
 * 
 */
public class NonEmptyCollectionTask extends RepelValidationTask {
	private String expression = "!empty(dso.getItems())";
	
	@Override
	protected String getExppresion(){
		return this.expression;
	}
	
	@Override
	protected Boolean accept(DSpaceObject dso){
		return (dso.getType() != Constants.COLLECTION);
	}
	

}
