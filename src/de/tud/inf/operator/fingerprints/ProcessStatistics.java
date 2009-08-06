package de.tud.inf.operator.fingerprints;


public class ProcessStatistics {
	
	private static ProcessStatistics instance = null;
	
	int sumFingerprintStringLength;
	int countFingerprintStringLength;
	
	int sumNumLetters;
	int countNumLetters;
	
	int sumNumSymbolVectors;
	int countNumSymbolVectors;
	
	private ProcessStatistics() {
		sumFingerprintStringLength = 0;
		countFingerprintStringLength = 0;
		
		sumNumLetters = 0;
		countNumLetters = 0;
		
		sumNumSymbolVectors = 0;
		countNumSymbolVectors = 0;
	}
	
	public static ProcessStatistics getInstance() {
		if (instance == null)
			instance =  new ProcessStatistics();
		return instance;
	}
	
	public void reset() {
		sumFingerprintStringLength = 0;
		countFingerprintStringLength = 0;
		
		sumNumLetters = 0;
		countNumLetters = 0;
		
		sumNumSymbolVectors = 0;
		countNumSymbolVectors = 0;
	}
	
	public void addFingerprintStringLength(int x) {
		sumFingerprintStringLength += x;
		countFingerprintStringLength++;
	}
	
	public double getAverageFingerprintStringLength() {
		return (double) sumFingerprintStringLength/(double) countFingerprintStringLength;
	}
	
	public void addNumLetters(int x) {
		sumNumLetters += x;
		countNumLetters++;
	}
	
	public double getAverageNumLetters() {
		return (double) sumNumLetters/(double) countNumLetters;
	}
	
	public void addNumSymbolVectors(int x) {
		sumNumSymbolVectors += x;
		countNumSymbolVectors++;
	}
	
	public double getAverageNumSymbolVectors() {
		return (double)sumNumSymbolVectors/(double)countNumSymbolVectors;
	}

}
