package org.dspace.xmlworkflow.state.actions.processingaction;

import java.util.List;

public class AccepEditRejectActionDefault extends AcceptEditRejectAction {
	
	@Override
    public List<String> getOptions() {
	    List<String> options = super.getOptions();
	    options.remove(SUBMIT_APPROVE);
	    options.add(SUBMITT_APPROVE_NOT_SELECT);
	    options.add(SUBMITT_APPROVE_SELECT);
	    return options;
	}

}
