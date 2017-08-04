package org.chanwr.eth.dao;

import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class NormalTransaction extends Transaction {

    // CREATE INDEX ON :Transaction(hash)
    @Index(unique = true, primary = true)
    @Property
    private String hash;

    @Property
    private String transactionIndex;

    @Property
    private String timeStamp;

    @Relationship(type = "CALLS")
    private Set<InternalTransaction> internalTransactionSet;

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setTransactionIndex(String transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void addTransaction(InternalTransaction transaction) {
        if (internalTransactionSet == null) {
            internalTransactionSet = new HashSet<>();
        }
        internalTransactionSet.add(transaction);
    }

}
