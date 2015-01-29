package urlshortener2014.mediumcandy.web;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.springframework.web.client.RestTemplate;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import urlshortener2014.common.domain.ShortURL;
import urlshortener2014.mediumcandy.util.RequestContextAwareCallable;

/**
 * Task used to treat the uploaded CSV file transforming all the uris it contanins in shortened uris.
 * 
 * The result is storaged in the same temporary file the uploaded CSV file was saved.
 * 
 * Return an "OK" or "Error" string.
 * 
 * @author Daniel
 *
 */
public class CSVFileTreatment extends RequestContextAwareCallable<String>  {

	private String fileName;
	private byte[] fileContent;
	UrlShortenerControllerWithLogs controller;
	
	public CSVFileTreatment(String fileNameServer, byte[] fileCont){
		this.fileName = fileNameServer;
		this.fileContent = fileCont;
	}
	
	@Override
	public String onCall() {
		
		ShortURL su = null;
		String fichero = "";
		File csvFile = new File("csv/"+fileName+".csv");
		
		try(BufferedOutputStream bis = new BufferedOutputStream(
				new FileOutputStream(
						new File(csvFile.getAbsolutePath())))) {
			
			String uri = "", restURI = "";
    		ArrayList<String> listURIs = new ArrayList<String>();
    		
    		//Read each line of the CSV file and parses it to a String
    		for (int i=0; i<fileContent.length; i++){
    			if (fileContent[i] == 13){
    				if (uri.startsWith("\"") && uri.endsWith("\"")){
    					//CSV file with quoted uris
    					uri = uri.substring(1, uri.length()-1);
    				}
    				if (!(listURIs.contains(uri))){
    					listURIs.add(uri);
    					i++;
    					uri = "";
    				}
				} else {
					uri += Character.toString ((char) fileContent[i]);
				}
    		}
    		if (uri.length()>0){
    			//CSV file with no \r\n EOF --> add last uri
    			if (!(listURIs.contains(uri))){
					listURIs.add(uri);
				}
    		}
			
    		int validURL = 0;
    		//Short the URIs from the uploaded CSV file
			for (String s : listURIs){
				restURI = linkTo(methodOn(UrlShortenerControllerWithLogs.class).
		                shortenerIfReachable(s, null, null, null)).toString();
				RestTemplate restTemplate = new RestTemplate();
				su = restTemplate.postForObject(restURI, null, ShortURL.class);
				
				//If the URI is shortened succesfully --> add to the response
				try{
					if (su.getUri() != null){
						String result = "\"" + s + "\",\"" +su.getUri().toString()+"\"\r\n"; 
						fichero += result;
					}
					validURL++;
				} catch (NullPointerException ne){
					//Invalid URI
				}
			}
			
			if (validURL == 0){
				return "Error: All uris the CSV file contains are either invalid or unreachable";
			} else {
				bis.write(fichero.getBytes());
				return "OK";
			}
		} catch (NullPointerException ne){
			return "Error: Either your CSV has a invalid format or contains an invalid/unreachable url";
		} catch (IOException ex) {
			return "Error: IOError writing file to output stream";
		}
	}
}
