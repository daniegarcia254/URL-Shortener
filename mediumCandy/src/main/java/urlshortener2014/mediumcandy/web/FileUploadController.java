package urlshortener2014.mediumcandy.web;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * 
 * Class in charge of controling the upload of a CSV file with a list of urls, and treat this file
 *  in order to generate a new CSV file by creating shortened urls fo the original urls.
 * 
 * @author MediumCandy
 *
 */
@Controller
public class FileUploadController {
	
	private ExecutorService executor;
	
	/**
	 * Returns the content of the CSV file that has been requested to be downlaoded.
	 * 
	 * @param fileNameServer
	 * @param response
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@RequestMapping(value = "/files/{file_name}", method = RequestMethod.GET)
	public void getFile(@PathVariable("file_name") String fileNameServer,
						HttpServletResponse response) throws IOException, InterruptedException, ExecutionException {
		
		//Delegates the CSV reading into a Task, passing it the name of the file
		executor = Executors.newCachedThreadPool();
		CompletionService<byte[]> pool = new ExecutorCompletionService<>(executor);
		CSVFileDownload answer = new CSVFileDownload(fileNameServer);
		
		pool.submit(answer);
		
		//Takes the Task answer and fill up the HttpServletResponse to the client
		try{
			byte[] res = pool.take().get();
			response.getOutputStream().write(res);
			response.setContentType("application/x-download");
			response.setHeader("Content-disposition", "attachment; filename=" + fileNameServer + ".csv");
			response.setHeader("Content-Lenght", String.valueOf(res.length));
			response.flushBuffer();
		} catch (InterruptedException | ExecutionException ex) {
			ex.printStackTrace();
            throw new IllegalStateException("Cannot get the answer", ex);
        } finally {
        	executor.shutdown();
        }
	}
	
	
	/*  Generates the name for the file to download */
	private String generateString(String name) {
		String timest = new Timestamp(System.currentTimeMillis()).toString();
		String concat = name + timest;
		String hash = String.valueOf(Math.abs(concat.hashCode()));
		
		return hash;
	}
    
	
	/**
	 * 
	 * Uploads the CSV file and passes its content to a new Task for its treatment
	 * 
	 * After the CSV treatment, reads the answer of the Thread and returns a response in consecuence.
	 * 
	 * @param request
	 * @return
	 * @throws InterruptedException
	 */
    @RequestMapping(value="/upload", method=RequestMethod.POST)
    public ResponseEntity<String> handleFileUpload(MultipartHttpServletRequest request) throws InterruptedException{
        Iterator<String> iterator = request.getFileNames();
        
        if ( iterator.hasNext() ) {
            String fileName = iterator.next();
            MultipartFile multipartFile = request.getFile(fileName);
            
            String fileNameServer = "medcandy-" + generateString(fileName);
            if ( !multipartFile.isEmpty() ) {
            	try {
            		byte[] fileContent = multipartFile.getBytes();

            		/*Delegates the CSV treatment into a Task, passing it the temporary name of the file
            		  in the server and the file content  */
            		executor = Executors.newCachedThreadPool();
            		CompletionService<String> pool = new ExecutorCompletionService<>(executor);
            		CSVFileTreatment answer = new CSVFileTreatment(fileNameServer, fileContent);

            		pool.submit(answer);

            		//Takes the Task answer and returns an error or success status
            		String res = pool.take().get();
            		if (res.startsWith("Error: ")){
            			executor.shutdown();
            			res = res.substring("Error: ".length());
            			File csvFile = new File("csv/"+fileNameServer+".csv");
            			csvFile.delete();
            			return new ResponseEntity<>(res, new HttpHeaders(), HttpStatus.BAD_REQUEST);
            		} else {
            			executor.shutdown();
            			return new ResponseEntity<>(fileNameServer, new HttpHeaders(), HttpStatus.OK);
            		}

            	//In case of error, always delete the temporary created file
            	} catch (IOException e) {
            		File csvFile = new File("csv/"+fileNameServer+".csv");
            		csvFile.delete();
            		return new ResponseEntity<>("File is empty", new HttpHeaders(), HttpStatus.BAD_REQUEST);
    			} catch (InterruptedException | ExecutionException ex) {
    				File csvFile = new File("csv/"+fileNameServer+".csv");
    				csvFile.delete();
    				return new ResponseEntity<>("Server Internal Error --> Threads failure", new HttpHeaders(), HttpStatus.BAD_REQUEST);
    			}
            } else {
            	File csvFile = new File("csv/"+fileNameServer+".csv");
				csvFile.delete();
            	return new ResponseEntity<>("Server Internal Error", new HttpHeaders(), HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>("Empty Request", new HttpHeaders(), HttpStatus.BAD_REQUEST); 
    }
}