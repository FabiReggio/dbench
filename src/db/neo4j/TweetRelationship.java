package db.neo4j;

import org.neo4j.graphdb.RelationshipType;

public interface TweetRelationship {
    public static enum Type implements RelationshipType
    {
        MENTIONS,
        SHARES_URL, 
        HASH_TAGS
    }
}
