package org.dspace.repelExpresionModule.dspaceProxy;

import java.sql.SQLException;

import org.dspace.repelExpresionModule.repel.RepelExpresionException;
import org.dspace.content.Collection;
import org.dspace.content.Community;

public class CommunityWrapper extends DspaceObjectWrapper<Community> {

	public CommunityWrapper(Community community) {
		super(community);
	}

	public int getType(){
		return this.getDso().getType();
	}

	public String getTypeText(){
		return this.getDso().getTypeText();
	}

	public int getID(){
		return this.getDso().getID();
	}

	public String getHandle(){
		return this.getDso().getHandle();
	}

	public String getName(){
		return this.getDso().getName();
	}
	
	public BitstreamWrapper getLogo(){
		return new BitstreamWrapper(this.getDso().getLogo());
	}

	public CollectionWrapper[] getCollections(){
		try{
			Collection[] array = this.getDso().getCollections();
			CollectionWrapper[] returner = new CollectionWrapper[array.length];
			for(int i=0; i< array.length;i++){
				returner[i]=new CollectionWrapper(array[i]);
			}
			return returner;
		}catch (SQLException e){
			throw new RepelExpresionException("Error inesperado",e);	
		}
	} 

	public CommunityWrapper[] getSubcommunities(){
		try{
			Community[] array = this.getDso().getSubcommunities();
			CommunityWrapper[] returner = new CommunityWrapper[array.length];
			for(int i=0; i< array.length;i++){
				returner[i]=new CommunityWrapper(array[i]);
			}
			return returner;
		}catch (SQLException e){
			throw new RepelExpresionException("Error al intentar recuperar subcomunidades",e);		
			}
	}

	public CommunityWrapper getParentCommunity(){
		try{
			return new CommunityWrapper(this.getDso().getParentCommunity());
		}catch (SQLException e){
			throw new RepelExpresionException("Error inesperado al intentar recuperar una comunidad padre",e);
		}
	};

	public CommunityWrapper[] getAllParents(){
		try{
			Community[] array = this.getDso().getAllParents();
			CommunityWrapper[] returner = new CommunityWrapper[array.length];
			for(int i=0; i< array.length;i++){
				returner[i]=new CommunityWrapper(array[i]);
			}
			return returner;
		}catch (SQLException e){
			throw new RepelExpresionException("Error inesperado al intentar recuperar todas las comunidades padre",e);
		}
	}

	public CollectionWrapper[] getAllCollections(){
		try{
			Collection[] array = this.getDso().getAllCollections();
			CollectionWrapper[] returner = new CollectionWrapper[array.length];
			for(int i=0; i< array.length;i++){
				returner[i]=new CollectionWrapper(array[i]);
			}
			return returner;
		}catch (SQLException e){
			throw new RepelExpresionException("Error inesperado al intentar recuperar las colecciones",e);
		}
	}

	public boolean equals(Object other){
			return this.getDso().equals(other);
	}

	public int countItems(){
		try{
			return this.getDso().countItems();
		}catch (SQLException e){
			throw new RepelExpresionException("Error inesperado al contar los items",e);
		}
	}
	
/**
 * FIXME todo debe ser implementado por un ELResolver
 * El recuperar los items desde una comunidad se implementará en los trabajos futuros
 */
		
//	/**
//	 * Esta clase retorna todos los items de una comunidad para mejorar expresividad del lenguaje 
//	 * @return itemWrapper
//	 */
//	public ItemWrapper[] getItems(){
//		return new ItemWrapper[]{null};
	
//	}
//	
//	/**
//	 * para cada uno de los pares que vienen metadato:valor en el criteria
//	 * analizar si estan en el item
//	 * caso afirmativo, devolver item
//	 * @param criteria
//	 * @return
//	 */
//	public ItemWrapper[] getItem(Map<String, String> criteria){
//		ItemWrapper[] arrayItems = this.getItems();
//		ItemWrapper[] returner = new ItemWrapper[arrayItems.length];
//		for(int i=0, j = 0; i<arrayItems.length;i++){
//			boolean metadatos = false;
//			for(Map.Entry<String, String> entry : criteria.entrySet()){
//				boolean encontre = false; 
//				//marca si encontro el valor para este metadato
//				MetadatumWrapper[] arrayMetadata = arrayItems[i].getMetadata(entry.getKey());
//				for (int k=0;i<arrayMetadata.length;k++){
//					if (arrayMetadata[k].getValue() == entry.getValue()){
//						encontre = true;
//						break;
//					}
//				}
//				if(!encontre) break;
//				metadatos = true;
//			}
//			if (metadatos ){
//				returner[j]= arrayItems[i];
//				j++;
//			}
//		}
//		return returner;
//	}

	
}