/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.PersonStatisticsProvider;
import org.dspace.core.Context;
import org.dspace.app.rest.model.PersonStatisticsRest;
import org.dspace.app.rest.model.hateoas.PersonStatisticsResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(PersonStatisticsRest.CATEGORY + "." + PersonStatisticsRest.NAME)

public class PersonStatisticsRestRepository extends AbstractDSpaceRestRepository {

	@Autowired(required = true)
	private ItemService itemService;

	private Item getItem(Context context, UUID uuid, PersonStatisticsProvider personStatisticsProvider)
			throws SQLException {
		Item item = itemService.find(context, uuid);
		if (item == null || !personStatisticsProvider.isPerson(item)) {
			throw new UnprocessableEntityException("The given targetId does not resolve to a Person entity: " + uuid);
		}
		return item;
	}

	public PersonStatisticsResource getCoauthorsNetworkStatisticsFor(UUID uuid)
			throws AuthorizeException, SQLException {
		Context context = obtainContext();
		PersonStatisticsProvider personStatisticsProvider = new PersonStatisticsProvider();
		Item item = getItem(context, uuid, personStatisticsProvider);
		HashMap<String, List<HashMap<String, String>>> statisticsData = personStatisticsProvider
				.getPersonCoauthorsNetwork(context, item);
		PersonStatisticsRest<HashMap<String, List<HashMap<String, String>>>> personStatRest = new PersonStatisticsRest<HashMap<String, List<HashMap<String, String>>>>();
		List<HashMap<String, List<HashMap<String, String>>>> statisticsDataList = new ArrayList<HashMap<String, List<HashMap<String, String>>>>();
		statisticsDataList.add(statisticsData);
		personStatRest.setStatisticsData(statisticsDataList);
		personStatRest.setId(uuid.toString() + "/coauthors");
		return converter.toResource(personStatRest);
	}

	public PersonStatisticsResource getPublicationsPerTypeStatisticsFor(UUID uuid) throws SQLException {
		Context context = obtainContext();
		PersonStatisticsProvider personStatisticsProvider = new PersonStatisticsProvider();
		Item item = getItem(context, uuid, personStatisticsProvider);
		List<HashMap<String, String>> statisticsData = personStatisticsProvider.getPersonPublicationsPerType(context,
				item);
		PersonStatisticsRest<HashMap<String, String>> personStatRest = new PersonStatisticsRest<HashMap<String, String>>();
		personStatRest.setStatisticsData(statisticsData);
		personStatRest.setId(uuid.toString() + "/publicationstype");
		return converter.toResource(personStatRest);
	}

	public PersonStatisticsResource getPublicationsPerTimeStatisticsFor(UUID uuid) throws SQLException {
		Context context = obtainContext();
		PersonStatisticsProvider personStatisticsProvider = new PersonStatisticsProvider();
		Item item = getItem(context, uuid, personStatisticsProvider);
		List<HashMap<String, String>> statisticsData = personStatisticsProvider.getPersonPublicationsPerTime(context,
				item);
		PersonStatisticsRest<HashMap<String, String>> personStatRest = new PersonStatisticsRest<HashMap<String, String>>();
		personStatRest.setStatisticsData(statisticsData);
		personStatRest.setId(uuid.toString() + "/publicationstime");
		return converter.toResource(personStatRest);
	}

}
