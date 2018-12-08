package com.pico.web3j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;

public class PICOClient {
    private static final Logger log = LoggerFactory.getLogger(PICOToken.class);

    @Autowired
    private Web3j web3j;

    @Autowired
    private Admin admin;

    @Value("${pico.contract.address}")
    private String contractAddress;

    public TransactionReceipt transfer(String from,String password,String to, BigInteger value){
        ClientTransactionManager clientTransactionManager=new ClientTransactionManager(web3j,from);
        PICOToken picoToken=PICOToken.load(contractAddress, web3j, clientTransactionManager,
                       new DefaultGasProvider());
        try {
            admin.personalUnlockAccount(from, password).send();
            TransactionReceipt transactionReceipt=picoToken.transfer(to, value).send();
            log.info(transactionReceipt.toString());
            return transactionReceipt;
        }catch(Exception e){
            log.error("");
            return null;
        }
    }

}
