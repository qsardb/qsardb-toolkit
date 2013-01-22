/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import java.io.*;

import net.sf.jniinchi.*;

import org.openscience.cdk.*;
import org.openscience.cdk.exception.*;
import org.openscience.cdk.inchi.*;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.io.*;
import org.openscience.cdk.smiles.*;

public class CdkUtil {

	private CdkUtil(){
	}

	static
	public IAtomContainer parseInChI(String inChI) throws CDKException {
		InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();

		InChIToStructure converter = factory.getInChIToStructure(inChI, DefaultChemObjectBuilder.getInstance());

		INCHI_RET status = converter.getReturnStatus();
		switch(status){
			case OKAY:
				break;
			default:
				throw new CDKException("Invalid InChI");
		}

		IAtomContainer atomContainer = converter.getAtomContainer();

		return new Molecule(atomContainer);
	}

	static
	public IAtomContainer parseMolfile(InputStream is) throws CDKException {
		MDLReader reader = new MDLReader(is);

		return reader.read(new Molecule());
	}

	static
	public IAtomContainer parseSMILES(String smiles) throws CDKException {
		SmilesParser parser = new SmilesParser(DefaultChemObjectBuilder.getInstance());

		return parser.parseSmiles(smiles);
	}
}