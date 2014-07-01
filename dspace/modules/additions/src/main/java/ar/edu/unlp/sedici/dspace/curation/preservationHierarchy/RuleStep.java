/**
 * Copyright (C) 2011 SeDiCI <info@sedici.unlp.edu.ar>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ar.edu.unlp.sedici.dspace.curation.preservationHierarchy;

import ar.edu.unlp.sedici.dspace.curation.preservationHierarchy.preservationRules.Rule;

public class RuleStep {
	private float weight;
	private Rule rule;
	private int order;
	
	public RuleStep(int order,Rule rule,float weigth){
		this.initialize(order,rule,weigth);
	}
	
	private void initialize(int order, Rule rule, float weigth) {
		this.setOrder(order);
		this.setRule(rule);
		this.setWeight(weigth);		
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public Rule getRule() {
		return rule;
	}

	public void setRule(Rule rule) {
		this.rule = rule;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

}
