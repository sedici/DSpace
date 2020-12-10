package org.dspace.content.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.core.Context;

public class PersonStatisticsProvider {

	ItemService itemService = ContentServiceFactory.getInstance().getItemService();

	public boolean isPerson(Item item) {
		List<MetadataValue> itemTypes = itemService.getMetadata(item, "relationship", "type", Item.ANY, Item.ANY,
				false);
		if (itemTypes.isEmpty() || !itemTypes.get(0).getValue().equals("Person")) {
			return false;
		}
		return true;
	}

	public List<String> getPersonCoauthors(Context context, Item person) throws SQLException {
		String[] coauthorsMetNames = new String[] { "dc.contributor.author", "dc.creator", "dc.contributor",
				"sedici.creator.*" };
		List<String> coauthors = new ArrayList<String>();
		List<MetadataValue> pubsMetadata = itemService.getMetadata(person, "relation", "isPublicationOfAuthor",
				Item.ANY, Item.ANY);
		for (MetadataValue pubMetadata : pubsMetadata) {
			UUID pubID = UUID.fromString(pubMetadata.getValue());
			Item publication = itemService.find(context, pubID);
			for (String metName : coauthorsMetNames) {
				List<MetadataValue> pubsAuthors = itemService.getMetadataByMetadataString(publication, metName);
				for (MetadataValue authMeta : pubsAuthors) {
					coauthors.add(authMeta.getValue());
				}
			}
		}

		return coauthors;
	}
}
