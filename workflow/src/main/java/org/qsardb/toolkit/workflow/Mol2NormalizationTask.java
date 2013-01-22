/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.workflow;

import java.util.*;

abstract
public class Mol2NormalizationTask extends FileUpdateTask {

	abstract
	public void normalize(Map<String, AtomDef> atoms, Map<String, BondDef> bonds);

	@Override
	public List<String> update(List<String> lines){
		Map<String, AtomDef> atoms = new LinkedHashMap<String, AtomDef>();

		List<String> atomLines = getBlock(lines, "ATOM");
		for(String atomLine : atomLines){
			AtomDef atom = new AtomDef(atomLine);

			atoms.put(atom.getId(), atom);
		}

		Map<String, BondDef> bonds = new LinkedHashMap<String, BondDef>();

		List<String> bondLines = getBlock(lines, "BOND");
		for(String bondLine : bondLines){
			BondDef bond = new BondDef(bondLine);

			bonds.put(bond.getId(), bond);
		}

		normalize(atoms, bonds);

		for(AtomDef atom : atoms.values()){

			if(atom.isUpdated()){
				lines = update(lines, atom);
			}
		}

		for(BondDef bond : bonds.values()){

			if(bond.isUpdated()){
				lines = update(lines, bond);
			}
		}

		return lines;
	}

	static
	private List<String> update(List<String> lines, Line line){
		String text = line.getText();

		int index = lines.indexOf(text);
		if(index < 0){
			throw new IllegalArgumentException();
		}

		String updatedText = Line.join(line.getParts());

		lines.set(index, updatedText);

		return lines;
	}

	static
	private List<String> getBlock(List<String> lines, String name){
		int begin = -1;
		int end = -1;

		lines:
		for(int i = 0; i < lines.size(); i++){
			String line = lines.get(i);

			if(line.startsWith("@<TRIPOS>")){

				if(line.equals("@<TRIPOS>" + name)){
					begin = i;

					continue lines;
				} // End if

				if(begin > -1){
					end = i;

					break lines;
				}
			}
		}

		if(begin == -1){
			throw new IllegalArgumentException("Block \"" + name + "\" not found");
		}

		return lines.subList(begin + 1, (end > -1 ? end : lines.size()));
	}

	static
	public class Line {

		private String text = null;

		private List<String> parts = null;

		private boolean updated = false;


		public Line(String text){
			List<String> parts = split(text);

			setText(text);
			setParts(parts);
		}

		public String getPart(int index){
			List<String> parts = getParts();

			return parts.get(index);
		}

		public void setPart(int index, String part){
			List<String> parts = getParts();

			parts.set(index, part);

			setUpdated(true);
		}

		public String getText(){
			return this.text;
		}

		public void setText(String text){
			this.text = text;
		}

		public List<String> getParts(){
			return this.parts;
		}

		public void setParts(List<String> parts){
			this.parts = parts;
		}

		public boolean isUpdated(){
			return this.updated;
		}

		public void setUpdated(boolean updated){
			this.updated = updated;
		}

		static
		private List<String> split(String string){
			List<String> result = new ArrayList<String>();

			StringTokenizer st = new StringTokenizer(string);

			while(st.hasMoreTokens()){
				result.add(st.nextToken());
			}

			return result;
		}

		static
		private String join(List<String> parts){
			StringBuffer sb = new StringBuffer();

			sb.append(SPACE);

			for(String part : parts){
				sb.append(part);

				sb.append(SPACE);
			}

			return sb.toString();
		}
	}

	static
	public class AtomDef extends Line {

		public AtomDef(String text){
			super(text);
		}

		public boolean isElement(String element){
			return (getElement()).equals(element);
		}

		public String getId(){
			return getPart(0);
		}

		public String getName(){
			return getPart(1);
		}

		public String getElement(){
			String type = getType();

			int dot = type.indexOf('.');
			if(dot > -1){
				return type.substring(0, dot);
			}

			return type;
		}

		public String getType(){
			return getPart(5);
		}

		public void setType(String type){
			setPart(5, type);
		}
	}

	static
	public class BondDef extends Line {

		public BondDef(String text){
			super(text);
		}

		public String getId(){
			return getPart(0);
		}

		public String getFrom(){
			return getPart(1);
		}

		public String getTo(){
			return getPart(2);
		}

		public String getOrder(){
			return getPart(3);
		}

		public void setOrder(String order){
			setPart(3, order);
		}
	}

	private static final String SPACE = " ";
}