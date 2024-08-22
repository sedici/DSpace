package org.dspace.xmlworkflow.state.actions.processingaction;

import java.util.List;

public class AcceptEditRejectActionAutoarchive extends AcceptEditRejectAction {
	
	@Override
    public List<String> getOptions() {
    List<String> options = super.getOptions();
    options.add(SUBMITT_APPROVE_SELECT);
    return options;
}
}