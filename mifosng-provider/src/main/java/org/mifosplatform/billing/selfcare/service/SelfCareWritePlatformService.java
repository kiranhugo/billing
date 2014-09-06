package org.mifosplatform.billing.selfcare.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface SelfCareWritePlatformService {

	public CommandProcessingResult createSelfCare(JsonCommand command);

	public CommandProcessingResult createSelfCareUDPassword(JsonCommand command);

	CommandProcessingResult updateClientStatus(JsonCommand command,Long entityId);

	public CommandProcessingResult registerSelfCare(JsonCommand command);

	public CommandProcessingResult selfCareEmailVerification(JsonCommand command);

	public CommandProcessingResult generateNewSelfcarePassword(JsonCommand command);

	public CommandProcessingResult selfcareChangePassword(JsonCommand command);
	
}
