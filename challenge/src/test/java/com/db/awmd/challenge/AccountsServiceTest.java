package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.NotificationService;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private NotificationService notificationService;
  
  @Test
  public void addAccount() throws Exception {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  public void addAccount_failsOnDuplicateId() throws Exception {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }

  }
  
  @Test
  public void fundTransferCommit() throws Exception {
	  	Account accountFrom = new Account("Id-1233");
	  	accountFrom.setBalance(new BigDecimal(100));
	    this.accountsService.createAccount(accountFrom);
	    Account accountTo = new Account("Id-124");
	    accountTo.setBalance(new BigDecimal(100));
	    this.accountsService.createAccount(accountTo);
	    this.accountsService.fundTransfer("Id-1233", "Id-124", new BigDecimal(100));
	    assertThat(this.accountsService.getAccount("Id-1233").getBalance()).isEqualTo(BigDecimal.ZERO);
	    assertThat(this.accountsService.getAccount("Id-124").getBalance()).isEqualTo(new BigDecimal(200));

  }
  
  @Test
  public void fundTransferRollBack() throws Exception {
		Account accountFrom = new Account("Id-127");
	  	accountFrom.setBalance(new BigDecimal(100));
	    this.accountsService.createAccount(accountFrom);
	    Account accountTo = new Account("Id-128");
	    accountTo.setBalance(new BigDecimal(100));
	    this.accountsService.createAccount(accountTo);
	    this.accountsService.fundTransfer("Id-127", "Id-128", new BigDecimal(100));
	  	
	    try {
 
	    	this.accountsService.fundTransfer("Id-127", "Id-128", new BigDecimal(50));
	    }catch(Exception e) {
	  		assertThat(e.getMessage()).isEqualTo("Insufficient balance in fromAccount");
	  	}

	    assertThat(this.accountsService.getAccount("Id-127").getBalance()).isEqualTo(BigDecimal.ZERO);
	    assertThat(this.accountsService.getAccount("Id-128").getBalance()).isEqualTo(new BigDecimal(200));

  }
  
  @Test
  public void transactionRollBackOnInvalidAccount() throws Exception {
	  	
	  	Account accountFrom = new Account("Id-125");
	  	accountFrom.setBalance(new BigDecimal(100));
	    this.accountsService.createAccount(accountFrom);
	    try {
	    	this.accountsService.fundTransfer("Id-125", "Id-126", new BigDecimal(50));
	    }catch(Exception e) {
	    	assertThat(e.getMessage()).isEqualTo("Account nullnot found");
	    }

	    assertThat(this.accountsService.getAccount("Id-125").getBalance()).isEqualTo(new BigDecimal(100));

  }

  @Test
  public void accountInsufficientFunds() {
      final String accountFromId = UUID.randomUUID().toString();
      final String accountToId = UUID.randomUUID().toString();
      this.accountsService.createAccount(new Account(accountFromId));
      this.accountsService.createAccount(new Account(accountToId));      
      try {
          this.accountsService.fundTransfer(accountFromId,accountToId,new BigDecimal(10));
          fail("Should have failed because account does not have enough balance for the transfer");
      } catch (InsufficientBalanceException nbe) {
          assertThat(nbe.getMessage()).isEqualTo("Insufficient balance in fromAccount");
      }
  }
  
  @Test
  public void transferFunds() {
      final String accountFromId = UUID.randomUUID().toString();
      final String accountToId = UUID.randomUUID().toString();
      final Account accountFrom = new Account(accountFromId, new BigDecimal("500.11"));
      final Account accountTo = new Account(accountToId, new BigDecimal("20.00"));
      final BigDecimal transferAmount = new BigDecimal("200.11");
      
      this.accountsService.createAccount(accountFrom);
      this.accountsService.createAccount(accountTo);

      this.accountsService.fundTransfer(accountFromId,accountToId,transferAmount);
      
      assertThat(this.accountsService.getAccount(accountFromId).getBalance()).isEqualTo(new BigDecimal("300.00"));
      assertThat(this.accountsService.getAccount(accountToId).getBalance()).isEqualTo(new BigDecimal("220.11"));

      verifyNotifications(accountFrom, accountTo, transferAmount);
  }
  
  @Test
  public void fundTransferClearbalance() {

      final String accountFromId = UUID.randomUUID().toString();
      final String accountToId = UUID.randomUUID().toString();
      final Account accountFrom = new Account(accountFromId, new BigDecimal("100.01"));
      final Account accountTo = new Account(accountToId, new BigDecimal("20.00"));
      final BigDecimal transferAmount = new BigDecimal("100.01");

      this.accountsService.createAccount(accountFrom);
      this.accountsService.createAccount(accountTo);      

      this.accountsService.fundTransfer(accountFromId,accountToId,transferAmount);

      assertThat(this.accountsService.getAccount(accountFromId).getBalance()).isEqualTo(new BigDecimal("0.00"));
      assertThat(this.accountsService.getAccount(accountToId).getBalance()).isEqualTo(new BigDecimal("120.01"));
      Mockito.mock(NotificationService.class);
      verifyNotifications(accountFrom, accountTo, transferAmount);
  }
  
  private void verifyNotifications(final Account accountFrom, final Account accountTo, final BigDecimal amount) {
      verify(notificationService, Mockito.times(1)).notifyAboutTransfer(accountFrom, "The transfer to the account with ID " + accountTo.getAccountId() + " is now complete for the amount of " + amount + ".");
      verify(notificationService, Mockito.times(1)).notifyAboutTransfer(accountTo, "The account with ID + " + accountFrom.getAccountId() + " has transferred " + amount + " into your account.");
  }
 
}
