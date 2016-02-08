package com.uthtechnologies.fuzon.weka.utils;

public class Metrics extends weka.experiment.Stats
{
	
	@Override
	public String toString()
	{
		return "Metrics [\nrootMeanSq=" + rootMeanSq + ", \nstdDev=" + stdDev
				+ ", \nmean=" + mean + ", \nmin=" + min + ", \nmax=" + max + "]";
	}

	public double rootMeanSq = Double.NaN;
	public double meanSq = Double.NaN;
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -4335736091259042736L;
	
	@Override
	public void calculateDerived()
	{
		super.calculateDerived();
		meanSq = sumSq / count;
		rootMeanSq = Math.sqrt(meanSq);
	}

}
