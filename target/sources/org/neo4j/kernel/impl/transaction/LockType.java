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
package org.neo4j.kernel.impl.transaction;

import org.neo4j.kernel.impl.core.LockReleaser;

/**
 * Enum defining the <CODE>READ</CODE> lock and the <CODE>WRITE</CODE> lock.
 */
public enum LockType
{
    READ
    {
        @Override
        public void acquire( Object resource, LockManager lockManager )
        {
            lockManager.getReadLock( resource );
        }

        @Override
        public void unacquire( Object resource, LockManager lockManager, LockReleaser lockReleaser )
        {
            lockManager.releaseReadLock( resource, null );
        }

        @Override
        public void release( Object resource, LockManager lockManager )
        {
            lockManager.releaseReadLock( resource, null );
        }
    },
    WRITE
    {
        @Override
        public void acquire( Object resource, LockManager lockManager )
        {
            lockManager.getWriteLock( resource );
        }

        @Override
        public void unacquire( Object resource, LockManager lockManager, LockReleaser lockReleaser )
        {
            lockReleaser.addLockToTransaction( resource, this );
        }

        @Override
        public void release( Object resource, LockManager lockManager )
        {
            lockManager.releaseWriteLock( resource, null );
        }
    };
    
    public abstract void acquire( Object resource, LockManager lockManager );

    public abstract void unacquire( Object resource, LockManager lockManager, LockReleaser lockReleaser );
    
    public abstract void release( Object resource, LockManager lockManager );
}