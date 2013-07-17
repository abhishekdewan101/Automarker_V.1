/*This class calculates the latent semantic space for the testing data
 * The following steps are followed
 * 1) Calculate the count matrix
 * 2) SVD computation
 * 3) Calculate the term vectors and document vectors.*/
package latent_semantic_analysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import Jama.Matrix;
import Jama.SingularValueDecomposition;


public class lsa {
	
	String textDirectory;
	get_ten_fold_files gtff;
	Matrix U;
	Matrix S;
	Matrix vTranspose;
	ArrayList<String> stopWords = new ArrayList<String>();
	ArrayList<String> dictionaryWords = new ArrayList<String>();
	ArrayList<String> refinedWords;
	ArrayList<String> documentsList = new ArrayList<String>();
	final int NUM_FACTORS = 1000;
	HashMap<Double,Double> finalSet = new HashMap<Double,Double>();
	
	public lsa(String textFilesDirectory){
		textDirectory = textFilesDirectory;
		gtff = new get_ten_fold_files(textDirectory);
		getDictionaryWords();
	}
	
	private double testQuery(File[] testingFiles){
		double[] actualMarks = new double[testingFiles.length];
		double[] predictedMarks = new double[testingFiles.length];
		int counter =0;
		for(int i =0;i<testingFiles.length;i++){
			try{
				FileInputStream fileInput = new FileInputStream(testingFiles[i]);
				FileChannel fileChannel = fileInput.getChannel();
				HashMap<String,Integer> words = new HashMap<String,Integer>();
				ByteBuffer contentsBuffer = ByteBuffer.allocate((int)fileChannel.size());
				fileChannel.read(contentsBuffer);
				fileChannel.close();
				String contents = new String(contentsBuffer.array());
				StringTokenizer st = new StringTokenizer(contents);
				while(st.hasMoreTokens()){
					String tempString = st.nextToken().toLowerCase();
					if(refinedWords.contains(tempString)){
						if(words.containsKey(tempString)){
							words.put(tempString, words.get(tempString)+1);
						}else{
							words.put(tempString, 1);
						}
					}
				}
				double[][] queryVector = new double[1][refinedWords.size()];
				
				for(int j=0;j<queryVector.length;j++)
				{
					queryVector[0][j] = 0.0;
				}
				
				
				for(String key:words.keySet()){
					int index = refinedWords.indexOf(key);
					queryVector[0][index] ++;
				}
				
				Matrix query = new Matrix(queryVector);
				Matrix sInverse = S.inverse();
				Matrix reducedQuery = query.times(U);
				reducedQuery = reducedQuery.times(sInverse);
//				System.out.println("U dimensions are "+U.getRowDimension()+" x "+U.getColumnDimension());
//				System.out.println("S dimensions are "+S.getRowDimension()+" x "+S.getColumnDimension());
//				System.out.println("vTranspose dimensions are "+vTranspose.getRowDimension()+" x "+vTranspose.getColumnDimension());
//				System.out.println("query dimensions are "+query.getRowDimension()+" x "+query.getColumnDimension());
//				System.out.println("sInverse dimensions are "+sInverse.getRowDimension()+" x "+sInverse.getColumnDimension());
//				System.out.println("reducedQuery dimensions are "+reducedQuery.getRowDimension()+" x "+reducedQuery.getColumnDimension());
//				
				double[][] queryArray =  reducedQuery.getArray();
				double[][] documents = vTranspose.getArray();
				double [] similarity = new double[documents.length];
				for(int j=0;j<documents.length;j++){
					double[] documentVector = documents[j];
					similarity[j] = calcSim(queryArray,documentVector);
				}
				System.out.println("Query v/s Documents\n for file "+testingFiles[i].getName());
				for(int j=0;j<similarity.length;j++){
					System.out.println(documentsList.get(j).toString() + "  has a similarity factor of "+ similarity[j]);
				}
				System.out.println("\n\n");
				
				HashMap<String,Double> bestResults = returnBestMatches(documentsList,similarity);
				
				double predicted =0.0;
				for(String key:bestResults.keySet()){
					String[] tmp = key.split("_");
					predicted += Integer.parseInt(tmp[1].substring(0,2));
				}
				String [] tmp = testingFiles[i].getName().split("_");
				double actualMark = Integer.parseInt(tmp[1].substring(0,2));
				
				actualMarks[counter] = actualMark;
				predictedMarks[counter] = predicted;
				finalSet.put(actualMark,predicted);
				counter++;
				}catch(IOException e){
				e.printStackTrace();
			}
		}
		double[] difference = new double[actualMarks.length];
		for(int i=0;i<actualMarks.length;i++){
			difference[i] = actualMarks[i] - predictedMarks[i];
			System.out.println(actualMarks[i]+"		"+predictedMarks[i]);
		}
		System.out.println("Correlation"+ new PearsonsCorrelation().correlation(actualMarks, predictedMarks));
		double averageDifference = 0;
		for(int i=0;i<difference.length;i++){
			averageDifference += difference[i];
		}
		
		return (double)(averageDifference/difference.length);
	}
	
