package org.mifosplatform.billing.inventory.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;


public class ActivePlansFoundException extends AbstractPlatformDomainRuleException {

    public ActivePlansFoundException() {
        super("error.msg.billing.active.plans.found", " plans are activated with this hardWare ");
    }

	public ActivePlansFoundException(int errorCode) {
		
		 super("error.msg.billing.active.plans.found.for.client", "plans are activated with this hardWare",errorCode);
	}
    
   
}
