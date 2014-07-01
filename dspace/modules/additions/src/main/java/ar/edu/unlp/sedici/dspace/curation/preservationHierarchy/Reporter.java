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
package ar.edu.unlp.sedici.dspace.curation.preservationHierarchy;

/**
 * @author terru
 * Esta clase mantiene la información para cada Item que procesa la tarea de curation
 * Cuando una regla encuentra algo para reportar,
 * debe utilizar esta clase para salvar el estado
 * Luego de cada item, el reporte se debe renovar
 */

public class Reporter {
	private String report;

	public String getReport() {
		return report;
	}
	
	public void addToItemReport(String Msg){
		this.report = this.report + Msg + "\n";
	}
	
}
