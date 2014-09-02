package org.mifosplatform.billing.selfcare.service;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.mifosplatform.billing.selfcare.domain.SelfCare;
import org.mifosplatform.billing.selfcare.domain.SelfCareTemporary;
import org.mifosplatform.billing.selfcare.domain.SelfCareTemporaryRepository;
import org.mifosplatform.billing.selfcare.exception.SelfCareAlreadyVerifiedException;
import org.mifosplatform.billing.selfcare.exception.SelfCareEmailIdDuplicateException;
import org.mifosplatform.billing.selfcare.exception.SelfCareTemporaryGeneratedKeyNotFoundException;
import org.mifosplatform.billing.selfcare.exception.SelfcareEmailIdNotFoundException;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.core.service.PlatformEmailService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.security.service.RandomPasswordGenerator;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplate;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplateRepository;
import org.mifosplatform.organisation.message.service.MessagePlatformEmailService;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.client.exception.ClientStatusException;
import org.mifosplatform.portfolio.transactionhistory.service.TransactionHistoryWritePlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;


@Service
public class SelfCareWritePlatformServiceImp implements SelfCareWritePlatformService{
	
	private PlatformSecurityContext context;
	private SelfCareRepository selfCareRepository;
	private FromJsonHelper fromJsonHelper;
	private SelfCareCommandFromApiJsonDeserializer selfCareCommandFromApiJsonDeserializer;
	private SelfCareReadPlatformService selfCareReadPlatformService;
	private PlatformEmailService platformEmailService; 
	private TransactionHistoryWritePlatformService transactionHistoryWritePlatformService;
	private MessagePlatformEmailService messagePlatformEmailService;
	private SelfCareTemporaryRepository selfCareTemporaryRepository;
	private final BillingMessageTemplateRepository billingMessageTemplateRepository;
	
	private final static Logger logger = (Logger) LoggerFactory.getLogger(SelfCareWritePlatformServiceImp.class);
	
	@Autowired
	public SelfCareWritePlatformServiceImp(final PlatformSecurityContext context, final SelfCareRepository selfCareRepository, 
			final FromJsonHelper fromJsonHelper, final SelfCareCommandFromApiJsonDeserializer selfCareCommandFromApiJsonDeserializer, 
			final SelfCareReadPlatformService selfCareReadPlatformService, final PlatformEmailService platformEmailService, 
			final TransactionHistoryWritePlatformService transactionHistoryWritePlatformService, 
			final SelfCareTemporaryRepository selfCareTemporaryRepository, final MessagePlatformEmailService messagePlatformEmailService,
			final BillingMessageTemplateRepository billingMessageTemplateRepository ){
		this.context = context;
		this.selfCareRepository = selfCareRepository;
		this.fromJsonHelper = fromJsonHelper;
		this.selfCareCommandFromApiJsonDeserializer = selfCareCommandFromApiJsonDeserializer;
		this.selfCareReadPlatformService = selfCareReadPlatformService;
		this.platformEmailService = platformEmailService;
		this.transactionHistoryWritePlatformService = transactionHistoryWritePlatformService;
		this.selfCareTemporaryRepository = selfCareTemporaryRepository;
		this.messagePlatformEmailService = messagePlatformEmailService;
		this.billingMessageTemplateRepository = billingMessageTemplateRepository;
		
	}
	
