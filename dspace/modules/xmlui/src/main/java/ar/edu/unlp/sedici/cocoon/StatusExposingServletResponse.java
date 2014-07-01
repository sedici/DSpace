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
package ar.edu.unlp.sedici.cocoon;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class StatusExposingServletResponse extends HttpServletResponseWrapper {
	
	private int httpStatus;

    public StatusExposingServletResponse(HttpServletResponse response) {
        super(response);
        httpStatus=-1;
    }

    @Override
    public void sendError(int sc) throws IOException {
        httpStatus = sc;
        super.sendError(sc);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        httpStatus = sc;
        super.sendError(sc, msg);
    }
    
    @Override
    public void sendRedirect(String location) throws IOException {
        httpStatus = 302;
        super.sendRedirect(location);
    }

    @Override
    public void setStatus(int sc) {
        httpStatus = sc;
        super.setStatus(sc);
    }

    public int getStatus() {
        return httpStatus;
    }
    
    @Override
    public void reset() {
        super.reset();
        this.httpStatus = SC_OK;
    }
    
    @Override
    public void setStatus(int status, String string) {
        super.setStatus(status, string);
        this.httpStatus = status;
    } 
    
    public boolean statusHasChanged(){
    	if (this.httpStatus==-1){
    		return false;
    	} else {
    		return true;
    	}
    }

}
