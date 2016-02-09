package com.reactivetechnologies.analytics.weka;

import java.util.List;

import com.reactivetechnologies.analytics.weka.impl.RegressionModel;
import com.reactivetechnologies.analytics.weka.impl.TrainModel;

public interface Regression
{
  /**
   * 
   * @param nextInstance
   * @throws Exception
   */
	void incrementModel(TrainModel nextInstance) throws Exception;
	/**
	 * 
	 * @return
	 */
	RegressionModel generateModelSnapshot();
	/**
	 * 
	 * @param models
	 * @return
	 */
	RegressionModel ensembleModels(List<RegressionModel> models);
}
