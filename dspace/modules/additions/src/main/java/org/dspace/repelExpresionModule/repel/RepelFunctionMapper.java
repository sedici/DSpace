/**
 * 
 */
package org.dspace.repelExpresionModule.repel;

import java.lang.reflect.Method;

import javax.el.FunctionMapper;

/**
 * @author terru
 *
 */
public class RepelFunctionMapper extends FunctionMapper {

	/* Retorna un mapeo de funciones si se quiere dar soporte para funciones en el EL
	 * @see javax.el.FunctionMapper#resolveFunction(java.lang.String, java.lang.String)
	 * en esta implementación el soporte para funciones se escapa del alcance
	 */
	@Override
	public Method resolveFunction(String prefix, String localName) {
		return null;
	}

}