	@Override
	public CommandProcessingResult createSelfCare(JsonCommand command) {
		SelfCare selfCare = null;
		Long clientId = null;
		try{
			context.authenticatedUser();
			selfCareCommandFromApiJsonDeserializer.validateForCreate(command);
			selfCare = SelfCare.fromJson(command);
			clientId = command.longValueOfParameterNamed("clientId");
			
			if(clientId == null){
				try{
					clientId = selfCareReadPlatformService.getClientId(selfCare.getUniqueReference());					
					}catch(EmptyResultDataAccessException erdae){
						throw new PlatformDataIntegrityException("this user is not registered","this user is not registered","");
					}catch(Exception e){
						if(e.getMessage() != null){
							throw new PlatformDataIntegrityException("this user not found","this user not found",e.getMessage());
						}else if(e.getCause().getLocalizedMessage() != null){
							throw new PlatformDataIntegrityException("this user not found","this user not found",e.getCause().getLocalizedMessage());
						}else{
							throw new PlatformDataIntegrityException("this user not found","this user not found","");
						}
							
					}
			}
			
			if(clientId !=null && clientId > 0 ){
				selfCare.setClientId(clientId);
				RandomPasswordGenerator passwordGenerator = new RandomPasswordGenerator(8);
				String unencodedPassword = passwordGenerator.generate();
				selfCare.setPassword(unencodedPassword);
				selfCareRepository.save(selfCare);
				
				//platformEmailService.sendToUserAccount(new EmailDetail("OBS Self Care Organisation ", "SelfCare",email, selfCare.getUserName()), unencodedPassword); 

				List<BillingMessageTemplate> messageDetails=this.billingMessageTemplateRepository.findOneByTemplate("SELFCARE REGISTRATION");
				
				String subject=messageDetails.get(0).getSubject();
				String body=messageDetails.get(0).getBody();
				String header=messageDetails.get(0).getHeader().replace("<PARAM1>", selfCare.getUserName() +","+"\n");
				body=body.replace("<PARAM2>", selfCare.getUniqueReference());
				body=body.replace("<PARAM3>", selfCare.getPassword());
				
				StringBuilder prepareEmail =new StringBuilder();
				prepareEmail.append(header);
				prepareEmail.append("\t").append(body);
				prepareEmail.append("\n").append("\n");
				prepareEmail.append(messageDetails.get(0).getFooter());
				
				String message = messagePlatformEmailService.sendGeneralMessage(selfCare.getUniqueReference(), prepareEmail.toString().trim(), subject);
				
				/*//
				
				
				StringBuilder builder = new StringBuilder();
				builder.append("Dear " + selfCare.getUserName() + "\n");
				builder.append("\n");
				builder.append("Your Selfcare User Account has been successfully created.");
				builder.append("Following are the User login Details. ");
				builder.append("\n");
				builder.append("userName :" + selfCare.getUniqueReference() + ".");
				builder.append("\n");
				builder.append("password :" + selfCare.getPassword() + ".");
				builder.append("\n");
				builder.append("Thankyou");
				
				String message = this.messagePlatformEmailService.sendGeneralMessage(selfCare.getUniqueReference(), builder.toString(), emailSubject);		
				*/
				
				transactionHistoryWritePlatformService.saveTransactionHistory(clientId, "Self Care user activation", new Date(), "USerName: "+selfCare.getUserName()+" ClientId" 
						+ selfCare.getClientId() + "Email Sending Result :" + message);
			}else{
				throw new PlatformDataIntegrityException("client does not exist", "client not registered","clientId", "client is null ");
			}
			
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			throw new PlatformDataIntegrityException("duplicate.username", "duplicate.username","duplicate.username", "duplicate.username");
		}catch(EmptyResultDataAccessException emp){
			throw new PlatformDataIntegrityException("empty.result.set", "empty.result.set");
		}
		
		return new CommandProcessingResultBuilder().withEntityId(selfCare.getId()).build();
	}
	

	@Override
	public CommandProcessingResult createSelfCareUDPassword(JsonCommand command) {
		SelfCare selfCare = null;
		Long clientId = null;
		try{
			context.authenticatedUser();
			selfCareCommandFromApiJsonDeserializer.validateForCreateUDPassword(command);
			selfCare = SelfCare.fromJsonODP(command);
			try{
			clientId = selfCareReadPlatformService.getClientId(selfCare.getUniqueReference());
			if(clientId == null || clientId <= 0 ){
				throw new PlatformDataIntegrityException("client does not exist", "this user is not registered","clientId", "client is null ");
			}
			selfCare.setClientId(clientId);
			this.selfCareRepository.save(selfCare);
			transactionHistoryWritePlatformService.saveTransactionHistory(clientId, "Self Care user activation", new Date(), "USerName: "+selfCare.getUserName()+" ClientId"+selfCare.getClientId());
			}
			catch(EmptyResultDataAccessException dve){
				throw new PlatformDataIntegrityException("invalid.account.details","invalid.account.details","this user is not registered");
			}
			
			
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			throw new PlatformDataIntegrityException("duplicate.email", "duplicate.email","duplicate.email", "duplicate.email");
		}catch(EmptyResultDataAccessException emp){
			throw new PlatformDataIntegrityException("empty.result.set", "empty.result.set");
		}
		
		return new CommandProcessingResultBuilder().withEntityId(selfCare.getClientId()).build();
	}

	
	
