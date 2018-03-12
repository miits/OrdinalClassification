/**
 * Copyright (C) Jerzy Błaszczyński, Marcin Szeląg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rulelearn.rules;

import org.rulelearn.data.Attribute;

/**
 * Information about an attribute.
 *
 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
 */
public class AttributeInformation {
	/**
	 * Number (index) of the attribute in the array of all attributes.
	 */
	protected int attributeIndex;

	/**
	 * Reference to the attribute.
	 */
	protected Attribute attribute;
	
	/**
	 * Constructor initializing all fields.
	 * 
	 * @param attribute attribute of an information table for which this object is created
	 * @param attributeIndex index of the attribute in the array of all attributes of an information table
	 */
	public AttributeInformation(Attribute attribute, int attributeIndex) {
		this.attribute = attribute;
		this.attributeIndex = attributeIndex;
	}
	
	/**
	 * Gets number (index) of the attribute.
	 * 
	 * @return number (index) of the attribute
	 */
	public int getAttributeIndex() {
		return this.attributeIndex;
	}
	
	/**
	 * Gets name of the attribute.
	 * 
	 * @return name of the attribute
	 */
	public String getAttributeName() {
		return this.attribute.getName();
	}
}
