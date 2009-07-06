/*
 * $Id$
 *
 * Copyright (c) 2008 Advanced Mask Technology Center GmbH & Co. KG
 * Raehnitzer Allee 9, D-01109 Dresden, Germany
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Advanced Mask Technology Center GmbH & Co. KG. (AMTC).
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the agreement you entered into with AMTC.
 */

package de.tud.inf.operator.mm.util;

 /**
 * 
 *
 * @version $Revision$
 * @author Andre Jaehnig
 */
public class ClusteringInfo {
   
   /**
    * @return Returns the sampleSize.
    */
   public Integer getSampleSize() {
      return sampleSize;
   }
   /**
    * @param sampleSize The sampleSize to set.
    */
   public void setSampleSize(Integer sampleSize) {
      this.sampleSize = sampleSize;
   }
   /**
    * @return Returns the selectedColumnName.
    */
   public String getSelectedColumnName() {
      return selectedColumnName;
   }
   /**
    * @param selectedColumnName The selectedColumnName to set.
    */
   public void setSelectedColumnName(String selectedColumnName) {
      this.selectedColumnName = selectedColumnName;
   }
   /**
    * @return Returns the infoColumnName.
    */
   public String getInfoColumnName() {
      return infoColumnName;
   }
   /**
    * @param infoColumnName The infoColumnName to set.
    */
   public void setInfoColumnName(String infoColumnName) {
      this.infoColumnName = infoColumnName;
   }
   private Integer sampleSize;
   private String selectedColumnName;
   private String infoColumnName;
   
   public ClusteringInfo() {
      
   }
}
