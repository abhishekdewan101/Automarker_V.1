import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import plotting.scatter_plot;


public class intrinsic_parameters_regression {

	Connection databaseConnection = null;
	ResultSet resultSet;
	String databaseName;
	String textFileDirectory;
	String databaseURL = null;
	String baseURL = "jdbc:mysql://localhost:3306/";
	String userName = "root";
	String password ="";
	
	ArrayList differentWords = new ArrayList();
	ArrayList dictionaryWords = new ArrayList();
	ArrayList ids = new ArrayList();
	ArrayList bestWord = new ArrayList();
	HashMap<String,Double> equationParams = new HashMap<String,Double>();
	 double[] regressionCoeff;
	
	public intrinsic_parameters_regression(String database,String inputDirectory){
		textFileDirectory = inputDirectory; 
		databaseName = database;
		databaseURL = baseURL + databaseName;
		try {
			databaseConnection = DriverManager.getConnection(databaseURL,userName,password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void executeSteps(){
	//	set_dicitionary_word(); //can be uncommented if the checking of words is deemed necessary
		get_ids();
		get_differentWords();
		//calculate_correlation();
		calculate_bestwords();
		calculate_regression();
		plot_marks();
	}
	
	
	
	
	private void plot_marks() {
		File [] textFiles = new File(textFileDirectory).listFiles();
		Statement databaseStatement;
		double[] actualMarks = new double[textFiles.length];
		double[] predictedMarks = new double[textFiles.length];
		
		try {
			databaseStatement = databaseConnection.createStatement();
			for(int i=0;i<textFiles.length;i++){
				double predictedMark = regressionCoeff[0];
				resultSet = databaseStatement.executeQuery("select * from wordDB where filename='"+textFiles[i].getName()+"'");
				while(resultSet.next()){
					if(equationParams.containsKey((resultSet.getString(1)))){
						actualMarks[i] = resultSet.getInt(5);
						predictedMark += (resultSet.getInt(3)*equationParams.get(resultSet.getString(1)));
					}
				}
				predictedMarks[i] = predictedMark;
			}			
			
			System.out.println("\n\n\n");
			for(int i =0;i<predictedMarks.length;i++){
				System.out.println(actualMarks[i]+"		"+predictedMarks[i]);
			}
			
			
			scatter_plot plot = new scatter_plot("Actual Mark v/s Predicted Mark", "Actual Mark", "Predicted Mark", actualMarks, predictedMarks);
			plot.pack();
			plot.setVisible(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}

	private void get_differentWords() {
		System.out.println("Getting the different words");
		try {
			Statement databaseStatement = databaseConnection.createStatement();
			resultSet = databaseStatement.executeQuery("select * from uniqueWords");
			while(resultSet.next()){
				differentWords.add(resultSet.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}

	private void calculate_regression() {
	   System.out.println("Starting the regresion this may take some time");
	   try{
		  Statement databaseStatement = databaseConnection.createStatement();  
		  double[] tmp_marks;
		  double[][] parameterData;
		  int rowCount = 0;
		  int counter =0;
		  
		  resultSet = databaseStatement.executeQuery("select count(marks) from imageDB");
		  while(resultSet.next()){
			 rowCount = resultSet.getInt(1);
		  }
		  tmp_marks = new double[rowCount+1];
		  parameterData = new double[rowCount+1][];
		 
		  resultSet = databaseStatement.executeQuery("select marks from imageDB");
		  while(resultSet.next()){
			 tmp_marks[counter] = resultSet.getInt(1);
			 counter++;
		  }
		
		  for(int i =0;i<tmp_marks.length;i++){
			  double [] tmp_parameters = new double[bestWord.size()];
			  for(int y=0;y<bestWord.size();y++){
				  resultSet = databaseStatement.executeQuery("select * from wordDB where marks ='"+tmp_marks[i]+"' and word ='"+bestWord.get(y).toString()+"'");
				  while(resultSet.next()){
					 tmp_parameters[y] = resultSet.getInt(3);
				  }
			  }
			  parameterData[i] = tmp_parameters;
		  }
		  tmp_marks[(tmp_marks.length)-1]=0;
		  double[]tmp_parameters = new double[bestWord.size()];
		  for(int i=0;i<bestWord.size();i++){
			  tmp_parameters[i]=0;
		  }
		  parameterData[(parameterData.length-1)]= tmp_parameters;
		  
		  OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
		  regression.newSampleData(tmp_marks, parameterData);
		  System.out.println("Regression started");
		  regressionCoeff = regression.estimateRegressionParameters();
		  System.out.println("Regression ended");
		  System.out.print("mark obtained = "+regressionCoeff[0]);
		  for(int i=1;i<regressionCoeff.length;i++){
			  System.out.print(" + "+regressionCoeff[i]+"*"+bestWord.get(i-1).toString());
			  equationParams.put(bestWord.get(i-1).toString(), regressionCoeff[i]);
		  }
		  
		  double[] errors = regression.estimateResiduals();
		  System.out.println("Error");
		  for(int i=1;i<regressionCoeff.length;i++){
			  System.out.println(bestWord.get(i-1).toString()+" - "+regressionCoeff[i]);
		  }
		  System.out.println("Standard Error = "+ regression.estimateRegressionStandardError());
		  System.out.println("R-Square = "+regression.calculateRSquared()+"\n");
	  }catch(SQLException e){
	  }
	}

	private void calculate_bestwords() {
		System.out.println("Finding the words with the highest correlation data.");
		try {
			double averageCount = 0;
			double average;
			double count = 0;
			
			Statement databaseStatment = databaseConnection.createStatement();
			resultSet = databaseStatment.executeQuery("select count(*) from correlationDB");
			while(resultSet.next()){
				averageCount = resultSet.getInt(1);
			}
			resultSet = databaseStatment.executeQuery("select * from correlationDB");
			
			while(resultSet.next()){
				count += Math.abs(resultSet.getDouble(1));
			}
			average = count/averageCount;
		    
			resultSet = databaseStatment.executeQuery("select * from correlationDB");
			while(resultSet.next()){
				if(Math.abs(resultSet.getDouble(1))>average){
					if(bestWord.size()<=63){
					bestWord.add(resultSet.getString(2));
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	private void calculate_correlation(){
		System.out.println("Calculating the correlation for the different words");
		try {
			Statement databaseStatment = databaseConnection.createStatement();
			double[] tmp_marks = null;
			double[] tmp_wordCount;
			int wordCount; 
			int marks;
			Double correlationFactor;
			
			for(int i=0;i<differentWords.size();i++){
				tmp_marks = new double[ids.size()];
				tmp_wordCount = new double[ids.size()];
				wordCount =0;
				marks =0;
				for(int y=0;y<ids.size();y++){
					String query = "select * from wordDB where word ='"+differentWords.get(i).toString()+"' and filename ='"+ids.get(y).toString()+"'";
					resultSet = databaseStatment.executeQuery(query);
					while(resultSet.next()){
						marks = resultSet.getInt(5);
						wordCount = resultSet.getInt(3);
					}
					tmp_marks[y] = marks;
					tmp_wordCount[y] = wordCount;
				}
				
				//can be uncommented to see the scatter plots for the different words.
				/*scatter_plot sp = new scatter_plot("Correlation Graph for"+differentWords.get(i).toString(),"marks","wordCount",tmp_marks,tmp_wordCount);
				sp.pack();
				sp.setVisible(true);*/
				
				correlationFactor = new PearsonsCorrelation().correlation(tmp_wordCount, tmp_marks);
			    if(correlationFactor.isNaN()){
			    	correlationFactor = 0.0;
			    }
				System.out.println(differentWords.get(i).toString()+" has a correlation factor of "+ correlationFactor);	
				databaseStatment.executeUpdate("insert into correlationDB values("+correlationFactor+",'"+differentWords.get(i).toString()+"')");
			}
		} catch (SQLException e) {
		}
	}
	
	private void get_ids() {
		File [] thesisFiles = new File(textFileDirectory).listFiles();
		for(int i=0;i<thesisFiles.length;i++){
			if(thesisFiles[i].getName().contains(".DS_Store")==false){
				ids.add(thesisFiles[i].getName());
			}
		}
		
	}
}
