/*
 * $Id$
 * 
 * Copyright (c) 2008 Advanced Mask Technology Center GmbH & Co. KG Raehnitzer Allee 9, D-01109
 * Dresden, Germany All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of Advanced Mask Technology Center
 * GmbH & Co. KG. (AMTC). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the agreement you entered into with AMTC.
 */

package de.tud.inf.operator.mm.util;


/**
 * Originally its a private class of one of the RapidMiner-classes.
 * 
 * @version $Revision$
 * @author Andre Jaehnig
 */
public class SortingIndex implements Comparable<SortingIndex> {

   private Object key;

   private int index;

   public SortingIndex(Object key, int index) {
      this.key = key;
      this.index = index;
   }

   public int hashCode() {
      if (key instanceof Double) {
         return ((Double) key).hashCode();
      }
      else if (key instanceof String) {
         return ((String) key).hashCode();
      }
      else {
         return 42;
      }
   }

   public boolean equals(Object other) {
      if (!(other instanceof SortingIndex))
         return false;
      SortingIndex o = (SortingIndex) other;
      if (key instanceof Double) {
         return ((Double) key).equals(o.key);
      }
      else if (key instanceof String) {
         return ((String) key).equals(o.key);
      }
      return true;
   }

   public int compareTo(SortingIndex o) {
      if (key instanceof Double) {
         return ((Double) key).compareTo((Double) o.key);
      }
      else if (key instanceof String) {
         return ((String) key).compareTo((String) o.key);
      }
      return 0;
   }

   public int getIndex() {
      return index;
   }

   public String toString() {
      return key + " --> " + index;
   }
}
