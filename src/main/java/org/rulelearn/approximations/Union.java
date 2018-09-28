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

package org.rulelearn.approximations;

import org.rulelearn.core.InvalidValueException;
import org.rulelearn.core.TernaryLogicValue;
import org.rulelearn.data.Attribute;
import org.rulelearn.data.AttributePreferenceType;
import org.rulelearn.data.AttributeType;
import org.rulelearn.data.Decision;
import org.rulelearn.data.EvaluationAttribute;
import org.rulelearn.data.EvaluationAttributeWithContext;
import org.rulelearn.data.InformationTableWithDecisionDistributions;
import org.rulelearn.dominance.DominanceConeCalculator;
import org.rulelearn.rules.SimpleCondition;
import org.rulelearn.rules.SimpleConditionAtLeast;
import org.rulelearn.rules.SimpleConditionAtMost;
import org.rulelearn.rules.SimpleConditionEqual;
import org.rulelearn.types.SimpleField;

import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.ints.IntSortedSets;

import static org.rulelearn.core.Precondition.notNull;

import java.util.ArrayList;
import java.util.List;

import org.rulelearn.core.InvalidTypeException;

/**
 * Union of ordered decision classes, i.e., set of objects whose decision is not worse or not better than given limiting decision.
 * Objects from the information table such that:<br>
 * - upward union's limiting decision is at most as good as their decision, or<br>
 * - downward union's limiting decision is at least as good as their decision,<br>
 * are considered to belong to this union (they are called positive objects).<br>
 * Objects from the information table such that:<br>
 * - upward union's limiting decision is better than their decision, or<br>
 * - downward union's limiting decision is worse than their decision,<br>
 * are considered to belong to the complement of this union (they are called negative objects).<br>
 * Remaining objects from the information table are called neutral objects. Their decision is uncomparable with this union's decision.
 * 
 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
 */
public class Union extends ApproximatedSet {
	
	/**
	 * Type of a union of decision classes.
	 *
	 * @author Jerzy Błaszczyński (<a href="mailto:jurek.blaszczynski@cs.put.poznan.pl">jurek.blaszczynski@cs.put.poznan.pl</a>)
	 * @author Marcin Szeląg (<a href="mailto:marcin.szelag@cs.put.poznan.pl">marcin.szelag@cs.put.poznan.pl</a>)
	 */
	public static enum UnionType {
		/**
		 * Type of an upward union of decision classes.
		 */
		AT_LEAST,
		/**
		 * Type of an downward union of decision classes.
		 */
		AT_MOST
	}
	
	/**
	 * Type of this union. See {@link UnionType}.
	 */
	protected UnionType unionType;
	
	/**
	 * Reference to complementary union of decision classes that complements this union w.r.t. set of all objects U. This reference is useful, e.g., when calculating the upper approximation of this union using VC-DRSA
	 * (by complementing the lower approximation of the complementary union). Initialized with {@code null}. Can be updated by {@link #setComplementaryUnion(Union)} method.
	 */
	protected Union complementaryUnion = null;
	
//	/**
//	 * Set with indices of objects such that this union's limiting decision is uncomparable with their decision.
//	 * Limiting decision is considered to be uncomparable with a particular decision, if it is neither at least as good as nor at most as good as that decision.
//	 */
//	protected IntSortedSet uncomparableObjects;
	
	/**
	 * Set of objects from information table that are neither positive nor negative with respect to this union.  
	 */
	protected IntSortedSet neutralObjects;
	
