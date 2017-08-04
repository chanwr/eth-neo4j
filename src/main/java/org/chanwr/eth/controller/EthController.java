package org.chanwr.eth.controller;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.tuple.Pair;
import org.chanwr.eth.dao.*;
import org.chanwr.eth.repo.BlockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

@RestController
public class EthController {

    private static Logger LOGGER = LoggerFactory.getLogger(EthController.class);

    @Value("${etherscan.apikey}")
    private String apikey;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/load")
    public String index() throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("fake_coindash.json");
        Object document = Configuration.defaultConfiguration().jsonProvider().parse(is, UTF_8);
        Pair<Integer, Integer> counter = processTransactions(document);
        return String.format("Processed %d out of %d records for fake_coindash.json", counter.getLeft(), counter.getRight());
    }

    @RequestMapping("/transactions/{address}")
    public String processNormal(@PathVariable("address") String address) {
        return process(address, "txlist");
    }

    @RequestMapping("/internal-transactions/{address}")
    public String processInternal(@PathVariable("address") String address) {
        return process(address, "txlistinternal");
    }

    private String process(String address, String action) {
        int processed = 0;
        int total = 0;

        boolean continueProcessing = true;
        int page = 1;
        while (continueProcessing) {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://api.etherscan.io/api")
                    .queryParam("module", "account")
                    .queryParam("action", action)
                    .queryParam("address", address)
                    //.queryParam("startBlock", 0)
                    //.queryParam("endBlock", 9999999)
                    .queryParam("startBlock", 0)
                    .queryParam("endBlock", 9999999)
                    .queryParam("page", page++)
                    .queryParam("offset", 1000)
                    .queryParam("sort", "desc")
                    .queryParam("apikey", apikey);
            String json = restTemplate.getForObject(builder.toUriString(), String.class);
            Object document = Configuration.defaultConfiguration().jsonProvider().parse(json);
            Pair<Integer, Integer> counter = processTransactions(document);
            processed += counter.getLeft();
            total += counter.getRight();
            LOGGER.info("Processed {} out of {} records", processed, total);
            // if the number of transactions is less than offset or equal to 0, then we should be done
            if (counter.getRight() < 1000 || counter.getRight() == 0) {
                continueProcessing = false;
            }
        }
        return String.format("Processed %d out of %d records for %s on %s", processed, total, action, address);
    }

    private Pair<Integer, Integer> processTransactions(Object document) {
        int count = 0;
        int len = JsonPath.read(document, "$.result.length()");
        for (int i = 0; i < len; i++) {
            Map<String, String> record = JsonPath.read(document,"$.result[" + i + "]");
            // 0 means OK for normal or internal transactions
            if (record.get("isError").equals("0")) {
                // 0x for "normal" transaction and empty string for "internal" transaction
                blockRepository.save(getBlock(record));
                count++;
            }
        }
        return Pair.of(count, len);
    }

    private Block getBlock(Map<String, String> record) {
        Block block = new Block();
        block.setBlockNumber(record.get("blockNumber"));
        block.setBlockHash(record.get("blockHash"));
        block.addTransaction(getNormalTransaction(record));
        return block;
    }

    private NormalTransaction getNormalTransaction(Map<String, String> record) {
        NormalTransaction transaction = new NormalTransaction();
        transaction.setHash(record.get("hash"));
        String input = record.get("input");
        if (!input.equals("")) {
            transaction.setTransactionIndex(record.get("transactionIndex"));
            transaction.setTimeStamp(record.get("timeStamp"));
            transaction.setFrom(getAccount(record.get("from")));
            transaction.setTo(getAccount(record.get("to")));
            transaction.setValue(record.get("value"));
        } else {
            // should be empty string if it is internal transaction
            // the goal is to effectively merging with an existing normal transaction
            // only works if the address is source of the call for internal transaction and not the target of it
            // TODO it's not elegant but it assumes that normal transactions are loaded first
            transaction.addTransaction(getInternalTransaction(record));
        }
        return transaction;
    }

    private InternalTransaction getInternalTransaction(Map<String, String> record) {
        InternalTransaction transaction = new InternalTransaction();
        transaction.setTraceId(record.get("traceId"));
        transaction.setFrom(getAccount(record.get("from")));
        transaction.setTo(getAccount(record.get("to")));
        transaction.setValue(record.get("value"));
        return transaction;
    }

    private Account getAccount(String address) {
        Account account = new Account();
        account.setAddress(address);
        return account;
    }

}
