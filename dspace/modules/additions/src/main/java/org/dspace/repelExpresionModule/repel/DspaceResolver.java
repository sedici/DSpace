/**
 * 
 */
package org.dspace.repelExpresionModule.repel;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELResolver;

/**
 * @author terru
 *
 */
public class DspaceResolver extends ELResolver {

	/* (non-Javadoc)
	 * @see javax.el.ELResolver#getValue(javax.el.ELContext, java.lang.Object, java.lang.Object)
	 */
	@Override
	public Object getValue(ELContext context, Object base, Object property) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.el.ELResolver#getType(javax.el.ELContext, java.lang.Object, java.lang.Object)
	 */
	@Override
	public Class<?> getType(ELContext context, Object base, Object property) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.el.ELResolver#setValue(javax.el.ELContext, java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setValue(ELContext context, Object base, Object property,
			Object value) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.el.ELResolver#isReadOnly(javax.el.ELContext, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean isReadOnly(ELContext context, Object base, Object property) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.el.ELResolver#getFeatureDescriptors(javax.el.ELContext, java.lang.Object)
	 */
	@Override
	public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context,
			Object base) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.el.ELResolver#getCommonPropertyType(javax.el.ELContext, java.lang.Object)
	 */
	@Override
	public Class<?> getCommonPropertyType(ELContext context, Object base) {
		// TODO Auto-generated method stub
		return null;
	}

}