	/**
	 * Tells if given decision is positive with respect to the union of ordered decision classes that would be constructed for given parameters.
	 * See {@link #isDecisionPositive(Decision)}.
	 * 
	 * @param decision decision to verify for being positive with the union of ordered decision classes that would be constructed for given parameters
	 * @param unionType see {@link #Union(UnionType, Decision, InformationTableWithDecisionDistributions, DominanceBasedRoughSetCalculator)}
	 * @param limitingDecision see {@link #Union(UnionType, Decision, InformationTableWithDecisionDistributions, DominanceBasedRoughSetCalculator)}
	 * @param informationTable see {@link #Union(UnionType, Decision, InformationTableWithDecisionDistributions, DominanceBasedRoughSetCalculator)}
	 * 
	 * @return {@code true} if given decision is positive with the union of ordered decision classes that would be constructed for given parameters,
	 *         {@code false} otherwise
	 * 
	 * @throws NullPointerException if any of the parameters is {@code null}
	 * @throws InvalidTypeException see {@link #validateLimitingDecision(Decision, InformationTableWithDecisionDistributions)}
	 * @throws InvalidValueException see {@link #validateLimitingDecision(Decision, InformationTableWithDecisionDistributions)}
	 */
	public static boolean isDecisionPositive(Decision decision, UnionType unionType, Decision limitingDecision, InformationTableWithDecisionDistributions informationTable) {
		Union minimalUnion = new Union(unionType, limitingDecision, informationTable); //construct a minimal union, sufficient to call isDecisionPositive method
		return minimalUnion.isDecisionPositive(decision);
	}
	
	/**
	 * Constructs union of ordered decision classes of given type (at least or at most), using given limiting decision (concerning the least or the most preferred decision class). Calculates objects
	 * belonging to this union and neutral objects. Stores given information table and given rough set calculator.
	 * 
	 * @param unionType type of this union; see {@link UnionType}
	 * @param limitingDecision decision that serves as a limit for this union; e.g., decision "3" is a limit for union "at least 3" and "at most 3" 
	 * @param informationTable information table with considered objects, some of which belong to this union
	 * @param roughSetCalculator dominance-based rough set calculator used to calculate approximations and boundary of this union
	 * 
	 * @throws NullPointerException if any of the parameters is {@code null}
	 * @throws InvalidTypeException if any of the attributes contributing to given limiting decision is not an evaluation attribute
	 * @throws InvalidValueException if any of the attributes contributing to given limiting decision is not an active decision attribute
	 * @throws InvalidValueException if none of the attributes contributing to given limiting decision is ordinal (i.e., has gain- or cost-type preference)
	 */
	public Union(UnionType unionType, Decision limitingDecision, InformationTableWithDecisionDistributions informationTable, DominanceBasedRoughSetCalculator roughSetCalculator) {
		super(informationTable, limitingDecision, roughSetCalculator);
		this.unionType = notNull(unionType, "Union type is null.");
		validateLimitingDecision(limitingDecision, informationTable);
		
		this.findObjects();
	}
	
	/**
	 * Constructs union of ordered decision classes of given type (at least or at most), using given limiting decision (concerning the least or the most preferred decision class). Calculates objects
	 * belonging to this union and neutral objects. Stores given information table and given rough set calculator. Takes into account the flag concerning inclusion of objects
	 * having decision equal to the limiting decision of this union. 
	 * 
	 * @param unionType see {@link #Union(UnionType, Decision, InformationTableWithDecisionDistributions, DominanceBasedRoughSetCalculator)}
	 * @param limitingDecision see {@link #Union(UnionType, Decision, InformationTableWithDecisionDistributions, DominanceBasedRoughSetCalculator)}
	 * @param informationTable see {@link #Union(UnionType, Decision, InformationTableWithDecisionDistributions, DominanceBasedRoughSetCalculator)}
	 * @param roughSetCalculator see {@link #Union(UnionType, Decision, InformationTableWithDecisionDistributions, DominanceBasedRoughSetCalculator)}
	 * @param includeLimitingDecision tells if objects having decision equal to the limiting decision of this union should be included in this union
	 * 
	 * @throws NullPointerException if any of the parameters is {@code null}
	 * @throws InvalidTypeException see {@link #validateLimitingDecision(Decision, InformationTableWithDecisionDistributions)}
	 * @throws InvalidValueException see {@link #validateLimitingDecision(Decision, InformationTableWithDecisionDistributions)}
	 */
	Union(UnionType unionType, Decision limitingDecision, InformationTableWithDecisionDistributions informationTable, DominanceBasedRoughSetCalculator roughSetCalculator, boolean includeLimitingDecision) {
		super(informationTable, limitingDecision, roughSetCalculator);
		this.unionType = notNull(unionType, "Union type is null.");
		validateLimitingDecision(limitingDecision, informationTable);
		
		this.includeLimitingDecision = includeLimitingDecision; //set flag concerning inclusion of limiting decision
		
		this.findObjects();
	}
	
