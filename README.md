# ADBS CW - LightDB

*The implementation of a lightweight database management system called LightDB as part of the Advanced Database Systems course at the University of Edinburgh.*

***Loucas Papalazarou***
***s2567498***

---

### General Design & Comments

***TODO***
- [ ] What is done and what is not etc.
 
### Build & Run
Currently, I have the maven binaries downloaded inside my repository. Therefore, I can use `build.sh` to build the project and `run.sh` to run. `run.sh` simply runs the program using `query0.sql`. 

```bash
build.sh && run.sh
```

The `test.sh` script runs the code with all the queries from the `/samples/input/` directory and places the results in the `/samples/output/` directory.

The `compare.sh` script compares the contents of every file in `/samples/output/` to every corresponding file in `/samples/expected_output/`.

### Debug Mode

Inside the `LightDB.java` file there is a line of code denoting the debug mode.

```java
final boolean DEBUG;
```

If `DEBUG` is set to `true`, the output of the query is shown on standard output.
If `DEBUG` is `false`, the output is written in the specified file.

## Task 1

### Join Strategy

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
- If no WHERE clause was present, the tree will only consist of scan operators and thus the operation is just a cross product. Otherwise, the tree will consit of select operators on top of scans.
- The `ExpressionVisitor` class is responsible for evaluating if a tuple is eligible. For a tuple that results from a join, the strategy is to concatenatethe left and right tuple and pass it to the `ExpressionVisitor`. The WHERE condition is evaluated for the new concatenated tuple, and if it passes, then the new tuple is valid and is returned.
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

### Selection Pushdown

Already implemented, exlpain...

### Early Projection

Explain...

#### Sort Merge Join

Do and explain...

#### Join Order

Do and explain...

