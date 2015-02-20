package org.dspace.repelExpresionModule.repel;

import java.lang.reflect.Method;
import java.sql.SQLException;

import javax.el.ELManager;
import javax.el.ELProcessor;

import org.apache.log4j.Logger;
import org.dspace.content.*;
import org.dspace.core.Context;
import org.dspace.repelExpresionModule.dspaceProxy.*;
import org.dspace.repelExpresionModule.factoryModel.*;


public class RepelExpressionModule {
	/**
	 * Modulo procesador de expresiones en el lenguaje
	 * Esta clase encapsula el procesamiento de las expresiones 
	 */
	private ELProcessor procesor;
	private Context context;
	/**
	 * FIXME debería ser un singleton e inscanciar el ELCONTEXT que pueda utilizarse
	 * con seguridad en un entorno concurrente, por ejemplo haciendo contextos compuestos
	 */
	
	private static Logger log = Logger.getLogger(RepelExpressionModule.class);
	
	/**
	 * En el constructor se instancia el ELManager que representa la facade hacia el EL
	 * tambien se instancia el objeto contexto para que se pueda setear
	 */
	public RepelExpressionModule (){
		this.procesor = new ELProcessor(); 
		DspaceResolver elr = new DspaceResolver();
		//añade un Dspace Resolver al contexto usando addElREsolver que propone un CompositeResolver
		this.getELManager().addELResolver(elr);
		try {
			//FIXME quizas debería recibirse un context en uso antes de instanciarse uno nuevo
			this.context = new Context();
			//añade el site que representa el contexto de los objetos que no poseen uno
			Site site = (Site) Site.find(this.context, Site.SITE_ID); 
			SiteWrapper siteWrapper = new SiteWrapper(site);
			this.procesor.defineBean("Site", siteWrapper);	
		} catch (SQLException e) {
			throw new RepelExpresionException("Error al instanciar un contexto Dspace",e);
		}
	}
	
	/**define uno de los objetos a tratar con el lenguaje, en una de las clases wrapper
	 * El objeto tiene que ser necesariamente un DSpaceObject
	 * @param name la referencia al objeto
	 * @param bean el objeto de una clase válida
	 * las clases válidas pueden ser:
	 * 		bitstream
	 * 		bundle
	 * 		item
	 * 		collection
	 * 		community
	 * 		site 
	 *TODO  añadir handle y site
	 *TODO mostrar a lira el defineObject, proponer alguna otra solución esto es horrible
	 *TODO como evitar el if si tengo un builder por clase
	 *
	 *
	 */
	public <T extends DSpaceObject> void defineObject(String name, T bean){
		AbstractFactory factory = new ReadOnlyFactory();
		this.procesor.defineBean(name, factory.getWrapper(bean));
	}
	
	
	/**
	 * matchea dos strings para que se puedan definir otras cosas
	 * TODO este método no debería ser publico o los wrapper no tendrian sentido
	 * @param name
	 * @param object
	 */
	public void defineObject(String name, String object){
		this.procesor.defineBean(name, object);
	}
	
	/**
	 * Este mensaje recibe una  variable y una expresion y la parsea y almacena
	 * para que pueda seguir siendo utilizada
	 */
		
	public void setVariable( String variable, String obj ) {
		this.procesor.setVariable(variable,obj);
	}
	
	/**
	 * Este mensaje recibe el prefijo y el nombre de la función y la bindea 
	 * para que el usuario pueda utilizarla
	 * @param prefix
	 * @param localName
	 * @param meth
	 * TODO consultar con lira si se puede dejar como trabajo futuro
	 * @throws NoSuchMethodException si no hay un método que se llame asi
	 */
	protected void setFunction (String prefix, String localName, Method meth) throws NoSuchMethodException{
		this.procesor.defineFunction(prefix, localName, meth);
	}
	
	/**
	 * Evalúa la expresion en el contexto seteado con aterioridad 
	 * @param expr la expresion es un string porque se convierte aquí
	 * @return objeto con resultado de la evaluación de la expresión
	 * 
	 *TODO ver con lira lo de ocultar la necesidad de utilizar un factory
	 *preguntar si no puedo usar un factory en el Módulo como estado interno
	 */
	public Object eval(String expr){
		return this.procesor.eval(expr);
	}
	
	
	private ELManager getELManager(){
		return this.procesor.getELManager();
	}
	
}
