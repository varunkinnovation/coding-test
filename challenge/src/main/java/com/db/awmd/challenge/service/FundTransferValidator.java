package com.db.awmd.challenge.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.FundTransferException;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.db.awmd.challenge.exception.SameAccountTransferException;
import com.db.awmd.challenge.exception.TransferAccountNotFoundException;

@Service
public class FundTransferValidator {
	void validate(final Account fromAccount, final Account toAccount, final BigDecimal transferAmount)
			throws TransferAccountNotFoundException, InsufficientBalanceException {
		
		 if (null == fromAccount  || null == toAccount ) {
			String exceptionMsg=fromAccount == null ?"Account " + fromAccount + "not found": "Account " + toAccount + "not found";
			throw new TransferAccountNotFoundException(exceptionMsg);
		  }

		
		if (sameAccount(fromAccount, toAccount)) {
			throw new SameAccountTransferException("Same Account Transfer not allowed");
		}
		if(transferAmount.compareTo(BigDecimal.ZERO)<=0) {
			throw new FundTransferException("Please try with valid amount");
		}

		if (!checkBalance(fromAccount, transferAmount)) {
			throw new InsufficientBalanceException("Insufficient balance in fromAccount");
		}
	}

	private boolean sameAccount(final Account accountFrom, final Account accountTo) {
		return accountFrom.getAccountId().equals(accountTo.getAccountId());
	}

	private boolean checkBalance(final Account account, final BigDecimal amount) {
		boolean balanceCheck;
		
		synchronized (amount) {
			balanceCheck=account.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) >= 0;
		}
		return balanceCheck;
	}

}
