package iteration_V1.extractFeatures;

import java.io.File;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import plotting.scatter_plot;

public class Ten_Fold_Summary {
	public static void main(String [] args){
		// Get Fold Files
		Extract_Summary_Parameters esp = new Extract_Summary_Parameters();
		File [] textFiles = new File("textFiles").listFiles();
		int [] index = new int[10];
		int filesPerFold;
		if(textFiles.length%10==0){
			filesPerFold = textFiles.length/10;
		}else{
			filesPerFold = textFiles.length/10 + 1;
		}
		File[][] folds = new File[10][filesPerFold];
		int fold;
		for(int i =0;i<textFiles.length;i++){
			fold = i%10;
			folds[fold][index[fold]]= textFiles[i];
			index[fold]++;
		}
		double[] correlationFactors = new double[10];
		// perform regression and calculate average correlation
		for(int i=9;i>=0;i--){
			System.out.println("Entered Fold "+ i);
			int testingFold = i;
			double [] marksDataSet = new double[textFiles.length-index[testingFold]];
			double [][] parameterData = new double[textFiles.length-index[testingFold]][];
			int counter =0;
			for(int j=0;j<folds.length;j++){
				for(int k=0;k<index[j];k++){
					if(j!=testingFold){
						File tmpFile = folds[j][k];
						String tmp[] = tmpFile.getName().split("_");
						marksDataSet[counter] = Integer.parseInt(tmp[1].substring(0,2));
						parameterData[counter] = new double[]{esp.totalImageCount(tmpFile),esp.totalTableCount(tmpFile),esp.totalWordCount(tmpFile),esp.submissionTimeLeft(tmpFile)};
						counter++;
					}
				}
			}
			System.out.println("Calculating regression coeff for testing set "+ testingFold);
			OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
			regression.newSampleData(marksDataSet, parameterData);
			double [] coff = regression.estimateRegressionParameters();
			
			double [] predictedMark = new double[index[testingFold]];
			double [] actualMark = new double[index[testingFold]];
			counter =0;
			for(int j=0;j<folds.length;j++){
				for(int k=0;k<index[j];k++){
					if(j==testingFold){
						File tmpFile = folds[j][k];
						String tmp[] = tmpFile.getName().split("_");
						actualMark[counter] = Integer.parseInt(tmp[1].substring(0,2));
						predictedMark[counter] = coff[0]+coff[1]*esp.totalImageCount(tmpFile)+coff[2]*esp.totalTableCount(tmpFile)+coff[3]*esp.totalWordCount(tmpFile)+coff[4]*esp.submissionTimeLeft(tmpFile);
						if(predictedMark[counter]>100){
							predictedMark[counter] =100;
						}
						System.out.println(actualMark[counter]+"	"+predictedMark[counter]);
						counter++;
					}
				}
			}
			/*scatter_plot sp = new scatter_plot(("Actual Mark v/s Predicted Mark (Testing Fold "+i+")"), "Actual Marks", "Predicted Marks", actualMark, predictedMark);
			sp.pack();
			sp.setVisible(true);
			*/
			double correlation = new PearsonsCorrelation().correlation( actualMark ,predictedMark);
			correlationFactors[i] = correlation;
			System.out.println("The correlation factor for testing set "+ testingFold + " equals to "+correlation);
			System.out.println("\n\n");
		}
		double averageCorrelation = 0;
		for(int i=0;i<correlationFactors.length;i++){
			averageCorrelation += Math.abs(correlationFactors[i]);
		}
		System.out.println("Average Correlation is "+ averageCorrelation/10);
	}
}
