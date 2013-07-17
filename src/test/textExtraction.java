package test;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
public class textExtraction {
	public static void main(String[] args){
		 File[] theseFiles = new File("theses").listFiles();
		 for(int i=0;i<theseFiles.length;i++){
			 System.out.println("processing ....."+theseFiles[i].getName());
	     PDDocument pd;
		 BufferedWriter wr;
		 try {
		         File input = new File("theses/"+theseFiles[i].getName());  // The PDF file from where you would like to extract
		         File output = new File("textFiles/"+theseFiles[i].getName().substring(0,7)+".txt"); // The text file where you are going to store the extracted data
		         pd = PDDocument.load(input);
		         System.out.println(pd.getNumberOfPages());
		         System.out.println(pd.isEncrypted());
		         pd.save("CopyOfInvoice.pdf"); // Creates a copy called "CopyOfInvoice.pdf"
		         PDFTextStripper stripper = new PDFTextStripper();
		         wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
		         stripper.writeText(pd, wr);
		         if (pd != null) {
		             pd.close();
		         }
		        // I use close() to flush the stream.
		        wr.close();
		 } catch (Exception e){
		         e.printStackTrace();
		        }
		 }
		 }
}
