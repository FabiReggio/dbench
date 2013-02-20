package db.neo4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;
import twitter4j.json.DataObjectFactory;

public class Neo4jTweetImporter
{
	// --- Fields ---
	private EmbeddedNeo4jClient local_db;
	private RestfulNeo4jClient restful_db;

	// --- Constructors ---
	public Neo4jTweetImporter(EmbeddedNeo4jClient client)
	{
		this.local_db = client;
	}

	public Neo4jTweetImporter(RestfulNeo4jClient client)
	{
		this.restful_db = client;
	}

	// --- Methods ---
	/**
	 * Add Tweets to Neo4j
	 * @param file
	 * @param tweets
	 * 		Number of tweets to add
	 * @param embedded
	 * @return
	 * 		True or False
	 */
	public boolean importTweets(String fp, int tweets, boolean embedded)
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
				// display line count
				if (count != 0 && (count % 1000) == 0)
					System.out.println("inserted:" + count);

				// check limit
				if (count == limit) break;
				count++;

				// try and parse tweet
				Transaction tx = this.local_db.graph_db.beginTx();
				try {
					json_string = line_iter.next();
					tweet = DataObjectFactory.createStatus(json_string);

					if (embedded) {
						addTweetToEmbeddedDB(tweet);
					} else {
//						addTweetToRestfulDB(tweet);
					}
					tx.success();
				} catch (TwitterException e) {
					System.out.println("error! bad tweet on line: " + count);
					bad_tweets_record.add(count);
					bad_tweets++;
				} finally {
					tx.finish();
				}
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
					System.out.println((i + 1) + ":" + bad_tweets_record.get(i));
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
	public boolean importTweets(String fp, boolean embedded)
	{
		return importTweets(fp, -1, embedded);
	}

	/**
	 * Insert Tweet to Embedded Neo4j
	 * @param tweet
	 */
	public boolean addTweetToEmbeddedDB(Status tweet) {
		try {
			String tweet_author = tweet.getUser().getScreenName();
			UserMentionEntity[] user_mentions = tweet.getUserMentionEntities();
			HashtagEntity[] hash_tags = tweet.getHashtagEntities();
			URLEntity[] urls = tweet.getURLEntities();
			Node author_node;
			Node node;

			// create a node for the author
			author_node = this.local_db.nodeExists(tweet_author, NodeType.USER);
			if (author_node == null) {
				this.local_db.addNode(NodeType.USER, tweet_author);
				author_node = this.local_db.nodeExists(
						tweet_author,
						NodeType.USER);
			}

			// iterate through user_mentions, hash tags and urls and create
			// relationships with the author node
			// USER MENTIONED
			String user_mentioned;
			for (UserMentionEntity user_mention : user_mentions) {
				user_mentioned = user_mention.getScreenName();
				if (user_mentioned != null) {
					node = this.local_db.nodeExists(
							user_mentioned,
							NodeType.USER);

					if (node != null) {
						this.local_db.incrementNodeWeight(node);
					} else {
						this.local_db.addNode(NodeType.USER, user_mentioned);
						node = this.local_db.nodeExists(
								user_mentioned,
								NodeType.USER);
					}

					this.local_db.createRelationship(
							author_node,
							node,
							TweetRelationship.Type.MENTIONS);
				}
			}

			// HASH TAGGED
			String tag;
			for (HashtagEntity hash_tag : hash_tags) {
				tag = hash_tag.getText();
				if (tag != null) {
					node = this.local_db.nodeExists(tag, NodeType.HASH_TAG);

					if (node != null) {
						this.local_db.incrementNodeWeight(node);
					} else {
						this.local_db.addNode(NodeType.HASH_TAG, tag);
						node = this.local_db.nodeExists(tag, NodeType.HASH_TAG);
					}

					this.local_db.createRelationship(
							author_node,
							node,
							TweetRelationship.Type.HASH_TAGS);
				}
			}

			// SHARED URLS
			String display_url;
			for (URLEntity url : urls) {
				display_url = url.getDisplayURL();
				if (display_url != null) {
					node = this.local_db.nodeExists(display_url, NodeType.URL);

					if (node != null) {
						this.local_db.incrementNodeWeight(node);
					} else {
						this.local_db.addNode(NodeType.URL, display_url);
						node = this.local_db.nodeExists(
								display_url,
								NodeType.URL);
					}

					this.local_db.createRelationship(
							author_node,
							node,
							TweetRelationship.Type.HASH_TAGS);
				}
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

//	/**
//	 * Insert Tweet to Embedded Neo4j
//	 * @param tweet
//	 */
//	public boolean addTweetToRestfulDB(Status tweet) {
//		try {
//			String tweet_author = tweet.getUser().getScreenName();
//			UserMentionEntity[] user_mentions = tweet.getUserMentionEntities();
//			HashtagEntity[] hash_tags = tweet.getHashtagEntities();
//			URLEntity[] urls = tweet.getURLEntities();
//			Node author_node;
//			Node node;
//
//			// create a node for the author
//			author_node = this.restful_db.nodeExists(tweet_author, NodeType.USER);
//			if (author_node == null) {
//				this.restful_db.addNode(NodeType.USER, tweet_author);
//				author_node = this.restful_db.nodeExists(
//						tweet_author,
//						NodeType.USER);
//			}
//
//			// iterate through user_mentions, hash tags and urls and create
//			// relationships with the author node
//			// USER MENTIONED
//			String user_mentioned;
//			for (UserMentionEntity user_mention : user_mentions) {
//				user_mentioned = user_mention.getScreenName();
//				if (user_mentioned != null) {
//					node = this.restful_Db.nodeExists(
//							user_mentioned,
//							NodeType.USER);
//
//					if (node != null) {
//						this.restful_db.incrementNodeWeight(node);
//					} else {
//						this.restful_db.addNode(NodeType.USER, user_mentioned);
//						node = this.restful_db.nodeExists(
//								user_mentioned,
//								NodeType.USER);
//					}
//
//					this.restful_db.createRelationship(
//							author_node,
//							node,
//							TweetRelationship.Type.MENTIONS);
//				}
//			}
//
//			// HASH TAGGED
//			String tag;
//			for (HashtagEntity hash_tag : hash_tags) {
//				tag = hash_tag.getText();
//				if (tag != null) {
//					node = this.restful_db.nodeExists(tag, NodeType.HASH_TAG);
//
//					if (node != null) {
//						this.restful_db.incrementNodeWeight(node);
//					} else {
//						this.restful_db.addNode(NodeType.HASH_TAG, tag);
//						node = this.restful_db.nodeExists(
//								tag,
//								NodeType.HASH_TAG);
//					}
//
//					this.restful_db.createRelationship(
//							author_node,
//							node,
//							TweetRelationship.Type.HASH_TAGS);
//				}
//			}
//
//			// SHARED URLS
//			String display_url;
//			for (URLEntity url : urls) {
//				display_url = url.getDisplayURL();
//				if (display_url != null) {
//					node = this.restful_db.nodeExists(
//							display_url,
//							NodeType.URL);
//
//					if (node != null) {
//						this.restful_db.incrementNodeWeight(node);
//					} else {
//						this.restful_db.addNode(NodeType.URL, display_url);
//						node = this.restful_db.nodeExists(
//								display_url,
//								NodeType.URL);
//					}
//
//					this.restful_db.createRelationship(
//							author_node,
//							node,
//							TweetRelationship.Type.HASH_TAGS);
//				}
//			}
//		} catch (NullPointerException e) {
//			e.printStackTrace();
//			return false;
//		}
//		return true;
//	}
}
