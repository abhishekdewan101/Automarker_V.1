import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

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
	}
	
	private void calculate_correlation(){
		try {
			Statement databaseStatment = databaseConnection.createStatement();
			double[] tmp_marks;
			double[] tmp_wordCount;
			int wordCount; 
			int marks;
			double correlationFactor;
			
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
				
				correlationFactor = new PearsonsCorrelation().correlation(tmp_marks, tmp_wordCount);
				System.out.println("Word "+differentWords.get(i).toString()+" has a correlation factor of "+correlationFactor);
			}
		} catch (SQLException e) {
			e.printStackTrace();
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
