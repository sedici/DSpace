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

public class PersonStatisticsProvider {

	private ItemService itemService = ContentServiceFactory.getInstance().getItemService();

	private String[] coauthorsMetNames = new String[] { "dc.contributor.author", "dc.creator", "dc.contributor",
			"sedici.creator.*" };

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
		}
		for (String authorName : coauthors) {
			List<HashMap<String, String>> nodes = coauthorsNetwork.get("nodes");
			HashMap<String, String> authors = new HashMap<String, String>();
			authors.put("id", authorName);
			authors.put("name", authorName);
			nodes.add(authors);
		}
		return coauthorsNetwork;

	}

	public List<HashMap<String, Integer>> getPersonPublicationsPerTime(Context context, Item person)
			throws SQLException {
		HashMap<String, Integer> pubsPerTime = new HashMap<String, Integer>();
		List<MetadataValue> pubsMetadata = itemService.getMetadata(person, "relation", "isPublicationOfAuthor",
				Item.ANY, Item.ANY);
		for (MetadataValue pubMetadata : pubsMetadata) {
			UUID pubID = UUID.fromString(pubMetadata.getValue());
			Item publication = itemService.find(context, pubID);
			List<MetadataValue> pubsTimesMeta = itemService.getMetadata(publication, "dc", "date", "issued", Item.ANY);
			if (!pubsTimesMeta.isEmpty()) {
				String pubTime = pubsTimesMeta.get(0).getValue().split("-", 2)[0];
				Integer currentValue = pubsPerTime.putIfAbsent(pubTime, 1);
				if (currentValue != null) {
					pubsPerTime.put(pubTime, currentValue + 1);
				}
			}
		}
		List<HashMap<String, Integer>> pubsPerTimeList = new ArrayList<HashMap<String, Integer>>();
		for (String pubTime : pubsPerTime.keySet()) {
			HashMap<String, Integer> timeHash = new HashMap<String, Integer>();
			timeHash.put("year", Integer.parseInt(pubTime));
			timeHash.put("value", pubsPerTime.get(pubTime));
			pubsPerTimeList.add(timeHash);
		}
		return pubsPerTimeList;
	}

	public List<HashMap<String, String>> getPersonPublicationsPerType(Context context, Item person)
			throws SQLException {
		HashMap<String, Integer> pubsPerType = new HashMap<String, Integer>();
		List<MetadataValue> pubsMetadata = itemService.getMetadata(person, "relation", "isPublicationOfAuthor",
				Item.ANY, Item.ANY);
		for (MetadataValue pubMetadata : pubsMetadata) {
			UUID pubID = UUID.fromString(pubMetadata.getValue());
			Item publication = itemService.find(context, pubID);
			List<MetadataValue> pubsTypesMeta = itemService.getMetadata(publication, "dc", "type", null, Item.ANY);
			if (!pubsTypesMeta.isEmpty()) {
				String pubType = pubsTypesMeta.get(0).getValue();
				Integer currentValue = pubsPerType.putIfAbsent(pubType, 1);
				if (currentValue != null) {
					pubsPerType.put(pubType, currentValue + 1);
				}
			}
		}
		List<HashMap<String, String>> pubsPerTypeList = new ArrayList<HashMap<String, String>>();
		for (String pubTime : pubsPerType.keySet()) {
			HashMap<String, String> typeHash = new HashMap<String, String>();
			typeHash.put("name", pubTime);
			typeHash.put("value", pubsPerType.get(pubTime).toString());
			pubsPerTypeList.add(typeHash);
		}
		return pubsPerTypeList;
	}
}
