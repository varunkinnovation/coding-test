package com.db.awmd.challenge.service;

import java.math.BigDecimal;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.rowset.spi.SyncResolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.TransferAccountNotFoundException;
import com.db.awmd.challenge.exception.FundTransferException;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.db.awmd.challenge.exception.SameAccountTransferException;
import com.db.awmd.challenge.repository.AccountsRepository;
import com.db.awmd.challenge.transfer.transaction.AccountTransferTnxManager;

import lombok.Getter;

@Service
public class AccountsService {

	@Getter
	private final AccountsRepository accountsRepository;

	private AccountTransferTnxManager transactionManager;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private FundTransferValidator transferValidator;

	@Autowired
	public AccountsService(AccountsRepository accountsRepository) {
		this.accountsRepository = accountsRepository;
		this.transactionManager = new AccountTransferTnxManager(accountsRepository);
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	
	public void fundTransfer(final String fromAccount, final String toAccount, final BigDecimal transferAmount)
			throws FundTransferException, TransferAccountNotFoundException, SameAccountTransferException,
			InsufficientBalanceException {
		synchronized (fromAccount) {
		transferValidator.validate(getAccount(fromAccount), getAccount(toAccount), transferAmount);
		transactionManager.doInTransaction(() -> {
           
			this.debit(fromAccount, transferAmount);
			this.credit(toAccount, transferAmount);
		});

		transactionManager.commitTransaction();
		}
		    			notificationService.notifyAboutTransfer(getAccount(fromAccount), "The transfer to the account with ID "+ toAccount + " is now complete for the amount of " + transferAmount + ".");
		notificationService.notifyAboutTransfer(getAccount(toAccount),"The account with ID + " + fromAccount + " has transferred " + transferAmount + " into your account.");
		    	

		    

	}

	private Account debit(String accountId, final BigDecimal amount) throws FundTransferException {
		
		 final Account account = transactionManager.getAccountsRepo().getAccount(accountId);
		
		 if (account == null) {
			throw new FundTransferException("Account does not exist");
		}
		if (account.getBalance().compareTo(amount) == -1) {
			throw new FundTransferException("Insufficient balance in account");
		}
		
		BigDecimal bal = account.getBalance().subtract(amount);
		account.setBalance(bal);
		
		return account;
	}

	private Account credit(String accountId, BigDecimal amount) throws FundTransferException {
		
		final Account account = transactionManager.getAccountsRepo().getAccount(accountId);
		
		if (account == null) {
			throw new FundTransferException("Account does not exist");
		}
		BigDecimal bal = account.getBalance().add(amount);
		account.setBalance(bal);
		 
		return account;
	}
}