	private HashMap<String, Double> returnBestMatches(ArrayList<String> documentsList2, double[] similarity) {
		HashMap<String,Double> temp = new HashMap<String,Double>();
		while(temp.size()<1){
			double best =0.0;
			int indexOfBest =0;
			System.out.println(temp.size());
			for(int i=0;i<similarity.length;i++){
				if(best<similarity[i] && !temp.containsKey(documentsList2.get(i).toString())){
					best = similarity[i];
					indexOfBest = i;
				}
			}
			System.out.println(best+"	"+ indexOfBest);
			temp.put(documentsList2.get(indexOfBest).toString(), best);
		}
		return temp;
	}

	private double calcSim(double[][] queryArray, double[] documentVector) {
		double similarity =0.0;
		// calculate numerator
		double sum =0;
		for(int i=0;i<documentVector.length;i++){
			sum += documentVector[i] * queryArray[0][i];
		}
		
		//calculate denominator
		double normB =0;
		for(int i=0;i<documentVector.length;i++){
			normB += documentVector[i]*documentVector[i];
		}
		normB = Math.sqrt(normB);
		
		double normA =0;
		for(int i=0;i<queryArray[0].length;i++){
			normA += queryArray[0][i]*queryArray[0][i];
		}
		normA = Math.sqrt(normA);
		
		similarity = (sum)/(normA * normB);
		
		return similarity;
	}

