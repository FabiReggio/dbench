package db.mongodb;

import java.util.ArrayList;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoDBTweetSocialGraph
{
	// --- Fields ---
	private DBCollection collection;
	private String collection_name;
	private MongoDBClient mongodb;

	// --- Constructor ---
	public MongoDBTweetSocialGraph(MongoDBClient mongodb, String collection)
	{
		this.mongodb = mongodb;
		this.collection_name = collection;
		mongodb.setCollection(collection);
		this.collection = mongodb.getCollection();
	}

	// --- Methods ---
	/**
	 * Builds an if statement containing nodes to igore during the Map-Reduce
	 * function.
	 *
	 * @param type
	 * @param node_list
	 * 		List of nodes to ignore
	 * @return
	 * 		part of an If statement (without ending braces and everything
	 * 		in between)
	 */
	private String setOfNodesToInclude(
			String type,
			ArrayList<String> node_list)
	{
		String cmd = "";

		cmd = "if (";

		for (String node : node_list) {
			cmd += type + " == '" + node + "' || ";
		}

		cmd = cmd.substring(0, cmd.length() - " || ".length());
		cmd += ") {";
		return cmd;
	}

	/**
	 * Builds an if statement to include only certain nodes during the
	 * Map-Reduce function.
	 *
	 * @param type
	 * @param node_list
	 * 		List of nodes to ignore
	 * @return
	 * 		Complete if statement
	 */
	private String includeOnlyTheseNodes(
			String type,
			ArrayList<String> node_list)
	{
		String cmd = "";

		cmd = "if (";

		for (String node : node_list) {
			cmd += type + " != '" + node + "' || ";
		}

		cmd = cmd.substring(0, cmd.length() - " || ".length());
		cmd += ") { return; }";

		return cmd;
	}

	/**
	 * Essentially an iterative breadth first search to obtain a social graph.
	 * But does not always start from the top, it works from the N-th degree
	 * set from param degree.
	 *
	 * @param node_list
	 * 		Nodes to ignore
	 * @param degree
	 * 		N-th degree currently at
	 */
	private void branchOut(ArrayList<String> node_list, int degree, boolean last)
	{
		try {
			String ignore_type = "mention.screen_name";
			String include_only = "";

			// if last degree, only consider previous discovered nodes
			if (last) {
				include_only = includeOnlyTheseNodes(
						"this.screen_name",
						node_list);
			}

			String map = ""
				+ "function() {"
			    + "		if (!this.entities) { return; }"
				+ "		" + include_only
			    + "		var screen_name = this.user.screen_name;"
				+ "		emit({"
				+ "				'type' : 'node',"
				+ "				'value' : screen_name"
				+ "			},"
				+ "		   	{ 'weight' : 0 });"
				+ ""
				+ "		this.entities.user_mentions.forEach("
				+ "			function(mention) {"
				+ "				" + setOfNodesToInclude(ignore_type, node_list)
				+ "					emit({"
				+ "							'type' : 'relationship',"
				+ "							'origin' : screen_name,"
				+ "							'relationship': 'mentions',"
				+ "							'destination' : mention.screen_name"
				+ "					 	},"
				+ "		   				{ 'weight' : 1 });"
				+ "					emit({"
				+ "							'type' : 'node',"
				+ "							'value' : mention.screen_name"
				+ "						},"
				+ "		   				{ 'weight' : 1 });"
				+ " 			}"	// end brace for the if statement
				+ "			}"
				+ "		)"
				+ "	};";

			String reduce = ""
				+ "function(key, values) {"
				+ "    var result = { weight : 0 };"
				+ "    values.forEach(function(value) {"
				+ "            result.weight += value.weight;"
				+ "    });"
				+ "    return result;"
				+ "}";

	        this.collection.mapReduce(
	                map,
	                reduce,
	                "degree_" + degree,
	                null);
	        System.out.println("finished map reduce");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Obtain a node list from newly created degree collection
	 * @param degree
	 * 		N-th degree
	 * @return
	 * 		ArrayList of nodes in the form of a string
	 */
	private ArrayList<String> obtainNodeList(int degree)
	{
		System.out.println("Get node list");
		ArrayList<String> node_list = new ArrayList<String>();
		DBObject pattern = new BasicDBObject();
		pattern.put("_id.type", "node");

		// change to degree collection
		String degree_string = "degree_" + degree;
		System.out.println("getting collection " + degree_string);
		this.mongodb.setCollection(degree_string);
		DBCollection degree_col = this.mongodb.getCollection();
		
		// get data from collection
		DBCursor cursor = degree_col.find(pattern);
		for (DBObject db_obj : cursor) {
			DBObject id = (DBObject) db_obj.get("_id");
			node_list.add((String) id.get("value"));
		}
		
		// revert back to original collection
		this.mongodb.setCollection(this.collection_name);
		this.collection = this.mongodb.getCollection();

		return node_list;
	}

	/**
	 * Creates a social graph
	 * @param start_node
	 * 		Node you wish to start transversing from
	 * @param degree
	 * 		Degress of freedom
	 */
	public void createSocialGraph(String start_node, int degree)
	{
		ArrayList<String> node_list = new ArrayList<String>();
		node_list.add(start_node);
		boolean last = false;

		for (int i = 1; i <= degree; i++) {
			if (i == degree) {
				last = true;
			}
			System.out.println("creating social graph degree leve " + i);

			branchOut(node_list, i, last);
			node_list = obtainNodeList(i);
		}
	}
}
