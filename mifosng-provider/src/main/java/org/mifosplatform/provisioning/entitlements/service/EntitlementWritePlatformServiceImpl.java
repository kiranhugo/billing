package org.mifosplatform.provisioning.entitlements.service;

import java.util.List;

import org.mifosplatform.billing.selfcare.domain.SelfCare;
import org.mifosplatform.billing.selfcare.service.SelfCareRepository;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.organisation.message.service.MessagePlatformEmailService;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequest;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequestDetails;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequestRepository;
import org.mifosplatform.provisioning.processrequest.service.ProcessRequestWriteplatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class EntitlementWritePlatformServiceImpl implements EntitlementWritePlatformService {

	private final ProcessRequestRepository entitlementRepository;
	private final ProcessRequestWriteplatformService processRequestWriteplatformService;
	private final ClientRepository clientRepository;
	private final MessagePlatformEmailService messagePlatformEmailService;
	private final SelfCareRepository selfCareRepository;
	

	@Autowired
	public EntitlementWritePlatformServiceImpl(
			final ProcessRequestWriteplatformService processRequestWriteplatformService,
			final ProcessRequestRepository entitlementRepository, final ClientRepository clientRepository,
			final MessagePlatformEmailService messagePlatformEmailService,final SelfCareRepository selfCareRepository) {

		this.processRequestWriteplatformService = processRequestWriteplatformService;
		this.entitlementRepository = entitlementRepository;
		this.clientRepository = clientRepository;
		this.messagePlatformEmailService = messagePlatformEmailService;
		this.selfCareRepository = selfCareRepository;
	}

	/* In This create(JsonCommand command) method, 
	 * The provSystem,clientId,Authpin parameters are sends only for beenius integration. 
	 * For sending Beenius Generated Authpin to Client Email Address.
	 */
	
	@Override
	public CommandProcessingResult create(JsonCommand command) {
		String authPin = null;
		String message = null;	
		String provSystem = command.stringValueOfParameterNamed("provSystem");
		String requestType = command.stringValueOfParameterNamed("requestType");
		
		if(provSystem != null && requestType !=null && provSystem.equalsIgnoreCase("Beenius") 
				&& requestType.equalsIgnoreCase("ACTIVATION")){
			
			authPin = command.stringValueOfParameterNamed("authPin");
			Long clientId = command.longValueOfParameterNamed("clientId");	
			
			if(clientId !=null && authPin !=null && authPin.length()>0 && clientId>0){
				
				Client client = this.clientRepository.findOne(clientId);
				SelfCare selfcare = this.selfCareRepository.findOneByClientId(clientId);
				
				if(client == null){
					throw new ClientNotFoundException(clientId);
				}
				
				if(selfcare == null){
					throw new PlatformDataIntegrityException("client does not exist", "client not registered","clientId", "client is null ");
				}
				StringBuilder builder = new StringBuilder();
				builder.append("Dear " + client.getFirstname() + " " + client.getLastname()+ "\n");
				builder.append("\n");
				builder.append("Your Beenius Subscriber Account has been successfully created.");
				builder.append("Following are the Beenius Account Details. ");
				builder.append("\n");
				builder.append("subscriberUid : " + client.getAccountNumber());
				builder.append("\n");
				builder.append("Authpin : " + authPin + ".");
				builder.append("\n");
				builder.append("PIN : 1234");
				builder.append("\n");
				builder.append("\n");
				builder.append("Thankyou");
				
				selfcare.setAuthPin(authPin);
				this.selfCareRepository.save(selfcare);
				
				message = this.messagePlatformEmailService.sendGeneralMessage(client.getEmail(), builder.toString(), 
						"Beenius StreamingMedia");	
						
			}/*else{
				throw new PlatformDataIntegrityException("error.msg.beenius.process.invalid","Invalid data from Beenius adapter," +
						" clientId: " + clientId + ",authpin: " + authPin, "clientId="+clientId+ ",authpin="+authPin);
			}*/
			
		}
		
		ProcessRequest processRequest = this.entitlementRepository.findOne(command.entityId());
		String receiveMessage = command.stringValueOfParameterNamed("receiveMessage");
		char status;
		if (receiveMessage.contains("failure :")) {
			status = 'F';
		} else {
			status = 'Y';
		}
			
		List<ProcessRequestDetails> details = processRequest.getProcessRequestDetails();
		

		for (ProcessRequestDetails processRequestDetails : details) {
			Long id = command.longValueOfParameterNamed("prdetailsId");
			if (processRequestDetails.getId().longValue() == id.longValue()) {
				processRequestDetails.updateStatus(command);

				if(provSystem != null && requestType !=null && authPin !=null && provSystem.equalsIgnoreCase("Beenius") && requestType.equalsIgnoreCase("ACTIVATION") ){
					processRequestDetails.setReceiveMessage(processRequestDetails.getReceiveMessage() +
							", generated authpin=" + authPin + ", Email output=" + message);
				}else{
					processRequestDetails.setReceiveMessage(processRequestDetails.getReceiveMessage());
				}
				
			}
		}

		if (processRequest.getRequestType().equalsIgnoreCase("DEVICE_SWAP") && !checkProcessDetailsUpdated(details)) {
			status = 'F';
		}
		processRequest.setProcessStatus(status);

		// this.entitlementRepository.save(processRequest);
		this.processRequestWriteplatformService.notifyProcessingDetails(processRequest, status);
		
		return new CommandProcessingResult(processRequest.getId());

	}

	private boolean checkProcessDetailsUpdated(List<ProcessRequestDetails> details) {
		boolean flag = true;
		if (details.get(0).getReceiveMessage().contains("failure : Exce")) {
			flag = false;
		}
		return flag;
	}

}
