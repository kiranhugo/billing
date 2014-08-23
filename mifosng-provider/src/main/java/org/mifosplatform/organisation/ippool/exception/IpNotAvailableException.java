package org.mifosplatform.organisation.ippool.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;


public class IpNotAvailableException extends AbstractPlatformDomainRuleException {

    public IpNotAvailableException(String ip) {
        super("error.msg.ipaddress.not.available.to.assign", "Ipaddress is not available to assign",ip);
    }

	public IpNotAvailableException(Long orderId) {
		 super("error.msg.duplicate.ipaddresses.are.not allow.to.assign", "Duplicate ip's are not allow to assign",orderId);
	}
    
}