	private void handleDataIntegrityIssues(JsonCommand command,DataIntegrityViolationException dve) {
		 Throwable realCause = dve.getMostSpecificCause();
		 logger.error(dve.getMessage(), dve);
	        if (realCause.getMessage().contains("username")){	
	        	throw new PlatformDataIntegrityException("validation.error.msg.selfcare.duplicate.userName", "User Name: " + command.stringValueOfParameterNamed("userName")+ " already exists", "userName", command.stringValueOfParameterNamed("userName"));
	        }else if (realCause.getMessage().contains("unique_reference")){
	        	throw new PlatformDataIntegrityException("validation.error.msg.selfcare.duplicate.email", "email: " + command.stringValueOfParameterNamed("uniqueReference")+ " already exists", "email", command.stringValueOfParameterNamed("uniqueReference"));
	        }

	}
	
	@Override
	public CommandProcessingResult updateClientStatus(JsonCommand command,Long entityId) {
            try{
            	
            	this.context.authenticatedUser();
            	String status=command.stringValueOfParameterNamed("status");
            	SelfCare client=this.selfCareRepository.findOneByClientId(entityId);
            	if(client == null){
            		throw new ClientNotFoundException(entityId);
            	}
            	if(status.equalsIgnoreCase("ACTIVE")){
            	
            		if(status.equals(client.getStatus())){
            			throw new ClientStatusException(entityId);
            		}
            	}
            	client.setStatus(status);
            	this.selfCareRepository.save(client);
            	return new CommandProcessingResult(Long.valueOf(entityId));
            	
            }catch(DataIntegrityViolationException dve){
            	handleDataIntegrityIssues(command, dve);
            	return new CommandProcessingResult(Long.valueOf(-1));
            }

	}

	@Override
	public CommandProcessingResult registerSelfCare(JsonCommand command) {
		
		SelfCareTemporary selfCareTemporary = null;
		Long clientId = 0L;
		try{
			context.authenticatedUser();
			selfCareCommandFromApiJsonDeserializer.validateForCreate(command);
			String uniqueReference = command.stringValueOfParameterNamed("userName");
			String returnUrl = command.stringValueOfParameterNamed("returnUrl");
			SelfCare repository=selfCareRepository.findOneByEmailId(uniqueReference);
			if(repository != null){				
				throw new SelfCareEmailIdDuplicateException(uniqueReference);				
			}else{		
				selfCareTemporary = SelfCareTemporary.fromJson(command);
				String unencodedPassword = RandomStringUtils.randomAlphanumeric(27);
				selfCareTemporary.setGeneratedKey(unencodedPassword);
				
				selfCareTemporaryRepository.save(selfCareTemporary);
				String generatedKey = selfCareTemporary.getGeneratedKey() + "11011";
				
				StringBuilder body = new StringBuilder();
				body.append("hi");
				body.append("\n");
				body.append("\n");
				body.append("Your Registration has been successfully completed.");
				body.append("\n");
				body.append("To approve this Registration please click on this link:");
				body.append("\n");
				body.append("URL: " + returnUrl + generatedKey);
				body.append("\n");
				body.append("\n");
				body.append("Thankyou");
				
				String subject = "Register Conformation";
				
					
				String result = messagePlatformEmailService.sendGeneralMessage(selfCareTemporary.getUserName(), body.toString(), subject);
					
				transactionHistoryWritePlatformService.saveTransactionHistory(clientId, "Self Care User Registration", new Date(),
						"EmailId: "+selfCareTemporary.getUserName() + ", returnUrl: "+ returnUrl +", Email Sending Resopnse: " + result);
				
			}
				
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			throw new PlatformDataIntegrityException("duplicate.username", "duplicate.username","duplicate.username", "duplicate.username");
		}catch(EmptyResultDataAccessException emp){
			throw new PlatformDataIntegrityException("empty.result.set", "empty.result.set");
		}
		
		return new CommandProcessingResultBuilder().withEntityId(selfCareTemporary.getId()).build();
	}

