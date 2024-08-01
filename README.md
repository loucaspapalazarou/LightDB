# ADBS CW - LightDB

*The implementation of a lightweight database management system called LightDB as part of the Advanced Database Systems course at the University of Edinburgh.*

***Loucas Papalazarou***
***s2567498***

---

## General

#### Design & Comments

The code was built using a bottom-up approach. I first created a `TupleElement` class to represent individual elements in the tuple. This class contains data that represent the origin table, possible alias, value etc. Naturally, the `Tuple` class contains a list of `TupleElement` instances.

When the `ScanOperator` reads the file contents, it is essentially creating `Tuple` instances that are then returned to the child operator.

Normal query planning involves the iterative creation of the tree of operators using the `QueryInterpreter` and the result of that is an object of type `QueryPlan`. The `QueryPlan` instance contains the `evaluate` function, that simply dumps the contents of the root operator into a print stream.

The optimized query plan, also uses the `QueryInterpreter`, but now the `createQueryPlanOptimized` function is used instead of the `createQueryPlan` that was used for normal queries. The query is first analyzed to check and implement possible optimizations (join order and early projection) and then continues with the tree building.

#### Bugs

- When aliases are declared but not used the `WHERE` clause does not get executed. This affects **Selections** and **Joins**. For example the following query would not work:

```sql
SELECT * FROM Sailors S, Boats B WHERE Sailors.A <= 3 AND Boats.D > 102;
```

While it's possible to address this issue through modifications, the time invested isn't justified given that these types of queries won't be tested according to the instructions.

That said, the code may contain more bugs triggered with edge cases. If that happens please do try to run the query without optimizations (see section **'Task 2'**).  
 
#### Debug Mode

Inside `LightDB.java` there is a line of code denoting the debug mode.

```java
final boolean DEBUG;
```

If `DEBUG` is set to `true`, the output of the query is shown on standard output.
If `DEBUG` is `false`, the output is written in the specified file.

#### Build & Run
Currently, I have the maven binaries downloaded inside my repository. Therefore, I can use `build.sh` to build the project and `run.sh` to run. `run.sh` simply runs the program using `samples/input/query0.sql`. 

```bash
build.sh && run.sh
```

The `test.sh` script runs the code with all the queries from the `/samples/input/` directory and places the results in the `/samples/output/` directory.

The `compare.sh` script compares the contents of every file in `/samples/output/` to every corresponding file in `/samples/expected_output/`.

## Task 1

All operators are complete.

#### Join Strategy

The Join strategy is described in the `QueryInterpreter`, at section of the code where the joins are handled. This section explains that strategy in detail.

- When the time comes to handle any joins, we already have the root operator.
- The join tables are given as a list of tables
- We iterate through these tables
- We create a new operator that we call `right`
- This new `right` operator is a scan with an optional selection (if a WHERE condition is present)
- Now the root operator is set as a join operator with the previous root as the left child and `right` as the right operator.
- With every iteration, the left part of the tree is getting deeper and deeper while any new operator is being added to the right of the previous root.
- When the iteration ends, we have the final root operator which is an instance of a join operator.
- Each node in the tree is an operator.
- If no WHERE clause was present, the tree will only consist of scan operators and thus the operation is just a cross product. Otherwise, the tree will consist of select operators on top of scans.
- The `ExpressionVisitor` class is responsible for evaluating if a tuple is eligible. For a tuple that results from a join, the strategy is to concatenate the left and right tuple and pass it to the `ExpressionVisitor`. The WHERE condition is evaluated for the new concatenated tuple, and if it passes, then the new tuple is valid and is returned.
- The join operator keeps track of the current left tuple it is holding. The first one will just be the result of `getNextTuple` on the left child.
- When `getNextTuple` is called on a join operator, it tries to get a tuple from the right child. If the result is `null`, the right child is reset, and the next tuple if fetched from the left child.
- The fact that the tree consists of operators that filter out tuples on the way up to the root, ensures that this approach does not compute a cross product and filter the tuples at the end.
- It is also a `left-deep` tree because any new operator is placed as the right child of the root operator and the previous root as the left.

## Task 2

To turn on the optimizations, a flag is defined in `LightDB.java`.

```java
final boolean OPTIMIZED;
```

If set to `true`, the query interpreter will use the `createQueryPlanOptimized` method to create the query plan.
If set to `false`, the `createQueryPlan` method will be used.

Both of these methods are defined in the `QueryInterpreter` class.

### Optimizations

#### (Built-in) Selection Pushdown

One way we can improve performance, is by performing selections before joining the tables. The way the system is built by default operates this way, in order to avoid cross product of tables and filtering afterwards. Thus, this optimization is baked in the system by the assignment definition.

#### (Implemented) Join Order

We can drastically decrease the number of intermediary tuples by manipulating the join order and joining tables with more constraints. However, it would not be very feasible to try and join tables based on their common condition because that might not be the case as the selection can be from any table. For this reason, a heuristic system can be considered very effective. The Join order is handled like so: The `WHERE` clause is analyzed and a value is assigned to each table based of how selective the expressions in WHERE are. Each inequality (>, <, >=, <=, !=) gets a value of 1 and the equality (=) a value of 2. The value of each table is added up and we end up with an estimate on the selectivity of the query based on tables. We then perform the joins in descending order of table selectiviity. This approach is a heuristic way to minimize the intermediate tuples because of the fact that Selection is performed before joins. However, if the query requested all the columns of the resulting tuple, we have an ordering problem because of the join order. To address this, we save a copy of the requested join order and if the selection is of type '*', we simply expand the '*' to all columns of all tables. Then, the projection operator handles the reordering of the tuple elements.

This optimization is correct because the join order does not matter in the correctness of a query result. Thus, changing it will still yield the correct output, given that we rearrange the column order to match the requested column order.

#### (Implemented) Early Projection
Another possible optimization is performing the projections before joining. Although this will not reduce the number of tuples processed, it will reduce the number of columns and essentially save some memory by reducing the "width of the tuples". The query is checked to see whether is possible to perform projection before joins. An early projection is possible if the columns referenced in the `WHERE` expression are a subset of the ones in the `SELECT`. If that is the case, we simply perform the Projection before joining.

The reason this optimization is correct, is because all of the columns needed to perform any selections will be present even after the projection. Thus, the evaluation of a query with early projection (if possible) will yield the same results.
