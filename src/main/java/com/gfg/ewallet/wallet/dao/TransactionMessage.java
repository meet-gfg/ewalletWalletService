package com.gfg.ewallet.wallet.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionMessage {

    private String transactionId;
    private Integer senderId;
    private Integer receiverId;
    private Double amount;
    private Boolean withdraw;

}
