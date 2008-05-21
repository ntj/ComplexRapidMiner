/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2008 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.operator.learner.clustering.constrained.constraints;

import com.rapidminer.operator.learner.clustering.Cluster;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.FlatClusterModel;

/**
 * Models Must-Link- or Cannot-Link-constraints
 * 
 * @author Alexander Daxenberger
 * @version $Id: LinkClusterConstraint.java,v 1.6 2008/05/09 19:23:17 ingomierswa Exp $
 */
public class LinkClusterConstraint implements ClusterConstraint {

    public static final byte MUST_LINK = 0;
    public static final byte CANNOT_LINK = 1;

    private String id0;
    private String id1;
    private double weight;
    private byte type;
    private int hashCode;

    public LinkClusterConstraint(String id0, String id1, double weight, byte type) {
        try {
            if (Double.parseDouble(id0) > Double.parseDouble(id1)) {
                this.id0 = id1;
                this.id1 = id0;
            } else {
                this.id0 = id0;
                this.id1 = id1;
            }
        } catch (Exception e) {
            this.id0 = id0;
            this.id1 = id1;
        }
        this.weight = weight;
        this.type = type;
        this.hashCode = this.toString().hashCode();
    }

    public String getId0() {
        return this.id0;
    }

    public String getId1() {
        return this.id1;
    }

    public String getOtherId(String id) {
        if (this.id0.equals(id))
            return this.id1;
        else if (this.id1.equals(id))
            return this.id0;
        else
            return null;
    }

    public byte getType() {
        return this.type;
    }

    public boolean constraintViolated(ClusterModel clusterModel) {
        FlatClusterModel cm;
        Cluster c;
        Cluster c0 = null;
        Cluster c1 = null;

        if (clusterModel instanceof FlatClusterModel) {
            cm = (FlatClusterModel)clusterModel;
            for (int i=0; i < cm.getNumberOfClusters(); i++) {
                c = cm.getClusterAt(i);
                if ((c0 == null) && (c.contains(this.id0))) c0 = c;
                if ((c1 == null) && (c.contains(this.id1))) c1 = c;
            }
            if (this.type == MUST_LINK)
                return (c0 != c1);
            else
                return (c0 == c1);
        }

        return false;
    }

    public boolean constraintViolated(Cluster cluster) {
        if (this.type == MUST_LINK)
            return (cluster.contains(this.id0) != cluster.contains(this.id1));
        else
            return (cluster.contains(this.id0) && cluster.contains(this.id1));
    }

    /**
     * Returns true, if this constraint will be violated if the given object id
     * is added to the cluster.
     * 
     * @param id
     * @param cluster
     */
    public boolean constraintViolatedIfAdded(String id, Cluster cluster) {
        if (this.type == MUST_LINK)
            return ((cluster.contains(this.id0) || (this.id0.equals(id))) != (cluster.contains(this.id1) || (this.id1.equals(id))));
        else
            return ((cluster.contains(this.id0) || (this.id0.equals(id))) && (cluster.contains(this.id1) || (this.id1.equals(id))));
    }

    public double getConstraintWeight(ClusterModel clusterModel) {
        return this.weight;
    }

    public boolean equals(Object o) {
        LinkClusterConstraint lct;

        if (o instanceof LinkClusterConstraint) {
            lct = (LinkClusterConstraint)o;
            if (this.type == lct.getType()) {
                if ((this.id0.equals(lct.getId0()) && this.id1.equals(lct.getId1())) ||
                        (this.id0.equals(lct.getId1()) && this.id1.equals(lct.getId0())))
                    return true;
            }
        }

        return false;
    }

    public int hashCode() {
        return this.hashCode;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(this.id0);
        if (this.type == MUST_LINK)
            sb.append("---");
        else
            sb.append("-X-");
        sb.append(this.id1);

        return sb.toString();
    }

    public LinkClusterConstraint clone() {
        return new LinkClusterConstraint(this.id0, this.id1, this.weight, this.type);
    }
}
