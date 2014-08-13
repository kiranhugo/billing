package org.mifosplatform.finance.payments.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class DalpayRequestFailureException extends AbstractPlatformDomainRuleException{

	public DalpayRequestFailureException(final Long user){
		 super("error.msg.finance.payment.dalpay.user1.not.found", "Dalpay Response 'user1' value invalid, user1= " + user);
	}
}