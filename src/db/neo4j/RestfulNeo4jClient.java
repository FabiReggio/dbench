package db.neo4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class RestfulNeo4jClient
{
	// --- Fields ---
//    private String SERVER_ADDRESS;
//    private String SERVER_PORT;
	private static String SERVER_URI;


	// --- Constructors ---
	public RestfulNeo4jClient(String addr, String port)
	{
//		this.SERVER_ADDRESS = addr;
//		this.SERVER_PORT = port;
//		this.SERVER_URI = addr + ":" + port + "/db/data";

		if (connect() == false)
			throw new RuntimeException();
		else
			initIndexes();
	}

	// --- Methods ---
	/**
	 * Connect to Neo4j server
	 * @return
	 * 		True or false for success or failure
	 */
	public boolean connect()
	{
		int status = 0;

		WebResource resource = Client.create().resource(SERVER_URI);
		ClientResponse response = resource.get(ClientResponse.class);

		status = response.getStatus();
		response.close();

		if (status == 200) return true;
		else return false;
	}

	private int queryNeo4j(String uri, String json)
	{
		int status = 0;

		// query
        WebResource resource = Client.create().resource(uri);
        ClientResponse response = resource.accept(
        		MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .entity(json)
                .post(ClientResponse.class);

        // response
		status = response.getStatus();
		response.close();

		return status;
	}

	private URI queryNeo4jLocation(String uri, String json)
	{
		URI location = null;

		// query
        WebResource resource = Client.create().resource(uri);
        ClientResponse response = resource.accept(
        		MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .entity(json)
                .post(ClientResponse.class);

        // response
		location = response.getLocation();
        response.close();

        return location;
	}

	/**
	 * Initializes Indexes
	 * @return
	 * 		True or false for success or failure
	 */
	private boolean initIndexes()
	{
		int status = 0;
        final String index_addr = SERVER_URI + "/index/node";
        ArrayList<String> indexes = new ArrayList<String>();

        // indexes
        indexes.add("users");
        indexes.add("hashtags");
        indexes.add("urls");
        indexes.add("rel_mentions");
        indexes.add("rel_hashtags");
        indexes.add("rel_shares");

        // create indexes
        for (String i : indexes) {
	    	status = queryNeo4j(index_addr, "{ \"name\": \"" + i + "\" }");
	    	if (status != 201) return false;
        }

        return true;
	}

	/**
	 * Creates a JSON string ready for CRUD operations with Neo4j
	 * @param property
	 * @param value
	 * @return
	 * 		JSON string
	 */
	public String createJsonString(String[] property, String[] value)
	{
		int array_length = property.length;
		String json = "";

		if (array_length != value.length) {
			throw new RuntimeException();
		} else {
			json += "{ ";
			for (int i = 0; i <= array_length; i++) {
				json += property[i] + ":" + value[i];
				if ((i + 1) != array_length) json += ", ";
			}
			json += " }";
		}

		return json;
	}

	public URI nodeExists(String node_name, String node_type)
	{
		URI node = null;

		return node;
	}

	/**
	 * Create a Neo4j node
	 * @return
	 *  	URL of newly created node else null for failure
	 */
	public URI createNode(String[] property, String[] value)
    {
		URI location = null;
        final String addr = SERVER_URI + "/node";
        // http://localhost:7474/db/data/node

        // query
        location = queryNeo4jLocation(addr, createJsonString(property, value));

        return location;
    }

	/**
	 * Delete Neo4j node
	 * @param node
	 * @return
	 * 		True or false for success or failure
	 */
	public boolean deleteNode(URI node)
	{
		int status = 0;

        // query
        status = queryNeo4j(node.toString(), "{}");

		if (status == 204) return true;
		else return false;
	}

	/**
	 * Add relationship between two nodes
	 * @param start_node
	 * @param end_node
	 * @param rel_type
	 * @param json_attr
	 * @return
	 * @throws URISyntaxException
	 */
    public URI addRelationship(
    		URI start_node,
    		URI end_node,
            String rel_type,
            String json_attr) throws URISyntaxException
    {
    	int status;
        URI fromUri = new URI(start_node.toString() + "/relationships" );
        String relationshipJson = generateJsonRelationship(
        		end_node,
        		rel_type,
        		json_attr );

        WebResource resource = Client.create().resource(fromUri);

        ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
                .type( MediaType.APPLICATION_JSON )
                .entity( relationshipJson )
                .post( ClientResponse.class );

        final URI location = response.getLocation();
        System.out.println( String.format(
                "POST to [%s], status code [%d], location header [%s]",
                fromUri, response.getStatus(), location.toString() ) );

        status = response.getStatus();
        response.close();

		if (status == 201) return location;
		else return null;
    }
    // END SNIPPET: insideAddRel

    public String generateJsonRelationship(
    		URI endNode,
            String relationshipType, String... jsonAttributes )
    {
        StringBuilder sb = new StringBuilder();
        sb.append( "{ \"to\" : \"" );
        sb.append( endNode.toString() );
        sb.append( "\", " );

        sb.append( "\"type\" : \"" );
        sb.append( relationshipType );
        if ( jsonAttributes == null || jsonAttributes.length < 1 )
        {
            sb.append( "\"" );
        }
        else
        {
            sb.append( "\", \"data\" : " );
            for ( int i = 0; i < jsonAttributes.length; i++ )
            {
                sb.append( jsonAttributes[i] );
                if ( i < jsonAttributes.length - 1 )
                { // Miss off the final comma
                    sb.append( ", " );
                }
            }
        }

        sb.append(" }");
        return sb.toString();
    }

    /**
     * Add property to node
     * @param node_uri
     * @param property
     * @param value
     */
    public boolean addProperty(
    		URI node_uri,
    		String property,
            String value)
    {
    	int status = 0;
        String propertyUri = node_uri.toString() + "/properties/" + property;
        // http://localhost:7474/db/data/node/{node_id}/properties/{property_name}

        WebResource resource = Client.create().resource(propertyUri);
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .entity( "\"" + value + "\"" )
                .put( ClientResponse.class );

        status = response.getStatus();
        response.close();

		if (status == 201) return true;
		else return false;
    }

    /**
     * Check to see if database is still running
     * @return
     * 		True or false for yes or no
     */
    public boolean isDatabaseRunning()
    {
    	int status = 0;

        WebResource resource = Client.create().resource(SERVER_URI);
        ClientResponse response = resource.get(ClientResponse.class);

        status = response.getStatus();
        response.close();

		if (status == 200) return true;
		else return false;
    }
}
