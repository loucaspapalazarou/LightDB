# ADBS CW - LightDB

Loucas Papalazarou\
s2567498

## Preamble

The implementation of a lightweight database management system called LightDB. Part of the Advanced Database Systems course at the University of Edinburgh.

### Build & Run
Currently, I have the maven binaries downloaded inside my repository. Therefore, I can use `build.sh` to build the project and `run.sh` to run.

```
build.sh && run.sh
```

## General Design

## Task 1

### Join Strategy

The Join strategy is described in the `QueryInterpreter`, in the part where the joins are handled. This section will also provide the strategy in more clearly explained.

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

