package com.db.awmd.challenge.transfer.transaction;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

public class AccountTransferTransactionContext<K, V> {
	@Getter
	private Map<K, V> transactionSaveState = new HashMap<>();	
}
