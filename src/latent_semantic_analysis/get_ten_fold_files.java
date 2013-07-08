package latent_semantic_analysis;

import java.io.File;

public class get_ten_fold_files {
	String textFileDirectory;
	File[][] foldFiles;
	File [] textFiles ;
	int [] index = new int[10];
	
	public get_ten_fold_files(String textDirectory){
		textFileDirectory = textDirectory;
		returnTenFold();
	}
	
	public void returnTenFold(){
		textFiles = new File(textFileDirectory).listFiles();
		int filesPerFold;
		if(textFiles.length%10==0){
			filesPerFold = textFiles.length/10;
		}else{
			filesPerFold = textFiles.length/10 + 1;
		}
		foldFiles = new File[10][filesPerFold];
		int fold;
		for(int i=0;i<textFiles.length;i++){
			fold = i%10;
			foldFiles[fold][index[fold]] = textFiles[i];
			index[fold]++;
		}
	}

	public int getLength(int trainingSetNumber){
		int length = textFiles.length - index[trainingSetNumber];
		return length;
	}
	public String getTextFileDirectory() {
		return textFileDirectory;
	}

	public File[][] getFoldFiles() {
		return foldFiles;
	}

	public int[] getIndex() {
		return index;
	}
}
