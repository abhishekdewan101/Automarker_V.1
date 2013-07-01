package plotting;

import java.sql.Connection;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

public class plot_summary_parameters_table_count {

	Connection databaseConnection = null;
	String database;
	double [] xData;
	double [] yData;
	int [] imageCount;
	int [] totalWordCount;
	int [] tables;
	float [] submissionTimeLeft;
	int [] actualMarks;
	double [] summaryCoeff;
	double correlationFactor;
	
	public plot_summary_parameters_table_count(double [] coeff,int[] marks,int[] image,int[]totalWord,float[]submissionTime, int[] tableCount){
		actualMarks = marks;
		imageCount = image;
		totalWordCount = totalWord;
		submissionTimeLeft = submissionTime;
		summaryCoeff = coeff;
		tables = tableCount;
		if(actualMarks.length==imageCount.length && actualMarks.length==totalWordCount.length && actualMarks.length == submissionTimeLeft.length){
		createXData();
		createYData();
		correlationFactor = new PearsonsCorrelation().correlation(xData, yData);
		
		System.out.println("\n\nThe correlation between the Actual Mark and the Predicted Marks is "+ correlationFactor);
		//scatter_plot sp = new scatter_plot("Predicted Mark v/s Actual Mark","Actual Mark","Predicted Mark",xData,yData);
		scatter_plot sp = new scatter_plot("Acutal Mark v/s Predicted Mark (TableCount)","Predicted Mark","Actual Mark",yData,xData);
		sp.pack();
		sp.setVisible(true);
		}else{
			System.out.println("Errror......Please check the data you have submitted");
			System.exit(0);
		}
	}	
	private void createXData(){
		xData = new double[actualMarks.length];
		for(int i=0;i<xData.length;i++){
			xData[i] = actualMarks[i];
		}
	}
	
	private void createYData(){
		yData = new double[actualMarks.length];
		for(int i=0;i<yData.length;i++){
			yData[i]  = summaryCoeff[0]+(summaryCoeff[1]*totalWordCount[i])+(summaryCoeff[2]*imageCount[i])+(summaryCoeff[3]*submissionTimeLeft[i])+(summaryCoeff[4]*tables[i]);
		    
		}
	}
}
