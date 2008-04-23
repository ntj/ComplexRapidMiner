/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.gui.tools;

/**
 * Contains information about the different parts of a version number.
 *
 * @author Ingo Mierswa
 * @version $Id: VersionNumber.java,v 1.3 2007/06/24 14:30:58 ingomierswa Exp $
 */
public class VersionNumber implements Comparable<VersionNumber> {

	private int majorNumber;
	
	private int minorNumber;
	
	private int patchLevel;
	
	private boolean beta;
	
	private int betaNumber;
	
	public VersionNumber(int majorNumber, int minorNumber, int patchLevel, boolean beta, int betaNumber) {
		this.majorNumber = majorNumber;
		this.minorNumber = minorNumber;
		this.patchLevel  = patchLevel;
		this.beta        = beta;
		this.betaNumber  = betaNumber;
	}

	public boolean equals(Object o) {
		if (!(o instanceof VersionNumber))
			return false;
		VersionNumber other = (VersionNumber)o;
		return 
		   this.majorNumber == other.majorNumber &&
		   this.minorNumber == other.minorNumber &&
		   this.patchLevel  == other.patchLevel  &&
		   this.beta        == other.beta        &&
		   this.betaNumber  == other.betaNumber;
	}
	
	public int hashCode() {
		return 
		    Double.valueOf(this.majorNumber).hashCode() ^
		    Double.valueOf(this.minorNumber).hashCode() ^
		    Double.valueOf(this.patchLevel).hashCode() ^
		    Boolean.valueOf(beta).hashCode() ^
		    Double.valueOf(this.betaNumber).hashCode();
	}
	
	public int compareTo(VersionNumber o) {
		int index = Double.compare(this.majorNumber, o.majorNumber);
		if (index != 0) {
			return index;
		} else {
			index = Double.compare(this.minorNumber, o.minorNumber);
			if (index != 0) {
				return index;
			} else {
				index = Double.compare(this.patchLevel, o.patchLevel);
				if (index != 0) {
					return index;
				} else {
					if (this.beta && !o.beta) {
						return -1;
					} else if (!this.beta && o.beta) {
						return 1;
					} else if (!this.beta && !o.beta) {
						return 0;
					} else {
						return Double.compare(this.betaNumber, o.betaNumber);
					}
				}
			}
		}
	}
	
	public String toString() {
		return majorNumber + "." + minorNumber + "." + patchLevel + (beta ? ("beta" + (betaNumber >= 2 ? betaNumber + "" : "")) : ""); 
	}
}
