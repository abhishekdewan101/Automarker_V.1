import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;


public class pdf_to_text {

	
	String inputDirectory;
	String outputDirectory;
	boolean singularFile;
	
	//----------CONSTRUCTOR----------------------------
	public pdf_to_text(String inDirectory,String outDirectory,boolean singleFile){
		inputDirectory = inDirectory;
		outputDirectory = outDirectory;
		singularFile = singleFile;
	}
	
	
	//-----------METHODS---------------------------
	public void convertToText(){
		
		File[] thesisFiles = new File(inputDirectory).listFiles();
		
		for(int i=0;i<thesisFiles.length;i++){
			if(!thesisFiles[i].getName().contains(".DS_Store")){
				System.out.println("Converting "+ thesisFiles[i].getName()+".....");
				String path = thesisFiles[i].getAbsolutePath();
				PdfReader reader;
				
				try {
				 reader = new PdfReader(path);
        		 int n = reader.getNumberOfPages();	
        		 File file = new File(outputDirectory+thesisFiles[i].getName().substring(0, 7)+".txt");
        		 
        		 if (!file.exists()) {
 					file.createNewFile(); // if file does not exsists then create a new one
        		 }else{
 					file.delete();  // if a old version of the file exsists then delete that and create a new one
 					file.createNewFile();
 				 }

        		 for(int j = 1;j <= n;j++){
        			 	String str=PdfTextExtractor.getTextFromPage(reader, j); //Extracting the content from a particular page.
        	          	FileWriter fileWritter = new FileWriter(file,true);
        		        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
        		        bufferWritter.write(str);
        		        bufferWritter.close();
   									}
				} catch (IOException e) {
					e.printStackTrace();
				} catch(java.lang.IllegalArgumentException e){
					System.out.println("File Not converted ..."+thesisFiles[i].getName());
				}
			}
		}
		System.out.println("Done.....");
	}
}
