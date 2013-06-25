import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

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
	double [] mark_list;
	
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
		get_different_words();		
		calculate_correlation();
		calculate_bestwords();
		calculate_regression();
	}
	
	
	
	private void calculate_regression() {
	   System.out.println("Starting the regresion this may take some time");
	   try{
		  double[] tmp_marks = mark_list;
		  double[][] parameterData = new double[tmp_marks.length][];
		  Statement databaseStatement = databaseConnection.createStatement();
		  for(int i =0;i<tmp_marks.length;i++){
			  double [] tmp_parameters = new double[bestWord.size()];
			  for(int y=0;y<bestWord.size();y++){
				//  System.out.println(i+"	"+y);
				  resultSet = databaseStatement.executeQuery("select * from wordDB where marks ='"+tmp_marks[i]+"' and word ='"+bestWord.get(y).toString()+"'");
				  while(resultSet.next()){
					 tmp_parameters[y] = resultSet.getInt(3);
				  }
			  }
			  parameterData[i] = tmp_parameters;
		  }
		  
		  OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
		  regression.newSampleData(tmp_marks, parameterData);
		  System.out.println("Regression started");
		  double[] regressionCoeff = regression.estimateRegressionParameters();
		  System.out.println("Regression ended");
		  System.out.print("mark obtained = "+regressionCoeff[0]);
		  for(int i=1;i<regressionCoeff.length;i++){
			  System.out.print(" + "+regressionCoeff[i]+"*"+bestWord.get(i-1).toString());
		  }
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
					bestWord.add(resultSet.getString(2));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	private void calculate_correlation(){
		System.out.println("Calculating the correlation for th diff");
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
			mark_list = tmp_marks;
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

	private void set_dicitionary_word() {
		System.out.println("Grabbing the dictionary. Please wait.....");
		try {
			Statement databaseStatement = databaseConnection.createStatement();
			resultSet = databaseStatement.executeQuery("select word from wordList");
			while(resultSet.next()){
				if(!dictionaryWords.contains(resultSet.getString(1))){
					dictionaryWords.add(resultSet.getString(1));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NullPointerException e){
			try {
				dictionaryWords.add(resultSet.getString(1));
			} catch (SQLException e1){
				e1.printStackTrace();
			}
		}
	}

	public void get_different_words(){
		System.out.println("Finding the different words that make sense");
		try {
			Statement databaseStatement = databaseConnection.createStatement();
			resultSet = databaseStatement.executeQuery("select word from wordDB");
			while(resultSet.next()){
				if(!differentWords.contains(resultSet.getString(1))){
					differentWords.add(resultSet.getString(1));
					
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NullPointerException e){
			try {
				differentWords.add(resultSet.getString(1));
			} catch (SQLException e1){
				e1.printStackTrace();
			}
		}
	}
}
