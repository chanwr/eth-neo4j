package org.chanwr.eth.dao;

import org.neo4j.ogm.annotation.*;

@NodeEntity
public class Transaction {

    @GraphId
    private Long id;

    @Relationship(type = "FROM", direction = Relationship.INCOMING)
    private Account from;

    @Relationship(type = "TO")
    private Account to;

    @Property
    private String value;

    public void setFrom(Account from) {
        this.from = from;
    }

    public void setTo(Account to) {
        this.to = to;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
