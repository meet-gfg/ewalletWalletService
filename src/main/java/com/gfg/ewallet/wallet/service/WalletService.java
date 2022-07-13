package com.gfg.ewallet.wallet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gfg.ewallet.wallet.dao.TransactionMessage;
import com.gfg.ewallet.wallet.dao.TransactionStatus;
import com.gfg.ewallet.wallet.dao.TransactionStatusMessage;
import com.gfg.ewallet.wallet.domain.Wallet;
import com.gfg.ewallet.wallet.exception.NegativeBalanceException;
import com.gfg.ewallet.wallet.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class WalletService {

    Logger logger = LoggerFactory.getLogger(WalletService.class);

    private final String TransactionStatusTopic="TRANS_STATUS";

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private ObjectMapper mapper;

    public void createWallet(Wallet wallet) {

        walletRepository.save(wallet);

    }

    public void updateWallet(TransactionMessage transactionMessage) throws JsonProcessingException {
        try {
            if (transactionMessage.getReceiverId() == -1 || transactionMessage.getSenderId() == -1) {
                updateWalletBalance(transactionMessage);
            } else {
                performTransaction(transactionMessage);
            }
            kafkaTemplate.send(TransactionStatusTopic,mapper.writeValueAsString(new TransactionStatusMessage(transactionMessage.getTransactionId(), TransactionStatus.SUCCESS)));
        } catch (NegativeBalanceException e) {
            logger.error("negative balance exception for transaction : {}", transactionMessage.getTransactionId());
            kafkaTemplate.send(TransactionStatusTopic,mapper.writeValueAsString(new TransactionStatusMessage(transactionMessage.getTransactionId(), TransactionStatus.FAILED)));
        } catch (Exception ex) {
            logger.error("Exception in transaction");
            kafkaTemplate.send(TransactionStatusTopic,mapper.writeValueAsString(new TransactionStatusMessage(transactionMessage.getTransactionId(), TransactionStatus.FAILED)));
        }

    }

    // fetch sender wallet
    // if sender wallet < amount -> exceptions
    // get receiver wallet
    // update sender wallet
    //update receiver wallet
    // post suncess or failure message
    private void performTransaction(TransactionMessage transactionMessage) throws Exception{

        Wallet senderWallet = walletRepository.findByUserId(transactionMessage.getSenderId());
        if (Objects.nonNull(senderWallet) && senderWallet.getBalance() < transactionMessage.getAmount()) {
            throw new NegativeBalanceException();
        }
        Wallet receiverWallet = walletRepository.findByUserId(transactionMessage.getReceiverId());

        try {
            Wallet senderWalletCopy = new Wallet();
            Wallet receiverWalletCopy = new Wallet();
            if (Objects.nonNull(receiverWallet)) {
                BeanUtils.copyProperties(senderWallet, senderWalletCopy);
                BeanUtils.copyProperties(receiverWallet, receiverWalletCopy);
                senderWalletCopy.setBalance(senderWallet.getBalance() - transactionMessage.getAmount());
                receiverWalletCopy.setBalance(receiverWallet.getBalance() + transactionMessage.getAmount());
            }
            walletRepository.save(senderWalletCopy);
            walletRepository.save(receiverWalletCopy);
        } catch (Exception ex) {
            logger.error("Exception in performing transaction {} ",ex.getMessage());
            walletRepository.save(senderWallet);
            walletRepository.save(receiverWallet);
            throw ex;
        }

    }

    private void updateWalletBalance(TransactionMessage transactionMessage) {
        int userId = transactionMessage.getSenderId() == -1 ? transactionMessage.getReceiverId() : transactionMessage.getSenderId();
        transactionMessage.setAmount(transactionMessage.getWithdraw() ? transactionMessage.getAmount() * -1 : transactionMessage.getAmount());
        Wallet wallet = walletRepository.findByUserId(userId);
        if (Objects.nonNull(wallet) && (wallet.getBalance() + transactionMessage.getAmount()) < 0) {
            logger.error("balance negative for transaction for user :{} with transaction : {}", userId, transactionMessage.getTransactionId());
            throw new NegativeBalanceException();
        }
        wallet.setBalance(wallet.getBalance() + transactionMessage.getAmount());
        walletRepository.save(wallet);
    }

}
