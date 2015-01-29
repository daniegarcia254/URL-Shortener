package urlshortener2014.mediumcandy.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import urlshortener2014.mediumcandy.util.RequestContextAwareCallable;

/**
 * Task used to read the content of the CSV file that is intended to be downloaded.
 * 
 * Returns the CSV file content.
 * 
 * @author Daniel
 *
 */
public class CSVFileDownload extends RequestContextAwareCallable<byte[]>  {

	private String fileName;
	UrlShortenerControllerWithLogs controller;
	
	public CSVFileDownload(String fileNameServer){
		this.fileName = fileNameServer;
	}
	
	@Override
	public byte[] onCall() {
		
		File csvFile = new File("csv/"+fileName+".csv");
		
		try(FileInputStream fis = 
				new FileInputStream(
						new File(csvFile.getAbsolutePath()))) {

    		byte[] fileContent = new byte[(int) csvFile.length()];
    		fis.read(fileContent);
    		
    		return fileContent;
    		
		} catch (IOException ex) {
			return ex.getMessage().getBytes();
		} finally {
			csvFile.delete();
		}
	}
}
