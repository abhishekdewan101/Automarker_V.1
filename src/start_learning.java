import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

/*Class that starts the learning process for the system.*/
public class start_learning {

	Connection databaseConnection = null;
	String databaseName;
	String databaseURL = null;
	String baseURL = "jdbc:mysql://localhost:3306/";
	String userName = "root";
	String password = "";
	Statement databaseStatement;
	Statement databaseStatement1;
	
	public start_learning(String database){
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
		multi_linear_regression();
	}

	private void multi_linear_regression() {
	  int imageCount = 0;
	  int totalWordCount = 0;
	  float submissionTimeLeft;
	  int extendedDeadline;
	  ResultSet resultSet = null;
	  ResultSet tmpResultSet = null;
	  int marks = 0;
	  String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	  
	  String id;
	  Date deadLine = new Date(2012,9,30,23,59,59);
	  Date submittedDate;
	  
	 
	  try {
		resultSet = databaseStatement.executeQuery("select * from submissionTime");
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
			submissionTimeLeft = (deadLine.getTime() - submittedDate.getTime())/(1000*60*60); // value in hrs
			
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
			
			System.out.format("Id %s got %d marks for %d totalWordCount %d images and %f timeLeftForSubmission\n",id,marks,totalWordCount,imageCount,submissionTimeLeft);
		}
	} catch (SQLException e) {
		
		e.printStackTrace();
	}
	  
	}
}
