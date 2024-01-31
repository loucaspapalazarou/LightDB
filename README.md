# LightDB

The implementation of a lightweight database management system called LightDB. Part of the Advanced Database Systems course at the University of Edinburgh.

### How to run
Currently, I have the maven binaries downloaded inside my repository. Therefore, I can use `build.bat` to build the project and `run.bat` to run, and the input comes from `input.txt`.

Generally, you build using
```
mvn clean compile assembly:single
```
and run using
```
java -jar target/lightdb-1.0.0-jar-with-dependencies.jar samples/db samples/input/query1.sql samples/output/query1.csv
```
or using the scripts
```
./build && ./run
```

#### more docs to come...

# TODO: Check expression evaluator