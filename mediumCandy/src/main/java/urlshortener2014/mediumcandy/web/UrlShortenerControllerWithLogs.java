package urlshortener2014.mediumcandy.web;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import urlshortener2014.common.domain.ShortURL;
import urlshortener2014.common.web.UrlShortenerController;
import urlshortener2014.mediumcandy.domain.ClickStats;

/**
 * Class that extends the given UrlShortenerController in order to acomplish some of the basic 
 * tasks of our service:
 *   
 *   - Short a given URL
 *   - Short and customize a given URL
 *   - Get the stats of a given URL
 * 
 * @author MediumCandy
 *
 */
@RestController
public class UrlShortenerControllerWithLogs extends UrlShortenerController {

	private static final Logger logger = LoggerFactory.getLogger(UrlShortenerControllerWithLogs.class);
	
	public ResponseEntity<?> redirectTo(@PathVariable String id, 
			HttpServletRequest request) {
		logger.info("Requested redirection with hash "+id);
		return super.redirectTo(id, request);
	}

	public ResponseEntity<ShortURL> shortener(@RequestParam("url") String url,
			@RequestParam(value = "sponsor", required = false) String sponsor,
			@RequestParam(value = "brand", required = false) String brand,
			HttpServletRequest request) {
		logger.info("Requested new short for uri "+url);
		return super.shortener(url, sponsor, brand, request);
	}
	
	/**
	 * Pings a HTTP URL. This effectively sends a HEAD request and returns TRUE if the response code is in 
	 * the 200-399 range.
	 * @param urlIn The HTTP URL to be pinged.
	 * @return TRUE if the given HTTP URL has returned response code 200-399 on a HEAD request within the
	 * given timeout, otherwise FALSE.
	 */
	private static boolean ping(String urlIn) {
		// Otherwise an exception may be thrown on invalid SSL certificates.
	    String url = urlIn.replaceFirst("https", "http");
	    
	    // The timeout in millis for both the connection timeout and the response read timeout. 
		// Note that the total timeout is effectively two times the given timeout.
	    int timeout = 2000;

	    try {
	    	
	        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
	        connection.setConnectTimeout(timeout);
	        connection.setReadTimeout(timeout);
	        connection.setRequestMethod("HEAD");
	        int responseCode = connection.getResponseCode();
	        
	        return (200 <= responseCode && responseCode <= 399);
	        
	    } catch (IOException exception) {
	        return false;
	    }
	}
	
	/**
	 * Shortens a given URL if this URL is reachable via HTTP.
	 * 
	 * @param url
	 * @param sponsor
	 * @param brand
	 * @param request
	 * @return if OK, status.CREATED and ShortURL else, status.BAD_REQUEST
	 */
	@RequestMapping(value = "/linkreachable", method = RequestMethod.POST)
	public ShortURL shortenerIfReachable(@RequestParam("url") String url,
			@RequestParam(value = "sponsor", required = false) String sponsor,
			@RequestParam(value = "brand", required = false) String brand,
			HttpServletRequest request) {
		ShortURL su = null;
		boolean isReachableUrl = ping(url);
		
		if (isReachableUrl){
			su = createAndSaveIfValid(url, sponsor, brand, UUID
					.randomUUID().toString(), extractIP(request));
		}
		
		return su;
	}
	
	/**
	 * Return stats a given URL.
	 * @param url
	 * @return if OK, status.CREATED and listStatics else, status.BAD_REQUEST
	 */
	@RequestMapping(value = "/linkstats", method = RequestMethod.GET)
	public List<ClickStats> getLinkStats(@RequestParam("url") String url){
		
		List<ShortURL> listShortURL;
		Long infoClicks; 
		List<ClickStats> listResult = new ArrayList<ClickStats>();
		listShortURL = shortURLRepository.findByTarget(url);
		
		for(ShortURL su: listShortURL){
			infoClicks = clickRepository.clicksByHash(su.getHash());
			ClickStats cs = new ClickStats(url, infoClicks, su.getOwner(), su.getUri());
			listResult.add(cs); 
		}
		
		return listResult;
	}
	
	/**
	 * Shortens and customizes a given URL.
	 * @param url
	 * @param brand
	 * @param request
	 * @return if OK status.CREATED and ShortURL else status.BAD_REQUEST
	 */
	@RequestMapping(value = "/linkcustomized", method = RequestMethod.POST)
	public ShortURL shortenerCustomized(@RequestParam("url") String url,
			@RequestParam("brand") String brand,
			HttpServletRequest request) {
		ShortURL su = createAndSaveCustomizedIfValid(url, brand, UUID
					.randomUUID().toString(), extractIP(request));
		
		return su;
	}
	
	private ShortURL createAndSaveCustomizedIfValid(String url, String brand, String owner, String ip) {
		UrlValidator urlValidator = new UrlValidator(new String[] { "http",
				"https" });
			ShortURL exists = shortURLRepository.findByKey(brand);
			if (urlValidator.isValid(url)) {
				if (exists != null) {
					ShortURL toUpdate = new ShortURL(brand, url, linkTo(
							methodOn(UrlShortenerController.class).redirectTo(
									brand, null)).toUri(), exists.getSponsor(), exists.getCreated(), 
									owner, HttpStatus.TEMPORARY_REDIRECT.value(), true, ip, null);
					shortURLRepository.update(toUpdate);
					return toUpdate;
				} else {
					ShortURL su = new ShortURL(brand, url,
							linkTo(
									methodOn(UrlShortenerController.class).redirectTo(
										brand, null)).toUri(), brand, new Date(
										System.currentTimeMillis()), owner,
										HttpStatus.TEMPORARY_REDIRECT.value(), true, ip, null);
					return shortURLRepository.save(su);
				}
			} else {
				return null;
			}		
	}
}