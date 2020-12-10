/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.PersonStatisticsProvider;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("person.statistics")
public class PersonStatisticsRestRepository extends AbstractDSpaceRestRepository {

	@Autowired(required = true)
	private ItemService itemService;

	public List<String> getStatisticsFor(UUID uuid) throws AuthorizeException, SQLException {
		Context context = obtainContext();
		PersonStatisticsProvider personStatisticsProvider = new PersonStatisticsProvider();
		Item item = itemService.find(context, uuid);
		if (item == null || !personStatisticsProvider.isPerson(item)) {
			throw new UnprocessableEntityException("The given targetId does not resolve to a DSpaceObject: " + uuid);
		}

		return personStatisticsProvider.getPersonCoauthors(context, item);
	}
}
