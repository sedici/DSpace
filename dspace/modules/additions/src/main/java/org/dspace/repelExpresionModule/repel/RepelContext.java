/**
 * 
 */
package org.dspace.repelExpresionModule.repel;

import java.lang.reflect.Method;
import java.util.HashMap;

import javax.el.BeanELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

import com.sun.el.ValueExpressionLiteral;

//import org.apache.el.ValueExpressionLiteral;

/**
 * @author terru
 *
 */
public class RepelContext extends ELContext {
	
	private final BeanELResolver resolver = new BeanELResolver();
	private final FunctionMapper functionMapper = new NoopFunctionMapper();
	private final VariableMapper variableMapper = new VariableMapperImpl();
	private final HashMap<String, ValueExpression> variables =  new HashMap<String, ValueExpression>();



	/* Devuelve un resolver para el lenguaje de expresion en este contexto
	 * @see javax.el.ELContext#getELResolver()
	 */
	@Override
	public ELResolver getELResolver() {
		return resolver;
	}

	/* Devuelve el mapeador de funciones para el EL implementado
	 * @see javax.el.ELContext#getFunctionMapper()
	 */
	@Override
	public FunctionMapper getFunctionMapper() {
		return functionMapper;
	}
	
	@Override
	public VariableMapper getVariableMapper() {
		return variableMapper;
	}

	
	/* Realiza el binding entre un nombre de variable y el objeto que representa en el contexto
	 * EJ: bind("car",Object Car);
	 */
	public void bind( String variable, Object obj ) {
		variables.put( variable, new ValueExpressionLiteral(obj,Object.class) );
	}
	    

	//TODO preguntar a ARI si las clases de variable Mapper irían acá y si se puede adaptar de otro modo
	
	private class VariableMapperImpl extends VariableMapper {
	      public ValueExpression resolveVariable(String s) {
	        return variables.get(s);
	      }
	      public ValueExpression setVariable(String s, ValueExpression valueExpression) {
	        return (variables.put(s, valueExpression));
	      }
	    }

	 private class NoopFunctionMapper extends FunctionMapper {
	      public Method resolveFunction(String s, String s1) {
	        return null;
	      }
	 }

}
