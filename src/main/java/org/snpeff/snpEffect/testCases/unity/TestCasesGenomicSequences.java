package org.snpeff.snpEffect.testCases.unity;

import java.io.File;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.snpeff.binseq.GenomicSequences;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Marker;
import org.snpeff.snpEffect.Config;
import org.snpeff.util.Gpr;
import org.snpeff.util.GprSeq;

/**
 * Test case
 *
 */
public class TestCasesGenomicSequences {

	boolean verbose = false;

	public TestCasesGenomicSequences() {
	}

	/**
	 * Count all "sequence*.bin" files in 'dir'
	 */
	int countSequenceBinFiles(String dir) {
		int count = 0;
		for (String fn : (new File(dir)).list()) {
			if (fn.startsWith("sequence") && fn.endsWith(".bin")) {
				count++;
				if (verbose) Gpr.debug("Found file (" + count + "): " + fn);
			}
		}

		return count;
	}

	/**
	 * Delete all sequence*.bin files in 'dir'
	 */
	void deleteAllBinFiles(String dir) {
		for (File f : (new File(dir)).listFiles()) {
			String fn = f.getName();
			if (fn.startsWith("sequence") && fn.endsWith(".bin")) {
				if (verbose) Gpr.debug("Deleting file: " + f.getAbsolutePath());
				f.delete();
			}
		}
	}

	/**
	 * Create many small chromosomes, check that they are saved in only one sequences.bin file
	 * Test that we can recover the exact sequences
	 */
	@Test
	public void test_01() {
		Gpr.debug("Test");
		int numberOfChromos = 1000;

		//---
		// Initialize
		//---
		String genomeVer = "test_too_many_chrs";
		Config config = new Config(genomeVer);
		Genome genome = config.getGenome();
		GenomicSequences gs = new GenomicSequences(genome);
		gs.setVerbose(verbose);

		// Remove all previous sequence*.bin files
		String dir = config.getDirDataGenomeVersion();
		deleteAllBinFiles(dir);
		Assert.assertEquals("Sequence files have not been deleted from " + dir, 0, countSequenceBinFiles(dir));

		//---
		// Create random chromosome sequences
		//---
		String chrSeqs[] = new String[numberOfChromos];
		Random random = new Random(20151124);
		for (int i = 0; i < numberOfChromos; i++) {
			String chrSeq = GprSeq.randSequence(random, 100);
			chrSeqs[i] = chrSeq;
			gs.addChromosomeSequence("chr" + i, chrSeq);
		}

		gs.save(config);

		// Count number of sequence*.bin files
		int countSeqFiles = countSequenceBinFiles(config.getDirDataGenomeVersion());
		Assert.assertEquals("Unexpected number of sequence*.bin files", 1, countSeqFiles);

		//---
		// Check that we can recover the sequences from the file
		// Note that we need to initialize a new set of objects to avoid fetching
		// sequences from the previous ones
		//---
		Config configRead = new Config(genomeVer);
		Genome genomeRead = configRead.getGenome();
		GenomicSequences gsRead = new GenomicSequences(genomeRead);
		for (int i = 0; i < numberOfChromos; i++) {
			Chromosome chr = genome.getChromosome("chr" + i);
			Marker marker = new Marker(chr, 0, chrSeqs[i].length() - 1);
			String seqRead = gsRead.querySequence(marker);
			if (verbose) Gpr.debug("Query marker: " + marker + ", sequence: " + seqRead);
			Assert.assertEquals("Chromosome sequences do not match", chrSeqs[i].toUpperCase(), seqRead.toUpperCase());
		}
	}

	/**
	 * Create many small chromosomes and a few large ones.
	 * Check that all small chromosomes are saved in only one sequences.bin file
	 * and the large ones are saved into separate files
	 * Test that we can recover the exact sequences
	 */
	@Test
	public void test_02() {
		Gpr.debug("Test");
		int numberOfLargeChromos = 10;
		int numberOfChromos = 1000;
		int longChrLen = GenomicSequences.CHR_LEN_SEPARATE_FILE + 1;
		int shortChrLen = 100;

		//---
		// Initialize
		//---
		String genomeVer = "test_too_many_chrs";
		Config config = new Config(genomeVer);
		Genome genome = config.getGenome();
		GenomicSequences gs = new GenomicSequences(genome);
		gs.setVerbose(verbose);

		// Remove all previous sequence*.bin files
		String dir = config.getDirDataGenomeVersion();
		deleteAllBinFiles(dir);
		Assert.assertEquals("Sequence files have not been deleted from " + dir, 0, countSequenceBinFiles(dir));

		//---
		// Create random chromosome sequences
		//---
		String chrSeqs[] = new String[numberOfChromos];
		Random random = new Random(20151124);
		for (int i = 0; i < numberOfChromos; i++) {
			String chrSeq = "";
			if (i < numberOfLargeChromos) chrSeq = GprSeq.randSequence(random, longChrLen);
			else chrSeq = GprSeq.randSequence(random, shortChrLen);

			chrSeqs[i] = chrSeq;
			gs.addChromosomeSequence("chr" + i, chrSeq);
		}

		gs.save(config);

		// Count number of sequence*.bin files
		int countSeqFiles = countSequenceBinFiles(config.getDirDataGenomeVersion());
		Assert.assertEquals("Unexpected number of sequence*.bin files", (numberOfLargeChromos + 1), countSeqFiles);

		//---
		// Check that we can recover the sequences from the file
		// Note that we need to initialize a new set of objects to avoid fetching
		// sequences from the previous ones
		//---
		Config configRead = new Config(genomeVer);
		Genome genomeRead = configRead.getGenome();
		GenomicSequences gsRead = new GenomicSequences(genomeRead);
		for (int i = 0; i < numberOfChromos; i++) {
			Chromosome chr = genome.getChromosome("chr" + i);
			Marker marker = new Marker(chr, 0, chrSeqs[i].length() - 1);
			String seqRead = gsRead.querySequence(marker);
			if (verbose) Gpr.debug("Query marker: " + marker + ", sequence: " + seqRead);
			Assert.assertEquals("Chromosome sequences do not match", chrSeqs[i].toUpperCase(), seqRead.toUpperCase());
		}
	}

}
