package db.solr;

/**
 * As the name suggests, this is a tweet object for solr. It aims to provide
 * just enough data for solr. Specifically:
 * - MongoDB object id
 * - Tweet id
 * - Tweet text (most important)
 * 
 * @author chris
 * 
 */
public class TweetForSolr 
{
	// --- Fields ---
	private String obj_id;
	private String tweet_id;
	private String tweet_text;
	
	// --- Constructors ---
	public TweetForSolr() {}
	
	// --- Methods ---
	public String getObjId() 
	{
		return obj_id;
	}

	public void setObjId(String obj_id) 
	{
		this.obj_id = obj_id;
	}

	public String getTweetId() 
	{
		return tweet_id;
	}

	public void setTweetId(String tweet_id) 
	{
		this.tweet_id = tweet_id;
	}

	public String getTweetText() 
	{
		return tweet_text;
	}

	public void setTweetText(String tweet_text) 
	{
		this.tweet_text = tweet_text;
	}
}
