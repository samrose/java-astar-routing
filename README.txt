Simple Routing Example with Neo4j

This is a small routing example, populating a Neo4j node space 
with a couple of towns using the Yahoo! geocoding service, and 
then doing a A* routing on it. 


Requirements

Java and Maven installed, see:
http://wiki.neo4j.org/content/Java_Setup_HowTo
Note that Java 6 is needed in this case.


Running

mvn clean compile exec:java -Dexec.mainClass="org.neo4j.examples.astarrouting.AStarRouting"

This command will download all dependencies that are needed on the first run.

After lots of Maven information you should see something like this
in the output:

Waypoint [name=New York, longitude=-74.007124, latitude=40.71455]
Waypoint [name=Kansas City, longitude=-94.626824, latitude=39.11338]
Waypoint [name=Santa Fe, longitude=-105.937406, latitude=35.691543]
Waypoint [name=Seattle, longitude=-122.329439, latitude=47.60356]
Waypoint [name=San Fransisco, longitude=-122.420139, latitude=37.7796]
New York
Kansas City
Santa Fe
San Fransisco
