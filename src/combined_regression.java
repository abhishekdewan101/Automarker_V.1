import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import plotting.scatter_plot;


public class combined_regression {
	File [] textFiles;
	String textFileDirectory;
	Connection databaseConnection = null;
	ResultSet resultSet;
	String databaseName;
	String databaseURL = null;
	String baseURL = "jdbc:mysql://localhost:3306/";
	String userName = "root";
	String password ="";
	
	ArrayList bestWord = new ArrayList();
    double[] regressionCoeff;
    HashMap<String,Double> equationParams = new HashMap<String,Double>();
	
	public combined_regression(String directory,String database){
		textFileDirectory = directory;
		databaseName = database;
		databaseURL = baseURL+databaseName;
		textFiles = new File(textFileDirectory).listFiles();
		try {
			databaseConnection = DriverManager.getConnection(databaseURL,userName,password);
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}
	
	public void execute_steps(){
		calculate_bestwords();
		get_regression_parameters();
		plot();
	}

	private void plot() {
		File [] textFiles = new File(textFileDirectory).listFiles();
		Statement databaseStatement;
		double[] actualMarks = new double[textFiles.length];
		double[] predictedMarks = new double[textFiles.length];
		double submissionTimeLeft;
		Date deadLine = new Date(2012,9,30,23,59,59);
		Date submittedDate = null;
		String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
		
		
		try {
			databaseStatement = databaseConnection.createStatement();
			for(int i=0;i<textFiles.length;i++){
				String [] tmp = textFiles[i].getName().split("_");
				String id = tmp[0];
				double predictedMark = regressionCoeff[0];
				
				resultSet = databaseStatement.executeQuery("select * from imageDB where filename='"+id+"'");
				while(resultSet.next()){
					predictedMark += regressionCoeff[1]*resultSet.getInt(1);
				}
				
				resultSet = databaseStatement.executeQuery("select * from totalWords where filename='"+id+"'");
				while(resultSet.next()){
					predictedMark += regressionCoeff[2]*resultSet.getInt(1);
				}
				
				resultSet = databaseStatement.executeQuery("select * from submissionTime where filename='"+id+"'");
				while(resultSet.next()){
					int day = Integer.parseInt(resultSet.getString(2).substring(0,2));
					int month = 0;
				    for(int j =0;j<months.length;j++){
					 if(months[j].contains(resultSet.getString(2).substring(3,6))){
						 month = j+1;
					 }
					}
				    
					int year = Integer.parseInt(resultSet.getString(2).substring(7,12).trim());
					int hrs = Integer.parseInt(resultSet.getString(3).substring(0,2));
					int mins = Integer.parseInt(resultSet.getString(3).substring(3,5));
					int seconds = Integer.parseInt(resultSet.getString(3).substring(6,8));
					
					submittedDate = new Date(year,month,day,hrs,mins,seconds);
					}
					submissionTimeLeft = (deadLine.getTime() - submittedDate.getTime())/(1000*60); // value in minutes
					predictedMark += regressionCoeff[3]*submissionTimeLeft;
					
					
				resultSet = databaseStatement.executeQuery("select * from wordDB where filename='"+textFiles[i].getName()+"'");
				while(resultSet.next()){
					if(equationParams.containsKey((resultSet.getString(1)))){
						System.out.println(predictedMark);
						actualMarks[i] = resultSet.getInt(5);
						predictedMark += (resultSet.getInt(3)*equationParams.get(resultSet.getString(1)));
					}
				}
				if(predictedMark>100){
					predictedMark = 100;
				}
				
				if(predictedMark<0){
					predictedMark =0;
				}
				
				
				predictedMarks[i] = predictedMark;
				System.out.println(actualMarks[i]+"		"+predictedMarks[i]);
				System.out.println("\n\n\n");
			}			
			
			for(int y =0;y<predictedMarks.length;y++){
				System.out.println(actualMarks[y]+"			"+ predictedMarks[y]);
			}
			System.out.println("Corrleation Factor "+ new PearsonsCorrelation().correlation(actualMarks, predictedMarks));
			scatter_plot plot = new scatter_plot("Actual Mark v/s Predicted Mark", "Actual Mark", "Predicted Mark", actualMarks, predictedMarks);
			plot.pack();
			plot.setVisible(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}

	private void get_regression_parameters() {
		System.out.println("Doing regression please wait.");
		String id;
		int marks;
		int imageCount = 0;
		int totalWords = 0;
		double submissionTimeLeft;
		Date deadLine = new Date(2012,9,30,23,59,59);
		Date submittedDate = null;
		String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
		
		double [] tmp_marks = new double[textFiles.length];
		double [][] parameterData = new double[textFiles.length][];
		
		for(int i=0;i<textFiles.length;i++){
			if(!textFiles[i].getName().contains(".DS_Store")){
			double[] tmp_parameters = new double[bestWord.size()+3];
			String [] tmp = textFiles[i].getName().split("_");
			id = tmp[0];
			marks = Integer.parseInt(tmp[1].substring(0,2));
			try {
				Statement databaseStatement = databaseConnection.createStatement();
				resultSet = databaseStatement.executeQuery("select * from imageDB where filename='"+id+"'");
				while(resultSet.next()){
					imageCount = resultSet.getInt(1);
				}
				
				resultSet = databaseStatement.executeQuery("select * from totalWords where filename ='"+id+"'");
				while(resultSet.next()){
					totalWords = resultSet.getInt(1);
				}
				
				resultSet = databaseStatement.executeQuery("select * from submissionTime where filename='"+id+"'");
				while(resultSet.next()){
				int day = Integer.parseInt(resultSet.getString(2).substring(0,2));
				int month = 0;
			    for(int j =0;j<months.length;j++){
				 if(months[j].contains(resultSet.getString(2).substring(3,6))){
					 month = j+1;
				 }
				}
			    
				int year = Integer.parseInt(resultSet.getString(2).substring(7,12).trim());
				int hrs = Integer.parseInt(resultSet.getString(3).substring(0,2));
				int mins = Integer.parseInt(resultSet.getString(3).substring(3,5));
				int seconds = Integer.parseInt(resultSet.getString(3).substring(6,8));
				
				submittedDate = new Date(year,month,day,hrs,mins,seconds);
				}
				submissionTimeLeft = (deadLine.getTime() - submittedDate.getTime())/(1000*60); // value in minutes
				
				
				tmp_parameters[0] = imageCount;
				tmp_parameters[1] = totalWords;
				tmp_parameters[2] = submissionTimeLeft;
				
			    for(int j=0;j<bestWord.size();j++){
			    	double wordCount =0.0;
			    	resultSet = databaseStatement.executeQuery("select * from wordDB where filename ='"+textFiles[i].getName()+"' and  BINARY word ='"+bestWord.get(j).toString()+"'");
			    	 while(resultSet.next()){
						  wordCount = resultSet.getInt(3);
					  }
			    	 tmp_parameters[j+3]= wordCount;
			    }
			    tmp_marks[i] = marks;
				parameterData[i] = tmp_parameters;	
			} catch (SQLException e) {
				e.printStackTrace();
			}	
		}  
			}
		
		//Set the zero coordinate for the system
		 tmp_marks[(tmp_marks.length)-1]=0;
		  double[]tmp_parameters = new double[bestWord.size()+3];
		  for(int i=0;i<bestWord.size();i++){
			  tmp_parameters[i]=0;
		  }
		  parameterData[(parameterData.length-1)]= tmp_parameters;
		  
		  OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
		  regression.newSampleData(tmp_marks, parameterData);
		  System.out.println("Regression started");
		  regressionCoeff = regression.estimateRegressionParameters();
		  System.out.println("Regression ended");
		  //System.out.print("mark obtained = "+regressionCoeff[0]+" + imageCount * "+regressionCoeff[1]+" + totalWordCount * "+regressionCoeff[2]+" + submissionTimeLeft * "+regressionCoeff[2]);
		  for(int i=0;i<regressionCoeff.length;i++){
              if(i==0){
            	  System.out.print("mark obtained = "+regressionCoeff[0]+" + ");
              }
              if(i==1){
            	  System.out.print(regressionCoeff[i]+"*imageCount +");
              }
              if(i==2){
            	  System.out.print(regressionCoeff[i]+"*totalWordCount +");
              }
              if(i==3){
            	  System.out.print(regressionCoeff[i]+"*submissionTimeLeft +");
              }
              if(i>3){
			  System.out.print(" + "+regressionCoeff[i]+"*"+bestWord.get(i-4).toString());
			  equationParams.put(bestWord.get(i-4).toString(), regressionCoeff[i]);
              }
		  }
		  
		  double[] errors = regression.estimateResiduals();
		  System.out.println("Error");
		  System.out.println("Intercept - "+regressionCoeff[0]);
		  System.out.println("ImageCount - "+regressionCoeff[1]);
		  System.out.println("TotalWords - "+regressionCoeff[2]);
		  System.out.println("SubmissionTimeLeft - "+regressionCoeff[3]);
		  for(int i=4;i<regressionCoeff.length;i++){
			  System.out.println(bestWord.get(i-4).toString()+" - "+regressionCoeff[i]);
		  }
		  System.out.println("Standard Error = "+ regression.estimateRegressionStandardError());
		  System.out.println("R-Square = "+regression.calculateRSquared()+"\n");
		
	}
	
	private void calculate_bestwords() {
		System.out.println("Finding out best words.....");
		double averageCount =0;
		double count =0;
		double average =0;
		int totalExamples = textFiles.length-1;
		try {
			Statement databaseStatement = databaseConnection.createStatement();
			resultSet = databaseStatement.executeQuery("select count(*) from correlationDB");
			while(resultSet.next()){
				count = resultSet.getInt(1);
			}
			
			resultSet = databaseStatement.executeQuery("select correlation from correlationDB");
			while(resultSet.next()){
				averageCount += Math.abs(resultSet.getInt(1));
			}
			
			average = averageCount/count;
			
			resultSet = databaseStatement.executeQuery("select * from correlationDB order by correlation DESC");
			while(resultSet.next()){
				if(Math.abs(resultSet.getDouble(1))>average){
					if(bestWord.size()<=(totalExamples-4)){
						bestWord.add(resultSet.getString(2));
					}
				}
			}
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	
	

}
