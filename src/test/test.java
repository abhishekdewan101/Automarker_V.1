package test;

import com.aliasi.matrix.SvdMatrix;

public class test {
	public static void main(String[] args){
		double[][] countMatrix = new double[65][6500];
		
		for(int i =0;i<countMatrix.length;i++){
			for(int j=0;j<countMatrix[i].length;j++){
				countMatrix [i][j] = i+j;
			}
		}
		
		System.out.println("SVD Started");
		SvdMatrix matrix
        = SvdMatrix.svd(countMatrix,
		    1000,
		    0.01,
		    0.005,
		    1000,
		    0.00,
            null,
		    0.00,
		    10,
		    50000);
		System.out.println("SVD ended");
		double[] scaleVector = matrix.singularValues();
		double[][] termVector = matrix.leftSingularVectors();
		double[][] documentVector = matrix.rightSingularVectors();
		
		for(int i=0;i<scaleVector.length;i++){
			System.out.println(scaleVector[i]);
		}	
	}
}
