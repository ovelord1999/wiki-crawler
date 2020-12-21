package crawler;

import java.lang.annotation.Retention;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WikiCrawler extends Thread {
	private ConcurrentHashMap<String, Integer> urlRefcountMap;
	private String startUrl;
	private List<String> toSearchQueue;
	private int maxCrawl;
	private int maxDepth;
	private int alreadyCrawled;
	private boolean running = false;
	private int currentLevel = 0;
	public int currentLevelUrlsCount = 1;
	public int nextLevelUrlsCount = 0;
	
	/**
	 * @param urlRefcountMap should be empty
	 * @param startUrl the url, where to start the search
	 * @param maxCrawl max number of wiki pages to crawl
	 */
	public WikiCrawler(ConcurrentHashMap<String, Integer> urlRefcountMap
			, String startUrl) {
		this.urlRefcountMap = urlRefcountMap;
		this.startUrl = startUrl;
		this.toSearchQueue = new LinkedList<>();
		this.alreadyCrawled = 0;
		this.maxCrawl = this.maxDepth = -1;
	}
	
	/**
	 * set max number of wiki pages to crawl IF crawling didn't start yet
	 * set to -1 for unbouned search
	 */
	public void setMaxCrawl(int maxCrawl) {
		if (!this.running) {
			this.maxCrawl = maxCrawl;
		}
	}
	
	/**
	 * set max searchDepth (number of steps starting from startUrl)
	 * IF crawling didn't start yet
	 * set to -1 for unbouned search
	 * set to 1 to only parse the startUrl
	 */
	public void setMaxSearchDepth(int maxDepth) {
		if (!this.running) {
			this.maxDepth = maxDepth;
		}
	}
	
	@Override
	public void run() {
		this.running = true;
		toSearchQueue.add(startUrl);
		alreadyCrawled++;
		while (toSearchQueue.size() != 0) {
			int refCount = follow(toSearchQueue.get(0));
			toSearchQueue.remove(0);
			nextLevelUrlsCount += refCount;
			currentLevelUrlsCount--;
			if (currentLevelUrlsCount == 0) {
				currentLevel++;
				currentLevelUrlsCount = nextLevelUrlsCount;
				nextLevelUrlsCount = 0;
			}
		}
		System.out.println("Done crawling");
	}
	
	/** This function generates the references made by <code>url</code>
	 * and adds them to urlRefcountMap.
	 * 
	 * References that were not visited before are added to toSearchQueue
	 * 
	 * @return number of references to NEW urls contained in <code>url</code>
	 */
	private int follow(String url) {
		//TODO: how to deal with unsuccessfull parses (followers == null)
		// mark url as already visited
		Collection<String> followers = HtmlParser.parseRefs(url);
		int newCrawled = 0;
		for (String follower : followers) {
			// add follower to toSearchQueue if search limit not reached yet
			// and follower not visited yet
			if ((maxCrawl == -1 || alreadyCrawled < maxCrawl)
					&& (maxDepth == -1 || currentLevel < maxDepth - 1)
					&& !urlRefcountMap.containsKey(follower)) {
				alreadyCrawled++;
				toSearchQueue.add(follower);
				newCrawled++;
			}
			
			// TODO: the current implementation counts multiple references from a page
			// to another page as a single reference. Allow to choose
			Integer oldCount = urlRefcountMap.get(follower);
			urlRefcountMap.put(follower, 1 + ((oldCount == null) ? 0 : oldCount));
		}
		return newCrawled;
	}
}
