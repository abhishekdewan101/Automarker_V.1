
public class automark_start {
	public static void main(String[] args){
		String inDirectory = "/Users/abhishekdewan/Documents/workspace/Automarker_V.1/theses";
		String outDirectory = "/Users/abhishekdewan/Documents/workspace/Automarker_V.1/textFiles/";
		String submissionTimeFile = "/Users/abhishekdewan/Documents/workspace/Automarker_V.1/subtime.txt";
		String databaseName = "test";
		
		
		//create objects for different classes
	    pdf_to_text ptt = new pdf_to_text(inDirectory,outDirectory,false);
        setup_databases sd = new setup_databases(databaseName);
	    extractFeatures ef = new extractFeatures(inDirectory,databaseName,submissionTimeFile,outDirectory);
	    
		//call the different methods
	    ptt.convertToText();
		ef.startExtraction();
	}
}
