package db.neo4j;

import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.cypher.javacompat.*;

public class CypherQueryController {
	// --- Fields ---
	private ExecutionEngine query_engine;

	// --- Constructors ---
	public CypherQueryController(EmbeddedNeo4jClient neo4j_client) {
		this.query_engine = new ExecutionEngine(neo4j_client.graph_db);
	}

	// --- Methods ---
	public void query(String q) {
		ExecutionResult result = (ExecutionResult) query_engine.execute(q);
		String rows = "";
		
		for (Map<String, Object> row : result) {
			for (Entry<String, Object> column : row.entrySet()) {
				rows += column.getKey() + ": " + column.getValue() + "; ";
			}
			rows += "\n";
		}
		
		System.out.println(rows);
	}
}
