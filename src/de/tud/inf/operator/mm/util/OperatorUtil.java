/*
 * $Id$
 * 
 * Copyright (c) 2008 Advanced Mask Technology Center GmbH & Co. KG Raehnitzer Allee 9, D-01109 Dresden, Germany All
 * Rights Reserved.
 * 
 * This software is the confidential and proprietary information of Advanced Mask Technology Center GmbH & Co. KG.
 * (AMTC). You shall not disclose such Confidential Information and shall use it only in accordance with the terms of
 * the agreement you entered into with AMTC.
 */

package de.tud.inf.operator.mm.util;

import java.util.LinkedList;
import java.util.List;

import de.tud.inf.operator.mm.CASSelector;
import de.tud.inf.operator.mm.ConvexHullSelector;
import de.tud.inf.operator.mm.DiversitySelector;
import de.tud.inf.operator.mm.JointCriterionSelector;
import de.tud.inf.operator.mm.QualitySelector;


/**
 * 
 * 
 * @version $Revision$
 * @author Andre Jaehnig
 */
public class OperatorUtil {

   public static List<String> getSelectorNames() {
      List<String> list = new LinkedList<String>();

      // QualitySelector
      list.add("Quality");

      // DiversityQuality
      list.add("Diversity");

      // JointCriterionSelector
      list.add("Joint Criterion");

      // CASSelector
      list.add("Cluster and Select");

      // ConvexHullSelector
      list.add("Convex Hull");

      return list;
   }
   
   public static String getSelectorName(String className) {
      if (className.equals(QualitySelector.class.getSimpleName()) || className.equals(QualitySelector.class.getName())) {
         return "Quality";
      }
      else if (className.equals(DiversitySelector.class.getSimpleName()) || className.equals(DiversitySelector.class.getName())) {
         return "Diversity";
      }
      else if (className.equals(JointCriterionSelector.class.getSimpleName()) || className.equals(JointCriterionSelector.class.getName())) {
         return "Joint Criterion";
      }
      else if (className.equals(CASSelector.class.getSimpleName()) || className.equals(CASSelector.class.getName())) {
         return "Cluster and Select";
      }
      else if (className.equals(ConvexHullSelector.class.getSimpleName()) || className.equals(ConvexHullSelector.class.getName())) {
         return "Convex Hull";
      }
      
      return null;
   }

   public static String getSelectorClassName(String description) {
      if (description.equals("Quality")) {
         return QualitySelector.class.getSimpleName();
      }
      else if (description.equals("Diversity")) {
         return DiversitySelector.class.getSimpleName();
      }
      else if (description.equals("Joint Criterion")) {
         return JointCriterionSelector.class.getSimpleName();
      }
      else if (description.equals("Cluster and Select")) {
         return CASSelector.class.getSimpleName();
      }
      else if (description.equals("Convex Hull")) {
         return ConvexHullSelector.class.getSimpleName();
      }
      
      return null;
   }
   
   public static String getFullSelectorClassName(String description) {
      if (description.equals("Quality")) {
         return QualitySelector.class.getName();
      }
      else if (description.equals("Diversity")) {
         return DiversitySelector.class.getName();
      }
      else if (description.equals("Joint Criterion")) {
         return JointCriterionSelector.class.getName();
      }
      else if (description.equals("Cluster and Select")) {
         return CASSelector.class.getName();
      }
      else if (description.equals("Convex Hull")) {
         return ConvexHullSelector.class.getName();
      }
      
      return null;
   }
}
