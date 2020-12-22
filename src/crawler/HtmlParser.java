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
	private static final String[] IGNORE = {"Category", "Cat%C3%A9gorie", "action=edit"};
	
	public static int parsed = 0;
	
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
			System.out.println(parsed + ". Now parsing " + urlString + " ----------- " + doc.title());
			// we only care about references in the body of a page
			Element content = doc.getElementById("content");
			
			Elements elems = content.getElementsByTag("a");
			String hostUrlString = new URL(urlString).getHost();
			for (Element elem : elems) {
				String nextLink = elem.absUrl("href");
				if (nextLink.length() != 0 
						&& hostUrlString.compareTo (new URL(nextLink).getHost()) == 0
						&& !result.contains(nextLink)
						&& !nextLink.startsWith(urlString)
						// TODO: are streams a good idea ?
						&& !Arrays.stream(IGNORE).anyMatch(nextLink::contains)) {
					result.add(nextLink);
				}
			}
			parsed++;
			return result;
		} catch (IOException e) {
			return null;
		}
	}
}
