package main;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import crawler.HtmlParser;
import crawler.WikiCrawler;

public class Main {
	public static void main(String[] args) {
		ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
		String start = "https://onepiece.fandom.com/wiki/Pandaman";
		int maxCrawl = 2;
		WikiCrawler crawler = new WikiCrawler(map, start);
		crawler.setMaxSearchDepth(maxCrawl);
		crawler.start();
		try {
			crawler.join();
		} catch (InterruptedException e) {
			System.out.println("Unable to join");
			e.printStackTrace();
		}
		map.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue())
			.forEach(pair -> {
				System.out.println(pair.getValue() + " \t " + pair.getKey());
			});
		System.out.println("Map has " + map.size() + " (url,refcount) pairs");
		System.out.println(HtmlParser.parsed);
	}
}