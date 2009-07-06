

package de.tud.inf.operator.mm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import Jama.Matrix;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;

import de.tud.inf.operator.mm.util.SortingIndex;


/**
 * This class calculates the convex hull of a given input set.
 * 
 * {@link http://www.ddj.com/architect/201806315}
 * 
 * @version $Revision$
 * @author Andre Jaehnig
 */
public class ConvexHullCalculator extends Operator {

   /************************************************************************************************
    * FIELDS
    ***********************************************************************************************/

   /** Column name with the indicator of the selected items. */
   public static final String CONVEX_HULL_MEMBER_COLUMN_NAME = "ch_member";

   /************************************************************************************************
    * GETTER & SETTER
    ***********************************************************************************************/

   /*
    * (non-Javadoc)
    * 
    * @see com.rapidminer.operator.Operator#getInputClasses()
    */
   @Override
   public Class<?>[] getInputClasses() {
      return new Class[] { ExampleSet.class };
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.rapidminer.operator.Operator#getOutputClasses()
    */
   @Override
   public Class<?>[] getOutputClasses() {
      return new Class[] { ExampleSet.class };
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.rapidminer.operator.Operator#getParameterTypes()
    */
   @Override
   public List<ParameterType> getParameterTypes() {
      List<ParameterType> types = super.getParameterTypes();
      types.add(new ParameterTypeString(CONVEX_HULL_MEMBER_COLUMN_NAME,
            "Column name with the indicator of the selected items.", "ch_member"));

      return types;
   }

   /************************************************************************************************
    * CONSTRUCTOR
    ***********************************************************************************************/

   /**
    * Constructor
    * 
    * @param description
    */
   public ConvexHullCalculator(OperatorDescription description) {
      super(description);
   }

   /************************************************************************************************
    * PUBLIC METHODS
    ***********************************************************************************************/

   /*
    * (non-Javadoc)
    * 
    * @see com.rapidminer.operator.Operator#apply()
    */
   @Override
   public IOObject[] apply() throws OperatorException {
      // get example set
      ExampleSet exampleSet = this.getInput(ExampleSet.class);
      int exampleSetSize = exampleSet.size();
      this.logNote("Input example-set has " + exampleSetSize + " elements.");

      // get parameters
      String chColumnName = this.getParameterAsString(CONVEX_HULL_MEMBER_COLUMN_NAME);

      // create attribute for the convex hull indicator
      Attribute chMemberAttr = AttributeFactory.createAttribute(chColumnName, Ontology.NOMINAL);
      exampleSet.getExampleTable().addAttribute(chMemberAttr);

      // add attribute to view
      exampleSet.getAttributes().setSpecialAttribute(chMemberAttr, CONVEX_HULL_MEMBER_COLUMN_NAME);

      // get first attribute as sort attribute and all attributes as dimensions
      Attribute sortAttr = null;
      int dimAttributesCount = exampleSet.getAttributes().size();
      Attribute[] dimAttributes = new Attribute[dimAttributesCount];
      boolean first = true;
      int counter = 0;
      for (Attribute attr : exampleSet.getAttributes()) {
         if (first) {
            sortAttr = attr;
            first = false;
         }
         dimAttributes[counter++] = attr;
      }

      // sort example set according to the sort attribute and set indicator to false
      List<SortingIndex> sortingIndex = new ArrayList<SortingIndex>(exampleSetSize);
      counter = 0;
      Iterator<Example> it = exampleSet.iterator();
      while (it.hasNext()) {
         Example example = it.next();
         sortingIndex.add(new SortingIndex(Double.valueOf(example.getNumericalValue(sortAttr)), counter));
         counter++;

         // indicator to default value false
         example.setValue(chMemberAttr, "false");
      }
      Collections.sort(sortingIndex);

      /*
       * put all data into a stack
       * 
       * one stack entry is a vector with the id of the example set and the dimension-attribute
       * values
       */
      Stack<List<Double>> points = new Stack<List<Double>>();
      Iterator<SortingIndex> k = sortingIndex.iterator();
      while (k.hasNext()) {
         int index = k.next().getIndex();
         Example example = exampleSet.getExample(index);
         List<Double> entry = new ArrayList<Double>();
         // id
         entry.add((double) index);
         // dimension values
         for (int i = 0; i < dimAttributesCount; i++) {
            entry.add(example.getValue(dimAttributes[i]));
         }

         // add to stack
         points.push(entry);
      }

      Stack<List<Double>> hull = this.buildHull(points);

      for (List<Double> list : hull) {
         exampleSet.getExample(list.get(0).intValue()).setValue(chMemberAttr, "true");
      }

      return new IOObject[] { exampleSet };
   }

   /************************************************************************************************
    * PRIVATE METHODS
    ***********************************************************************************************/

   private Stack<List<Double>> buildHull(Stack<List<Double>> rawPoints) {

      /************************************************************************************************
       * STEP 1
       * 
       * The initial array of points is stored in the stack points. They are sorted according to
       * their first dimension, which gives us the far left and far right points of the hull. These
       * are special values, and they are stored off separately in the left and right value.
       * 
       * Then we go through the list of points, and one by one determine whether each point is above
       * or below the line formed by the right and left points. If it is above, the point is moved
       * into the upperPartitionPoints sequence. If it is below, the point is moved into the
       * lowerPartitionPoints sequence.
       ***********************************************************************************************/
      List<Double> left = rawPoints.firstElement();
      rawPoints.removeElement(rawPoints.firstElement());
      List<Double> right = rawPoints.lastElement();
      rawPoints.removeElement(rawPoints.lastElement());

      Stack<List<Double>> upperPartitionPoints = new Stack<List<Double>>();
      Stack<List<Double>> lowerPartitionPoints = new Stack<List<Double>>();

      for (int i = 0; i < rawPoints.size(); i++) {
         List<Double> point = rawPoints.get(i);
         double dir = getDirection(left, right, point);
         if (dir < 0) {
            upperPartitionPoints.push(point);
         }
         else {
            lowerPartitionPoints.push(point);
         }
      }

      /************************************************************************************************
       * STEP 2
       * 
       * Building the hull consists of two procedures: building the lower and then the upper hull.
       * The two procedures are nearly identical - the main difference between the two is the test
       * for convexity. When building the upper hull, our rule is that the middle point must always
       * be *above* the line formed by its two closest neighbors. When building the lower hull, the
       * rule is that point must be *below* its two closest neighbors. We pass this information to
       * the building routine as the last parameter, which is either -1 or 1.
       ***********************************************************************************************/
      Stack<List<Double>> lowerHull = this.buildHalfHull(left, right, lowerPartitionPoints, 1);
      Stack<List<Double>> upperHull = this.buildHalfHull(left, right, upperPartitionPoints, -1);

      /*
       * The convex hull is created, the lower hull and upper hull are stored in sorted sequences.
       * There is a bit of duplication between the two, because both sets include the leftmost and
       * rightmost point.
       */

      Stack<List<Double>> hull = new Stack<List<Double>>();
      // add all of the lower hull
      hull.addAll(lowerHull);
      // remove last one (the most right one)
      hull.removeElement(hull.lastElement());
      // add all but the most left one from the upper hull
      upperHull.removeElement(upperHull.firstElement());
      hull.addAll(upperHull);

      return hull;
   }

   /**
    * This is the method that builds either the upper or the lower half convex hull. It takes as its
    * input a sorted list of points in one of the two halfs. It produces as output a list of the
    * points in the corresponding convex hull.
    * 
    * The factor should be 1 for the lower hull, and -1 for the upper hull.
    * 
    * @param left
    * @param right
    * @param partitionPoints
    * @param factor
    * @return
    */
   private Stack<List<Double>> buildHalfHull(List<Double> left, List<Double> right,
         Stack<List<Double>> partitionPoints, int factor) {

      Stack<List<Double>> halfHull = new Stack<List<Double>>();

      /*
       * The hull will always start with the left point, and end with the right point. According, we
       * start by adding the left point as the first point in the output sequence, and make sure the
       * right point is the last point in the input sequence.
       */
      halfHull.push(left);
      partitionPoints.push(right);

      // The construction loop runs until the input is exhausted
      while (partitionPoints.size() != 0) {
         /*
          * Repeatedly add the leftmost point to the hull, then test to see if a convexity violation
          * has occurred. If it has, fix things up by removing the next-to-last point in the output
          * sequence until convexity is restored.
          */
         halfHull.push(partitionPoints.firstElement());
         partitionPoints.removeElement(partitionPoints.firstElement());
         while (halfHull.size() >= 3) {
            int endPos = halfHull.size() - 1;
            if (factor * this.getDirection(halfHull.get(endPos - 2), halfHull.get(endPos), halfHull.get(endPos - 1)) <= 0) {
               halfHull.removeElement(halfHull.get(halfHull.indexOf(halfHull.firstElement()) + endPos - 1));
            }
            else {
               break;
            }
         }
      }

      return halfHull;
   }

   /**
    * In this program we frequently want to look at three consecutive points, p0, p1, and p2, and
    * determine whether p2 has taken a turn to the left or a turn to the right.
    * 
    * We can do this by by translating the points so that p0 is at the origin, then taking the cross
    * product of p1 and p2. The result will be positive, negative, or 0, meaning respectively that
    * p2 has turned right, left, or is on a straight line.
    * 
    * {@link ls2-www.cs.uni-dortmund.de/lehre/winter200304/dap2ergseminar/vortr/alggeo.ppt}
    * 
    * @param p0
    * @param p1
    * @param p2
    * @return
    */
   private double getDirection(List<Double> p0, List<Double> p1, List<Double> p2) {
      // Matrix with p0-p1 and p2-p1 as columns
      int rows = p0.size() - 1;
      Matrix matrix = new Matrix(rows, 2);
      for (int r = 0; r < rows; r++) {
         matrix.set(r, 0, p0.get(r + 1) - p1.get(r + 1));
         matrix.set(r, 1, p2.get(r + 1) - p1.get(r + 1));
      }

      return matrix.det();
   }

}
