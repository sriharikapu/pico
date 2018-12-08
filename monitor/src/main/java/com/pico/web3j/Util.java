package com.pico.web3j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.ManagedTransaction;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.io.IOException;
import java.math.BigInteger;

public class Util {
    private static Logger log= LoggerFactory.getLogger(Util.class);

    public String     RPC_URL        = "http://18.144.37.250:8333/";
    public String     baseAddress    = "0xecfdbe9a0d897100cd386e785cd2bacc00c36736";
    public String     basePassword   = "12345678";
    public BigInteger unlockDuration = BigInteger.valueOf(60L);
    public String contractAddress= "0x2d7dc58d59ff7c4e91e3c5c39854907bbaef884c";

    public Web3j                    web3j                        = Web3j.build(new HttpService(RPC_URL));
    public Admin                    admin                        = Admin.build(new HttpService(RPC_URL));
    public ClientTransactionManager baseClientTransactionManager = new ClientTransactionManager(web3j,
                                                                                                baseAddress);

    public boolean deploy() {
        ContractGasProvider contractGasProvider = new StaticGasProvider(ManagedTransaction.GAS_PRICE, BigInteger.valueOf(7_300_000));

        if (!unlock(baseAddress, basePassword)) {
            return false;
        }

        try {
            PICOToken contract = PICOToken.deploy(
                    web3j,
                    baseClientTransactionManager,
                    contractGasProvider,
                    new BigInteger("500"),
                    "AToken",
                    "AToken",
                    new BigInteger("0"),
                    new BigInteger("1000"),
                    new BigInteger("300000"),
                    new BigInteger("1000")
            ).send();

            contract.getTransactionReceipt().get().getBlockNumber();
            contract.getTransactionReceipt().get().getTransactionHash();
            log.info(contract.getContractAddress());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean invest() {
        ContractGasProvider contractGasProvider = new StaticGasProvider(BigInteger.valueOf(10),BigInteger.valueOf(6_300_000));

        if (!unlock(baseAddress, basePassword)) {
            return false;
        }

        try {
            PICOToken contract = PICOToken.load(contractAddress, web3j, baseClientTransactionManager,
                                                new DefaultGasProvider());
            log.info(contract.name().send());
            log.info("total_reserve:{}",contract.totalReserve().send());
            log.info("balance:{}",contract.balanceOf(baseAddress).send());

            TransactionReceipt receipt=contract.invest(contractAddress, BigInteger.valueOf(100), BigInteger.valueOf(100)).send();
            log.info("block:{}",receipt.getBlockNumber());
            log.info("tx:{}",receipt.getTransactionHash());
            log.info("balance:{}",contract.balanceOf(baseAddress).send());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean withdraw() {
        ContractGasProvider contractGasProvider = new StaticGasProvider(BigInteger.valueOf(10),BigInteger.valueOf(6_300_000));

        if (!unlock(baseAddress, basePassword)) {
            return false;
        }

        try {
            PICOToken contract = PICOToken.load(contractAddress, web3j, baseClientTransactionManager,
                                                new DefaultGasProvider());
            log.info("total_reserve:{}",contract.totalReserve().send());
            BigInteger balance=contract.balanceOf(baseAddress).send();
            log.info("balance:{}",balance);
            if(balance.compareTo(BigInteger.valueOf(100))>0){
                balance=BigInteger.valueOf(100);
            }
            TransactionReceipt receipt=contract.withdraw(contractAddress, balance).send();
            log.info("block:{}",receipt.getBlockNumber());
            log.info("tx:{}",receipt.getTransactionHash());
            log.info("balance:{}",contract.balanceOf(baseAddress).send());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean unlock(String address, String password) {
        try {
            admin.personalUnlockAccount(address, password, unlockDuration).send();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void main(String[] args){
        Util util=new Util();
        util.deploy();
        for(int i=0;i<100;i++) {
            util.invest();
            util.withdraw();
        }
    }

}
