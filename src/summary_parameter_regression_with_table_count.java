import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import plotting.plot_summary_parameters;
import plotting.plot_summary_parameters_table_count;

/*Class that starts the learning process for the system.*/
public class summary_parameter_regression_with_table_count {

	Connection databaseConnection = null;
	String databaseName;
	String databaseURL = null;
	String baseURL = "jdbc:mysql://localhost:3306/";
	String userName = "root";
	String password = "";
	Statement databaseStatement;
	Statement databaseStatement1;
	
	public summary_parameter_regression_with_table_count(String database){
		databaseName = database;
		databaseURL = baseURL + databaseName;
		try {
			databaseConnection = DriverManager.getConnection(databaseURL,userName,password);
			databaseStatement = databaseConnection.createStatement();
			databaseStatement1 = databaseConnection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void execute_steps(){
		multi_linear_regression_summrary_parameters();
		//multi_linear_regression_word();
	}
	
	// this is the regression method for the word parameters
	private void multi_linear_regression_word(){
		System.out.println("---------------------REGRESSION STATISTICS FOR THE WORD PARAMETERS ----------------------------------------\n\n\n");
		OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
		int wordCount;
		int correctGrammar;
		int wordLength;
		int marks;
		
		ResultSet resultSet = null;
		String [] parameters = {"intercept","wordCount","wordLength","correctGrammar"};
		int counter =0;
		double[] marksDataSet = null;
		double[][] parametersDataSet = null;
		int totalRowsInDB= 0;
		
		try {
			Statement databaseStatement = databaseConnection.createStatement();
			resultSet = databaseStatement.executeQuery("select count(*) from wordDB");
			while(resultSet.next()){
				totalRowsInDB = resultSet.getInt(1);
				
			}
			marksDataSet = new double[totalRowsInDB];
			parametersDataSet = new double[totalRowsInDB][];
			
			
			resultSet = databaseStatement.executeQuery("select * from wordDB");
			while(resultSet.next()){
				wordCount = resultSet.getInt(3);
				wordLength = resultSet.getInt(4);
				marks = resultSet.getInt(5);
				if(resultSet.getString(2).contains("YES")){
					correctGrammar = 1;
				}else{
					correctGrammar = 0;
				}
			marksDataSet[counter] = marks;
			parametersDataSet[counter] = new double[]{wordCount,wordLength};
			counter++;
			}
			
			
			//marksDataSet[counter] = 0;
			//parametersDataSet[counter] = new double[]{0,0};
			
			regression.newSampleData(marksDataSet,parametersDataSet);
			
			double [] regressionCoefficients = regression.estimateRegressionParameters();
			double [] errors = regression.estimateResiduals();
			System.out.format("Regression Equation becomes :\n" +"marks obtained = %f + %f*%s + %f*%s\n",regressionCoefficients[0],regressionCoefficients[1],parameters[1],regressionCoefficients[2],parameters[2]);
			//System.out.format("Regression Equation becomes :\n" +"marks obtained = %f + %f*%s\n",regressionCoefficients[0],regressionCoefficients[1],parameters[0]);			  
			
			
			System.out.println("		-------------");
			System.out.println();
			System.out.println("		|	Errors	|");
			System.out.println(parameters[0]+"     |"+errors[0]+"|");
			System.out.println(parameters[1]+"     |"+errors[1]+"|");
			System.out.println(parameters[2]+"     |"+errors[2]+"|");
			//System.out.println(parameters[3]+"     |"+errors[3]+"|");
			System.out.println("		-------------");
			
			System.out.println("Standard Error = "+ regression.estimateRegressionStandardError());
			System.out.println("R-Square = "+regression.calculateRSquared()+"\n");
			
			double[][] parameterVariance = regression.estimateRegressionParametersVariance();
			System.out.println("		intercept			totalWordCount		imageCount		submissionTimeLeft");
			
			for(int i=0;i<parameterVariance.length;i++){
				System.out.print(parameters[i]+"	");
				for(int y=0;y<parameterVariance[0].length;y++){
					System.out.print(parameterVariance[i][y]+"	");
				}
				System.out.println();
			}
		
			
			/*Covariance cov = new Covariance(parameterVariance);
			RealMatrix covData = cov.getCovarianceMatrix();
			double[][] tempData = covData.getData();
			
			for(int i=0;i<tempData.length;i++){
				System.out.println();
				for(int y=0;y<tempData[0].length;y++){
					System.out.print(tempData[i][y]+"		");
				}
				System.out.println();
			}*/
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// this is the regression method for the summary parameters
	private void multi_linear_regression_summrary_parameters() {
	  System.out.println("---------------------REGRESSION STATISTICS FOR THE SUMMARY PARAMETERS ----------------------------------------\n\n\n");
	 
	  OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
	  int imageCount = 0;
	  int totalWordCount = 0;
	  float submissionTimeLeft;
	  int extendedDeadline;
	  ResultSet resultSet = null;
	  ResultSet tmpResultSet = null;
	  String [] parameters = {"intercept","totalWordCount","imageCount","submissionTimeLeft","tableCount"};
	  int marks = 0;
	  int counter=0;
	  int tables =0;
	  double[] marksDataSet = null;
	  double[][] parametersDataSet = null;
	  int totalRowsInDB = 0;
	  String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	  
	  int []actualMarks;
	  int []totalWords;
	  int []image;
	  float[] submissionTime;
	  int [] tableCount;
	  
	  String id;
	  Date deadLine = new Date(2012,9,30,23,59,59);
	  Date submittedDate;
	  
	 
	  try {
		resultSet = databaseStatement.executeQuery("select * from submissionTime");
		tmpResultSet = databaseStatement1.executeQuery("select count(*) from imageDB");
		while(tmpResultSet.next()){
			totalRowsInDB = tmpResultSet.getInt(1);
		}
		marksDataSet = new double[totalRowsInDB+1];
	    parametersDataSet = new double[totalRowsInDB+1][];
	   
	    actualMarks = new int[totalRowsInDB];
	    totalWords = new int[totalRowsInDB];
	    image = new int[totalRowsInDB];
	    submissionTime = new float[totalRowsInDB];
	    tableCount = new int[totalRowsInDB];
	    
		while(resultSet.next()){
			id = resultSet.getString(1);
		    int day = Integer.parseInt(resultSet.getString(2).substring(0,2));
			int month = 0;
		    for(int i =0;i<months.length;i++){
			 if(months[i].contains(resultSet.getString(2).substring(3,6))){
				 month = i+1;
			 }
			}
		    
			int year = Integer.parseInt(resultSet.getString(2).substring(7,12).trim());
			int hrs = Integer.parseInt(resultSet.getString(3).substring(0,2));
			int mins = Integer.parseInt(resultSet.getString(3).substring(3,5));
			int seconds = Integer.parseInt(resultSet.getString(3).substring(6,8));
			
			submittedDate = new Date(year,month,day,hrs,mins,seconds);
			submissionTimeLeft = (deadLine.getTime() - submittedDate.getTime())/(1000*60); // value in minutes
			
			if(resultSet.getString(5).contains("YES")){
				extendedDeadline = 1;
			}else{
				extendedDeadline = 0;
			}
			
			tmpResultSet = databaseStatement1.executeQuery("select * from imageDB where filename ='"+id+"'");
			while(tmpResultSet.next()){
				imageCount = Integer.parseInt(tmpResultSet.getString(1));
			}
			
			tmpResultSet = databaseStatement1.executeQuery("select * from totalWords where filename ='"+id+"'");
			while(tmpResultSet.next()){
				totalWordCount = Integer.parseInt(tmpResultSet.getString(1));
				marks = Integer.parseInt(tmpResultSet.getString(2));
			}
			
			tmpResultSet = databaseStatement1.executeQuery("select * from tableInfo where filename='"+id+"'");
			while(tmpResultSet.next()){
				tables = tmpResultSet.getInt(1);
			}
			//System.out.format("Id %s got %d marks for %d totalWordCount %d images and %f timeLeftForSubmission\n",id,marks,totalWordCount,imageCount,submissionTimeLeft);
			marksDataSet[counter] = marks;
			parametersDataSet[counter] = new double[]{totalWordCount,imageCount,submissionTimeLeft,tables};
			
			actualMarks[counter] = marks;
			totalWords[counter] = totalWordCount;
			image[counter] = imageCount;
			submissionTime[counter] = submissionTimeLeft;
			tableCount[counter] = tables;
			
			//System.out.format("%d,%f\n",marks,submissionTimeLeft);
			//parametersDataSet[counter] = new double[]{totalWordCount};
			counter++;
		}
		
	    marksDataSet[counter] =0;
	    parametersDataSet[counter] = new double[]{0,0,0,0};
	    
	   
		
		regression.newSampleData(marksDataSet,parametersDataSet);
	
		double [] regressionCoefficients = regression.estimateRegressionParameters();
		double [] errors = regression.estimateResiduals();
		System.out.format("Regression Equation becomes :\n" +"marks obtained = %f%s + %f*%s + %f*%s + %f*%s+ %f*%s\n",regressionCoefficients[0],parameters[0],regressionCoefficients[1],parameters[1],regressionCoefficients[2],parameters[2],regressionCoefficients[3],parameters[3],regressionCoefficients[4],parameters[4]);
		//System.out.format("Regression Equation becomes :\n" +"marks obtained = %f + %f*%s\n",regressionCoefficients[0],regressionCoefficients[1],parameters[0]);			  
		
		
		System.out.println("		-------------");
		System.out.println();
		System.out.println("		|	Errors	|");
		System.out.println(parameters[0]+"     |"+errors[0]+"|");
		System.out.println(parameters[1]+"     |"+errors[1]+"|");
		System.out.println(parameters[2]+"     |"+errors[2]+"|");
		System.out.println(parameters[3]+"     |"+errors[3]+"|");
		System.out.println(parameters[4]+"     |"+errors[4]+"|");
		System.out.println("		-------------");
		
		System.out.println("Standard Error = "+ regression.estimateRegressionStandardError());
		System.out.println("R-Square = "+regression.calculateRSquared()+"\n");
		
		double[][] parameterVariance = regression.estimateRegressionParametersVariance();
		System.out.println("		intercept			totalWordCount		imageCount		submissionTimeLeft   tableCount");
		
		for(int i=0;i<parameterVariance.length;i++){
			System.out.print(parameters[i]+"	");
			for(int y=0;y<parameterVariance[0].length;y++){
				System.out.print(parameterVariance[i][y]+"	");
			}
			System.out.println();
		}
		
		// Ploting actual mark v/s mark from regression equation.
		
		plot_summary_parameters_table_count psp = new plot_summary_parameters_table_count(regressionCoefficients,actualMarks,image,totalWords,submissionTime,tableCount);
		//-----------------------------
		
		
	/*	Covariance cov = new Covariance(parameterVariance);
		RealMatrix covData = cov.getCovarianceMatrix();
		double[][] tempData = covData.getData();
		
		for(int i=0;i<tempData.length;i++){
			System.out.println();
			for(int y=0;y<tempData[0].length;y++){
				System.out.print(tempData[i][y]+"		");
			}
			System.out.println();
		}
	*/
		 
		
	} catch (SQLException e) {
		
		e.printStackTrace();
	}
	  
	}
}
