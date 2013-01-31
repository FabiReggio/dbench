package db.neo4j;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.neo4j.cypher.SyntaxException;
import org.neo4j.cypher.javacompat.*;

public class CypherQueryController 
{
	// --- Fields ---
	private ExecutionEngine query_engine;

	// --- Constructors ---
	public CypherQueryController(EmbeddedNeo4jClient neo4j_client) {
		this.query_engine = new ExecutionEngine(neo4j_client.graph_db);
	}

	// --- Methods ---
	/**
	 * Query neo4j
	 * @param q
	 * 		query string
	 */
	public void query(String q) {
		try {
			ExecutionResult result = (ExecutionResult) query_engine.execute(q);
			String rows = "";
		
			for (Map<String, Object> row : result) {
				for (Entry<String, Object> column : row.entrySet()) {
					rows += column.getKey() + ": " + column.getValue() + "; ";
					rows += "\n";
				}
				rows += "\n";
			}
			
			System.out.println(rows);
		} catch (SyntaxException e) {
			System.out.println(e);
		}
	}
	
	/**
	 * Lauches a live query interpreter
	 */
	public void launchQueryInterpreter() 
	{
		boolean loop = true;
		Scanner reader = new Scanner(System.in);
		String query = "";
		
		while(loop) {
			System.out.print("> ");
			query = reader.nextLine();
			this.query(query);
		}
	}
}
