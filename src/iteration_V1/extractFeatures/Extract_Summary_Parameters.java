/*Extract_Features class provides methods for extracting
 * the following features
 * 1. Image Count - No: of images in a particular file
 * 2. Table Count - No: of tables present in a particular file
 * 3. Submission Times - difference between the time of submission
 *     					 and the deadline.
 * 4. TotalWord Count - total No: of words in a theses*/

package iteration_V1.extractFeatures;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Extract_Summary_Parameters {
	ArrayList<String> stopWords = new ArrayList<String>();
	ArrayList<String> dictionaryWords = new ArrayList<String>();
	public Extract_Summary_Parameters(){
		// sets up the stopWords and dictionaryWords
		String[] fileName = {"brit-a-z.txt","wordList","britcaps.txt","csWords","stopWordList"};
		try {
			for(int i =0;i<fileName.length;i++){
				System.out.println("Setting up "+fileName[i]);
				File tmpFile = new File(fileName[i]);
			    FileChannel fileChannel = new FileInputStream(tmpFile).getChannel();
			    ByteBuffer contentsBuffer = ByteBuffer.allocate((int)fileChannel.size()); 
			    fileChannel.read(contentsBuffer);
				fileChannel.close();
				String contents = new String(contentsBuffer.array());
				StringTokenizer st = new StringTokenizer(contents);
				while(st.hasMoreTokens()){
					String tmp = st.nextToken();
					if(fileName[i].equals("stopWordList")){
						stopWords.add(tmp);
					}else{
						if(!dictionaryWords.contains(tmp)){
							dictionaryWords.add(tmp);
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int totalWordCount(File inputFile){
		System.out.println("Extracting total word count for "+ inputFile.getName());
		
		int totalWordCount = 0;
		try{
		FileChannel fileChannel = new FileInputStream(inputFile).getChannel();
	    ByteBuffer contentsBuffer = ByteBuffer.allocate((int)fileChannel.size()); 
	    fileChannel.read(contentsBuffer);
		fileChannel.close();
		String contents = new String(contentsBuffer.array());
		StringTokenizer st = new StringTokenizer(contents);
		while(st.hasMoreTokens()){
			st.nextToken();
			totalWordCount++;
		}
		}catch(IOException e){
			e.printStackTrace();
		}
		return totalWordCount;
	}
	
	public int totalImageCount(File inputFile){
		System.out.println("Extracting total image count for "+ inputFile.getName());
		String regex="(?i)(Figure [0-9]*[?.][?.0-9]* |Figure\\s[0-9]*[?-][0-9]*|Figure [0-9]*|Figure[0-9]*|Fig. [0-9][a-zA-Z]|Image [0-9]|Diagram [0-9])";
		Pattern stringChecker = Pattern.compile(regex);
		Matcher match;
		ArrayList<String> images = new ArrayList<String>();
		try{
			FileChannel fileChannel = new FileInputStream(inputFile).getChannel();
		    ByteBuffer contentsBuffer = ByteBuffer.allocate((int)fileChannel.size()); 
		    fileChannel.read(contentsBuffer);
			fileChannel.close();
			String contents = new String(contentsBuffer.array());
			match = stringChecker.matcher(contents);
			while(match.find()){
				if(match.group().length()!=0){
					
					if(images.contains(match.group())==false){
					images.add(match.group());
					}
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		return images.size();
		
	}
	
	public int totalTableCount(File inputFile){
		System.out.println("Extracting total table count for "+ inputFile.getName());
		String regex="\\bTable [0-9]*-[0-9]*\\b|\\bTable ?[a-zA-Z][0-9]*?\\.|\\bTable [0-9]*?\\.[0-9]*?\\.?[0-9]*\\b|\\bTable [0-9]*\\b";
		Pattern stringChecker = Pattern.compile(regex);
		Matcher match;
		ArrayList<String> foundTables = new ArrayList<String>();
		try{
			FileChannel fileChannel = new FileInputStream(inputFile).getChannel();
		    ByteBuffer contentsBuffer = ByteBuffer.allocate((int)fileChannel.size()); 
		    fileChannel.read(contentsBuffer);
			fileChannel.close();
			String contents = new String(contentsBuffer.array());
			match = stringChecker.matcher(contents);
			while(match.find()){
				if(match.group().length()!=0){
					
					if(foundTables.contains(match.group())==false){
					foundTables.add(match.group());
					}
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		return foundTables.size();
	}
	
	public double submissionTimeLeft(File inputFile){
		System.out.println("Extracting submission time left for "+ inputFile.getName());
		double submissionTimeLeft = 0;
		try {
			Connection databaseConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/test","root","");  //change the dependency on SQL to serializable file later
			ResultSet resultSet;
			Statement databaseStatement = databaseConnection.createStatement();
			String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
			Date deadLine = new Date(2012,9,30,23,59,59);
			Date submittedDate;
			
			
			String tmp[] = inputFile.getName().split("_");
			resultSet = databaseStatement.executeQuery("select * from submissionTime where filename='"+tmp[0]+"'");
			while(resultSet.next()){
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
			}
 		} catch (SQLException e) {
			e.printStackTrace();
		}
		return submissionTimeLeft;
	}
}
