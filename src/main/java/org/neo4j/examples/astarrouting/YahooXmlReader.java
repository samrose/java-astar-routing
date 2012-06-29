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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class YahooXmlReader
{
    private static final String APP_ID = "JzJ0LQ_V34EWH5agHt7TZxD0Eqz2CoEkX.xAM9y8PeAIjYALdy4C9Psh0pcZ1t6dpPf9zxXXjICw";
    private static final String YAHOO_URI = "http://local.yahooapis.com/MapsService/V1/geocode?appid="
                                            + APP_ID;

    private static Coordinates readYahooXml( final InputStream in )
    {
        Double latitude = null, longitude = null;
        try
        {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLEventReader eventReader = inputFactory.createXMLEventReader( in );
            while ( eventReader.hasNext() )
            {
                XMLEvent event = eventReader.nextEvent();
                if ( event.isStartElement() )
                {
                    if ( event.asStartElement().getName().getLocalPart() == ( "Latitude" ) )
                    {
                        event = eventReader.nextEvent();
                        latitude = Double.valueOf( event.asCharacters().getData() );
                        continue;
                    }
                    if ( event.asStartElement().getName().getLocalPart() == ( "Longitude" ) )
                    {
                        event = eventReader.nextEvent();
                        longitude = Double.valueOf( event.asCharacters().getData() );
                        continue;
                    }
                }
            }
        }
        catch ( XMLStreamException e )
        {
            e.printStackTrace();
        }
        return new Coordinates( latitude, longitude );
    }

    public static Coordinates getCoordinates( final String city,
            final String state )
    {
        String address = null;
        try
        {
            address = YAHOO_URI + "&state="
                      + URLEncoder.encode( state, "UTF-8" ) + "&city="
                      + URLEncoder.encode( city, "UTF-8" );
            URL url;
            InputStream in = null;
            try
            {
                url = new URL( address );
                in = url.openStream();
                return YahooXmlReader.readYahooXml( in );
            }
            catch ( MalformedURLException e )
            {
                e.printStackTrace();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    in.close();
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
            }
        }
        catch ( UnsupportedEncodingException e1 )
        {
            e1.printStackTrace();
        }
        return null;
    }
}
