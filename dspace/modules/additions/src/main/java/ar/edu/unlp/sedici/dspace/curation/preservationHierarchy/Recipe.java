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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Recipe {
	private ArrayList<RuleStep> steps;
	private Iterator<RuleStep> iterator;
	
	public Recipe(){
		ArrayList<RuleStep> steps = new ArrayList<RuleStep>();
		this.setSteps(steps);
		this.setIterator(null);//el iterador no existe al inicio
	}
	
	public void addStep(RuleStep step){
		this.getSteps().add(step.getOrder(), step);
	}
	
	public void delStep(RuleStep step){
		this.getSteps().remove(step);
	}

	public boolean hasNextStep(){
		if(this.getIterator()==null){ //puede preguntar si tiene next antes de inicializarse
			this.setIterator(this.getSteps().iterator());
		}
		return this.getIterator().hasNext();
	}
	
	public void resetRecipe(){
		this.setIterator(null);
	}
	
	public RuleStep nextStep() throws NoSuchElementException{
		if(this.getIterator()==null){
			this.setIterator(this.getSteps().iterator());
		}
		return this.getIterator().next();	
	}

	private ArrayList<RuleStep> getSteps() {
		return steps;
	}

	private void setSteps(ArrayList<RuleStep> steps) {
		this.steps = steps;
	}

	private void setIterator(Iterator<RuleStep> iterator) {
		this.iterator = iterator;		
	}

	private Iterator<RuleStep> getIterator() {
		return this.iterator;
	}
}
