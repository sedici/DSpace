package org.dspace.repelExpresionModule.repel;

import java.lang.reflect.Method;
import java.sql.SQLException;

import javax.el.ELException;
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
			log.error("Error al instanciar un contexto o un site Dspace: " + e.getMessage() + "\n\n");
			throw new RepelExpresionException("Error al instanciar un contexto o un site Dspace",e);
		} catch (RepelExpresionException re){
			log.error("Error en al intentar instanciar el site del metamodelo" + re.getMessage());
			//dejo el error en el errormodule loger y levanto la excepción
			throw re;
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
	 *
	 *
	 */
	public <T extends DSpaceObject> void defineObject(String name, T bean){
		try{
			AbstractFactory factory = new ReadOnlyFactory();
			this.procesor.defineBean(name, factory.getWrapper(bean));
		}catch (ELException e ){
			log.error("Excepción al intentar definir un objeto, el error fue: \n\n" + e.getMessage());
			throw new RepelExpresionException("Algo ha ocurrido verifique el log",e);
		}
	}
	
	
	/**
	 * matchea dos strings para que se puedan definir otras cosas
	 * @param name
	 * @param object
	 */
	public void defineObject(String name, String object){
		try{
		this.procesor.defineBean(name, object);
		}catch (ELException ele){
			log.error("Excepción al intetnar definir una constante");
			throw new RepelExpresionException("Algo ha ocurrido verifique el log",ele);
		}
	}
	
	/**
	 * Este mensaje recibe una  variable y una expresion y la parsea y almacena
	 * para que pueda seguir siendo utilizada
	 */
		
	public void setVariable( String variable, String obj ) {
		try{
			this.procesor.setVariable(variable,obj);	
		}catch (ELException ele){
			log.error("Excepción al intentar definir una variable");
			throw new RepelExpresionException("Algo ha ocurrido verifique el log",ele);
		}
	}
	
	/**
	 * Este mensaje recibe el prefijo y el nombre de la función y la bindea 
	 * para que el usuario pueda utilizarla
	 * @param prefix
	 * @param localName
	 * @param meth
	 * @throws NoSuchMethodException si no hay un método que se llame asi
	 */
	protected void setFunction (String prefix, String localName, Method meth) throws NoSuchMethodException{
		try{
		this.procesor.defineFunction(prefix, localName, meth);
		}catch (ELException ele){
			log.error("Excepción al intentar definir una funcion");
			throw new RepelExpresionException("Algo ha ocurrido verifique el log",ele);
		}
	}
	
	/**
	 * Evalúa la expresion en el contexto seteado con aterioridad 
	 * @param expr la expresion es un string porque se convierte aquí
	 * @return objeto con resultado de la evaluación de la expresión
	 * 
	 */
	public Object eval(String expr){
		try{
			return this.procesor.eval(expr);
		}catch (ELException ele){
			log.error("Error al interpretar la expresion " + ele.getMessage());
			throw new RepelExpresionException("Error en la libreria de lenguaje", ele);
		}
	}
	
	private ELManager getELManager(){
		return this.procesor.getELManager();
	}
	
}
