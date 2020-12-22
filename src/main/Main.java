package main;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import crawler.HtmlParser;
import crawler.WikiCrawler;

public class Main {
	public static void main(String[] args) {
		String start = "https://onepiece.fandom.com/wiki/Pandaman";
		int maxCrawlDepth = 3;
		int threadCount = 20;
		
		
		ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
		WikiCrawler crawler = new WikiCrawler(map, start);
		crawler.setThreadCount(threadCount);
		crawler.setMaxSearchDepth(maxCrawlDepth);
		long beginTime = System.currentTimeMillis();
		crawler.start();
		try {
			crawler.join();
		} catch (InterruptedException e) {
			System.out.println("Unable to join");
			e.printStackTrace();
		}
		long finishTime = System.currentTimeMillis();
		
		
		map.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue())
			.forEach(pair -> {
				System.out.println(pair.getValue() + " \t " + pair.getKey());
			});
		System.out.println("Map has: " + map.size() + " (url,refcount) pairs");
		System.out.println("Total number of parsed websites: " + HtmlParser.parsed);
		System.out.println("Time spent crawling " + (finishTime - beginTime));
	}
}