	private void calculateVectors(File[] trainingFiles) {
		refinedWords = new ArrayList<String>();
		HashMap<String,ArrayList> words = new HashMap<String,ArrayList>();
		for(int i=0;i<trainingFiles.length;i++){
			System.out.println("initial "+trainingFiles[i]);
			try {
				FileInputStream fileInput = new FileInputStream(trainingFiles[i]);
				FileChannel fileChannel = fileInput.getChannel();
				ByteBuffer contentsBuffer = ByteBuffer.allocate((int)fileChannel.size());
				fileChannel.read(contentsBuffer);
				fileChannel.close();
				String contents = new String(contentsBuffer.array());
				StringTokenizer st = new StringTokenizer(contents);
				while(st.hasMoreTokens()){
				String tempString = st.nextToken().toLowerCase();
				
				if(!refinedWords.contains(tempString) && dictionaryWords.contains(tempString) && !stopWords.contains(tempString)){
						refinedWords.add(tempString);
				}
				
				if(dictionaryWords.contains(tempString) && !stopWords.contains(tempString)){
					if(words.containsKey(tempString)){
						if(!words.get(tempString).contains(trainingFiles[i])){
							ArrayList tmp = words.get(tempString);
							tmp.add(trainingFiles[i]);
							words.put(tempString, tmp);
						}
					}else{
						ArrayList tmp = new ArrayList();
						tmp.add(trainingFiles[i]);
						words.put(tempString,tmp);
					}
				}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println(refinedWords.size());
		
		for(int i =0;i<refinedWords.size();i++){
			if(words.get(refinedWords.get(i).toString()).size()<trainingFiles.length/2){
				refinedWords.remove(refinedWords.get(i).toString());
			}
		}
		
		System.out.println(refinedWords.size());
		
		double[][] countMatrix = new double[refinedWords.size()][trainingFiles.length];
		for(int i=0;i<countMatrix.length;i++){
			for(int j=0;j<countMatrix[i].length;j++){
				countMatrix[i][j] = 0;
			}
		}
		
		for(int i=0;i<trainingFiles.length;i++){
			try{
		    System.out.println("countmatrix "+trainingFiles[i]);
		    documentsList.add(trainingFiles[i].getName());
			FileInputStream fileInput = new FileInputStream(trainingFiles[i]);
			FileChannel fileChannel = fileInput.getChannel();
			ByteBuffer contentsBuffer = ByteBuffer.allocate((int)fileChannel.size());
			fileChannel.read(contentsBuffer);
			fileChannel.close();
			String contents = new String(contentsBuffer.array());
			StringTokenizer st = new StringTokenizer(contents);
			while(st.hasMoreTokens()){
				String tempString = st.nextToken().toLowerCase();
				if(refinedWords.contains(tempString)){
				 int index = refinedWords.indexOf(tempString);
				 countMatrix[index][i] += 1; 
				}
			}
			
			}catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//calculating the TDIF matrix from count Matrix
		double[][] tdif = new double[refinedWords.size()][trainingFiles.length];
		for(int i=0;i<countMatrix.length;i++){
			for(int j=0;j<countMatrix[i].length;j++){
				tdif[i][j]= (double) (((double)countMatrix[i][j]*columnAdd(countMatrix,j)))/(((double)countMatrix[i].length * nonZero(countMatrix,i)));
			}
		}
			
		System.out.println("SVD Started");
		Matrix A = new Matrix(tdif);
		SingularValueDecomposition s = A.svd();
		
		 U = s.getU();
	     S = s.getS();
	     Matrix V = s.getV();
	      
	    vTranspose = V.transpose();
	      
	    
	      
	      
		/*Matrix a = new Basic2DMatrix(countMatrix);
		Matrix[] usv = a.decompose(Matrices.SINGULAR_VALUE_DECOMPOSITOR);
		
		Matrix U = usv[0];
		
		System.out.println("rows are "+U.rows());
		System.out.println("columns are "+U.columns());
		
		
		Matrix S = usv[1];
		
		System.out.println("rows are "+S.rows());
		System.out.println("columns are "+S.columns());
		
		Matrix V = usv[1].transpose();
		
		System.out.println("rows are "+V.rows());
		System.out.println("columns are "+V.columns());
		*/
		
//		SvdMatrix matrix
//        = SvdMatrix.svd(countMatrix,
//		    1000,
//		    0.01,
//		    0.005,
//		    1000,
//		    0.00,
//            null,
//		    0.00,
//		    10,
//		    50000);
//		
//		scaleVector = matrix.singularValues();
//		termVector = matrix.leftSingularVectors();
//		documentVector = matrix.rightSingularVectors();
//		for(int i=0;i<scaleVector.length;i++){
//			System.out.println(scaleVector[i]);
//		}
//		 
	}

	private double nonZero(double[][] countMatrix, int i) {
		double count =0;
		for(int j=0;j<countMatrix[i].length;j++){
			if(countMatrix[i][j]>0){
				count++;
			}
		}
		return count;
	}

	private double columnAdd(double[][] countMatrix, int j) {
		double count = 0 ;
		for(int i=0;i<countMatrix.length;i++){
			count += countMatrix[i][j];
		}
		return count;
	}

	public void execute(){
		File[][] foldFiles = gtff.getFoldFiles();
		int [] index = gtff.getIndex();
		File[] trainingFiles;
		File[] testingFiles;
		int trainingCounter =0;
		int testingCounter =0;
		double[] correlation = new double[10];
		for(int i=0;i<10;i++){
			int trainingFold =i;
			trainingCounter =0;
			testingCounter = 0;
			trainingFiles = new File[gtff.getLength(trainingFold)]; 
			testingFiles = new File[index[trainingFold]];
			//training Phase
			for(int j=0;j<foldFiles.length;j++){
				for(int k=0;k<index[j];k++){
					if(j!=trainingFold){
						trainingFiles[trainingCounter] = foldFiles[j][k];
						trainingCounter++;
					}else{
						testingFiles[testingCounter] = foldFiles[j][k];
						testingCounter++;
					}
				}
			}
			calculateVectors(trainingFiles);
			correlation[i]=testQuery(testingFiles);
		}
		
		double[] aMarks = new double[finalSet.size()];
		double[] pMarks = new double[finalSet.size()];
		
		int i =0;
		double averageError =0.0;
		for(Double key:finalSet.keySet()){
			aMarks[i] = key;
			pMarks[i] = finalSet.get(key);
			System.out.println(key +"	"+finalSet.get(key));
			averageError += key - finalSet.get(key);
			i++;
 		}
		
		System.out.println("\n\n\n");
		System.out.println("Final Correlation of the system is "+ new PearsonsCorrelation().correlation(aMarks, pMarks));
		System.out.println("Average Error is "+ averageError/finalSet.size());
	}

	public void getDictionaryWords(){
		System.out.println("Updating local lists....");
		String[] fileName = {"brit-a-z.txt","wordList","britcaps.txt","csWords","stopWordList"};
		try {
			for(int i =0;i<fileName.length;i++){
				System.out.println("Setting up "+fileName[i]);
				File tmpFile = new File(fileName[i]);
			    FileChannel fileChannel = new FileInputStream(tmpFile).getChannel();
			    ByteBuffer contentsBuffer = ByteBuffer.allocate((int)fileChannel.size()); 
			    fileChannel.read(contentsBuffer);
				fileChannel.close();
				String contents = new String(contentsBuffer.array());
				StringTokenizer st = new StringTokenizer(contents);
				while(st.hasMoreTokens()){
					String tmp = st.nextToken().toLowerCase();
					if(fileName[i].equals("stopWordList")){
						stopWords.add(tmp);
					}else{
						if(!dictionaryWords.contains(tmp)){
							dictionaryWords.add(tmp);
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String [] args){
		lsa lsa = new lsa("textFiles");
		lsa.execute();
	}
}
