/**
 * Copyright (c) 2002-2012 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.impl.nioneo.xa;

import org.neo4j.kernel.impl.core.ReadOnlyDbException;
import org.neo4j.kernel.impl.persistence.EntityIdGenerator;
import org.neo4j.kernel.impl.persistence.NeoStoreTransaction;
import org.neo4j.kernel.impl.persistence.PersistenceSource;
import org.neo4j.kernel.impl.transaction.XaDataSourceManager;
import org.neo4j.kernel.impl.transaction.xaframework.XaConnection;
import org.neo4j.kernel.impl.transaction.xaframework.XaDataSource;
import org.neo4j.kernel.lifecycle.Lifecycle;

/**
 * The NioNeo persistence source implementation. If this class is registered as
 * persistence source for Neo4j kernel operations that are performed on the node space
 * will be forwarded to this class {@link NeoStoreTransaction} implementation.
 */
public class NioNeoDbPersistenceSource implements PersistenceSource, EntityIdGenerator, Lifecycle
{
    private NeoStoreXaDataSource xaDs = null;
    private String dataSourceName = null;
    private NeoStoreTransaction readOnlyResourceConnection;
    private XaDataSourceManager xaDataSourceManager;

    public NioNeoDbPersistenceSource(XaDataSourceManager xaDataSourceManager)
    {
        assert(xaDataSourceManager != null);
        this.xaDataSourceManager = xaDataSourceManager;
    }

    public void init()
    {
        // Do nothing
    }

    public void start()
    {
        xaDs = xaDataSourceManager.getNeoStoreDataSource();
        if ( xaDs == null )
        {
            throw new IllegalStateException(
                "Unable to get nioneodb datasource" );
        }
        readOnlyResourceConnection = new ReadTransaction( xaDs.getNeoStore() );
    }

    public void stop()
    {
        if ( xaDs != null )
        {
            // This close is owned by the XaDS xaDs.close();
        }
    }

    @Override
    public void shutdown()
        throws Throwable
    {
    }

    public NeoStoreTransaction createTransaction( XaConnection connection )
    {
        if ( xaDs.isReadOnly() )
        {
            throw new ReadOnlyDbException();
        }
        NeoStoreTransaction result = ((NeoStoreXaConnection) connection).getWriteTransaction();
        
        // This is not a very good solution. The XaConnection is only used when
        // delisting/releasing the nioneo xa resource. Maybe it should be stored
        // outside the ResourceConnection interface?
        result.setXaConnection( connection );
        return result;
    }
    
    public NeoStoreTransaction createReadOnlyResourceConnection()
    {
        return readOnlyResourceConnection; 
    }

    public String toString()
    {
        return "A persistence source to [" + dataSourceName + "]";
    }

    public long nextId( Class<?> clazz )
    {
        return xaDs.nextId( clazz );
    }

    public long getHighestPossibleIdInUse( Class<?> clazz )
    {
        return xaDs.getHighestPossibleIdInUse( clazz );
    }

    public long getNumberOfIdsInUse( Class<?> clazz )
    {
        return xaDs.getNumberOfIdsInUse( clazz );
    }
    
    public XaDataSource getXaDataSource()
    {
        return xaDs;
    }
}