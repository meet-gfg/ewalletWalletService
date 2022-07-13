package com.gfg.ewallet.wallet.repository;

import com.gfg.ewallet.wallet.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<Wallet,Integer> {

    Wallet findByUserId(Integer userId);

}