	/**
	 * Constructs union of ordered decision classes of given type (at least or at most), using given limiting decision
	 * (concerning the least or the most preferred decision class).
	 * Stores given information table.<br>
	 * <br>
	 * This is a minimal constructor that can be used in static methods of this class to quickly construct a lightweight union,
	 * providing limited functionality. In particular, this constructor does not calculate objects belonging to this union (nor neutral objects),
	 * which is a time consuming process. Moreover, it does not set rough set calculator.
	 * 
	 * @param unionType see {@link #Union(UnionType, Decision, InformationTableWithDecisionDistributions, DominanceBasedRoughSetCalculator)}
	 * @param limitingDecision see {@link #Union(UnionType, Decision, InformationTableWithDecisionDistributions, DominanceBasedRoughSetCalculator)}
	 * @param informationTable see {@link #Union(UnionType, Decision, InformationTableWithDecisionDistributions, DominanceBasedRoughSetCalculator)}
	 * 
	 * @throws NullPointerException if any of the parameters is {@code null}
	 * @throws InvalidTypeException see {@link #validateLimitingDecision(Decision, InformationTableWithDecisionDistributions)}
	 * @throws InvalidValueException see {@link #validateLimitingDecision(Decision, InformationTableWithDecisionDistributions)}
	 */
	private Union(UnionType unionType, Decision limitingDecision, InformationTableWithDecisionDistributions informationTable) {
		super();
		
		this.unionType = notNull(unionType, "Union type is null.");
		notNull(limitingDecision, "Limiting decision for constructed union is null.");
		notNull(informationTable, "Information table for constructed union is null.");
		validateLimitingDecision(limitingDecision, informationTable);
		this.limitingDecision = limitingDecision;
		this.informationTable = informationTable; //may be redundant
	}
	
	/**
	 * Validates given limiting decision, taking into account given information table. Checks if each attribute contributing to the given decision
	 * is an evaluation attribute, is active, and of type {@link AttributeType#DECISION}. Moreover, at least one of the attributes has to have
	 * preference type different than {@link AttributePreferenceType#NONE}.
	 * 
	 * @param limitingDecision decision that serves as a limit for this union; e.g., decision "3" is a limit for union "at least 3" and "at most 3"
	 * @param informationTable information table with considered objects, some of which belong to this union
	 * @return {@code true} if given decision is valid in the context of the given information table, throws exception otherwise
	 * 
	 * @throws InvalidTypeException if any of the attributes contributing to given limiting decision is not an evaluation attribute
	 * @throws InvalidValueException if any of the attributes contributing to given limiting decision is not an active decision attribute
	 * @throws InvalidValueException if none of the attributes contributing to given limiting decision is ordinal (i.e., has gain- or cost-type preference)
	 */
	protected boolean validateLimitingDecision(Decision limitingDecision, InformationTableWithDecisionDistributions informationTable) {
		IntSet attributeIndices = limitingDecision.getAttributeIndices();
		IntIterator attributeIndicesIterator  = attributeIndices.iterator();
		int attributeIndex;
		Attribute attribute;
		EvaluationAttribute evaluationAttribute;
		
		boolean activeDecisionCriterionFound = false;
		
		//check attributes contributing to the limiting decision
		while (attributeIndicesIterator.hasNext()) {
			attributeIndex = attributeIndicesIterator.nextInt();
			attribute = informationTable.getAttribute(attributeIndex);
			
			if (attribute instanceof EvaluationAttribute) {
				evaluationAttribute = (EvaluationAttribute) attribute;
				if (evaluationAttribute.isActive() && evaluationAttribute.getType() == AttributeType.DECISION) { //active decision attribute
					if (evaluationAttribute.getPreferenceType() != AttributePreferenceType.NONE) { //gain/cost-type attribute
						activeDecisionCriterionFound = true; //mark finding, but do not break the loop, as it should be also checked if next attributes are correct
					}
				} else {
					throw new InvalidValueException("Attribute no. "+attributeIndex+" contributing to union's limiting decision is not an active decision attribute.");
				}
			} else {
				throw new InvalidTypeException("Attribute no. "+attributeIndex+" contributing to union's limiting decision is not an evaluation attribute.");
			}
		} //while
		
		if (!activeDecisionCriterionFound) {
			throw new InvalidValueException("Cannot create union of ordered decision classes - none of the attributes contributing to union's limiting decision is ordinal.");
		}
		
		return true;
	}
	
