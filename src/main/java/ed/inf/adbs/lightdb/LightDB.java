package ed.inf.adbs.lightdb;

import ed.inf.adbs.lightdb.catalog.DatabaseCatalog;
import ed.inf.adbs.lightdb.operators.ScanOperator;
import ed.inf.adbs.lightdb.planners.QueryPlanner;
import ed.inf.adbs.lightdb.types.QueryResult;

/**
 * Lightweight in-memory database system
 *
 */
public class LightDB {

	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Usage: LightDB database_dir input_file output_file");
			return;
		}

		String databaseDir = args[0];
		String inputFile = args[1];
		String outputFile = args[2];

		DatabaseCatalog catalog = new DatabaseCatalog(databaseDir);
		QueryPlanner queryPlanner = new QueryPlanner(catalog);
		queryPlanner.parseQuery(inputFile);
		queryPlanner.evaluate(System.out);
	}
}
