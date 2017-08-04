package org.chanwr.eth.repo;

import org.chanwr.eth.dao.Block;
import org.springframework.data.neo4j.repository.GraphRepository;

public interface BlockRepository extends GraphRepository<Block> {

}
