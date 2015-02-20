/**
 * 
 */
package org.dspace.repelExpresionModule.repel;

/**
 * @author terru
 *
 */
public class RepelExpresionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public RepelExpresionException(String errorMsg, Throwable cause){
		super(errorMsg,cause);
	}
	
	public RepelExpresionException(String errorMsg){
		super(errorMsg);
	}

}
