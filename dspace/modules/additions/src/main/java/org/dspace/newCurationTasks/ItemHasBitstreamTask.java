/**
 * 
 */
package org.dspace.newCurationTasks;

import org.dspace.content.DSpaceObject;
import org.dspace.core.Constants;

/**
 * Clase que extiende la tarea que representa el caso de uso de validación 
 * Chequea que un item tenga al menos un bitstream en alguno de sus bundles
 * @usa REPEL EXPRESION Module
 * @see  org.dspace.repelExpresionModule
 * @author terru
 *
 */
public class ItemHasBitstreamTask extends RepelValidationTask {
	private String expression = "dso.bundles.stream().anyMatch(b->!empty(b.bitstreams)))";
	
	
	@Override
	protected String getExppresion(){
		return this.expression;
	}
	
	@Override
	protected Boolean accept(DSpaceObject dso){
		return (dso.getType() == Constants.ITEM);
	}
	
}