	/**
	 * Finds (positive) objects belonging to this union and neutral objects (i.e., objects such that this union's limiting decision is neutral with their decision).
	 * Assumes that information table and limiting decision have already been set.
	 */
	@Override
	protected void findObjects() {
		IntSortedSet objects = new IntLinkedOpenHashSet(); //TODO: estimate hash set capacity using distribution of decisions?
		IntSortedSet uncomparableObjects = new IntLinkedOpenHashSet(); //TODO: estimate hash set capacity using distribution of decisions?
		
		int objectsCount = this.informationTable.getNumberOfObjects();
		
		for (int i = 0; i < objectsCount; i++) {
			if (this.isDecisionPositive(this.informationTable.getDecision(i))) {
				objects.add(i);
			} else {
				if (this.isDecisionNeutral(this.informationTable.getDecision(i))) {
					uncomparableObjects.add(i);
				}
			}
		}
		
		this.objects = IntSortedSets.unmodifiable(objects);
		this.neutralObjects = IntSortedSets.unmodifiable(uncomparableObjects);
	}
	
	/**
	 * Gets set of objects from information table that are neither positive nor negative with respect to this union.
	 * 
	 * @return set of objects from information table that are neither positive nor negative with respect to this union
	 */
	@Override
	public IntSortedSet getNeutralObjects() {
		return this.neutralObjects;
	}

