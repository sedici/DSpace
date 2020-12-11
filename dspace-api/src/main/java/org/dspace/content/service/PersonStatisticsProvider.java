package org.dspace.content.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.core.Context;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;

public class PersonStatisticsProvider {

	private ItemService itemService = ContentServiceFactory.getInstance().getItemService();

	private ConfigurationService confService = DSpaceServicesFactory.getInstance().getConfigurationService();

	public boolean isPerson(Item item) {
		List<MetadataValue> itemTypes = itemService.getMetadata(item, "relationship", "type", Item.ANY, Item.ANY,
				false);
		if (itemTypes.isEmpty() || !itemTypes.get(0).getValue().equals("Person")) {
			return false;
		}
		return true;
	}

	public HashMap<String, List<HashMap<String, String>>> getPersonCoauthorsNetwork(Context context, Item person)
			throws SQLException {
		String[] coauthorsMetNames = confService.getArrayProperty("person.statistics.coauthors-metadata");
		HashMap<String, List<HashMap<String, String>>> coauthorsNetwork = new HashMap<String, List<HashMap<String, String>>>();
		coauthorsNetwork.put("nodes", new ArrayList<HashMap<String, String>>());
		coauthorsNetwork.put("links", new ArrayList<HashMap<String, String>>());
		Set<String> coauthors = new HashSet<String>();
		List<MetadataValue> pubsMetadata = itemService.getMetadata(person, "relation", "isPublicationOfAuthor",
				Item.ANY, Item.ANY);
		for (MetadataValue pubMetadata : pubsMetadata) {
			UUID pubID = UUID.fromString(pubMetadata.getValue());
			Item publication = itemService.find(context, pubID);
			for (String metName : coauthorsMetNames) {
				List<MetadataValue> pubsAuthors = itemService.getMetadataByMetadataString(publication, metName);
				setPublicationCoauthorsRelation(coauthorsNetwork, coauthors, pubsAuthors);
			}
		}
		setCoauthorNodes(coauthorsNetwork, coauthors);
		return coauthorsNetwork;

	}

	private void setPublicationCoauthorsRelation(HashMap<String, List<HashMap<String, String>>> coauthorsNetwork,
			Set<String> coauthors, List<MetadataValue> pubsAuthors) {
		Set<String> processedCoauthors = new HashSet<String>();
		for (MetadataValue authorMeta : pubsAuthors) {
			coauthors.add(authorMeta.getValue());
			processedCoauthors.add(authorMeta.getValue());
			for (MetadataValue relatedAuthorMeta : pubsAuthors) {
				if (!processedCoauthors.contains(relatedAuthorMeta.getValue())) {
					List<HashMap<String, String>> links = coauthorsNetwork.get("links");
					HashMap<String, String> relations = new HashMap<String, String>();
					relations.put("source", authorMeta.getValue());
					relations.put("target", relatedAuthorMeta.getValue());
					links.add(relations);
				}
			}
		}
	}

	private void setCoauthorNodes(HashMap<String, List<HashMap<String, String>>> coauthorsNetwork,
			Set<String> coauthors) {
		for (String authorName : coauthors) {
			List<HashMap<String, String>> nodes = coauthorsNetwork.get("nodes");
			HashMap<String, String> authors = new HashMap<String, String>();
			authors.put("id", authorName);
			authors.put("name", authorName);
			nodes.add(authors);
		}
	}

	public List<HashMap<String, String>> getPersonPublicationsPerTime(Context context, Item person)
			throws SQLException {
		String timeMetadata = confService.getProperty("person.statistics.pubtime-metadata");
		HashMap<String, Integer> pubsPerTime = getPubsPerMetadataCount(context, person, timeMetadata);
		return createPubsPerMetadataList(pubsPerTime);
	}

	public List<HashMap<String, String>> getPersonPublicationsPerType(Context context, Item person)
			throws SQLException {
		String typeMetadata = confService.getProperty("person.statistics.pubtype-metadata");
		HashMap<String, Integer> pubsPerType = getPubsPerMetadataCount(context, person, typeMetadata);
		return createPubsPerMetadataList(pubsPerType);
	}

	private HashMap<String, Integer> getPubsPerMetadataCount(Context context, Item person, String metadata)
			throws SQLException {
		HashMap<String, Integer> pubsPerMetadata = new HashMap<String, Integer>();
		List<MetadataValue> pubsMetadata = itemService.getMetadata(person, "relation", "isPublicationOfAuthor",
				Item.ANY, Item.ANY);
		for (MetadataValue pubMetadata : pubsMetadata) {
			UUID pubID = UUID.fromString(pubMetadata.getValue());
			Item publication = itemService.find(context, pubID);
			List<MetadataValue> pubsMeta = itemService.getMetadataByMetadataString(publication, metadata);
			if (!pubsMeta.isEmpty()) {
				String pubMetaValue = pubsMeta.get(0).getValue().split("-", 2)[0];
				Integer currentValue = pubsPerMetadata.putIfAbsent(pubMetaValue, 1);
				if (currentValue != null) {
					pubsPerMetadata.put(pubMetaValue, currentValue + 1);
				}
			}
		}
		return pubsPerMetadata;
	}

	private List<HashMap<String, String>> createPubsPerMetadataList(HashMap<String, Integer> pubsPerMetadata) {
		List<HashMap<String, String>> pubsPerMetadataList = new ArrayList<HashMap<String, String>>();
		for (String pubMetadataValue : pubsPerMetadata.keySet()) {
			HashMap<String, String> metadataValueHash = new HashMap<String, String>();
			metadataValueHash.put("name", pubMetadataValue);
			metadataValueHash.put("value", pubsPerMetadata.get(pubMetadataValue).toString());
			pubsPerMetadataList.add(metadataValueHash);
		}
		return pubsPerMetadataList;
	}
}
