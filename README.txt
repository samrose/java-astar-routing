Simple Routing Example with Neo4j

This is a small routing example, populating a Neo4j node space 
with a couple of towns using the Yahoo! geocoding service, and 
then doing a A* routing on it. 


Requirements

To execute the project, Java 6 is needed. To run from source,
JDK 1.6 is needed, and optionally Maven2 for dependency management.


Download packages

There's download packages located here:
http://m2.neo4j.org/org/neo4j/examples/java-astar-routing/1.0.0-SNAPSHOT/
Look for files ending in .zip or .tar.gz
Only Java 6 is required to execute the example using the download.


Contents of the download package

bin/   start scripts for the application in Unix and Windows format
docs/  javadoc of all Neo4j component dependencies of the project
lib/   all dependency jars of the project
src/   source code of the project


Run from source

Using JDK:
   Use the download package and add the lib/ directory
   to the project classpath ("build path" in Eclipse).
   In this case, only JDK 1.6 is required to run the code
   from source.
Using JDK + Maven:
   Checkout the code from GitHub and use the Maven2
   setup originally used for the project. This option requires
   JDK 1.6 and Maven2 to be installed, see:
   http://wiki.neo4j.org/content/Java_Setup_HowTo
   With Maven2 installed, simply issue the following command
   at the command prompt:
   $ mvn clean compile exec:java -Dexec.mainClass="org.neo4j.examples.astarrouting.AStarRouting"
   This command will download all dependencies that are needed on the first run.


Sample output

Waypoint [name=New York, longitude=-74.007124, latitude=40.71455]
Waypoint [name=Kansas City, longitude=-94.626824, latitude=39.11338]
Waypoint [name=Santa Fe, longitude=-105.937406, latitude=35.691543]
Waypoint [name=Seattle, longitude=-122.329439, latitude=47.60356]
Waypoint [name=San Fransisco, longitude=-122.420139, latitude=37.7796]
New York
Kansas City
Santa Fe
San Fransisco
