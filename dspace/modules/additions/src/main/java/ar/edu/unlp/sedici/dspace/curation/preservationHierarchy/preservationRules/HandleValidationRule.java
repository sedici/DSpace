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
package ar.edu.unlp.sedici.dspace.curation.preservationHierarchy.preservationRules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dspace.content.DCValue;
import org.dspace.content.Item;

import ar.edu.unlp.sedici.dspace.curation.preservationHierarchy.Reporter;


/**
 * @author terru
 * Toma el handle del item y evalúa:
 * 		que el handle exista 
 * 		que no sea 123456789
 * 		que sea igual al item.getHandle();
 */

public class HandleValidationRule extends Rule {
	/* (non-Javadoc)
	 * @see ar.edu.unlp.sedici.dspace.curation.preservationHierarchy.preservationRules.Rule#evaluate(org.dspace.content.Item)
	 * Extiende la clase abstracta, para poder validar Handle
	 */
	@Override
	public int evaluate(Item item, Reporter reporter) {
		DCValue[] dcHandle = item.getMetadata("dc.identifier.uri");
		String handle = item.getHandle();
		if(dcHandle.length == 0){
		String Msg = "El ítem: "+item.getID()+" fue evaluado con 0 porque no posee Handle";
		reporter.addToItemReport(Msg);
		return 0;
		}
		Pattern pattern = Pattern.compile("\\d+/\\d+");
		Matcher matcher = pattern.matcher(dcHandle[0].value);
		matcher.find();
		String handleValue = matcher.group();
		if(handleValue.equals("123456789")){
			String Msg = "El item: "+item.getID()+" fue evaluado con 0 porque su Handle es el predefinido para Dspace";
			reporter.addToItemReport(Msg);
			return 0;
		}
		if(!handleValue.equals(handle)){
			String Msg = "El item: "+item.getID()+" fue evaluado con 0 porque su handle es diferente al que debería tener\n";
			Msg += "El handle del item hallado para la comparación fue :"+handleValue;
			reporter.addToItemReport(Msg);
			return 0;
		}
		String Msg = "El Handle del item: "+item.getID()+" es válido, el item fue evaluado con 1";
		reporter.addToItemReport(Msg);
		return 1;
	}
}
