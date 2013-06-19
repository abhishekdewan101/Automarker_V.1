import java.util.Date;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;


public class regression {

	public static void main(String [] args){
		Date deadLine = new Date(2012,9,30,23,59,59);
		Date deadLine1 = new Date(2012,9,29,21,41,59);
		
		System.out.println((deadLine.getTime()-deadLine1.getTime())/(1000*60*60));
		/*OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
		double[] y = new double[]{11.0, 12.0, 13.0, 14.0, 15.0, 16.0};
		double[][] x = new double[6][];
		x[0] = new double[]{0, 0, 0, 0, 0};
		x[1] = new double[]{2.0, 0, 0, 0, 0};
		x[2] = new double[]{0, 3.0, 0, 0, 0};
		x[3] = new double[]{0, 0, 4.0, 0, 0};
		x[4] = new double[]{0, 0, 0, 5.0, 0};
		x[5] = new double[]{0, 0, 0, 0, 6.0};          
		regression.newSampleData(y, x);
		
		
		double[] beta = regression.estimateRegressionParameters();
		for(int i =0;i<beta.length;i++){
			System.out.format("The beta value for the %d parameter is %f\n",i,beta[i]);
		}*/
	}
}
