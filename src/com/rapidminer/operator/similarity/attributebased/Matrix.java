package com.rapidminer.operator.similarity.attributebased;


/**
 * Simple implementation of a matrix.
 * 
 * @author 
 * @see com.rapidminer.operator.learner.clustering.clusterer.uncertain.FDBScanClustering
 */
public class Matrix {

	//m rows (index: i)
	private int m;

	//n columns (index: j)
	private int n;

	//mxn matrix
	private int[][] matrix;


	/**
	 * constructs a mxn matrix
	 */
	public Matrix(int m, int n) {
		this.m = m;
		this.n = n;
		matrix = new int[m][n];
		reset();
	}
	
	/**
	 * constructs a quadratic matrix (m=n)
	 */
	public Matrix(int dim) {
		this.m = dim;
		this.n = dim;
		matrix = new int[dim][dim];
		reset();
	}

	/**
	 * gets value of row i and column j
	 */
	public int getValue(int i, int j) {
		return matrix[i][j];
	}

	/**
	 * sets value of row i and column j
	 */
	public void setValue(int i, int j, int value) {
		matrix[i][j] = value;
	}

	/**
	 * increments value of row i and column j
	 */
	public void inc(int i, int j) {
		matrix[i][j]++;
	}

	/**
	 * decrements value of row i and column j
	 */
	public void dec(int i, int j) {
		matrix[i][j]--;
	}

	public void reset() {
		for(int i=0; i<m; i++) {
			for(int j=0; j<n; j++)
				setValue(i, j, 0);
		}
	}

	public void reset(int value) {
		for(int i=0; i<m; i++) {
			for(int j=0; j<n; j++)
				setValue(i, j, value);
		}
	}

	public String toString() {
		String s = new String();
		for(int i=0; i<m; i++) {
			for(int j=0; j<n; j++) {
				s += "[" + getValue(i, j) + "]";
			}
		s += "\n";
		}
	return s;
	}
	
}