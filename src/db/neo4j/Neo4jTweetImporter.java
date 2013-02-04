package db.neo4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

public class Neo4jTweetImporter
{
	// --- Fields ---
	private EmbeddedNeo4jClient db;

	// --- Constructors ---
	public Neo4jTweetImporter(EmbeddedNeo4jClient db)
	{
		this.db = db;
	}

	// --- Methods ---
	/**
	 * Add Tweets to Neo4j
	 * @param file
	 * @param tweets
	 * 		Number of tweets to add
	 * @return
	 * 		True or False
	 */
	public boolean importTweets(String fp, int tweets)
	{
		String json_string = "";
	    long bad_tweets = 0; // number of bad tweets
	    ArrayList<Long> bad_tweets_record = new ArrayList<Long>();
		long limit = tweets;
		long count = 0;
		Status tweet = null;

		try {
			LineIterator line_iter = FileUtils.lineIterator(new File(fp));

			while (line_iter.hasNext()) {
				// check limit
				if (count == limit) break;
				count++;

				// try and parse tweet
				try {
					json_string = line_iter.next();
					tweet = DataObjectFactory.createStatus(json_string);
				} catch (TwitterException e) {
					System.out.println("error! bad tweet on line: " + count);
					bad_tweets_record.add(count);
					bad_tweets++;
				}

				if (tweet != null) this.db.addTweet(tweet);
				tweet = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			System.out.println("Inserted: " + (count - bad_tweets));
			System.out.println("Number of Bad Tweets: " + bad_tweets);
			if (bad_tweets > 0) {
				System.out.println("On line numbers:");
				for (int i = 0; i < bad_tweets_record.size(); i++)
					System.out.println((i + 1) + ": " + bad_tweets_record.get(i));
			}
		}
		return true;
	}

	/**
	 * Add Tweets to Neo4j
	 * @param file
	 * @param tweets
	 * 		Number of tweets to add
	 * @return
	 * 		True or False
	 */
	public boolean importTweets(String fp)
	{
		return importTweets(fp, -1);
	}
}
