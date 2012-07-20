package org.neo4j.kernel.impl;

import org.neo4j.kernel.Version;
import org.neo4j.helpers.Service;

@Service.Implementation(Version.class)
public class ComponentVersion extends Version
{
    public ComponentVersion()
    {
        super("neo4j-kernel", "1.8.M05");
    }

    @Override
    public String getRevision()
    {
        return "1.8.M05-1-ge9cdca9";
    }
}