package com.db.awmd.challenge.transfer.transaction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.math.BigDecimal;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.repository.AccountsRepository;

import lombok.Getter;

public class AccountTransferTransacrionHandler<E> implements InvocationHandler {
	
	private final AccountsRepository accountsRepository;
	
	@Getter
	ThreadLocal<AccountTransferTransactionContext<Account, Account>> localContext = new ThreadLocal<>();;
	
	AccountTransferTransacrionHandler(AccountsRepository accountsRepository){
		this.accountsRepository = accountsRepository;
	}
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		String methodName = method.getName();
		if(methodName.startsWith("get")) {
			Account account = accountsRepository.getAccount((String)args[0]);
			BigDecimal balanceCopy = BigDecimal.ZERO;
			Account proxyAccount = new Account(account.getAccountId(), balanceCopy.add(account.getBalance()));
			
			AccountTransferTransactionContext<Account, Account> context = localContext.get();
			if(context != null) {
				context.getTransactionSaveState().put(proxyAccount, account);
				return proxyAccount;
			}else {

				return account;
			}
		}
		
		return null;
	}
}