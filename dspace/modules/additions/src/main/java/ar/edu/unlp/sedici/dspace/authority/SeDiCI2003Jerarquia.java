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
package ar.edu.unlp.sedici.dspace.authority;

import java.util.ArrayList;
import java.util.List;

import ar.edu.unlp.sedici.sedici2003.model.JerarquiasTermino;

public class SeDiCI2003Jerarquia extends SeDiCI2003Hierarchy {

	@Override
	protected List<Object> getSeDiCI2003HierarchyElements(String text, String[] parents, boolean includeChilds, boolean includeSelf, int start, int limit) {
		List<JerarquiasTermino> resultados = JerarquiasTermino.findAll(text, parents, includeChilds, includeSelf, start, limit);
		return new ArrayList<Object>(resultados);
	}

	@Override
	protected String getSeDiCI2003EntityLabel(String field, String key) {
		JerarquiasTermino t = JerarquiasTermino.findJerarquiasTermino(key);
		if (t == null){
			this.reportMissingAuthorityKey(field, key);
			return key;
		}else{
			return t.getNombreEs();
		}
	}

	@Override
	protected String getAuthority(Object entity) {
		return ((JerarquiasTermino) entity).getId();
	}

	@Override
	protected String getLabel(Object entity) {
		String separador = "::";
		String camino = JerarquiasTermino.getCamino((JerarquiasTermino)entity, separador);
		return ((JerarquiasTermino) entity).getNombreEs() + " - " + camino + separador + ((JerarquiasTermino) entity).getNombreEs();
	}

	@Override
	protected String getValue(Object entity) {
		return ((JerarquiasTermino) entity).getNombreEs();
	}

}
