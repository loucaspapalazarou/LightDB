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
		String databaseDir = args[0];
		String inputFile = args[1];
		String outputFile = args[2];

		DatabaseCatalog catalog = new DatabaseCatalog(databaseDir);
		QueryInterpreter queryInterpreter = new QueryInterpreter(catalog);

		QueryPlan queryPlan = queryInterpreter.parseQuery(inputFile);

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

		queryPlan.evaluate(printStream);

	}
}
