package test;

import org.la4j.matrix.Matrices;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

public class test2 {
	public static void main(String [] args){
		double [][] countMatrix = new double[6500][65];
		for(int i =0;i<countMatrix.length;i++){
			for(int j=0;j<countMatrix[i].length;j++){
				countMatrix [i][j] = i+j;
			}
		}
		
		Matrix a = new Basic2DMatrix(countMatrix);
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
		
	}
}
