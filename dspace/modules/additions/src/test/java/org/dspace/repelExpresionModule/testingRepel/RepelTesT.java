package org.dspace.repelExpresionModule.testingRepel;

import org.dspace.repelExpresionModule.repel.RepelExpressionModule;

public abstract class RepelTesT {

	private RepelExpressionModule expModule;

	protected void initConfig(String casename) {
		System.out.print("Realizando test para el caso de uso:\n\n");
		System.out.print(casename);
		this.expModule = new RepelExpressionModule(null);
		this.setMetamodel();
	}

	protected RepelExpressionModule getExpModule() {
		return this.expModule;
	}
	
	protected abstract void setMetamodel();
}