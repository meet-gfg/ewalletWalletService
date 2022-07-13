package com.gfg.ewallet.wallet.dao;


import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionStatusMessage {

    private String transactionId;
    private TransactionStatus status;
}
