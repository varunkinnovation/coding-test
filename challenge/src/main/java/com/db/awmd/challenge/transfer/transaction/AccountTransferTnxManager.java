package com.db.awmd.challenge.transfer.transaction;

import java.lang.reflect.Proxy;
import java.util.Map;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.repository.AccountsRepository;

import lombok.Getter;

public class AccountTransferTnxManager {

	private final AccountsRepository accountsRepository;
	
	private AccountTransferTransacrionHandler<Account> accountTransferhandler;
	
	@Getter
	private boolean autoCommit = false;
	
	@Getter
	private AccountsRepository accountsRepo;
	
	public AccountTransferTnxManager(AccountsRepository accountsRepository){
		this.accountsRepository = accountsRepository;
		
		accountTransferhandler = new AccountTransferTransacrionHandler<Account>(accountsRepository);
		accountsRepo = (AccountsRepository)Proxy.newProxyInstance(AccountsRepository.class.getClassLoader()
				, new Class[] { AccountsRepository.class }, accountTransferhandler);
		
	}
	
	public void doInTransaction(AccountTransferTransactionCallback callback) {
		AccountTransferTransactionContext<Account, Account> accountTransferContext = new AccountTransferTransactionContext<>();
		ThreadLocal<AccountTransferTransactionContext<Account, Account>> localContext = accountTransferhandler.getLocalContext();
		localContext.set(accountTransferContext);
		try {
			callback.process();
			if(autoCommit) {
				commitTransaction();
			}
		}catch(Exception e) {
			rollBackTransaction();
			throw e;
		}finally {
			
		}
		
	}
	
	
	public void commitTransaction() {
		AccountTransferTransactionContext<Account, Account> localContext = accountTransferhandler.getLocalContext().get();
		Map<Account, Account> savePoints = localContext.getTransactionSaveState();
	 
		savePoints.entrySet().forEach(entry -> {
			Account key = entry.getKey();
			Account value = entry.getValue();
			value.setBalance(key.getBalance());
		});
	}
	
	public void rollBackTransaction() {
	
		AccountTransferTransactionContext<Account, Account> localContext = accountTransferhandler.getLocalContext().get();
		localContext.getTransactionSaveState().clear();
	}
	
	
	
	
}