	/**
	 * Registers complementary union of decision classes that complements this union w.r.t. set of all objects U.
	 * This reference is useful, e.g., when calculating the upper approximation of this union using VC-DRSA
	 * (by complementing the lower approximation of the complementary union).
	 * Complementary union may be set only if it is not already stored in this union.
	 * 
	 * @param union complementary union of decision classes; e.g., if there are five decision classes: 1, 2, 3, 4, 5,
	 *        and this union concerns classes 3-5 (^gt;=3), then the complementary union concerns classes 1-2 (&lt;=2)
	 * @return {@code true} if given union has been set as a complementary union,
	 *         {@code false} otherwise 
	 */
	public boolean setComplementaryUnion(Union union) {
		//accept change only if the complementary union has not been set nor calculated yet
		if (this.complementaryUnion == null) {
			this.complementaryUnion = union;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Gets complementary union of decision classes that complements this union w.r.t. set of all objects U.
	 * E.g., if there are five decision classes: 1, 2, 3, 4, 5, and this union concerns classes 3-5 (&gt;=3),
	 * then the complementary union concerns classes 1-2 (&lt;=2). If complementary union has not been previously set
	 * using {@link #setComplementaryUnion(Union)} method, it is first calculated.
	 * 
	 * @return complementary union of decision classes 
	 */
	public Union getComplementaryUnion() {
		if (this.complementaryUnion == null) {
			this.complementaryUnion = calculateComplementaryUnion();
			this.complementaryUnion.setComplementaryUnion(this); //set this union as complementary to the returned one
		}
		
		return this.complementaryUnion;
	}
	
	/**
	 * Calculates complementary union of decision classes that complements this union w.r.t. set of all objects U.
	 * Calculated union has the same limiting decision, but does not include objects with that decision. Moreover, it has opposite union type.
	 * 
	 * @return complementary union of decision classes
	 */
	protected Union calculateComplementaryUnion() {
		UnionType complementaryUnionType = null;
		
		switch (this.unionType) {
		case AT_LEAST:
			complementaryUnionType = UnionType.AT_MOST;
			break;
		case AT_MOST:
			complementaryUnionType = UnionType.AT_LEAST;
			break;
		}
		
		return new Union(complementaryUnionType, this.limitingDecision, this.getInformationTable(), this.getRoughSetCalculator(), false);
	}

	/**
	 * Gets type of this union. See {@link UnionType}.
	 * 
	 * @return type of this union
	 */
	public UnionType getUnionType() {
		return unionType;
	}

	/**
	 * Gets the dominance-based rough set calculator used to calculate approximations of this union.
	 * 
	 * @return the dominance-based rough set calculator used to calculate approximations of this union
	 */
	@Override
	public DominanceBasedRoughSetCalculator getRoughSetCalculator() {
		return (DominanceBasedRoughSetCalculator)roughSetCalculator;
	}
	
	/**
	 * Calculates the negative region of this union.
	 * This region is composed of objects belonging to the positive region of the complementary union, but not to the positive region of this union.
	 * 
	 * @return the negative region of this union
	 */
	@Override
	protected IntSet calculateNegativeRegion() {
		IntSet complementaryUnionPositiveRegion = this.getComplementaryUnion().getPositiveRegion();
		IntSet positiveRegion = this.getPositiveRegion();
		
		IntSet negativeRegion = new IntOpenHashSet();
		
		IntIterator iterator = complementaryUnionPositiveRegion.iterator();
		int objectIndex;
		
		while (iterator.hasNext()) {
			objectIndex = iterator.nextInt();
			if (!positiveRegion.contains(objectIndex)) {
				negativeRegion.add(objectIndex);
			}
		}
		
		return negativeRegion;
	}
	
	/**
	 * Tests if this union is concordant with given decision. In case of an upward union, returns:<br>
	 * - {@link TernaryLogicValue#TRUE} if limiting decision of this union is at most as good as the given decision,<br>
	 * - {@link TernaryLogicValue#FALSE} if limiting decision of this union	is strictly better than the given decision,<br>
	 * - {@link TernaryLogicValue#UNCOMPARABLE} otherwise.<br> 
	 * In case of a downward union, return:<br>
	 * - {@link TernaryLogicValue#TRUE} if limiting decision of this union is at least as good as the given decision,<br>
	 * - {@link TernaryLogicValue#FALSE} if limiting decision of this union	is strictly worse than the given decision,<br>
	 * - {@link TernaryLogicValue#UNCOMPARABLE} otherwise.
	 * 
	 * @param decision decision that limiting decision of this union should be compared with
	 * @return {@link TernaryLogicValue#TRUE} if this unions' limiting decision is concordant with given decision,
	 *         {@link TernaryLogicValue#FALSE} if this unions' limiting decision is not concordant with given decision,
	 *         {@link TernaryLogicValue#UNCOMPARABLE} if this unions' limiting decision is uncomparable with given decision
	 * 
	 * @throws NullPointerException if given decision is {@code null}
	 */
	@Override
	protected TernaryLogicValue isConcordantWithDecision(Decision decision) {
		notNull(decision, "Decision tested for concordance with union is null.");
		
		if (this.includeLimitingDecision) { //"normal" union
			switch (this.unionType) {
			case AT_LEAST:
				if (this.limitingDecision.isAtMostAsGoodAs(decision) == TernaryLogicValue.TRUE) {
					return TernaryLogicValue.TRUE;
				} else {
					//limiting decision is strictly better (as equality was eliminated above)
					if (this.limitingDecision.isAtLeastAsGoodAs(decision) == TernaryLogicValue.TRUE) {
						return TernaryLogicValue.FALSE;
					} else { //union's limiting decision is uncomparable with given decision 
						return TernaryLogicValue.UNCOMPARABLE;
					}
				}
			case AT_MOST:
				if (this.limitingDecision.isAtLeastAsGoodAs(decision) == TernaryLogicValue.TRUE) {
					return TernaryLogicValue.TRUE;
				} else {
					//limiting decision is strictly worse (as equality was eliminated above)
					if (this.limitingDecision.isAtMostAsGoodAs(decision) == TernaryLogicValue.TRUE) {
						return TernaryLogicValue.FALSE;
					} else { //union's limiting decision is uncomparable with given decision 
						return TernaryLogicValue.UNCOMPARABLE;
					}
				}
			default:
				throw new InvalidValueException("Unexpected union type."); //this should not happen
			}
		} else { //"strict union"
			switch (this.unionType) {
			case AT_LEAST:
				if (this.limitingDecision.isAtLeastAsGoodAs(decision) == TernaryLogicValue.TRUE) { //includes equality of decisions
					return TernaryLogicValue.FALSE;
				} else {
					//limiting decision is strictly worse (as equality was eliminated above)
					if (this.limitingDecision.isAtMostAsGoodAs(decision) == TernaryLogicValue.TRUE) {
						return TernaryLogicValue.TRUE;
					} else { //union's limiting decision is uncomparable with given decision 
						return TernaryLogicValue.UNCOMPARABLE;
					}
				}
			case AT_MOST:
				if (this.limitingDecision.isAtMostAsGoodAs(decision) == TernaryLogicValue.TRUE) { //includes equality of decisions
					return TernaryLogicValue.FALSE;
				} else {
					//limiting decision is strictly better (as equality was eliminated above)
					if (this.limitingDecision.isAtLeastAsGoodAs(decision) == TernaryLogicValue.TRUE) {
						return TernaryLogicValue.TRUE;
					} else { //union's limiting decision is uncomparable with given decision 
						return TernaryLogicValue.UNCOMPARABLE;
					}
				}
			default:
				throw new InvalidValueException("Unexpected union type."); //this should not happen
			}
		}
	}
	
	/**
	 * Tells if given decision is positive with respect to this union, i.e., in case of an upward union - limiting decision of this union is at most as good as the given decision,
	 * and in case of a downward union - limiting decision of this union is at least as good as the given decision.
	 * 
	 * @param decision decision to verify for being positive with respect to this union
	 * @return {@code true} if given decision is positive with respect to this union,
	 *         {@code false} otherwise
	 * 
	 * @throws NullPointerException if given decision is {@code null}
	 */
	@Override
	public boolean isDecisionPositive(Decision decision) {
		notNull(decision, "Decision tested for being positive with respect to union is null.");
		
		return this.isConcordantWithDecision(decision) == TernaryLogicValue.TRUE;
	}
	
	/**
	 * Tells if given decision is negative with respect to this union, i.e., in case of an upward union - limiting decision of this union is strictly better than the given decision,
	 * and in case of a downward union - limiting decision of this union is strictly worse than the given decision.
	 * 
	 * @param decision decision to verify for being negative with respect to this union
	 * @return {@code true} if given decision is negative with respect to this union,
	 *         {@code false} otherwise
	 * 
	 * @throws NullPointerException if given decision is {@code null}
	 */
	@Override
	public boolean isDecisionNegative(Decision decision) {
		notNull(decision, "Decision tested for being negative with respect to union is null.");
		
		return this.isConcordantWithDecision(decision) == TernaryLogicValue.FALSE;
	}
	
	/**
	 * Tells if given decision is neutral with respect to this union, i.e., limiting decision of this union is neither at most as good as the given decision nor at least as good as the given decision.
	 * 
	 * @param decision decision to verify for being neutral with respect to this union
	 * @return {@code true} if given decision is neutral with respect to this union,
	 *         {@code false} otherwise
	 * 
	 * @throws NullPointerException if given decision is {@code null}
	 */
	protected boolean isDecisionNeutral(Decision decision) {
		notNull(decision, "Decision tested for being neutral with union is null.");
		
		return this.isConcordantWithDecision(decision) == TernaryLogicValue.UNCOMPARABLE;
	}
	
	/**
	 * Gets the information table for which this union was defined.
	 * 
	 * @return the information table for which this union was defined
	 */
	public InformationTableWithDecisionDistributions getInformationTable() {
		return (InformationTableWithDecisionDistributions)informationTable;
	}

//	/**
//	 * Gets indices of objects from the information table such that this union's limiting decision is uncomparable with their decision.
//	 * Limiting decision is considered to be uncomparable with a particular decision, if it is neither at least as good as nor at most as good as that decision.
//	 * 
//	 * @return indices of objects from the information table such that this union's limiting decision is uncomparable with their decision
//	 */
//	@Override
//	public IntSortedSet getUncomparableObjects() {
//		return this.uncomparableObjects; //uncomparable objects have been calculated in class constructor, so just return them
//	}
	
//	/**
//	 * Tells if this union's limiting decision is uncomparable with decision of a particular object from the information table.
//	 * 
//	 * @param objectNumber index of an object from the information table
//	 * @return {@code true} if this union's limiting decision is uncomparable with decision assigned to the object with given index,
//	 *         {@code false} otherwise
//	 */
//	public boolean objectIsUncomparable(int objectNumber) {
//		return this.uncomparableObjects.contains(objectNumber);
//	}
	
	/**
	 * Tells if given object is positive with respect to this union.
	 * 
	 * @param objectNumber index of an object from the information table
	 * @return {@code true} if object with given number is positive with respect to this union,
	 *         {@code false} otherwise
	 */
	@Override
	public boolean isObjectPositive(int objectNumber) {
		return this.objects.contains(objectNumber);
	}
	
	/**
	 * Tells if given object is neutral with respect to this union.
	 * 
	 * @param objectNumber index of an object from the information table
	 * @return {@code true} if object with given number is neutral with respect to this union,
	 *         {@code false} otherwise
	 */
	protected boolean isObjectNeutral(int objectNumber) {
		return this.neutralObjects.contains(objectNumber);
	}
	
	/**
	 * Tells if given object is negative with respect to this union.
	 * 
	 * @param objectNumber index of an object from the information table
	 * @return {@code true} if object with given number is negative with respect to this union,
	 *         {@code false} otherwise
	 */
	@Override
	public boolean isObjectNegative(int objectNumber) {
		return !this.objects.contains(objectNumber) &&
				!this.neutralObjects.contains(objectNumber);
	}

	/**
	 * Calculates lower approximation of this union, using the dominance-based rough set calculator.
	 * 
	 * @return set of indices of objects belonging to the lower approximation of this union, calculated using the dominance-based rough set calculator
	 */
	@Override
	protected IntSortedSet calculateLowerApproximation() {
		return this.getRoughSetCalculator().calculateLowerApproximation(this);
	}

	/**
	 * Calculates upper approximation of this union, using the dominance-based rough set calculator.
	 * 
	 * @return set of indices of objects belonging to the upper approximation of this union, calculated using the dominance-based rough set calculator
	 */
	@Override
	protected IntSortedSet calculateUpperApproximation() {
		return this.getRoughSetCalculator().calculateUpperApproximation(this);
	}
	
	/**
	 * Calculates positive region of this union, using the given lower approximation.
	 * This region is composed of objects belonging to the lower approximation of this union plus
	 * objects belonging to dominance cones defined with respect to the objects from the lower approximation.
	 * 
	 * @return set of indices of objects belonging to the positive region of this union, calculated using given lower approximation
	 * @throws NullPointerException if given lower approximation is {@code null}
	 */
	@Override
	protected IntSet calculatePositiveRegion(IntSortedSet lowerApproximation) {
		notNull(lowerApproximation, "Lower approximation for calculation of positive region is null.");
		IntSet positiveRegion = new IntOpenHashSet(lowerApproximation.size()); //use estimation of the size of calculated positive region
		
		IntBidirectionalIterator iterator = lowerApproximation.iterator();
		int objectIndex;
		IntSortedSet dominanceCone;
		
		while (iterator.hasNext()) {
			objectIndex = iterator.nextInt();
		
			switch (this.getUnionType()) {
			case AT_LEAST:
				dominanceCone = DominanceConeCalculator.INSTANCE.calculatePositiveInvDCone(objectIndex, this.informationTable); //SIC! hardcoded type of dominance cone
				break;
			case AT_MOST:
				dominanceCone = DominanceConeCalculator.INSTANCE.calculateNegativeDCone(objectIndex, this.informationTable); //SIC! hardcoded type of dominance cone
				break;
			default:
				throw new InvalidValueException("Unexpected union type."); //this should not happen
			}
			
			positiveRegion.addAll(dominanceCone);
		}
		
		return positiveRegion;
	}
	
	/**
	 * Gets the size of the set of objects that is complementary to the set of objects belonging to this union.
	 * The result is calculated as the number of all objects in the information tables minus number of objects belonging to this union,
	 * and minus number of objects that are neutral with respect to this union.
	 * 
	 * @return the size of the set of objects that is complementary to the set of (positive) objects belonging to this union
	 */
	public int getComplementarySetSize() {
		return this.informationTable.getNumberOfObjects() - this.size() - this.neutralObjects.size();
	}

	/**
	 * Gets list of elementary decisions associated with this union, each dependent on the type of this union and one of the evaluations contributing to the limiting decision.
	 * Assumes that all the contributing evaluations are of type {@link SimpleField}.
	 * 
	 * @return list of elementary decisions associated with this union, each dependent on the type of this union and one of the evaluations contributing to the limiting decision
	 * @throws InvalidTypeException if any evaluation contributing to the limiting decision is not of type {@link SimpleField}
	 */
	@Override
	public List<SimpleCondition> getElementaryDecisions() {
		IntSet attributeIndices = this.limitingDecision.getAttributeIndices();
		SimpleField evaluation;
		EvaluationAttribute attribute;
		int attributeIndex;
		List<SimpleCondition> elementaryDecisions = new ArrayList<>();
		
		IntIterator attributeIndicesIterator = attributeIndices.iterator();
		
		while (attributeIndicesIterator.hasNext()) {
			attributeIndex = attributeIndicesIterator.nextInt();
			attribute = (EvaluationAttribute)this.informationTable.getAttribute(attributeIndex);
			try {
				evaluation = (SimpleField)this.limitingDecision.getEvaluation(attributeIndex);
			} catch (ClassCastException exception) {
				throw new InvalidTypeException("Evaluation contributing to union's limiting decision is not a simple field.");
			}
			
			switch (this.unionType) {
			case AT_LEAST:
				switch (attribute.getPreferenceType()) {
				case GAIN:
					elementaryDecisions.add(new SimpleConditionAtLeast(
							new EvaluationAttributeWithContext(attribute, attributeIndex), evaluation));
					break;
				case COST:
					elementaryDecisions.add(new SimpleConditionAtMost(
							new EvaluationAttributeWithContext(attribute, attributeIndex), evaluation));
					break;
				case NONE:
					elementaryDecisions.add(new SimpleConditionEqual(
							new EvaluationAttributeWithContext(attribute, attributeIndex), evaluation));
					break;
				}
				break;
			case AT_MOST:
				switch (attribute.getPreferenceType()) {
				case GAIN:
					elementaryDecisions.add(new SimpleConditionAtMost(
							new EvaluationAttributeWithContext(attribute, attributeIndex), evaluation));
					break;
				case COST:
					elementaryDecisions.add(new SimpleConditionAtLeast(
							new EvaluationAttributeWithContext(attribute, attributeIndex), evaluation));
					break;
				case NONE:
					elementaryDecisions.add(new SimpleConditionEqual(
							new EvaluationAttributeWithContext(attribute, attributeIndex), evaluation));
					break;
				}
				break;
			}
		}
		
		//arrange elementary decisions in order
		elementaryDecisions.sort((SimpleCondition x, SimpleCondition y) -> {
			int i = x.getAttributeWithContext().getAttributeIndex();
			int j = y.getAttributeWithContext().getAttributeIndex();
			if (i < j) {
				return -1;
			} else {
				return (i == j) ? 0 : 1;
			}
		});
		
		return elementaryDecisions;
	}

}
