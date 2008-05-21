import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.rapidminer.RapidMiner;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.learner.Learner;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.tools.GroupTree;
import com.rapidminer.tools.OperatorService;

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

/**
 * 
 * @author Ingo Mierswa
 * @version $Id: OperatorListCreator.java,v 1.1 2007/09/25 11:05:07 ingomierswa Exp $
 */
public class OperatorListCreator {

    public static void main(String[] argv) throws IOException {
        RapidMiner.init();
        
        GroupTree tree = OperatorService.getGroups();
        PrintWriter out = new PrintWriter(new FileWriter(argv[0]));
        printGroup(out, "", tree);
        //printMainGroups(out, tree);
        out.close();
    }

    public static void printMainGroups(PrintWriter out, GroupTree tree) {
        Set<OperatorDescription> descriptions = tree.getOperatorDescriptions();
        List<OperatorDescription> descriptionsList = new LinkedList<OperatorDescription>(descriptions);
        Collections.sort(descriptionsList);
        for (OperatorDescription description : descriptionsList) {
            out.println(description.getName());
        }

        List<GroupTree> subgroups = new LinkedList<GroupTree>(tree.getSubGroups());
        Collections.sort(subgroups);
        for (GroupTree subtree : subgroups) {
            out.println("--------------------------");
            out.println(subtree.getName());
            out.println("--------------------------");

            descriptions = subtree.getAllOperatorDescriptions();
            descriptionsList = new LinkedList<OperatorDescription>(descriptions);
            Collections.sort(descriptionsList);
            for (OperatorDescription description : descriptionsList) {
                out.println(description.getName());
            }
        }
    }
    
    public static void printGroup(PrintWriter out, String parentName, GroupTree tree) {
        out.println("--------------------------");
        if (parentName.length() > 0)
            out.println(parentName + "." + tree.getName());
        else
            out.println(tree.getName());
        out.println("--------------------------");
        Set<OperatorDescription> descriptions = tree.getOperatorDescriptions();
        List<OperatorDescription> descriptionsList = new LinkedList<OperatorDescription>(descriptions);
        Collections.sort(descriptionsList);
        for (OperatorDescription description : descriptionsList) {
            out.print(description.getName());
            Operator op = null;
            try {
                op = description.createOperatorInstance();
            } catch (OperatorCreationException e1) {
                e1.printStackTrace();
            }

            if (op != null) {
                if (op instanceof Learner) {
                    Learner learner = (Learner) op;
                    StringBuffer learnerCapabilities = new StringBuffer();
                    Iterator<LearnerCapability> i = LearnerCapability.getAllCapabilities().iterator();
                    boolean first = true;
                    while (i.hasNext()) {
                        LearnerCapability capability = i.next();
                        try {
                            if (learner.supportsCapability(capability)) {
                                if (!first)
                                    learnerCapabilities.append(", ");
                                learnerCapabilities.append(capability.getDescription());
                                first = false;
                            }
                        } catch (Exception e) {
                            break;
                        }
                    }
                    String result = learnerCapabilities.toString();
                    if (result.length() > 0) {
                        out.print("  [" + result + "]");
                    }
                }
            }
            out.println();
        }
        
        List<GroupTree> subgroups = new LinkedList<GroupTree>(tree.getSubGroups());
        Collections.sort(subgroups);
        for (GroupTree subtree : subgroups) {
            if (parentName.length() > 0)
                printGroup(out, parentName + "." + tree.getName(), subtree);
            else
                printGroup(out, tree.getName(), subtree);
        }
    }
}
