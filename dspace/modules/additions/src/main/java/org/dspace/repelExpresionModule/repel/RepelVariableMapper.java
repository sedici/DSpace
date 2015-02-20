package org.dspace.repelExpresionModule.repel;

import java.util.HashMap;

import javax.el.ValueExpression;
import javax.el.VariableMapper;


public class RepelVariableMapper extends VariableMapper {

	private final HashMap<String, ValueExpression> variables =  new HashMap<String, ValueExpression>();
	/* 
	 * @see javax.el.VariableMapper#resolveVariable(java.lang.String)
	 */
	@Override
	public ValueExpression resolveVariable(String s) {
		return variables.get(s);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.el.VariableMapper#setVariable(java.lang.String, javax.el.ValueExpression)
	 */
	@Override
	public ValueExpression setVariable(String s,ValueExpression valueExpression) {
		return (variables.put(s, valueExpression));
	}
}
