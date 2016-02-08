package com.uthtechnologies.fuzon.weka;

import java.util.List;

import com.uthtechnologies.fuzon.weka.impl.TrainModel;
import com.uthtechnologies.fuzon.weka.impl.RegressionModel;

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
