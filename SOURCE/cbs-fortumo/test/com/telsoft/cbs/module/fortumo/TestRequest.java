package com.telsoft.cbs.module.fortumo;

import com.telsoft.cbs.module.fortumo.domain.AuthRequest;
import com.telsoft.cbs.module.fortumo.domain.CurrencyAmount;
import com.telsoft.cbs.module.fortumo.domain.RefType;
import com.telsoft.cbs.module.fortumo.domain.UserAccount;

public class TestRequest {



	public static void main(String[] args) throws Exception {

		UserAccount userAccount = new UserAccount();
		userAccount.setAccount("0979080988");
		userAccount.setRefType(RefType.MSISDN);
		CurrencyAmount currencyAmount = new CurrencyAmount();
		currencyAmount.setAmount("30000");
		currencyAmount.setCurrency("VND");
		AuthRequest request = new AuthRequest();
		request.setCorrelationId("abcxyz");
		request.setAccount(userAccount);
		request.setPurchaseAmount(currencyAmount);
		request.setProductDescription("Test payment - AKSJDAKHm ạhê kánhekáh hgế hỏi chấm");
		request.setPurchaseTime("202302311100");


		System.out.println(request.toString());
	}
}
