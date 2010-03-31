package org.neo4j.examples.astarrouting;

import org.neo4j.graphalgo.Path;
import org.neo4j.graphalgo.RelationshipExpander;
import org.neo4j.graphalgo.shortestpath.AStar;
import org.neo4j.graphalgo.shortestpath.CostEvaluator;
import org.neo4j.graphalgo.shortestpath.EstimateEvaluator;
import org.neo4j.graphalgo.shortestpath.std.DoubleEvaluator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;

public class AStarRouting
{
    private static final EstimateEvaluator<Double> estimateEval = new GeoCostEvaluator();
    private static final CostEvaluator<Double> costEval = new DoubleEvaluator(
            Waypoint.COST );
    private static final GraphDatabaseService graphDb = new EmbeddedGraphDatabase(
            "target/neo4j-db" );

    public static void main( final String[] args )
    {
        Transaction tx = graphDb.beginTx();
        Waypoint NYC, KAN, SFE, SEA, SF;
        try
        {
            NYC = new Waypoint( graphDb, "New York", "New York" );
            KAN = new Waypoint( graphDb, "Kansas City", "Kansas" );
            SFE = new Waypoint( graphDb, "Santa Fe", "New Mexico" );
            SEA = new Waypoint( graphDb, "Seattle", "Washington" );
            SF = new Waypoint( graphDb, "San Fransisco", "CA" );
            NYC.createRoadTo( KAN );
            NYC.createRoadTo( SEA );
            SEA.createRoadTo( SF );
            KAN.createRoadTo( SFE );
            SFE.createRoadTo( SF );
            tx.success();
        }
        finally
        {
            tx.finish();
        }
        tx = graphDb.beginTx();
        try
        {
            AStar sp = new AStar( graphDb, RelationshipExpander.forTypes(
                    RelationshipTypes.ROAD, Direction.BOTH ), costEval,
                    estimateEval );
            Path path = sp.findSinglePath( NYC.getUnderlyingNode(),
                    SF.getUnderlyingNode() );
            for ( Node node : path.getNodes() )
            {
                System.out.println( node.getProperty( Waypoint.NAME ) );
            }
        }
        finally
        {
            tx.finish();
        }
        graphDb.shutdown();
    }
}
