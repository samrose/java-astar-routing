/**
 * Copyright (c) 2002-2012 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.examples.astarrouting;

import org.neo4j.graphalgo.CommonEvaluators;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.EstimateEvaluator;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Expander;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.Traversal;

public class AStarRouting
{
    private static final EstimateEvaluator<Double> estimateEval = CommonEvaluators.geoEstimateEvaluator(
            Waypoint.LATITUDE, Waypoint.LONGITUDE );
    private static final CostEvaluator<Double> costEval = CommonEvaluators.doubleCostEvaluator( Waypoint.COST );

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
            Expander relExpander = Traversal.expanderForTypes(
                    RelationshipTypes.ROAD, Direction.BOTH );
            relExpander.add( RelationshipTypes.ROAD, Direction.BOTH );
            PathFinder<WeightedPath> sp = GraphAlgoFactory.aStar( relExpander,
                    costEval, estimateEval );
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
