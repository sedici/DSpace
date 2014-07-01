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
/**
 * 
 * Clase Abstracta PreservationRule
 * Esta clase abstracta representa la iterfaz que las reglas a evaluar 
 * en cada validación deben respetar.
 * De esta forma, el validador podrá utilizar cualquier conjunto arbitrario
 * de reglas que se le especifique cuando es invocado
 * @author terru
 * 
 */
package ar.edu.unlp.sedici.dspace.curation.preservationHierarchy.preservationRules;



public abstract class Rule {
	public abstract int evaluate(org.dspace.content.Item item, ar.edu.unlp.sedici.dspace.curation.preservationHierarchy.Reporter reporter);
}
