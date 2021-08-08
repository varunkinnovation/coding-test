package com.db.awmd.challenge.transfer.transaction;

@FunctionalInterface
public interface AccountTransferTransactionCallback {
	
	public void process();
}
