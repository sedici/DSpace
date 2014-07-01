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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

public class FlashMessagesUtil {

    private static final String MessagesName="MessagesUtil.mensajes";
    
    public static void setMessage(HttpSession sesion, String mensaje, List<String> parametros, FlashMessage.TYPE tipo){
    	FlashMessage mensajeAGuardar= new FlashMessage(mensaje, parametros, tipo);
    	List<FlashMessage> mensajesGuardados=(List<FlashMessage>)(sesion.getAttribute(MessagesName));
    	if (mensajesGuardados==null){
    		mensajesGuardados=new ArrayList<FlashMessage>();
    	}
		mensajesGuardados.add(mensajeAGuardar);
		sesion.setAttribute(MessagesName, mensajesGuardados);   	
    }
    
    public static void setAlertMessage(HttpSession sesion, String mensaje, List<String> parametros){
    	setMessage(sesion, mensaje, parametros, FlashMessage.TYPE.ALERT);   	
    }
    
    public static void setAlertMessage(HttpSession sesion, String mensaje){
    	setMessage(sesion, mensaje, new ArrayList<String>(), FlashMessage.TYPE.ALERT);   	
    }
    
    public static void setNoticeMessage(HttpSession sesion, String mensaje, List<String> parametros){
    	setMessage(sesion, mensaje, parametros, FlashMessage.TYPE.NOTICE);   	
    }
    
    public static void setNoticeMessage(HttpSession sesion, String mensaje){
    	setMessage(sesion, mensaje, new ArrayList<String>(), FlashMessage.TYPE.NOTICE);   	
    }
    
    public static void setErrorMessage(HttpSession sesion, String mensaje, List<String> parametros){
    	setMessage(sesion, mensaje, parametros, FlashMessage.TYPE.ERROR);   	
    }
    
    public static void setErrorMessage(HttpSession sesion, String mensaje){
    	setMessage(sesion, mensaje, new ArrayList<String>(), FlashMessage.TYPE.ERROR);   	
    }    
    
    public static List<FlashMessage> consume(HttpSession sesion){
    	List<FlashMessage> mensajesGuardados=(List<FlashMessage>)(sesion.getAttribute(MessagesName));
    	if (mensajesGuardados==null){
    		mensajesGuardados=new ArrayList<FlashMessage>();
    	};
    	//remuevo los mensajes de la sesion
    	sesion.setAttribute(MessagesName, new ArrayList<FlashMessage>());
    	//retorno los mensajes previos
    	return mensajesGuardados;
    }
	
}
