/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.workflow;

import java.util.*;

public class Mol2NitroNormalizationTask extends Mol2NormalizationTask {

	@Override
	public void normalize(Map<String, AtomDef> atoms, Map<String, BondDef> bonds){
		Map<String, Nitro> nitros = new LinkedHashMap<String, Nitro>();

		for(AtomDef atom : atoms.values()){

			if(atom.isElement("N")){
				nitros.put(atom.getId(), new Nitro(atom));
			}
		}

		for(BondDef bond : bonds.values()){
			String from = bond.getFrom();
			String to = bond.getTo();

			Nitro fromNitro = nitros.get(from);
			if(fromNitro != null){
				AtomDef toAtom = atoms.get(to);

				if(toAtom.isElement("O")){
					fromNitro.addOxygen(bond, toAtom);
				}
			}

			Nitro toNitro = nitros.get(to);
			if(toNitro != null){
				AtomDef fromAtom = atoms.get(from);

				if(fromAtom.isElement("O")){
					toNitro.addOxygen(bond, fromAtom);
				}
			}
		}

		for(Nitro nitro : nitros.values()){
			nitro.normalize();
		}
	}

	static
	private class Nitro {

		private AtomDef nitrogen = null;

		private BondDef firstBond = null;

		private AtomDef firstOxygen = null;

		private BondDef secondBond = null;

		private AtomDef secondOxygen = null;


		public Nitro(AtomDef nitrogen){
			setNitrogen(nitrogen);
		}

		public void addOxygen(BondDef bond, AtomDef oxygen){

			if(getFirstOxygen() == null){
				setFirstBond(bond);
				setFirstOxygen(oxygen);
			} else

			if(getSecondOxygen() == null){
				setSecondBond(bond);
				setSecondOxygen(oxygen);
			} else

			{
				throw new IllegalArgumentException();
			}
		}

		public void normalize(){
			BondDef firstBond = getFirstBond();
			BondDef secondBond = getSecondBond();

			if(firstBond == null || secondBond == null){
				return;
			} // End if

			if((firstBond.getOrder()).equals("1")){
				firstBond.setOrder("2");
			} // End if

			if((secondBond.getOrder()).equals("1")){
				secondBond.setOrder("2");
			}
		}

		public AtomDef getNitrogen(){
			return this.nitrogen;
		}

		public void setNitrogen(AtomDef nitrogen){
			this.nitrogen = nitrogen;
		}

		public BondDef getFirstBond(){
			return this.firstBond;
		}

		public void setFirstBond(BondDef firstBond){
			this.firstBond = firstBond;
		}

		public AtomDef getFirstOxygen(){
			return this.firstOxygen;
		}

		public void setFirstOxygen(AtomDef firstOxygen){
			this.firstOxygen = firstOxygen;
		}

		public BondDef getSecondBond(){
			return this.secondBond;
		}

		public void setSecondBond(BondDef secondBond){
			this.secondBond = secondBond;
		}

		public AtomDef getSecondOxygen(){
			return this.secondOxygen;
		}

		public void setSecondOxygen(AtomDef secondOxygen){
			this.secondOxygen = secondOxygen;
		}
	}
}