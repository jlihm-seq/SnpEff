package org.snpeff.fileIterator;

import java.io.IOException;
import java.util.Iterator;

import org.broad.igv.bbfile.BBFileHeader;
import org.broad.igv.bbfile.BBFileReader;
import org.broad.igv.bbfile.BedFeature;
import org.snpeff.interval.Variant;
import org.snpeff.interval.VariantWithScore;
import org.snpeff.util.Gpr;

/**
 * FileIterator for BigBed features
 *
 * Note: I use Broad's IGV code to do all the work, this is just a wrapper
 *
 * @author pablocingolani
 */
public class BigBedFileIterator extends VariantFileIterator {

	BBFileReader readerBb;
	BBFileHeader bbFileHdr;
	Iterator<BedFeature> iterator;
	String label;

	public BigBedFileIterator(String fileName) {
		super(null);
		inOffset = 0;
		open(fileName);
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public Variant next() {
		return readNext();
	}

	/**
	 * Open file
	 * @param fileName
	 */
	void open(String fileName) {
		try {
			label = Gpr.removeExt(Gpr.baseName(fileName));

			// Open file
			BBFileReader readerBb = new BBFileReader(fileName);
			BBFileHeader bbFileHdr = readerBb.getBBFileHeader(); // Get header
			if (!bbFileHdr.isHeaderOK()) throw new IOException("Bad header for file '" + fileName + "'"); // Sanity check
			if (!bbFileHdr.isBigBed()) throw new RuntimeException("Unrecognized header type for file '" + fileName + "' (expecting BigBed)"); // Get file type

			iterator = readerBb.getBigBedIterator();
		} catch (Exception e) {
			throw new RuntimeException("Error loading file '" + fileName + "'.", e);
		}
	}

	@Override
	protected Variant readNext() {
		// Get next item
		BedFeature f = iterator.next();
		if (f == null) return null;

		// Create an ID
		String id = label + ":" + (f.getStartBase() + 1) + "_" + f.getEndBase(); // Show as one-based coordinates

		// Get score
		String restOfFields[] = f.getRestOfFields();
		double score = Gpr.parseDoubleSafe(restOfFields[1]);

		// Create variant
		Variant variant = new VariantWithScore(getChromosome(f.getChromosome()), f.getStartBase(), f.getEndBase() - 1, id, score);

		return variant;
	}

}
