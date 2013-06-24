package test;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

public class test {
	public static void main(String[] args){
		double[] xData = new double[100];
		double[] yData = new double[100];
		
		for(int i=0;i<100;i++){
			xData[i] = i;
			yData[i] = i;
		}
		
		double correlation = new PearsonsCorrelation().correlation(xData, yData);
		System.out.println("Correlation is "+correlation);
	}
}
