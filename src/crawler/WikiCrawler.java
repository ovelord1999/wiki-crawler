package crawler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;



public class WikiCrawler extends Thread {
	private ConcurrentHashMap<String, Integer> urlRefcountMap;
	private String startUrl;
	private boolean running = false;
	private int maxDepth;
	private int threadCount = 1;
	
	/**
	 * @param urlRefcountMap expected empty
	 * @param startUrl the url, where to start the search
	 */
	public WikiCrawler(ConcurrentHashMap<String, Integer> urlRefcountMap
			, String startUrl) {
		/* Here ConcurrentHashMap is used instead of generic Map because
		 * different threads (threadCount) working on the crawling need
		 * a synchronized map to avoid race conditions */
		
		this.urlRefcountMap = urlRefcountMap;
		this.startUrl = startUrl;
	}
	
	/**
	 * set max searchDepth (number of steps starting from startUrl)
	 * This method has effect only if the Thread didn't start yet
	 * set to 1 to only parse the startUrl
	 * set to -1 for unbouned search
	 */
	public void setMaxSearchDepth(int maxDepth) {
		if (!this.running) {
			this.maxDepth = maxDepth;
		}
	}
	
	/**
	 * default value 1
	 * This method has effect only if the Thread didn't start yet
	 * @param threadCount number of threads to used in the search
	 */
	public void setThreadCount(int threadCount) {
		if (!this.running) {
			this.threadCount = threadCount;
		}
	}
	
	public void run() {
		this.running = true;
		
		ConcurrentLinkedQueue<String> currentLevelUrls = new ConcurrentLinkedQueue<>();
		ConcurrentLinkedQueue<String> nextLevelUrls = new ConcurrentLinkedQueue<>();
		currentLevelUrls.add(startUrl);
		int currentLevel = 0;
		while (currentLevel < maxDepth) {
			
			List<List<String>> searcherThreadsLists = new ArrayList<List<String>>();
			for (int i = 0; i < threadCount; i++) {
				searcherThreadsLists.add(new LinkedList<>());
			}
			
			// distribute the urls between different searcherThreadLists
			int counter = 0;
			for (String url : currentLevelUrls) {
				searcherThreadsLists.get(counter).add(url);
				counter = (counter + 1) % threadCount;
			}
			
			// start searcherThreads
			Thread[] searcherThreads = new Thread[threadCount];
			for (int i = 0; i < threadCount; i++) {
				searcherThreads[i] = new SearcherThread(searcherThreadsLists.get(i)
						, nextLevelUrls, urlRefcountMap);
				searcherThreads[i].start();
			}
			
			// join searcherThreads
			for (int i = 0; i < threadCount; i++) {
				try {
					searcherThreads[i].join();
				} catch (InterruptedException e) {
					System.err.println("Failed joining thread " + 
							searcherThreads[i].getName());
				}
			}
			
			// prepare for next search level
			currentLevelUrls = nextLevelUrls;
			nextLevelUrls = new ConcurrentLinkedQueue<>();
			currentLevel++;
		}
		
		System.out.println("Done parsing");
	}
	
	static class SearcherThread extends Thread {
		private List<String> urls;
		private Queue<String> nextToParse;
		ConcurrentHashMap<String, Integer> urlRefcountMap;
		
		public SearcherThread(List<String> urls, Queue<String> nextToParse, 
				ConcurrentHashMap<String, Integer> urlRefcountMap) {
			this.urls = urls;
			this.nextToParse = nextToParse;
			this.urlRefcountMap = urlRefcountMap;
		}
		
		@Override
		public void run() {
			if (urls == null || urls.size() == 0) return;
			
			for(String url : urls) {
				follow (url);
			}
		}

		/** This function generates the references made by <code>url</code>
		 * and adds them to urlRefcountMap.
		 * 
		 * References that were not visited before are added to toSearchQueue
		 */
		private void follow(String url) {
			//TODO: how to deal with unsuccessfull parses (followers == null)
			Collection<String> followers = HtmlParser.parseRefs(url);
			for (String follower : followers) {
				if (!urlRefcountMap.containsKey(follower)) {
					nextToParse.add(follower);
				}
				
				//TODO: the current implementation counts multiple references from a page
				// to another page as a single reference. Allow to choose
				//TODO: solve racing condition here!
				Integer oldCount = urlRefcountMap.get(follower);
				urlRefcountMap.put(follower, 1 + ((oldCount == null) ? 0 : oldCount));
			}
		}
	}
}
