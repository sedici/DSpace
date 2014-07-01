/*
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

function llamar_alerta(div, boton, value_ocultar, value_ver){
	if ($('#'+boton).attr('value')==value_ocultar){
		$('#'+boton).attr('value', value_ver);
		$('#'+div).siblings("ul").hide("blind", { direction: "vertical" }, 1500);
		$('#'+boton).text('+');
	} else {
		$('#'+boton).attr('value', value_ocultar);
		$('#'+div).siblings("ul").show("blind", { direction: "vertical" }, 1500);
		$('#'+boton).text('-');

	 };
}

/*$(document).ready(function (){
	$(".artifact-description-community").siblings("ul").css('display', 'none');
}); */
