package org.dspace.newCurationTasks;

import java.io.IOException;

import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;
import org.dspace.repelExpresionModule.repel.RepelExpresionException;
import org.dspace.repelExpresionModule.repel.RepelExpressionModule;

/**
 * Tarea de curación que realiza validaciones utilizando REPEL 1.0 para DSPACE
 * 5.0
 * 
 * @author terru
 * @see org.dspace.repelExpresionModule
 *
 */
public class RepelValidationTask extends AbstractCurationTask {
	private RepelExpressionModule expModule;
	private Context ctx;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dspace.curate.AbstractCurationTask#perform(org.dspace.content.DSpaceObject)
	 */
	@Override
	public final int perform(DSpaceObject dso) throws IOException {
		this.configExpressionModule();
		if (!this.accept(dso)) {
			//Se evita la sobrecarga de reportes de errores en items salteados
			return Curator.CURATE_SKIP;
		}
		try {
			String exp = this.getExppresion();
			this.expModule.defineObject("dso", dso);
			Object result = this.expModule.eval(exp);
			if ((Boolean) result) {
				report("Validación exitosa en el dso: " + dso.getID());
				setResult("Validación exitosa en el dso: " + dso.getID());
				return Curator.CURATE_SUCCESS;
			}
			report("Validación fallida en el dso: " + dso.getID());
			setResult("Validación fallida en el dso: " + dso.getID());
			return Curator.CURATE_FAIL;
		} catch (RepelExpresionException re) {
			String print = "Excepcion del módulo de expresiones";
			print = print + "Ha ocurrido un error procesando el objeto " + dso.getID();
			print = print + ":\n [" + re.getClass().getName() + "]" + re.getMessage() + "\n";
			re.printStackTrace();
			setResult(print);
			report(print);
			return Curator.CURATE_FAIL;
		}
	}

	protected void configExpressionModule() {
		this.expModule = new RepelExpressionModule(this.ctx);
	}

	// las clases hijas deben reescribir estos dos métodos
	protected String getExppresion() {
		throw new UnsupportedOperationException(
				"Se debe implementar en subclase");
	}

	protected Boolean accept(DSpaceObject dso) {
		throw new UnsupportedOperationException(
				"Se debe implementar en subclase");
	}
	
	@Override
	public int perform(Context ctx, String id) throws IOException {
		this.ctx = ctx;
		return super.perform(ctx, id);
	}

}