	@Override
	public CommandProcessingResult selfCareEmailVerification(JsonCommand command) {
		SelfCareTemporary selfCareTemporary = null;
		Long clientId = 0L;
		try{
			context.authenticatedUser();
			selfCareCommandFromApiJsonDeserializer.validateForCreate(command);
			String verificationKey = command.stringValueOfParameterNamed("verificationKey");
			String uniqueReference = command.stringValueOfParameterNamed("uniqueReference");
			
			
			selfCareTemporary =selfCareTemporaryRepository.findOneByGeneratedKey(verificationKey,uniqueReference);
			
			if(selfCareTemporary == null){				
				throw new SelfCareTemporaryGeneratedKeyNotFoundException(verificationKey,uniqueReference);				
			}else{		
				
				if(selfCareTemporary.getStatus().equalsIgnoreCase("INACTIVE") || selfCareTemporary.getStatus().equalsIgnoreCase("PENDING")){
					
					selfCareTemporary.setStatus("PENDING");
					
					transactionHistoryWritePlatformService.saveTransactionHistory(clientId, "Self Care User Registration is Verified Through Email", new Date(),
							"EmailId: "+selfCareTemporary.getUserName());			
				} else{
					transactionHistoryWritePlatformService.saveTransactionHistory(clientId, "Self Care User Registration is Already Verified Through this GeneratedKey"+verificationKey, new Date(),
							"EmailId: "+selfCareTemporary.getUserName());	
					throw new SelfCareAlreadyVerifiedException(verificationKey);		
				}
			}
				
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			throw new PlatformDataIntegrityException("duplicate.username", "duplicate.username","duplicate.username", "duplicate.username");
		}catch(EmptyResultDataAccessException emp){
			throw new PlatformDataIntegrityException("empty.result.set", "empty.result.set");
		}
		
		return new CommandProcessingResultBuilder().withEntityId(selfCareTemporary.getId()).build();
	}

	@Override
	public CommandProcessingResult generateNewSelfcarePassword(JsonCommand command) {
		
		try{
			context.authenticatedUser();
			selfCareCommandFromApiJsonDeserializer.validateForCreate(command);
			String uniqueReference = command.stringValueOfParameterNamed("uniqueReference");

			SelfCare selfCare =selfCareRepository.findOneByEmailId(uniqueReference);
			
			if(selfCare == null){				
				throw new SelfcareEmailIdNotFoundException(uniqueReference);			
			}else{		
				String generatedKey = RandomStringUtils.randomAlphabetic(10);	
				selfCare.setPassword(generatedKey);
				
				
				StringBuilder body = new StringBuilder();
				body.append("Dear "+selfCare.getUserName() + ",");
				body.append("\n");
				body.append("\n");
				body.append("The password for your SelfCare User Portal Account- "+ uniqueReference +" was reset. .");
				body.append("\n");
				body.append("Password:"+ generatedKey);
				body.append("\n");
				body.append("\n");
				body.append("Thankyou");
				
				String subject = "Reset Password";
				
					
				String result = messagePlatformEmailService.sendGeneralMessage(selfCare.getUniqueReference(), body.toString(), subject);
					
				transactionHistoryWritePlatformService.saveTransactionHistory(selfCare.getClientId(), "Self Care Password Reset", new Date(),
						"EmailId: "+selfCare.getUniqueReference() + ", Email Sending Resopnse: " + result);
			}
			
			return new CommandProcessingResultBuilder().withEntityId(selfCare.getId()).build();
			
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			throw new PlatformDataIntegrityException("duplicate.username", "duplicate.username","duplicate.username", "duplicate.username");
		}catch(EmptyResultDataAccessException emp){
			throw new PlatformDataIntegrityException("empty.result.set", "empty.result.set");
		}
		
		
	}

	@Override
	public CommandProcessingResult selfcareChangePassword(JsonCommand command) {
		
		try{
			context.authenticatedUser();
			selfCareCommandFromApiJsonDeserializer.validateForCreate(command);
			String uniqueReference = command.stringValueOfParameterNamed("uniqueReference");
			String password = command.stringValueOfParameterNamed("password");

			SelfCare selfCare =selfCareRepository.findOneByEmailId(uniqueReference);
			
			if(selfCare == null){				
				throw new SelfcareEmailIdNotFoundException(uniqueReference);			
			}else{		
				
				selfCare.setPassword(password);
				this.selfCareRepository.save(selfCare);
				
				transactionHistoryWritePlatformService.saveTransactionHistory(selfCare.getClientId(), "Self Care Password Reset", new Date(),
						"EmailId: "+selfCare.getUniqueReference());
			}
			
			return new CommandProcessingResultBuilder().withEntityId(selfCare.getId()).build();
			
		} catch(EmptyResultDataAccessException emp){
			throw new PlatformDataIntegrityException("empty.result.set", "empty.result.set");
		}
		
	}
	
}