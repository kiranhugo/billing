package org.mifosplatform.cms.eventorder.service;

import java.math.BigDecimal;
import java.util.List;

import org.mifosplatform.cms.eventmaster.domain.EventDetails;
import org.mifosplatform.cms.eventmaster.domain.EventDetailsRepository;
import org.mifosplatform.cms.eventmaster.domain.EventMaster;
import org.mifosplatform.cms.eventmaster.domain.EventMasterRepository;
import org.mifosplatform.cms.eventorder.domain.EventOrder;
import org.mifosplatform.cms.eventorder.domain.EventOrderRepository;
import org.mifosplatform.cms.eventorder.domain.EventOrderdetials;
import org.mifosplatform.cms.eventpricing.domain.EventPricing;
import org.mifosplatform.cms.eventpricing.domain.EventPricingRepository;
import org.mifosplatform.cms.media.domain.MediaAsset;
import org.mifosplatform.cms.media.exceptions.NoEventMasterFoundException;
import org.mifosplatform.cms.media.exceptions.NoMoviesFoundException;
import org.mifosplatform.cms.mediadetails.domain.MediaAssetRepository;
import org.mifosplatform.cms.mediadetails.domain.MediaassetLocation;
import org.mifosplatform.cms.mediadetails.exception.NoMediaDeviceFoundException;
import org.mifosplatform.cms.mediadevice.data.MediaDeviceData;
import org.mifosplatform.cms.mediadevice.service.MediaDeviceReadPlatformService;
import org.mifosplatform.finance.clientbalance.data.ClientBalanceData;
import org.mifosplatform.finance.clientbalance.service.ClientBalanceReadPlatformService;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.mifosplatform.infrastructure.configuration.domain.GlobalConfigurationProperty;
import org.mifosplatform.infrastructure.configuration.domain.GlobalConfigurationRepository;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.logistics.itemdetails.exception.ActivePlansFoundException;
import org.mifosplatform.logistics.onetimesale.data.OneTimeSaleData;
import org.mifosplatform.logistics.onetimesale.serialization.EventOrderCommandFromApiJsonDeserializer;
import org.mifosplatform.logistics.onetimesale.service.InvoiceOneTimeSale;
import org.mifosplatform.logistics.onetimesale.service.OneTimeSaleReadPlatformService;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.order.data.CustomValidationData;
import org.mifosplatform.portfolio.order.service.OrderDetailsReadPlatformServices;
import org.mifosplatform.portfolio.transactionhistory.service.TransactionHistoryWritePlatformService;
import org.mifosplatform.workflow.eventaction.data.ActionDetaislData;
import org.mifosplatform.workflow.eventaction.service.ActionDetailsReadPlatformService;
import org.mifosplatform.workflow.eventaction.service.ActiondetailsWritePlatformService;
import org.mifosplatform.workflow.eventaction.service.EventActionConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventOrderWriteplatformServiceImpl implements EventOrderWriteplatformService {

	private final PlatformSecurityContext context;
	private final ClientRepository clientRepository;
	private final InvoiceOneTimeSale invoiceOneTimeSale;
	private final MediaAssetRepository mediaAssetRepository;
	private final EventOrderRepository eventOrderRepository;
	private final EventMasterRepository eventMasterRepository;
	private final EventDetailsRepository eventDetailsRepository;
	private final EventPricingRepository eventPricingRepository;
	private final GlobalConfigurationRepository configurationRepository; 
	private final MediaDeviceReadPlatformService deviceReadPlatformService;
	private final EventOrderReadplatformServie eventOrderReadplatformServie;
	private final ClientBalanceReadPlatformService balanceReadPlatformService;
	private final EventOrderCommandFromApiJsonDeserializer apiJsonDeserializer;
	private final ActionDetailsReadPlatformService actionDetailsReadPlatformService;
    private final OrderDetailsReadPlatformServices orderDetailsReadPlatformServices; 
	private final ActiondetailsWritePlatformService actiondetailsWritePlatformService;
	private final TransactionHistoryWritePlatformService transactionHistoryWritePlatformService;

	@Autowired
	public EventOrderWriteplatformServiceImpl(final PlatformSecurityContext context,final EventOrderRepository eventOrderRepository,
			final EventOrderCommandFromApiJsonDeserializer apiJsonDeserializer,final EventOrderReadplatformServie eventOrderReadplatformServie,
			final InvoiceOneTimeSale invoiceOneTimeSale,final OneTimeSaleReadPlatformService oneTimeSaleReadPlatformService,
			final EventMasterRepository eventMasterRepository,final MediaAssetRepository mediaAssetRepository,final EventPricingRepository eventPricingRepository,
			final MediaDeviceReadPlatformService deviceReadPlatformService,final ClientBalanceReadPlatformService balanceReadPlatformService,
			final EventDetailsRepository eventDetailsRepository,final TransactionHistoryWritePlatformService transactionHistoryWritePlatformService,
			final ActionDetailsReadPlatformService actionDetailsReadPlatformService,final ActiondetailsWritePlatformService actiondetailsWritePlatformService,
			final OrderDetailsReadPlatformServices orderDetailsReadPlatformServices,final GlobalConfigurationRepository configurationRepository,
			final ClientRepository clientRepository) {
		
		this.context = context;
		this.clientRepository=clientRepository;
		this.invoiceOneTimeSale = invoiceOneTimeSale;
		this.apiJsonDeserializer = apiJsonDeserializer;
		this.mediaAssetRepository = mediaAssetRepository;
		this.eventOrderRepository = eventOrderRepository;
		this.eventDetailsRepository=eventDetailsRepository;
		this.eventMasterRepository = eventMasterRepository;
		this.eventPricingRepository = eventPricingRepository;
		this.configurationRepository=configurationRepository;
		this.deviceReadPlatformService = deviceReadPlatformService;
		this.balanceReadPlatformService = balanceReadPlatformService;
		this.eventOrderReadplatformServie = eventOrderReadplatformServie;
		this.actionDetailsReadPlatformService=actionDetailsReadPlatformService;
		this.orderDetailsReadPlatformServices=orderDetailsReadPlatformServices;
		this.actiondetailsWritePlatformService=actiondetailsWritePlatformService;
		this.transactionHistoryWritePlatformService = transactionHistoryWritePlatformService;

	}

	private void handleCodeDataIntegrityIssues(JsonCommand command,DataIntegrityViolationException dve) {
		// TODO Auto-generated method stub

	}

	@Transactional
	@Override
	public CommandProcessingResult createEventOrder(JsonCommand command) {
		
		try {
			String response = "";
			this.context.authenticatedUser();
			GlobalConfigurationProperty configuration=this.configurationRepository.findOneByName(ConfigurationConstants.CONFIR_PROPERTY_REGISTRATION_DEVICE);
			this.apiJsonDeserializer.validateForCreate(command.json(),configuration.isEnabled());
			final Long eventId = command.longValueOfParameterNamed("eventId");
			final String deviceId = command.stringValueOfParameterNamed("deviceId");
			 Long clientId = command.longValueOfParameterNamed("clientId");
			Long clientType=Long.valueOf(0);
			//GlobalConfigurationProperty configuration=this.configurationRepository.findOneByName(ConfigurationConstants.CONFIR_PROPERTY_REGISTRATION_DEVICE);
			
			if(configuration != null && configuration.isEnabled()){
				MediaDeviceData deviceData = this.deviceReadPlatformService.retrieveDeviceDetails(deviceId);
					if (deviceData == null) {
						throw new NoMediaDeviceFoundException();
					}
					clientId=deviceData.getClientId();
					clientType=deviceData.getClientTypeId();
					
			}else if(clientId != null){
				Client client=this.clientRepository.findOne(clientId);
				clientType=client.getCategoryType();
			}
			
			//Check Client Custome Validation
				CustomValidationData customValidationData = this.orderDetailsReadPlatformServices.checkForCustomValidations(clientId,EventActionConstants.EVENT_EVENT_ORDER, command.json());
			
				if(customValidationData.getErrorCode() != 0 && customValidationData.getErrorMessage() != null){
				 throw new ActivePlansFoundException(customValidationData.getErrorMessage()); 
			}
			
			
			final String formatType = command.stringValueOfParameterNamed("formatType");
			final String optType=command.stringValueOfParameterNamed("optType");
			
			EventMaster eventMaster = this.eventMasterRepository.findOne(eventId);
					if(eventMaster == null){
						throw new NoEventMasterFoundException();
					}
					List<EventDetails> eventDetails=eventMaster.getEventDetails();
					EventOrder eventOrder = EventOrder.fromJson(command,eventMaster,clientType);
					
						for(EventDetails detail:eventDetails){
							
							EventDetails eventDetail=this.eventDetailsRepository.findOne(detail.getId());
							MediaAsset mediaAsset = this.mediaAssetRepository.findOne(eventDetail.getMediaId());
							List<MediaassetLocation> mediaassetLocations = mediaAsset.getMediaassetLocations();
							String movieLink = "";
							
								for (MediaassetLocation location : mediaassetLocations) {
									if (location.getFormatType().equalsIgnoreCase(formatType)) {
										movieLink = location.getLocation();
									}
								}
					EventOrderdetials eventOrderdetials=new EventOrderdetials(eventDetail,movieLink,formatType,optType);
				    eventOrder.addEventOrderDetails(eventOrderdetials);
				      if (movieLink.isEmpty()) {
				    	  throw new NoMoviesFoundException();
				    }
				}
				/*boolean hasSufficientMoney = this.checkClientBalance(eventOrder.getBookedPrice(), deviceData.getClientId());
					if (!hasSufficientMoney) {
						throw new InsufficientAmountException();
					}*/
				this.eventOrderRepository.save(eventOrder);
				List<OneTimeSaleData> oneTimeSaleDatas = eventOrderReadplatformServie.retrieveEventOrderData(eventOrder.getClientId());
					for (OneTimeSaleData oneTimeSaleData : oneTimeSaleDatas) {
							this.invoiceOneTimeSale.invoiceOneTimeSale(eventOrder.getClientId(), oneTimeSaleData);
							this.updateOneTimeSale(oneTimeSaleData);
					}

						//Add New Action 
				List<ActionDetaislData> actionDetaislDatas=this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_EVENT_ORDER);
				   if(!actionDetaislDatas.isEmpty()){
					  response = this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas,clientId,eventOrder.getId().toString());
				   }		
				   transactionHistoryWritePlatformService.saveTransactionHistory(eventOrder.getClientId(), "Event Order", eventOrder.getEventBookedDate(),
						   "CancelFlag:"+eventOrder.getCancelFlag(),"bookedPrice:"+eventOrder.getBookedPrice(),"EventValidTillDate:"+eventOrder.getEventValidtill(),
						   "EventId:"+eventOrder.getEventId(),"EventOrderID:"+eventOrder.getId());
			
		//	return new CommandProcessingResult(eventOrder.getEventOrderdetials().get(0).getMovieLink());
			    return new CommandProcessingResultBuilder().withEntityId(eventMaster.getId()).withResourceIdAsString(response).build();
			
			} catch (DataIntegrityViolationException dve) {
				handleCodeDataIntegrityIssues(command, dve);
				return new CommandProcessingResult(Long.valueOf(-1));
			}

		}

	
	@Override
	public CommandProcessingResult updateEventOrderPrice(JsonCommand command) {
		
			Long id = eventOrderReadplatformServie.getCurrentRow(command.stringValueOfParameterNamed("formatType"), 
						command.stringValueOfParameterNamed("optType"), command.longValueOfParameterNamed("clientId"));
			EventPricing eventPricing = eventPricingRepository.findOne(id);
			eventPricing.setPrice(Double.valueOf(command.stringValueOfParameterNamed("price")));
			eventPricingRepository.save(eventPricing);
			return new CommandProcessingResultBuilder().withResourceIdAsString(eventPricing.getPrice().toString()).build();
	}
	
	
	public boolean checkClientBalance(Double bookedPrice, Long clientId) {
		
		  boolean isBalanceAvailable = false;
		  ClientBalanceData clientBalance=this.balanceReadPlatformService.retrieveBalance(clientId);
		  BigDecimal eventPrice=new BigDecimal(bookedPrice);
		  	if(clientBalance!=null){
		  		BigDecimal resultantBalance = clientBalance.getBalanceAmount().add(eventPrice);
		  		if(resultantBalance.compareTo(BigDecimal.ZERO) == -1 || resultantBalance.compareTo(BigDecimal.ZERO) == 0){
		  			isBalanceAvailable = true;   
			  	}else {
			  			isBalanceAvailable = false;
			  	}
		  	}
		  return isBalanceAvailable;
	}

	public void updateOneTimeSale(OneTimeSaleData oneTimeSaleData) {
		EventOrder oneTimeSale = eventOrderRepository.findOne(oneTimeSaleData.getId());
		oneTimeSale.setInvoiced();
		eventOrderRepository.save(oneTimeSale);

	}
}