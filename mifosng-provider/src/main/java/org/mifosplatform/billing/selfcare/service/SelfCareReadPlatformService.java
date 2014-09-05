package org.mifosplatform.billing.selfcare.service;

import org.mifosplatform.billing.selfcare.data.SelfCareData;

public interface SelfCareReadPlatformService {
	
	public Long getClientId(String uniqueReference);

	public String getEmail(Long clientId);
	
	public SelfCareData login(String userName, String password);
}
