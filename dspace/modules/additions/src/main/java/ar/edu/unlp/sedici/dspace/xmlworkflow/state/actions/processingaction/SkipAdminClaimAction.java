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
package ar.edu.unlp.sedici.dspace.xmlworkflow.state.actions.processingaction;

import java.sql.SQLException;

import org.dspace.authorize.AuthorizeManager;
import org.dspace.core.Context;
import org.dspace.xmlworkflow.WorkflowConfigurationException;
import org.dspace.xmlworkflow.state.actions.userassignment.ClaimAction;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;


/**
 * Behaves like the ClaimAction processing action, except that when the logged user is a target collection's admin, 
 * this step is not executed.
 * 
 * @author nestor
 */
public class SkipAdminClaimAction extends ClaimAction {

	@Override
	public boolean isValidUserSelection(Context context, XmlWorkflowItem wfi, boolean hasUI) throws WorkflowConfigurationException, SQLException {
		if(AuthorizeManager.isAdmin(context, wfi.getCollection()))
			return false;
		return super.isValidUserSelection(context, wfi, hasUI);
	}
}
