package org.dspace.app.rest.model;

import java.util.List;
import org.dspace.app.rest.RestResourceController;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PersonStatisticsRest<T> extends BaseObjectRest<String> {

	public static final String NAME = "person";
	public static final String PLURAL_NAME = "persons";
	public static final String CATEGORY = RestAddressableModel.STATISTICS;

	private String id;

	@JsonProperty("data")
	private List<T> statisticsData;

	public String getCategory() {
		return CATEGORY;
	}

	public Class getController() {
		return RestResourceController.class;
	}

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public String getType() {
		return NAME;
	}

	@Override
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setStatisticsData(List<T> data) {
		this.statisticsData = data;
	}

	public List<T> getStatisticsData(List<T> data) {
		return this.statisticsData;
	}
}
