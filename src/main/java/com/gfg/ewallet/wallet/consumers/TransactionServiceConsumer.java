package com.gfg.ewallet.wallet.consumers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gfg.ewallet.wallet.dao.TransactionMessage;
import com.gfg.ewallet.wallet.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceConsumer {

    Logger logger= LoggerFactory.getLogger(TransactionServiceConsumer.class);

    @Autowired
    private WalletService walletService;

    @Autowired
    private ObjectMapper objectMapper;

@KafkaListener(topics = {"WALLET_TRANS"},groupId = "walletGroup")
    public void createWalletEvent(String message) throws JsonProcessingException {
        logger.info("message -> {}",message);
        TransactionMessage transactionMessage=objectMapper.readValue(message, TransactionMessage.class);
        walletService.updateWallet(transactionMessage);
    }
}
