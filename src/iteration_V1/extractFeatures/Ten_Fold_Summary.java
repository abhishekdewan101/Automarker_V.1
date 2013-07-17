package iteration_V1.extractFeatures;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

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
		double [] predictedMark = new double[textFiles.length];
		double [] actualMark = new double[textFiles.length];
		int counter1 =0;
		// perform regression and calculate average correlation
		for(int i=9;i>=0;i--){
			ArrayList<String> bestWords = esp.trainingWords("textFiles", i);
			System.out.println("Entered Fold "+ i);
			int testingFold = i;
			double [] marksDataSet = new double[textFiles.length-index[testingFold]+1];
			double [][] parameterData = new double[textFiles.length-index[testingFold]+1][];
			int counter =0;
			for(int j=0;j<folds.length;j++){
				for(int k=0;k<index[j];k++){
					if(j!=testingFold){
						double[] parameters = new double[bestWords.size()+4];
						File tmpFile = folds[j][k];
						parameters[0] = esp.totalImageCount(tmpFile);
						parameters[1] = esp.totalTableCount(tmpFile);
						parameters[2] = esp.totalWordCount(tmpFile);
						parameters[3] = esp.submissionTimeLeft(tmpFile);
						HashMap<String,Integer> tmpHash = new HashMap<String,Integer>();
					try{
						FileChannel fileChannel;
						fileChannel = new FileInputStream(tmpFile).getChannel();
						ByteBuffer contentsBuffer = ByteBuffer.allocate((int)fileChannel.size()); 
					    fileChannel.read(contentsBuffer);
						fileChannel.close();
						String contents = new String(contentsBuffer.array());
						StringTokenizer st = new StringTokenizer(contents);
						
						while(st.hasMoreTokens()){
							String tempString = st.nextToken().toLowerCase();
							if(bestWords.contains(tempString)){
								if(tmpHash.containsKey(tempString)){
									tmpHash.put(tempString, tmpHash.get(tempString)+1);
								}else{
									tmpHash.put(tempString, 1);
								}
							}
						}
					}catch(IOException e){
						e.printStackTrace();
					}
					
					for(int l=0;l<bestWords.size();l++){
							int wordCount =0;
							if(tmpHash.containsKey(bestWords.get(l).toString())){
								wordCount = tmpHash.get(bestWords.get(l).toString());
							}else{
								wordCount = 0;
							}
							parameters[l+4] = wordCount;
						}
						
						String tmp[] = tmpFile.getName().split("_");
						marksDataSet[counter] = Integer.parseInt(tmp[1].substring(0,2));
						parameterData[counter] = parameters;
						counter++;
					}
				}
			}
			
			
			marksDataSet[counter] =0;
			double [] tempArray = new double[bestWords.size()+4];
			for(int l=0;l<tempArray.length;l++){
				tempArray[l] =0;
			}
			parameterData[counter] = tempArray;
			
			 
			System.out.println("Calculating regression coeff for testing set "+ testingFold);
			OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
			regression.newSampleData(marksDataSet, parameterData);
			double [] coff = regression.estimateRegressionParameters();
			
			
			for(int j=0;j<folds.length;j++){
				for(int k=0;k<index[j];k++){
					if(j==testingFold){
						File tmpFile = folds[j][k];
						String tmp[] = tmpFile.getName().split("_");
						actualMark[counter1] = Integer.parseInt(tmp[1].substring(0,2));
				
				double[] predictingValues = new double[bestWords.size()+4];
				predictingValues[0] = esp.totalImageCount(tmpFile);
				predictingValues[1] = esp.totalTableCount(tmpFile);
				predictingValues[2] = esp.totalWordCount(tmpFile);
				predictingValues[3] = esp.submissionTimeLeft(tmpFile);
				HashMap<String,Integer> tmpHash1 = new HashMap<String,Integer>();	
				try{
						FileChannel fileChannel;
						fileChannel = new FileInputStream(tmpFile).getChannel();
						ByteBuffer contentsBuffer = ByteBuffer.allocate((int)fileChannel.size()); 
					    fileChannel.read(contentsBuffer);
						fileChannel.close();
						String contents = new String(contentsBuffer.array());
						StringTokenizer st = new StringTokenizer(contents);
						while(st.hasMoreTokens()){
							String tempString = st.nextToken();
							if(bestWords.contains(tempString)){
								if(tmpHash1.containsKey(tempString)){
									tmpHash1.put(tempString, tmpHash1.get(tempString)+1);
								}else{
									tmpHash1.put(tempString, 1);
								}
							}
						}
						System.out.println(tmpHash1);
						for(int l=0;l<bestWords.size();l++){
							if(tmpHash1.containsKey(bestWords.get(l).toString())){
							predictingValues[l+4] = tmpHash1.get(bestWords.get(l).toString());
							}else{
								predictingValues[l+4] =0;
							}
						}
						
						
					}catch(IOException e){
						e.printStackTrace();
					}
						predictedMark[counter1] = coff[0];
						System.out.println(coff[0]+" + ");
						for(int l=0;l<predictingValues.length;l++){
							if(l==predictingValues.length-1){
							System.out.println(coff[l+1]*predictingValues[l]);
							}else{
							System.out.print(coff[l+1]*predictingValues[l]+" + ");
							}
							predictedMark[counter1] += coff[l+1]*predictingValues[l];
						}
						
						if(predictedMark[counter1]>100){
							predictedMark[counter1] =100;
						}
						System.out.println(actualMark[counter1]+"	"+predictedMark[counter1]);
						counter1++;
					}
				}
			}
		}
		
		double averageError =0.0;
		for(int i =0;i<actualMark.length;i++){
			System.out.println(actualMark[i]+"	"+predictedMark[i]);
			averageError += actualMark[i] - predictedMark[i];
		}
		
		double correlation = new PearsonsCorrelation().correlation( actualMark ,predictedMark);
		scatter_plot sp = new scatter_plot(("Actual Mark v/s Predicted Mark"), "Actual Marks", "Predicted Marks", actualMark, predictedMark);
		sp.pack();
		sp.setVisible(true);
		System.out.println("Average Correlation is "+ correlation);
		System.out.println("Average Error is "+ averageError/actualMark.length);
	}
}
