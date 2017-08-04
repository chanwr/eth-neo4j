package org.chanwr.eth.dao;

import org.neo4j.ogm.annotation.*;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class Block {

    @GraphId
    private Long id;

    // CREATE INDEX ON :Block(blockNumber)
    @Index(unique = true, primary = true)
    @Property
    private String blockNumber;

    // CREATE INDEX ON :Block(blockHash)
    @Property
    private String blockHash;

    @Relationship(type = "CONTAINS")
    private Set<NormalTransaction> transactionSet;

    public void setBlockNumber(String blockNumber) {
        this.blockNumber = blockNumber;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public void addTransaction(NormalTransaction transaction) {
        if (transactionSet == null) {
            transactionSet = new HashSet<>();
        }
        transactionSet.add(transaction);
    }

}
