package crawler;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlParser {
	// if link contains any of these strings as substring, it's ignored
	private static final String[] IGNORE = {"Category", "Cat%C3%A9gorie"};
	
	/**
	 * A url reference is <a href="url" ...>.
	 * We are interested only in the references inside the body of a wiki page
	 * @return a collection of distinct absolute urls that this url references 
	 * or null if connection to the url unsuccessfull 
	 * or empty collection if url has no references
	 */
	public static Collection<String> parseRefs(String urlString) {
		HashSet<String> result = new HashSet<>();
		try {
			Document doc = Jsoup.connect(urlString).get();
			System.out.println("Now parsing " + doc.title());
			// we only care about references in the body of a page
			Element content = doc.getElementById("content");
			
			Elements elems = content.getElementsByTag("a");
			String hostUrlString = new URL(urlString).getHost();
			for (Element elem : elems) {
				String nextLink = elem.absUrl("href");
				if (nextLink.length() != 0 
						&& hostUrlString.compareTo (new URL(nextLink).getHost()) == 0
						&& !result.contains(nextLink)
						&& !nextLink.contains("action=edit")
						&& !nextLink.startsWith(urlString)
						// TODO: are streams a good idea ?
						&& !Arrays.stream(IGNORE).anyMatch(nextLink::contains)) {
					result.add(nextLink);
				}
			}
			return result;
		} catch (IOException e) {
			return null;
		}
	}
	
	public static void main(String[] args) {
		Collection<String> c = HtmlParser.parseRefs("https://fr.wikipedia.org/wiki/France");
		for (String s : c) {
			System.out.println(s);
		}
		System.out.println(c.size());
	}
}
