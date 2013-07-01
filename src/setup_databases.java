import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.BufferedReader;

public class setup_databases {

	Connection databaseConnection = null;
	String 	   database;
	ResultSet  resultSet = null;
	String 	   databaseURL = null;
	String 	   baseURL = "jdbc:mysql://localhost:3306/";
	String	   userName = "root";
	String     password = "";
	
	
	//--------CONSTRUCTOR----------------------------------------
	public setup_databases(String databaseName){
		databaseURL = baseURL+databaseName;
		database = databaseName;
		try {
			databaseConnection = DriverManager.getConnection(databaseURL,userName,password);
		} catch (SQLException e) {
			try {
				databaseConnection = DriverManager.getConnection(baseURL,userName,password); // if the database name given is not present then log into any database and run the create database query.
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		create_database();
		create_tables();
		populate_tables();
	}
	
	
	//----------METHODS---------------------------------------
	public void create_database(){
		System.out.println("Creating database "+database+".....");
		try {
			Statement databaseStatement = databaseConnection.createStatement();
			databaseStatement.executeUpdate("Create database "+database+";");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Created database "+database+".....");
	}
	
	
	public void create_tables(){
		System.out.println("Populating database with tables. Please wait.....");
		try {
			Statement databaseStatement = databaseConnection.createStatement();
			databaseStatement.executeUpdate("use "+database);
			
			databaseStatement.executeUpdate("CREATE TABLE `imageDB` (`noOfImages` int(11) NOT NULL,`marks` int(11) NOT NULL,`filename` varchar(255) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=latin1;");
			
			databaseStatement.executeUpdate("CREATE TABLE `stopWordDB` (`word` varchar(255) NOT NULL DEFAULT '',UNIQUE KEY `word` (`word`)) ENGINE=InnoDB DEFAULT CHARSET=latin1;");
			
			databaseStatement.executeUpdate("CREATE TABLE `submissionTime` (`filename` varchar(255) NOT NULL,`date` varchar(255) NOT NULL DEFAULT '',`time` varchar(255) NOT NULL DEFAULT '',`marks` int(11) NOT NULL,`extendedDeadline` varchar(255) NOT NULL DEFAULT '') ENGINE=InnoDB DEFAULT CHARSET=latin1;");
			
			databaseStatement.executeUpdate("CREATE TABLE `wordDB` (`word` varchar(255) NOT NULL DEFAULT '',`correctGrammar` varchar(255) NOT NULL DEFAULT '',`wordCount` int(11) NOT NULL DEFAULT '0',`wordLength` int(11) NOT NULL DEFAULT '0',`marks` int(11) NOT NULL DEFAULT '0',`filename` varchar(255) NOT NULL DEFAULT '') ENGINE=InnoDB DEFAULT CHARSET=latin1;");
		    
			databaseStatement.executeUpdate("CREATE TABLE `wordList` (`word` varchar(255) NOT NULL DEFAULT '',UNIQUE KEY `word` (`word`)) ENGINE=InnoDB DEFAULT CHARSET=latin1;");
		
			databaseStatement.executeUpdate("CREATE TABLE `totalWords` (`wordCount` int(11) NOT NULL,`marks` int(11) NOT NULL,`filename` varchar(255) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=latin1;");
			
			databaseStatement.executeUpdate("CREATE TABLE `correlationDB` (`correlation` double NOT NULL,`word` varchar(255) NOT NULL DEFAULT '') ENGINE=InnoDB DEFAULT CHARSET=latin1;");
			
			databaseStatement.executeUpdate("CREATE TABLE `uniqueWords` (`word` varchar(255) NOT NULL DEFAULT '') ENGINE=InnoDB DEFAULT CHARSET=latin1;");
			
			databaseStatement.executeUpdate("CREATE TABLE `tableInfo` (`tableCount` int(11) NOT NULL,`filename` varchar(255) NOT NULL DEFAULT '' ENGINE=InnoDB DEFAULT CHARSET=latin1;");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Done.......");
	}
	
	
	public void populate_tables(){
		System.out.println("Setting up default tables with the nessecary information. Please wait....");
		
		String defaultFileNames[] = {"/Users/abhishekdewan/Documents/workspace/Thesis Automarker/britcaps.txt","/Users/abhishekdewan/Documents/workspace/Thesis Automarker/brit-a-z.txt","/Users/abhishekdewan/Documents/workspace/Thesis Automarker/stopWordList"};
		Statement databaseStatement = null;
		BufferedReader bufferedReader = null;
		String line = null;
		
		
		try {
			databaseStatement = databaseConnection.createStatement();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		
		for(int i =0;i<defaultFileNames.length;i++){
			try {
				  bufferedReader = new BufferedReader(new FileReader(defaultFileNames[i]));
				  line = bufferedReader.readLine();
				
				// populate stopwords table;  
				  if(i==2){   
					try{
				    System.out.println("Populating the stop words...");
					while(line!=null){
						if(line != ""){
							if(line.contains("'")){
								databaseStatement.executeUpdate("insert into stopWordDB values(\""+line+"\")");// inserting values into the database	
							}else{
								databaseStatement.executeUpdate("insert into stopWordDB values(\'"+line+"\')");// inserting values into the database
							}
						}
					line = bufferedReader.readLine();
					}
					}catch(SQLException e){
					  line = bufferedReader.readLine();
					}
				}
				
				//populate dictionary table;
				  if(i<=1){  
					  System.out.println("Populating the dictionary...");
					  while(line!=null){
					  try{
					  	if(line != ""){
								if(line.contains("'")){
									databaseStatement.executeUpdate("insert into wordList values(\""+line+"\")");// inserting values into the database	
								}else{
									databaseStatement.executeUpdate("insert into wordList values(\'"+line+"\')");// inserting values into the database
								}
							}
						line = bufferedReader.readLine();
						}catch(SQLException e){
							line = bufferedReader.readLine();
						  }
					  }
					}
				
				  bufferedReader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Done.....");
	}
}
