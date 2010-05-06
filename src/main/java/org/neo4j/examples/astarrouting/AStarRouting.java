package org.neo4j.examples.astarrouting;

import org.neo4j.graphalgo.shortestpath.AStar;
import org.neo4j.graphalgo.shortestpath.CostEvaluator;
import org.neo4j.graphalgo.shortestpath.EstimateEvaluator;
import org.neo4j.graphalgo.shortestpath.std.DoubleEvaluator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.DefaultExpander;
import org.neo4j.kernel.EmbeddedGraphDatabase;

public class AStarRouting
{
    private static final EstimateEvaluator<Double> estimateEval = new GeoCostEvaluator();
    private static final CostEvaluator<Double> costEval = new DoubleEvaluator(
            Waypoint.COST );

    public static void main( final String[] args )
    {
        GraphDatabaseService graphDb = new EmbeddedGraphDatabase(
                "target/neo4j-db" );
        try
        {
            routing( graphDb );
        }
        finally
        {
            graphDb.shutdown();
        }
    }

    private static void routing( final GraphDatabaseService graphDb )
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
            DefaultExpander relExpander = new DefaultExpander();
            relExpander.add( RelationshipTypes.ROAD, Direction.BOTH );
            AStar sp = new AStar( graphDb, relExpander, costEval, estimateEval );
            Path path = sp.findSinglePath( NYC.getUnderlyingNode(),
                    SF.getUnderlyingNode() );
            for ( Node node : path.nodes() )
            {
                System.out.println( node.getProperty( Waypoint.NAME ) );
            }
        }
        finally
        {
            tx.finish();
        }
    }
}
