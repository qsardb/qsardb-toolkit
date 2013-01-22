/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.toolkit.curation;

import java.io.*;
import java.util.regex.*;

import chemaxon.formats.*;
import chemaxon.marvin.calculations.*;
import chemaxon.marvin.io.formats.name.nameimport.*;
import chemaxon.naming.*;
import chemaxon.struc.*;

public class MarvinUtil {

	private MarvinUtil(){
	}

	/**
	 * @throws CurationException If the identity changes during conversion.
	 */
	static
	public String nameToIUPACName(String name) throws Exception {
		String[] parsedName = split(name);

		Molecule molecule = parseName(parsedName[0]);

		parsedName[0] = formatName(molecule);

		Molecule nameMolecule = parseName(parsedName[0]);
		if(!(formatInChI(molecule)).equals(formatInChI(nameMolecule))){
			throw new CurationException("InChI conflict", molecule, nameMolecule);
		}

		return merge(parsedName);
	}

	/**
	 * @throws CurationException If the identity changes during conversion.
	 */
	static
	public String nameToInChI(String name) throws Exception {
		Molecule molecule = parseName(name);

		String inChI = formatInChI(molecule);

		Molecule inChIMolecule = parseInChI(inChI);
		if(!(formatName(molecule)).equals(formatName(inChIMolecule))){
			throw new CurationException("IUPAC name conflict", molecule, inChIMolecule);
		}

		return inChI;
	}

	static
	public Molecule parseName(String name) throws Exception {
		NameConverter converter = new SystematicNameConverter();

		try {
			String[] parsedName = split(name);

			return converter.convert(parsedName[0]);
		} catch(NameFormatException nfe){
			System.err.println(nfe);

			throw nfe;
		}
	}

	static
	public String formatName(Molecule molecule) throws Exception {
		IUPACNamingPlugin namingPlugin = new IUPACNamingPlugin();
		namingPlugin.setMolecule(molecule);
		namingPlugin.run();

		return namingPlugin.getPreferredIUPACName();
	}

	static
	public Molecule parseInChI(String inChI) throws Exception {

		if(inChI == null || ("").equals(inChI.trim())){
			return null;
		}

		return MolImporter.importMol(inChI);
	}

	static
	public String formatInChI(Molecule molecule) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream(512);

		try {
			MolExporter exporter = new MolExporter(os, "inchi:AuxNone");
			exporter.write(molecule);

			String inChI = os.toString("US-ASCII");

			// Remove trailing whitespace
			inChI = inChI.trim();

			return inChI;
		} finally {
			os.close();
		}
	}

	static
	public boolean isChanged(String left, String right){
		return (left == null && right != null) || (left != null && right == null) || !(left).equals(right);
	}

	static
	private String[] split(String string){

		if(string.indexOf('(') > -1){
			Matcher matcher = pattern.matcher(string);

			if(matcher.matches()){
				return new String[]{matcher.group(1), matcher.group(2)};
			}
		}

		return new String[]{string, null};
	}

	static
	private String merge(String[] strings){

		if(strings[1] != null){
			return strings[0] + " (" + strings[1] + ")";
		}

		return strings[0];
	}

	private static final Pattern pattern = Pattern.compile("(.*)\\s+\\((.*)\\)");
}