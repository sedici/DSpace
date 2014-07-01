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
package ar.edu.unlp.sedici.util;

import java.util.List;

public class FlashMessage {
	
    public static enum TYPE{
        ERROR, NOTICE, ALERT
    }

	String mensaje;
	List<String> parametros;
	FlashMessage.TYPE tipo;
	
	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public List<String> getParametros() {
		return parametros;
	}

	public void setParametros(List<String> parametros) {
		this.parametros = parametros;
	}

	public FlashMessage.TYPE getTipo() {
		return tipo;
	}

	public void setTipo(FlashMessage.TYPE tipo) {
		this.tipo = tipo;
	}
	
	public FlashMessage(String mensaje, List<String> parametros, FlashMessage.TYPE tipo){
		super();
		this.mensaje=mensaje;
		this.parametros=parametros;
		this.tipo=tipo;
	}
	
	
    
}
