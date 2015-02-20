/**
 * 
 */
package org.dspace.repelExpresionModule.dspaceProxy;

import java.sql.SQLException;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.LinkedList;

import org.dspace.content.Item;
import org.dspace.content.ItemIterator;
import org.dspace.core.Context;
import org.dspace.repelExpresionModule.repel.RepelExpresionException;

/**
 * @author terru
 *
 */
public class ItemCollectionWrapper extends AbstractCollection<ItemWrapper> {

	private LinkedList<Integer> ids = new LinkedList<Integer>();
	private Context actualContext;

	/**
	 * FIXME para mantener el comportamiento de una colección se itera sobre el
	 * item iterator de Dspace, el mismo NO es una colección ni un ITERADOR de
	 * java, por lo que se deben hacer de esta forma para poder dar soporte a
	 * colecciones de items en el lenguaje
	 * 
	 * @param it
	 */
	public ItemCollectionWrapper(ItemIterator it, Context context) {
		try {
			this.actualContext = context;
			while (it.hasNext()) {
				ids.add(it.nextID());
				// FIXME no esta claro si nextid avanza el iterador
			}
		} catch (SQLException e) {
			throw new RepelExpresionException("Error inesperado", e);
		}
	}

	@Override
	public Iterator<ItemWrapper> iterator() {
		return new ItemIteratorWrapper();
	}

	@Override
	public int size() {
		return this.ids.size();
	}

	 class ItemIteratorWrapper implements Iterator<ItemWrapper> {

		private Iterator<Integer> iterator;
		//FIXME esta clase debería tener estado interno y no depender de un iterador<Integer>

		public ItemIteratorWrapper(){
			this.iterator = ids.iterator();
		}
		public boolean hasNext() {
			return this.iterator.hasNext();
		}

		@Override
		public ItemWrapper next() {
			Item it;
			try {
				if(!this.hasNext()) return null;
				it = Item.find(actualContext, this.iterator.next());
				return new ItemWrapper(it);
			} catch (SQLException e) {
				throw new RepelExpresionException("Error inesperado", e);
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}