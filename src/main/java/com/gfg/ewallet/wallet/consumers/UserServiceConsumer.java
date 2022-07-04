package com.gfg.ewallet.wallet.consumers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gfg.ewallet.wallet.dao.UserMessage;
import com.gfg.ewallet.wallet.domain.Wallet;
import com.gfg.ewallet.wallet.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;



@Service
public class UserServiceConsumer {

    Logger logger= LoggerFactory.getLogger(UserServiceConsumer.class);

    @Autowired
    private WalletService walletService;

    @Autowired
    private ObjectMapper objectMapper;


    /**
     * {
     *     "userId":"123"
     *
     * }
     * */


    @KafkaListener(topics = {"USER_CREATE"},groupId = "walletGroup")
    public void createWalletEvent(String message) throws JsonProcessingException {
        logger.info("message -> {}",message);
        UserMessage userMessage=objectMapper.readValue(message,UserMessage.class);

        Wallet wallet=Wallet.builder().userId(Integer.parseInt(userMessage.getUserId())).balance(0.0).build();
        walletService.createWallet(wallet);
    }
}
