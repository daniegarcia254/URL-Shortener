package urlshortener2014.mediumcandy.domain;

import java.net.URI;



public class ClickStats {

	private String target;
	private Long numClicks;
	private String owner;
	private URI uri; 
	
	public ClickStats(String target, Long numClicks, String owner, URI uri){
		this.target = target; 
		this.numClicks = numClicks; 
		this.owner = owner; 
		this.setUri(uri); 
	}
	
	public String getTarget() {
		return target;
	}
	public void setTarget(String uri) {
		this.target = uri;
	}
	public Long getNumClicks() {
		return numClicks;
	}
	public void setNumClicks(Long numClicks) {
		this.numClicks = numClicks;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public URI getUri(){
		return uri; 
	}
	public void setUri(URI uri) {
		this.uri = uri;
	}
}
