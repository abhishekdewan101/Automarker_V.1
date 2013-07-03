package test;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

public class lsa{
	public static void main(String [] args){
		try{
			ArrayList dictionaryWords = new ArrayList();
			ArrayList stopWords = new ArrayList();
			ArrayList uniqueWords = new ArrayList();
			ArrayList fileNames = new ArrayList();
			int [][] countMatrix;
			double [][] tdifMatrix;
			HashMap<String,HashMap<String,Integer>> outerMap = new HashMap<String,HashMap<String,Integer>>();
			String [] files = {"brit-a-z.txt","wordList","britcaps.txt","stopWordList","csWords"};

			for(int i=0;i<files.length;i++){
			File tmpFile = new File(files[i]);
			FileInputStream fileInput = new FileInputStream(tmpFile);
			FileChannel fileChannel = fileInput.getChannel();
			ByteBuffer contentsBuffer = ByteBuffer.allocate((int)fileChannel.size());
			fileChannel.read(contentsBuffer);
			fileChannel.close();
			String contents = new String(contentsBuffer.array());
			StringTokenizer st = new StringTokenizer(contents);
			while(st.hasMoreTokens()){
				if(files[i].contains("stopWordList")){
					stopWords.add(st.nextToken());
				}else{
					dictionaryWords.add(st.nextToken());
					}	
				}
			}
			System.out.println("dictionaryWords size "+ dictionaryWords.size());
			System.out.println("stopWords size "+stopWords.size());

			File [] file = new File("textFiles").listFiles();
			for(int i=0;i<file.length;i++){
				System.out.println("Processing.... "+ file[i].getName());
				HashMap<String,Integer> innerMap = new HashMap<String,Integer>();
				FileInputStream fileInput = new FileInputStream(file[i]);
				FileChannel fileChannel = fileInput.getChannel();
				ByteBuffer contentsBuffer = ByteBuffer.allocate((int)fileChannel.size());
				fileChannel.read(contentsBuffer);
				fileChannel.close();
				String contents = new String(contentsBuffer.array());
				StringTokenizer st = new StringTokenizer(contents);
				while(st.hasMoreTokens()){
					String tempString = st.nextToken();
					if(dictionaryWords.contains(tempString) && !stopWords.contains(tempString)){
						if(innerMap.containsKey(tempString)){
							innerMap.put(tempString,innerMap.get(tempString)+1);
						}else{
							innerMap.put(tempString,1);
						}
					}
					if(!uniqueWords.contains(tempString)){
						uniqueWords.add(tempString);
					}
				}
				outerMap.put(file[i].getName(),innerMap);
				fileNames.add(file[i].getName());
			}
			System.out.println("fileNames size "+ fileNames.size());
			System.out.println("uniqueWords size "+ uniqueWords.size());

			//Creating the count matrix

			countMatrix = new int[fileNames.size()][uniqueWords.size()]; 

			for(int i=0;i<countMatrix.length;i++){
				for(int j=0;j<countMatrix[1].length;j++){
					countMatrix[i][j] = 0;
				}
			}
			
			System.out.println("building count matrix");
			for(int i=0;i<fileNames.size();i++){
				String filename = fileNames.get(i).toString();
				HashMap<String,Integer> tmpHash = outerMap.get(filename);
				for(String key : tmpHash.keySet()){
					countMatrix[i][uniqueWords.indexOf(key)]++;
				}
			}
			System.gc();
			tdifMatrix = new double[fileNames.size()][uniqueWords.size()];
			
			System.out.println("building tdif matrix");
			for(int i=0;i<countMatrix.length;i++){
				HashMap<String,Integer> tmpHash = outerMap.get(fileNames.get(i).toString());
				int totalWordCount =0;
				for(String key:tmpHash.keySet()){
					totalWordCount += tmpHash.get(key);
				}
				for(int j=0;j<countMatrix[1].length;j++){
					int wordVal;
					if(tmpHash.containsKey(uniqueWords.get(j).toString())){
						wordVal = tmpHash.get(uniqueWords.get(j).toString());
					}else{
						wordVal = 0;
					}
					double numerator = (wordVal) / (totalWordCount);
					double denominator = Math.log((fileNames.size()) / (getCount(uniqueWords,countMatrix,uniqueWords.get(j).toString())));
					//System.out.println(i+"-"+j+"	"+numerator*denominator);
					tdifMatrix[i][j] = numerator*denominator;
				}
			}
			System.out.println("writing file to serialize object");
			RealMatrix A = MatrixUtils.createRealMatrix(tdifMatrix);
			SingularValueDecomposition svd = new SingularValueDecomposition(A);
			RealMatrix U = svd.getU();
			double [][] temp = U.getData();
			for(int i=0;i<temp.length;i++){
				System.out.println();
				for(int k=0;k<temp[1].length;k++){
					System.out.print(temp[i][k]+" ");
				}
			}
			} catch(IOException e){
			e.printStackTrace();
		}
	}

	public static int getCount(ArrayList uniqueWords,int[][] countMatrix, String key){
		int documentCount = 1;
		int rowCount = uniqueWords.indexOf(key);
		for(int i=0;i<countMatrix.length;i++){
			if(countMatrix[i][rowCount]!=0){
				documentCount++;
			}
		}
		return documentCount;
	} 
}
	