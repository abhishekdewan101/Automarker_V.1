import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStream;

/*This class would be responsible for extracting the different features from the text files and
 * putting them in the database tables.
 * The main features being currently extracted from the text files are
 * 1. Word Length
 * 2. Word Count - the word count is taken in tandom with the marks alloted for that word.
 * 3. Checking for grammar.
 * 4. Images are extracted out of the pdf files.
 * 5. The submission times for the different theses*/
public class extractFeatures {

	String inputDirectory;
	String submissionTimeFile;
	String outDirectory;
	Connection databaseConnection = null;
	String 	   databaseURL = null;
	String 	   baseURL = "jdbc:mysql://localhost:3306/";
	String	   userName = "root";
	String     password = "";
	
	
	//-----------------CONSTRUCTOR---------------------------------
	public extractFeatures(String inDirectory,String databaseName,String subTime,String outputDirectory){
		outDirectory = outputDirectory;
		inputDirectory = inDirectory;
		submissionTimeFile = subTime;
		databaseURL = baseURL+databaseName;
		try {
			databaseConnection = DriverManager.getConnection(databaseURL,userName,password);
		} catch (SQLException e) {
			try {
				databaseConnection = DriverManager.getConnection(baseURL,userName,password);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	
	//-----------------METHODS---------------------------------
	public void startExtraction(){
		extractImages();
		extractSubmissionTime();
		extractTotalWord();
	}
	
	
	
	// Extract the images of the PDF files and store them in the mysql database along with the marks awarded.
	public void extractImages(){
		System.out.println("Extracting Images and populating the database");
		File[] thesisFiles = new File(inputDirectory).listFiles();
		PdfReader reader;
		int imageCount;
		int marks;
		Statement databaseStatement = null;
		
		try{
			databaseStatement = databaseConnection.createStatement();
		}catch(SQLException e){
			e.printStackTrace();
		}
		
		for(int i=0;i<thesisFiles.length;i++){
			if(thesisFiles[i].getName().contains(".DS_Store")==false){
			System.out.println("Processing "+thesisFiles[i].getName()+"....");	
			imageCount = 0;
		
			try {
				reader = new PdfReader(thesisFiles[i].getAbsolutePath());
				for (int y = 0; y < reader.getXrefSize(); y++) {
			        PdfObject pdfobj = reader.getPdfObject(y);
			        if (pdfobj == null || !pdfobj.isStream()) {
			            continue;
			        }
			        PdfStream stream = (PdfStream) pdfobj;
			        PdfObject pdfsubtype = stream.get(PdfName.SUBTYPE);
			        if (pdfsubtype != null && pdfsubtype.toString().equals(PdfName.IMAGE.toString())) {
			         imageCount++;
			        }
			   }
			  String [] temp = thesisFiles[i].getName().split("\\_");
			  marks = Integer.parseInt(temp[1].substring(0,2));	
			  databaseStatement.executeUpdate("insert into imageDB values("+imageCount+","+marks+")");
			
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
		System.out.println("Done......");
  }
	
	public void extractSubmissionTime(){
		System.out.println("Extracting Submission time and populating the database");
		
		File submissionFile = new File(submissionTimeFile);
        File[] thesisFiles = new File(inputDirectory).listFiles();		
		String line;
		String date;
		String time;
		String id;
		String extended;
		int marks = 0;
		Statement databaseStatement = null;
		BufferedReader reader = null;
		try {
			 databaseStatement = databaseConnection.createStatement();
			 reader = new BufferedReader(new FileReader(submissionFile));
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try {			
			while((line = reader.readLine())!= null){
				id = line.substring(0, 4);
				date = line.substring(9,21);
				time = line.substring(21,29);
				
				//find the marks associated with the id
				for(int i =0;i<thesisFiles.length;i++){
					if(thesisFiles[i].getName().contains(id)){
						System.out.println("Processing "+thesisFiles[i].getName()+"....");
						String[] tmp = thesisFiles[i].getName().split("\\_");
						marks = Integer.parseInt(tmp[1].substring(0,2));
						break;
					}
				}
				
				//find whether there was a extended deadline
				if(line.substring(line.length()-1,line.length()).contains("E")){
					extended = "YES";
				}else{
					extended = "NO";
				}
				databaseStatement.executeUpdate("insert into submissionTime values('"+date+"','"+time+"','"+marks+"','"+extended+"')");
				
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		System.out.println("Done......");
	}
	
	public void extractTotalWord(){
		 System.out.println("Extracting Total Words ........");
		 Statement databaseStatement = null;
		try {
			databaseStatement = databaseConnection.createStatement();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		 File[] thesisFiles = new File(outDirectory).listFiles();
		 String regex = "[a-zA-z0-9]+@?+[\\.]?";
		 String contents;
		 int totalCountOfWords;
		 int marks = 0;
		 FileInputStream fileInput;
		 FileChannel fileChannel; 
		 ByteBuffer contentsBuffer;
		 Pattern stringChecker = Pattern.compile(regex);
		 Matcher match;
		 
		 for(int i = 0;i<thesisFiles.length;i++){
		   totalCountOfWords = 0;	 
		  if(thesisFiles[i].getName().contains(".DS_Store")==false){
			  System.out.println("Processing "+thesisFiles[i].getName()+".....");	
			  String [] tmp = thesisFiles[i].getName().split("\\_");
			  marks = Integer.parseInt(tmp[1].substring(0,2));
			  try {
				fileInput = new FileInputStream(thesisFiles[i]);
				fileChannel = fileInput.getChannel();
				contentsBuffer = ByteBuffer.allocate((int)fileChannel.size());
				fileChannel.read(contentsBuffer);
				fileChannel.close();
				contents = new String(contentsBuffer.array());
				
				match = stringChecker.matcher(contents);
				
				while(match.find()){
					if(match.group().length()!=0){
						totalCountOfWords++;
					}
				}
				databaseStatement.executeUpdate("insert into totalWords values("+totalCountOfWords+","+marks+")");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		  }
		 }
		 
		 System.out.println("Done.....");
	}
	
}
