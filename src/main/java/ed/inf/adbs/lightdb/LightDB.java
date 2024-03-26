package ed.inf.adbs.lightdb;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import ed.inf.adbs.lightdb.catalog.DatabaseCatalog;
import ed.inf.adbs.lightdb.query.QueryInterpreter;
import ed.inf.adbs.lightdb.query.QueryPlan;

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

		final boolean DEBUG = true;
		final boolean OPTIMIZED = true;

		String databaseDir = args[0];
		String inputFile = args[1];
		String outputFile = args[2];

		// initialize catalog
		DatabaseCatalog catalog = new DatabaseCatalog(databaseDir);

		// initialize interpreter
		QueryInterpreter queryInterpreter = new QueryInterpreter(catalog);

		// parse the query and create the evaluation plan based on optimization flag
		QueryPlan queryPlan;
		if (OPTIMIZED) {
			queryPlan = queryInterpreter.createQueryPlanOptimized(inputFile);
		} else {
			queryPlan = queryInterpreter.createQueryPlan(inputFile);
		}

		// if the debug flag is set to true, the query plan will be evaluated
		// with its result being dumped in stdout. Otherwise it will be stored
		// in the specified output file
		PrintStream printStream = null;
		if (DEBUG) {
			printStream = System.out;
		} else {
			try {
				printStream = new PrintStream(new FileOutputStream(outputFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		// evaluate to the specified stream
		queryPlan.evaluate(printStream);

	}
}
