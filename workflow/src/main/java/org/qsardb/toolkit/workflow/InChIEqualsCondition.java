/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.workflow;

import java.util.*;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.condition.*;

public class InChIEqualsCondition implements Condition {

	private String left = null;

	private String right = null;


	@Override
	public boolean eval() throws BuildException {
		String left = getLeft();
		if(left == null){
			throw new BuildException("Attribute \"left\" not set");
		}

		String right = getRight();
		if(right == null){
			throw new BuildException("Attribute \"right\" not set");
		}

		Map<String, String> leftLayers = parseLayers(left);
		Map<String, String> rightLayers = parseLayers(right);

		for(String name : Arrays.asList(CHEMICAL_FORMULA, "c", "h")){

			if(!layerEquals(name, leftLayers.get(name), rightLayers.get(name))){
				return false;
			}
		}

		for(String name : Arrays.asList("b", "t")){

			if(!stereoLayerEquals(name, leftLayers.get(name), rightLayers.get(name))){
				return false;
			}
		}

		for(String name : Arrays.asList("m", "s")){

			if(!stereoFlagLayerEquals(name, leftLayers.get(name), rightLayers.get(name))){
				return false;
			}
		}

		return true;
	}

	public String getLeft(){
		return this.left;
	}

	public void setLeft(String left){
		this.left = left;
	}

	public String getRight(){
		return this.right;
	}

	public void setRight(String right){
		this.right = right;
	}

	static
	private boolean layerEquals(String name, String left, String right){

		if(left == null || right == null){
			return (left == right);
		}

		List<String> leftFragments;
		List<String> rightFragments;

		if(CHEMICAL_FORMULA.equals(name)){
			leftFragments = stripMultipliers(splitFragments(left, "."), false);
			rightFragments = stripMultipliers(splitFragments(right, "."), false);
		} else

		{
			leftFragments = stripMultipliers(splitFragments(left, ";"), true);
			rightFragments = stripMultipliers(splitFragments(right, ";"), true);
		}

		return (leftFragments.get(0)).equals(rightFragments.get(0));
	}

	static
	private boolean stereoLayerEquals(String name, String left, String right){
		Map<String, String> leftParts = parseLayerParts(left);
		Map<String, String> rightParts = parseLayerParts(right);

		if(left == null){
			return (right == null || isUndefined(rightParts));
		} // End if

		if(right == null){
			return (left == null || isUndefined(leftParts));
		} // End if

		Set<String> centres = new LinkedHashSet<String>();
		centres.addAll(leftParts.keySet());
		centres.addAll(rightParts.keySet());

		centres:
		for(String centre : centres){
			String leftValue = leftParts.get(centre);
			String rightValue = rightParts.get(centre);

			if(leftValue == null){

				if(rightValue == null || isUndefined(rightValue)){
					continue centres;
				}

				return false;
			} // End if

			if(rightValue == null){

				if(leftValue == null || isUndefined(leftValue)){
					continue centres;
				}

				return false;
			} // End if

			if(!(leftValue).equals(rightValue)){

				if(isUndefined(leftValue) || isUndefined(rightValue)){
					continue centres;
				}

				return false;
			}
		}

		return true;
	}

	static
	private boolean stereoFlagLayerEquals(String name, String left, String right){

		if(left == null || right == null){
			return true;
		}

		return (left).equals(right);
	}

	static
	private Map<String, String> parseLayers(String string){
		Map<String, String> result = new LinkedHashMap<String, String>();

		StringTokenizer st = new StringTokenizer(string, "/");

		result.put(VERSION, st.nextToken());
		result.put(CHEMICAL_FORMULA, st.nextToken());

		while(st.hasMoreTokens()){
			String layer = st.nextToken();

			String name = layer.substring(0, 1);
			String value = layer.substring(1);

			if(!result.containsKey(name)){
				result.put(name, value);
			}
		}

		return result;
	}

	static
	private Map<String, String> parseLayerParts(String string){
		Map<String, String> result = new LinkedHashMap<String, String>();

		if(string == null){
			return result;
		}

		StringTokenizer st = new StringTokenizer(string, ",");

		while(st.hasMoreElements()){
			String part = st.nextToken();

			result.put(part.substring(0, part.length() - 2), part.substring(part.length() - 2));
		}

		return result;
	}

	static
	private List<String> splitFragments(String string, String separator){
		List<String> result = new ArrayList<String>();

		StringTokenizer st = new StringTokenizer(string, separator);

		while(st.hasMoreTokens()){
			String fragment = st.nextToken();

			result.add(fragment);
		}

		return result;
	}

	static
	private List<String> stripMultipliers(List<String> strings, boolean asterisk){
		List<String> result = new ArrayList<String>();

		for(String string : strings){

			if(asterisk){
				int index = string.indexOf('*');

				if(index > -1){
					string = string.substring(index + 1);
				}
			} else

			{
				digits:
				while(true){
					char c = string.charAt(0);

					if(Character.isDigit(c)){
						string = string.substring(1);

						continue digits;
					}

					break digits;
				}
			}

			result.add(string);
		}

		return result;
	}

	static
	private boolean isUndefined(String value){
		return (value != null) && value.endsWith("?");
	}

	static
	private boolean isUndefined(Map<String, String> parts){
		Collection<String> values = parts.values();

		for(String value : values){

			if(!isUndefined(value)){
				return false;
			}
		}

		return true;
	}

	private static final String VERSION = ".version";
	private static final String CHEMICAL_FORMULA = ".chemical_formula";
}