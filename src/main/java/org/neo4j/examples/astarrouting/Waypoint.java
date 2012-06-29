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

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class Waypoint
{
    static final String LATITUDE = "lat";
    static final String LONGITUDE = "lon";
    static final String NAME = "name";
    static final String COST = "cost";
    private final Node underlyingNode;

    public Waypoint( final Node node )
    {
        this.underlyingNode = node;
    }

    public Waypoint( final GraphDatabaseService graphDb, final String city,
            final String state )
    {
        this.underlyingNode = graphDb.createNode();
        Coordinates coordinates = YahooXmlReader.getCoordinates( city, state );
        underlyingNode.setProperty( LATITUDE, coordinates.getLatitude() );
        underlyingNode.setProperty( LONGITUDE, coordinates.getLongtude() );
        underlyingNode.setProperty( NAME, city );
        System.out.println( this );
    }

    public Node getUnderlyingNode()
    {
        return underlyingNode;
    }

    public String getName()
    {
        return (String) underlyingNode.getProperty( NAME );
    }

    public Coordinates getCoordinates()
    {
        double latitude = (Double) underlyingNode.getProperty( LATITUDE );
        double longitude = (Double) underlyingNode.getProperty( LONGITUDE );
        return new Coordinates( latitude, longitude );
    }

    public void createRoadTo( final Waypoint other )
    {
        Relationship road = underlyingNode.createRelationshipTo(
                other.underlyingNode, RelationshipTypes.ROAD );
        road.setProperty( COST, GeoCostEvaluator.distance( underlyingNode,
                other.underlyingNode ) );
    }

    @Override
    public String toString()
    {
        return "Waypoint [name=" + getName() + ", " + getCoordinates() + "]";
    }
}
