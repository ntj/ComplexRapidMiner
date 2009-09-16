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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ho.yaml.Yaml;

/**
 * 
 * 
 * @version $Revision$
 * @author Andre Jaehnig
 */
public class MetaConfig {

	public static MetaConfig load(String fileName) {
		MetaConfig mc = null;
		try {
			mc = Yaml.loadType(new File(fileName), MetaConfig.class);
			mc.sourceFileName = fileName;
		} catch (FileNotFoundException e) {
			throw new Error(e.getMessage());
		}

		return mc;
	}

	private Integer aggregationClusterCount;

	private String aggregationColumnName;

	private String aggregationFileName;

	private List<String> classifyingAttributeNames;

	private String clusteringColumnPrefix;

	private Integer clusteringCount;

	private Map<String, ClusteringInfo> clusteringInfo = new HashMap<String, ClusteringInfo>();

	private String dataFileName;

	private String ensembleFileName;

	private String idColumnName;

	private String nmiFileName;

	private Boolean nmiNormalized = false;

	private String selectorFileName;

	private String selectorUsedForAggregation;

	private Boolean snmiAdded = false;

	private String snmiColumnName;

	private String sourceFileName;

	/**
	 * @return Returns the aggregationClusterCount.
	 */
	public Integer getAggregationClusterCount() {
		return aggregationClusterCount;
	}

	/**
	 * @return Returns the aggregationColumnName.
	 */
	public String getAggregationColumnName() {
		return aggregationColumnName;
	}

	/**
	 * @return Returns the aggregationFileName.
	 */
	public String getAggregationFileName() {
		return aggregationFileName;
	}

	/**
	 * @return Returns the classifyingAttributeNames.
	 */
	public List<String> getClassifyingAttributeNames() {
		return classifyingAttributeNames;
	}

	/**
	 * @return Returns the clusteringColumnPrefix.
	 */
	public String getClusteringColumnPrefix() {
		return clusteringColumnPrefix;
	}

	/**
	 * @return Returns the clusteringCount.
	 */
	public Integer getClusteringCount() {
		return clusteringCount;
	}

	/**
	 * @return Returns the clusteringInfo.
	 */
	public Map<String, ClusteringInfo> getClusteringInfo() {
		return clusteringInfo;
	}

	/**
	 * @return Returns the dataFileName.
	 */
	public String getDataFileName() {
		return dataFileName;
	}

	/**
	 * @return Returns the ensembleFileName.
	 */
	public String getEnsembleFileName() {
		return ensembleFileName;
	}

	/**
	 * @return Returns the idColumnName.
	 */
	public String getIdColumnName() {
		return idColumnName;
	}

	/**
	 * @return Returns the nmiFileName.
	 */
	public String getNmiFileName() {
		return nmiFileName;
	}

	/**
	 * @return Returns the nmiNormalized.
	 */
	public Boolean getNmiNormalized() {
		return nmiNormalized;
	}

	/**
	 * @return Returns the selectorFileName.
	 */
	public String getSelectorFileName() {
		return selectorFileName;
	}

	/**
	 * @return Returns the selectorUsedForAggregation.
	 */
	public String getSelectorUsedForAggregation() {
		return selectorUsedForAggregation;
	}

	/**
	 * @return Returns the snmiAdded.
	 */
	public Boolean getSnmiAdded() {
		return snmiAdded;
	}

	/**
	 * @return Returns the snmiColumnName.
	 */
	public String getSnmiColumnName() {
		return snmiColumnName;
	}

	/**
	 * @return Returns the sourceFileName.
	 */
	public String getSourceFileName() {
		return sourceFileName;
	}

	public void save() {
		try {
			File f = new File(sourceFileName);
			if (f.exists() == false) {
				try {
					f.mkdirs();
				
					f.createNewFile();
				} catch (Exception e) {
					throw new Error(e.getMessage());
				}
			}
			Yaml.dump(this, f);
		} catch (FileNotFoundException e) {
			throw new Error(e.getMessage());
		}
	}

	public void save(String fileName) {
		this.sourceFileName = fileName;
		this.save();
	}

	/**
	 * @param aggregationClusterCount
	 *            The aggregationClusterCount to set.
	 */
	public void setAggregationClusterCount(Integer aggregationClusterCount) {
		this.aggregationClusterCount = aggregationClusterCount;
	}

	/**
	 * @param aggregationColumnName
	 *            The aggregationColumnName to set.
	 */
	public void setAggregationColumnName(String aggregationColumnName) {
		this.aggregationColumnName = aggregationColumnName;
	}

	/**
	 * @param aggregationFileName
	 *            The aggregationFileName to set.
	 */
	public void setAggregationFileName(String aggregationFileName) {
		this.aggregationFileName = aggregationFileName;
	}

	/**
	 * @param classifyingAttributeNames
	 *            The classifyingAttributeNames to set.
	 */
	public void setClassifyingAttributeNames(
			List<String> classifyingAttributeNames) {
		this.classifyingAttributeNames = classifyingAttributeNames;
	}

	/**
	 * @param clusteringColumnPrefix
	 *            The clusteringColumnPrefix to set.
	 */
	public void setClusteringColumnPrefix(String clusteringColumnPrefix) {
		this.clusteringColumnPrefix = clusteringColumnPrefix;
	}

	/**
	 * @param clusteringCount
	 *            The clusteringCount to set.
	 */
	public void setClusteringCount(Integer clusteringCount) {
		this.clusteringCount = clusteringCount;
	}

	/**
	 * @param clusteringInfo
	 *            The clusteringInfo to set.
	 */
	public void setClusteringInfo(Map<String, ClusteringInfo> clusteringInfo) {
		this.clusteringInfo = clusteringInfo;
	}

	/**
	 * @param dataFileName
	 *            The dataFileName to set.
	 */
	public void setDataFileName(String dataFileName) {
		this.dataFileName = dataFileName;
	}

	/**
	 * @param ensembleFileName
	 *            The ensembleFileName to set.
	 */
	public void setEnsembleFileName(String ensembleFileName) {
		this.ensembleFileName = ensembleFileName;
	}

	/**
	 * @param idColumnName
	 *            The idColumnName to set.
	 */
	public void setIdColumnName(String idColumnName) {
		this.idColumnName = idColumnName;
	}

	/**
	 * @param nmiFileName
	 *            The nmiFileName to set.
	 */
	public void setNmiFileName(String nmiFileName) {
		this.nmiFileName = nmiFileName;
	}

	/**
	 * @param nmiNormalized
	 *            The nmiNormalized to set.
	 */
	public void setNmiNormalized(Boolean nmiNormalized) {
		this.nmiNormalized = nmiNormalized;
	}

	/**
	 * @param selectorFileName
	 *            The selectorFileName to set.
	 */
	public void setSelectorFileName(String selectorFileName) {
		this.selectorFileName = selectorFileName;
	}

	/**
	 * @param selectorUsedForAggregation
	 *            The selectorUsedForAggregation to set.
	 */
	public void setSelectorUsedForAggregation(String selectorUsedForAggregation) {
		this.selectorUsedForAggregation = selectorUsedForAggregation;
	}

	/**
	 * @param snmiAdded
	 *            The snmiAdded to set.
	 */
	public void setSnmiAdded(Boolean snmiAdded) {
		this.snmiAdded = snmiAdded;
	}

	/**
	 * @param snmiColumnName
	 *            The snmiColumnName to set.
	 */
	public void setSnmiColumnName(String snmiColumnName) {
		this.snmiColumnName = snmiColumnName;
	}

}
