/**
 * Copyright (C) 2011 SeDiCI <info@sedici.unlp.edu.ar>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ar.edu.unlp.sedici.aspect.redirect;

import java.util.Map;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.selection.AbstractSwitchSelector;
import org.apache.cocoon.selection.Selector;
import org.apache.log4j.Logger;

import org.dspace.core.ConfigurationManager;


public class ConfigurationParameterSelector extends AbstractSwitchSelector implements
        Selector
{

    private static final String MODULE_PARAMETER_NAME = "module";
	private static final String PROPERTY_PARAMETER_NAME = "property";
	private static Logger log = Logger.getLogger(ConfigurationParameterSelector.class);


    /*
     * Devuelve el valor de la entrada "property" del modulo "module" de los archivos de configuracion, ambos pasados como parametros.
     */
    public Object getSelectorContext(Map objectModel, Parameters parameters) {
    	String retorno=null;
    	try {
			retorno=ConfigurationManager.getProperty(parameters.getParameter(MODULE_PARAMETER_NAME), parameters.getParameter(PROPERTY_PARAMETER_NAME));
        } catch (Exception e) {
        	this.getLogger().error("Fallo la recuperación de la property "+ PROPERTY_PARAMETER_NAME + "del modulo " + MODULE_PARAMETER_NAME +" en el selector 'ConfigurationParameterSelector'", e);
		}
    	return retorno;
    }


    public boolean select(String expression, Object selectorContext) {
        if ( selectorContext == null ) {
            return false;
        }
        // Just compare the expression with the previously found name
		boolean result = expression.equals((String)selectorContext);

		return result; 
    }

}

