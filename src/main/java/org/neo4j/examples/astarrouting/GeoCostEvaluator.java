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

import org.neo4j.graphalgo.EstimateEvaluator;
import org.neo4j.graphdb.Node;

public class GeoCostEvaluator implements EstimateEvaluator<Double>
{
    private static final double RADIUS_EARTH = 6371 * 1000; // Meters

    public Double getCost( final Node node, final Node goal )
    {
        return ( distance( node, goal ) );
    }

    static Double distance( final Node point1, final Node point2 )
    {
        return distance( (Double) point1.getProperty( Waypoint.LATITUDE ),
                (Double) point1.getProperty( Waypoint.LONGITUDE ),
                (Double) point2.getProperty( Waypoint.LATITUDE ),
                (Double) point2.getProperty( Waypoint.LONGITUDE ) );
    }

    static double distance( double latitude1, double longitude1,
            double latitude2, double longitude2 )
    {
        latitude1 = Math.toRadians( latitude1 );
        longitude1 = Math.toRadians( longitude1 );
        latitude2 = Math.toRadians( latitude2 );
        longitude2 = Math.toRadians( longitude2 );
        double cLa1 = Math.cos( latitude1 );
        double x_A = RADIUS_EARTH * cLa1 * Math.cos( longitude1 );
        double y_A = RADIUS_EARTH * cLa1 * Math.sin( longitude1 );
        double z_A = RADIUS_EARTH * Math.sin( latitude1 );
        double cLa2 = Math.cos( latitude2 );
        double x_B = RADIUS_EARTH * cLa2 * Math.cos( longitude2 );
        double y_B = RADIUS_EARTH * cLa2 * Math.sin( longitude2 );
        double z_B = RADIUS_EARTH * Math.sin( latitude2 );
        return Math.sqrt( ( x_A - x_B ) * ( x_A - x_B ) + ( y_A - y_B )
                          * ( y_A - y_B ) + ( z_A - z_B ) * ( z_A - z_B ) );
    }
}
