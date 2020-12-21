package crawler;

import java.util.Collection; 
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class WikiCrawler extends Thread {
	private ConcurrentHashMap<String, Integer> urlRefcountMap;
	private String startUrl;
	private List<String> toSearchQueue;
	private int maxCrawl;
	private int alreadyCrawled;
	
	/**
	 * @param urlRefcountMap should be empty
	 * @param startUrl the url, where to start the search
	 * @param maxCrawl max number of wiki pages to crawl
	 */
	public WikiCrawler(ConcurrentHashMap<String, Integer> urlRefcountMap, String startUrl
			, int maxCrawl) {
		this.urlRefcountMap = urlRefcountMap;
		this.startUrl = startUrl;
		this.toSearchQueue = new LinkedList<>();
		this.alreadyCrawled = 0;
		this.maxCrawl = maxCrawl;
	}
	
	@Override
	public void run() {
		toSearchQueue.add(startUrl);
		alreadyCrawled++;
		while (toSearchQueue.size() != 0) {
			follow(toSearchQueue.get(0));
			toSearchQueue.remove(0);
		}
		System.out.println("Done crawling");
	}
	
	/* This function generates the references made by url
	 * and adds them to urlRefcountMap.
	 * 
	 * References that were not visited before are added to toSearchQueue
	 */
	private void follow(String url) {
		// mark url as already visited
		Collection<String> followers = HtmlParser.parseRefs(url);
		for (String follower : followers) {
			// TODO: the current implementation counts multiple references from a page
			// to another page as a single reference. Allow to choose
			Integer oldCount = urlRefcountMap.get(follower);
			urlRefcountMap.put(follower, 1 + ((oldCount == null) ? 0 : oldCount));
			
			if (alreadyCrawled < maxCrawl 
					&& !urlRefcountMap.contains(follower)) {
				alreadyCrawled++;
				toSearchQueue.add(follower);
			}
		}
	}
}
