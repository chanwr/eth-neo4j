package org.chanwr.eth.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@NodeEntity
public class Account {

    @GraphId
    private Long id;

    // CREATE INDEX ON :Account(address)
    @Index(unique = true, primary = true)
    @Property
    private String address;

    public void setAddress(String address) {
        this.address = address;
    }

}
