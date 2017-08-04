package org.chanwr.eth.dao;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@NodeEntity
public class InternalTransaction extends Transaction {

    @Property
    private String traceId;

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

}
