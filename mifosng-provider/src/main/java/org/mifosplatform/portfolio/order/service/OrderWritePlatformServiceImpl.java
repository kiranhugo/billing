package org.mifosplatform.portfolio.order.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.LocalDate;
import org.mifosplatform.billing.chargecode.domain.ChargeCode;
import org.mifosplatform.billing.chargecode.domain.ChargeCodeRepository;
import org.mifosplatform.billing.discountmaster.domain.DiscountMaster;
import org.mifosplatform.billing.discountmaster.domain.DiscountMasterRepository;
import org.mifosplatform.billing.discountmaster.exceptions.DiscountMasterNoRecordsFoundException;
import org.mifosplatform.billing.pricing.data.PriceData;
import org.mifosplatform.billing.pricing.domain.Price;
import org.mifosplatform.billing.pricing.domain.PriceRepository;
import org.mifosplatform.billing.pricing.service.PriceReadPlatformService;
import org.mifosplatform.billing.promotioncodes.domain.Promotion;
import org.mifosplatform.billing.promotioncodes.domain.PromotionRepository;
import org.mifosplatform.cms.eventorder.service.PrepareRequestWriteplatformService;
import org.mifosplatform.finance.billingorder.exceptions.NoPromotionFoundException;
import org.mifosplatform.finance.billingorder.service.ReverseInvoice;
import org.mifosplatform.finance.payments.api.PaymentsApiResource;
import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.codes.domain.CodeValueRepository;
import org.mifosplatform.infrastructure.codes.exception.CodeNotFoundException;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.mifosplatform.infrastructure.configuration.domain.EnumDomainService;
import org.mifosplatform.infrastructure.configuration.domain.EnumDomainServiceRepository;
import org.mifosplatform.infrastructure.configuration.domain.GlobalConfigurationProperty;
import org.mifosplatform.infrastructure.configuration.domain.GlobalConfigurationRepository;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.logistics.itemdetails.exception.ActivePlansFoundException;
import org.mifosplatform.logistics.onetimesale.data.AllocationDetailsData;
import org.mifosplatform.portfolio.allocation.service.AllocationReadPlatformService;
import org.mifosplatform.portfolio.association.data.AssociationData;
import org.mifosplatform.portfolio.association.domain.HardwareAssociation;
import org.mifosplatform.portfolio.association.exception.HardwareDetailsNotFoundException;
import org.mifosplatform.portfolio.association.service.HardwareAssociationReadplatformService;
import org.mifosplatform.portfolio.association.service.HardwareAssociationWriteplatformService;
import org.mifosplatform.portfolio.client.domain.AccountNumberGenerator;
import org.mifosplatform.portfolio.client.domain.AccountNumberGeneratorFactory;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.domain.ClientStatus;
import org.mifosplatform.portfolio.contract.data.SubscriptionData;
import org.mifosplatform.portfolio.contract.domain.Contract;
import org.mifosplatform.portfolio.contract.domain.SubscriptionRepository;
import org.mifosplatform.portfolio.contract.service.ContractPeriodReadPlatformService;
import org.mifosplatform.portfolio.order.data.CustomValidationData;
import org.mifosplatform.portfolio.order.data.OrderStatusEnumaration;
import org.mifosplatform.portfolio.order.data.UserActionStatusEnumaration;
import org.mifosplatform.portfolio.order.domain.HardwareAssociationRepository;
import org.mifosplatform.portfolio.order.domain.Order;
import org.mifosplatform.portfolio.order.domain.OrderDiscount;
import org.mifosplatform.portfolio.order.domain.OrderDiscountRepository;
import org.mifosplatform.portfolio.order.domain.OrderHistory;
import org.mifosplatform.portfolio.order.domain.OrderHistoryRepository;
import org.mifosplatform.portfolio.order.domain.OrderLine;
import org.mifosplatform.portfolio.order.domain.OrderPrice;
import org.mifosplatform.portfolio.order.domain.OrderPriceRepository;
import org.mifosplatform.portfolio.order.domain.OrderRepository;
import org.mifosplatform.portfolio.order.domain.PaymentFollowup;
import org.mifosplatform.portfolio.order.domain.PaymentFollowupRepository;
import org.mifosplatform.portfolio.order.exceptions.NoOrdersFoundException;
import org.mifosplatform.portfolio.order.exceptions.NoRegionalPriceFound;
import org.mifosplatform.portfolio.order.exceptions.OrderNotFoundException;
import org.mifosplatform.portfolio.order.exceptions.SchedulerOrderFoundException;
import org.mifosplatform.portfolio.order.serialization.OrderCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.plan.data.ServiceData;
import org.mifosplatform.portfolio.plan.domain.Plan;
import org.mifosplatform.portfolio.plan.domain.PlanRepository;
import org.mifosplatform.portfolio.plan.domain.StatusTypeEnum;
import org.mifosplatform.portfolio.plan.domain.UserActionStatusTypeEnum;
import org.mifosplatform.portfolio.service.domain.ProvisionServiceDetails;
import org.mifosplatform.portfolio.service.domain.ProvisionServiceDetailsRepository;
import org.mifosplatform.portfolio.service.domain.ServiceMaster;
import org.mifosplatform.portfolio.service.domain.ServiceMasterRepository;
import org.mifosplatform.portfolio.transactionhistory.service.TransactionHistoryWritePlatformService;
import org.mifosplatform.provisioning.preparerequest.domain.PrepareRequest;
import org.mifosplatform.provisioning.preparerequest.domain.PrepareRequsetRepository;
import org.mifosplatform.provisioning.preparerequest.exception.PrepareRequestActivationException;
import org.mifosplatform.provisioning.preparerequest.service.PrepareRequestReadplatformService;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequest;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequestDetails;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequestRepository;
import org.mifosplatform.provisioning.provisioning.api.ProvisioningApiConstants;
import org.mifosplatform.provisioning.provisioning.service.ProvisioningWritePlatformService;
import org.mifosplatform.useradministration.domain.AppUser;
import org.mifosplatform.workflow.eventaction.data.ActionDetaislData;
import org.mifosplatform.workflow.eventaction.domain.EventAction;
import org.mifosplatform.workflow.eventaction.domain.EventActionRepository;
import org.mifosplatform.workflow.eventaction.service.ActionDetailsReadPlatformService;
import org.mifosplatform.workflow.eventaction.service.ActiondetailsWritePlatformService;
import org.mifosplatform.workflow.eventaction.service.EventActionConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
public class OrderWritePlatformServiceImpl implements OrderWritePlatformService {
	
	private final PlanRepository planRepository;
	private final ReverseInvoice reverseInvoice;
	private final PlatformSecurityContext context;
	private final OrderRepository orderRepository;
	private final PriceRepository  priceRepository;
	private final ClientRepository clientRepository;
	private final PromotionRepository promotionRepository;
	private final PaymentsApiResource paymentsApiResource;
	private final CodeValueRepository codeValueRepository;
	private final ChargeCodeRepository chargeCodeRepository;
	private final OrderPriceRepository OrderPriceRepository;
	private final EventActionRepository eventActionRepository;
	private final OrderHistoryRepository orderHistoryRepository;
	private final SubscriptionRepository subscriptionRepository;
	private final OrderDiscountRepository orderDiscountRepository;
	private final ServiceMasterRepository serviceMasterRepository;
	private final PriceReadPlatformService priceReadPlatformService;
	private final ProcessRequestRepository processRequestRepository;
	private final DiscountMasterRepository discountMasterRepository;
	private final OrderReadPlatformService orderReadPlatformService;
	private final PrepareRequsetRepository prepareRequsetRepository;
	private final PaymentFollowupRepository paymentFollowupRepository;
	private final HardwareAssociationRepository associationRepository;
	private final GlobalConfigurationRepository configurationRepository;
	private final EnumDomainServiceRepository enumDomainServiceRepository;
	private final AllocationReadPlatformService allocationReadPlatformService; 
	private final OrderCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	private final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory;
	private final OrderDetailsReadPlatformServices orderDetailsReadPlatformServices; 
	private final ActionDetailsReadPlatformService actionDetailsReadPlatformService; 
	private final ProvisioningWritePlatformService provisioningWritePlatformService;
    private final ProvisionServiceDetailsRepository provisionServiceDetailsRepository;
    private final PrepareRequestReadplatformService prepareRequestReadplatformService;
    private final ActiondetailsWritePlatformService actiondetailsWritePlatformService;
    private final ContractPeriodReadPlatformService contractPeriodReadPlatformService;
    private final PrepareRequestWriteplatformService prepareRequestWriteplatformService;
    private final HardwareAssociationWriteplatformService associationWriteplatformService;
    private final TransactionHistoryWritePlatformService transactionHistoryWritePlatformService;
    private final HardwareAssociationReadplatformService hardwareAssociationReadplatformService;
    
   
    

    @Autowired
	public OrderWritePlatformServiceImpl(final PlatformSecurityContext context,final OrderRepository orderRepository,
			final PlanRepository planRepository,final OrderPriceRepository OrderPriceRepository,final CodeValueRepository codeRepository,
            final ServiceMasterRepository serviceMasterRepository,final EnumDomainServiceRepository enumDomainServiceRepository,
			final SubscriptionRepository subscriptionRepository,final OrderCommandFromApiJsonDeserializer fromApiJsonDeserializer,final ReverseInvoice reverseInvoice,
			final PrepareRequestWriteplatformService prepareRequestWriteplatformService,final DiscountMasterRepository discountMasterRepository,
			final TransactionHistoryWritePlatformService transactionHistoryWritePlatformService,final OrderHistoryRepository orderHistoryRepository,
			final  GlobalConfigurationRepository configurationRepository,final AllocationReadPlatformService allocationReadPlatformService,
			final HardwareAssociationWriteplatformService associationWriteplatformService,final PrepareRequestReadplatformService prepareRequestReadplatformService,
			final ProvisionServiceDetailsRepository provisionServiceDetailsRepository,final OrderReadPlatformService orderReadPlatformService,
		    final ProcessRequestRepository processRequestRepository,final HardwareAssociationReadplatformService hardwareAssociationReadplatformService,
		    final PaymentsApiResource paymentsApiResource,final PrepareRequsetRepository prepareRequsetRepository,final PromotionRepository promotionRepository,
		    final OrderDiscountRepository orderDiscountRepository,final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory,
		    final ClientRepository clientRepository,final ActionDetailsReadPlatformService actionDetailsReadPlatformService,
		    final ActiondetailsWritePlatformService actiondetailsWritePlatformService,final OrderDetailsReadPlatformServices orderDetailsReadPlatformServices,
		    final EventActionRepository eventActionRepository,final ContractPeriodReadPlatformService contractPeriodReadPlatformService,
		   final HardwareAssociationRepository associationRepository,final ProvisioningWritePlatformService provisioningWritePlatformService,
		   final PaymentFollowupRepository paymentFollowupRepository,final PriceRepository priceRepository,
		   final PriceReadPlatformService priceReadPlatformService,final ChargeCodeRepository chargeCodeRepository) {
		
		this.context = context;
		this.reverseInvoice=reverseInvoice;
		this.priceRepository=priceRepository;
		this.planRepository = planRepository;
		this.orderRepository = orderRepository;
		this.clientRepository=clientRepository;
		this.codeValueRepository=codeRepository;
		this.promotionRepository=promotionRepository;
		this.paymentsApiResource=paymentsApiResource;
		this.chargeCodeRepository=chargeCodeRepository;
		this.OrderPriceRepository = OrderPriceRepository;
		this.eventActionRepository=eventActionRepository;
		this.associationRepository=associationRepository;
		this.orderHistoryRepository=orderHistoryRepository;
		this.subscriptionRepository = subscriptionRepository;
		this.serviceMasterRepository=serviceMasterRepository;
		this.fromApiJsonDeserializer=fromApiJsonDeserializer;
		this.configurationRepository=configurationRepository;
		this.orderDiscountRepository=orderDiscountRepository;
		this.discountMasterRepository=discountMasterRepository;
		this.processRequestRepository=processRequestRepository;
		this.priceReadPlatformService=priceReadPlatformService;
		this.prepareRequsetRepository=prepareRequsetRepository;
		this.orderReadPlatformService = orderReadPlatformService;
		this.paymentFollowupRepository=paymentFollowupRepository;
		this.enumDomainServiceRepository=enumDomainServiceRepository;
		this.allocationReadPlatformService=allocationReadPlatformService;
		this.associationWriteplatformService=associationWriteplatformService;
		this.actionDetailsReadPlatformService=actionDetailsReadPlatformService;
		this.orderDetailsReadPlatformServices=orderDetailsReadPlatformServices;
		this.provisioningWritePlatformService=provisioningWritePlatformService;
		this.prepareRequestReadplatformService=prepareRequestReadplatformService;
		this.provisionServiceDetailsRepository=provisionServiceDetailsRepository;
		this.accountIdentifierGeneratorFactory=accountIdentifierGeneratorFactory;
		this.actiondetailsWritePlatformService=actiondetailsWritePlatformService;
		this.contractPeriodReadPlatformService=contractPeriodReadPlatformService;
		this.prepareRequestWriteplatformService=prepareRequestWriteplatformService;
		this.hardwareAssociationReadplatformService=hardwareAssociationReadplatformService;
		this.transactionHistoryWritePlatformService = transactionHistoryWritePlatformService;

		

	}
	
	@Override
	public CommandProcessingResult createOrder(Long clientId,JsonCommand command) {
	
		try{
				LocalDate endDate = null;
				Long orderStatus=null;
				this.fromApiJsonDeserializer.validateForCreate(command.json());
			
			//Check for Custome_Validation
				CustomValidationData customValidationData   = this.orderDetailsReadPlatformServices.checkForCustomValidations(clientId,EventActionConstants.EVENT_CREATE_ORDER,command.json());
					if(customValidationData.getErrorCode() != 0 && customValidationData.getErrorMessage() != null){
						throw new ActivePlansFoundException(customValidationData.getErrorMessage()); 
					}
					List<OrderLine> serviceDetails = new ArrayList<OrderLine>();
					List<OrderPrice> orderprice = new ArrayList<OrderPrice>();
					List<PriceData> datas = new ArrayList<PriceData>();
					Order order=Order.fromJson(clientId,command);
					Plan plan = this.planRepository.findOne(order.getPlanId());
					List<ServiceData> details =this.orderDetailsReadPlatformServices.retrieveAllServices(order.getPlanId());
					datas=this.orderDetailsReadPlatformServices.retrieveAllPrices(order.getPlanId(),order.getBillingFrequency(),clientId);
						if(datas.isEmpty()){
							datas=this.orderDetailsReadPlatformServices.retrieveDefaultPrices(order.getPlanId(),order.getBillingFrequency(),clientId);
						}
						if(datas.isEmpty()){
							throw new NoRegionalPriceFound();
						}
						Contract subscriptionData = this.subscriptionRepository.findOne(order.getContarctPeriod());
						LocalDate startDate=new LocalDate(order.getStartDate());
							if(plan.getProvisionSystem().equalsIgnoreCase("None")){
								orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE).getId();
							}else{
								orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId();
							}

							//Calculate EndDate
							endDate = calculateEndDate(startDate,subscriptionData.getSubscriptionType(),subscriptionData.getUnits());
							order=new Order(order.getClientId(),order.getPlanId(),orderStatus,null,order.getBillingFrequency(),startDate, endDate,
							order.getContarctPeriod(), serviceDetails, orderprice,order.getbillAlign(),UserActionStatusTypeEnum.ACTIVATION.toString());
							BigDecimal priceforHistory=BigDecimal.ZERO;
		
								for (PriceData data : datas) {
									LocalDate billstartDate = startDate;
									LocalDate billEndDate = null;
									//end date is null for rc
										if (data.getChagreType().equalsIgnoreCase("RC")	&& endDate != null) {
											billEndDate = endDate;
										} else if(data.getChagreType().equalsIgnoreCase("NRC")) {
											billEndDate = billstartDate;
										}
										final DiscountMaster discountMaster=this.discountMasterRepository.findOne(data.getDiscountId());
										if(discountMaster == null){
											throw new DiscountMasterNoRecordsFoundException();
										}

										//	If serviceId Not Exist
									OrderPrice price = new OrderPrice(data.getServiceId(),data.getChargeCode(), data.getCharging_variant(),data.getPrice(), 
											null, data.getChagreType(),
									data.getChargeDuration(), data.getDurationType(),billstartDate.toDate(), billEndDate,data.isTaxInclusive());
									order.addOrderDeatils(price);
									priceforHistory=priceforHistory.add(data.getPrice());
									//discount Order
									OrderDiscount orderDiscount=new OrderDiscount(order,price,discountMaster.getId(),discountMaster.getStartDate(),null,discountMaster.getDiscountType(),
									discountMaster.getDiscountRate());
									price.addOrderDiscount(orderDiscount);
								}
								for (ServiceData data : details) {
									OrderLine orderdetails = new OrderLine(data.getPlanId(),data.getServiceType(), plan.getStatus(), 'n');
									order.addServiceDeatils(orderdetails);
								}
								this.orderRepository.save(order);
								Long userId=null;
								SecurityContext context = SecurityContextHolder.getContext();
									if (context.getAuthentication() != null) {
										AppUser appUser=this.context.authenticatedUser();
										userId=appUser.getId();
									}else{
										userId=new Long(0);
									}
					boolean isNewPlan=command.booleanPrimitiveValueOfParameterNamed("isNewplan");
					String requstStatus =UserActionStatusTypeEnum.ACTIVATION.toString();
						if(isNewPlan){
							final AccountNumberGenerator orderNoGenerator = this.accountIdentifierGeneratorFactory.determineClientAccountNoGenerator(order.getId());
							order.updateOrderNum(orderNoGenerator.generate());
							this.orderRepository.save(order);

							//Prepare a Requset For Order
							CommandProcessingResult processingResult=this.prepareRequestWriteplatformService.prepareNewRequest(order,plan,requstStatus);
							
							//For Transaction History
							transactionHistoryWritePlatformService.saveTransactionHistory(order.getClientId(), "New Order", order.getStartDate(),"Price:"+priceforHistory,
								"PlanId:"+order.getPlanId(),"contarctPeriod:"+order.getContarctPeriod(),"OrderID:"+order.getId(),
								"BillingAlign:"+order.getbillAlign());
			     
							//	For Order History
							OrderHistory orderHistory=new OrderHistory(order.getId(),new LocalDate(),new LocalDate(),processingResult.commandId(),requstStatus,userId,null);
							this.orderHistoryRepository.save(orderHistory);
						}

						//For Plan And HardWare Association
						GlobalConfigurationProperty configurationProperty=this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY);
							if(configurationProperty.isEnabled()){
								configurationProperty=this.configurationRepository.findOneByName(ConfigurationConstants.CPE_TYPE);
									if(plan.isHardwareReq() == 'Y'){
										List<AllocationDetailsData> allocationDetailsDatas=this.allocationReadPlatformService.retrieveHardWareDetailsByItemCode(clientId,plan.getPlanCode(),configurationProperty.getValue());
											if(!allocationDetailsDatas.isEmpty()){
												this.associationWriteplatformService.createNewHardwareAssociation(clientId,plan.getId(),allocationDetailsDatas.get(0).getSerialNo(),order.getId());
												transactionHistoryWritePlatformService.saveTransactionHistory(order.getClientId(), "Association", new Date(),"Serial No:"
														+allocationDetailsDatas.get(0).getSerialNo(),"Item:"+allocationDetailsDatas.get(0).getItemDescription(),"Plan Code:"+plan.getPlanCode());
											}
									}
							}
							if(plan.getProvisionSystem().equalsIgnoreCase("None")){
								
								Client client=this.clientRepository.findOne(clientId);
								client.setStatus(ClientStatus.ACTIVE.getValue());
								this.clientRepository.save(client);
								List<ActionDetaislData> actionDetaislDatas=this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_CREATE_ORDER);
								if(actionDetaislDatas.size() != 0){
									this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas,command.entityId(), order.getId().toString());
								}
							}
					return new CommandProcessingResult(order.getId());	
		}catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));

		}
		}

    //Calculate EndDate
	public LocalDate calculateEndDate(LocalDate startDate,String durationType,Long duration) {

			LocalDate contractEndDate = null;
			 		if (durationType.equalsIgnoreCase("DAY(s)")) {
			 			contractEndDate = startDate.plusDays(duration.intValue() - 1);
			 		} else if (durationType.equalsIgnoreCase("MONTH(s)")) {
			 			contractEndDate = startDate.plusMonths(duration.intValue()).minusDays(1);
			 		} else if (durationType.equalsIgnoreCase("YEAR(s)")) {
			 		contractEndDate = startDate.plusYears(duration.intValue()).minusDays(1);
			 		} else if (durationType.equalsIgnoreCase("week(s)")) {
			 		contractEndDate = startDate.plusWeeks(duration.intValue()).minusDays(1);
			 		}
			 	return contractEndDate;
			}
			
	private void handleCodeDataIntegrityIssues(JsonCommand command,DataIntegrityViolationException dve) {
				//   	Throwable realCause = dve.getMostSpecificCause();
				throw new PlatformDataIntegrityException("error.msg.office.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
		}
    
	@Transactional
	@Override
	public CommandProcessingResult updateOrderPrice(Long orderId,JsonCommand command) {
		try
		{
				final Long userId=context.authenticatedUser().getId();
				final Order order = retrieveOrderBy(orderId);
				Long orderPriceId=command.longValueOfParameterNamed("priceId");
				BigDecimal price=command.bigDecimalValueOfParameterNamed("price");
				OrderPrice orderPrice=this.OrderPriceRepository.findOne(orderPriceId);
				orderPrice.setPrice(price);
				this.OrderPriceRepository.save(orderPrice);
				
				//For Order History
				OrderHistory orderHistory=new OrderHistory(order.getId(),new LocalDate(),new LocalDate(),null,"UPDATE PRICE",userId,null);
				this.orderHistoryRepository.save(orderHistory);
				return new CommandProcessingResultBuilder() //
				.withCommandId(command.commandId()) //
				.withEntityId(order.getId()) //
				.with(null) //
				.build();
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}
	private Order retrieveOrderBy(Long orderId) {
		 final Order order= this.orderRepository.findOne(orderId);
	        if (order == null) { throw new CodeNotFoundException(orderId.toString()); }
	        return order;
	}

	@Transactional
	@Override
	public CommandProcessingResult deleteOrder(Long orderId, JsonCommand command) {
		
		Order order = this.orderRepository.findOne(orderId);
		List<OrderLine> orderline = order.getServices();
		List<OrderPrice> orderPrices=order.getPrice();
		Plan plan=this.planRepository.findOne(order.getPlanId());
				if(plan.isPrepaid() == 'N' && !plan.getProvisionSystem().equalsIgnoreCase("None")){
					List<Long> prepareIds=this.prepareRequestReadplatformService.getPrepareRequestDetails(orderId);
						if(prepareIds.isEmpty()){
							throw new PrepareRequestActivationException();	
						}
						for(Long id:prepareIds){
							PrepareRequest prepareRequest=this.prepareRequsetRepository.findOne(id);
							prepareRequest.setCancelStatus("CANCEL");
							this.prepareRequsetRepository.save(prepareRequest);
						}
				}
				for(OrderPrice price:orderPrices){
					price.delete();
				}
				for (OrderLine orderData : orderline) {
					orderData.delete();
				}
		order.delete();
		this.orderRepository.save(order);
		
		//For OrderHistory
		AppUser appUser=this.context.authenticatedUser();
		Long userId=appUser.getId();
		
		//For Order History
		OrderHistory orderHistory=new OrderHistory(order.getId(),new LocalDate(),new LocalDate(),null,"CANCELLED",userId,null);
		this.orderHistoryRepository.save(orderHistory);
		transactionHistoryWritePlatformService.saveTransactionHistory(order.getClientId(), "Order Canceled", order.getEndDate(),"Price:"+order.getAllPriceAsString(),"PlanId:"+order.getPlanId(),"contarctPeriod:"+order.getContarctPeriod(),"Services:"+order.getAllServicesAsString(),"OrderID:"+order.getId(),"BillingAlign:"+order.getbillAlign());

		return new CommandProcessingResult(order.getId());
	}
	
    @Transactional
	@Override
	public CommandProcessingResult disconnectOrder(JsonCommand command,Long orderId ) {
		try {
			
				this.fromApiJsonDeserializer.validateForDisconnectOrder(command.json());
				Order order = this.orderRepository.findOne(orderId);
				LocalDate disconnectionDate=command.localDateValueOfParameterNamed("disconnectionDate");
				LocalDate currentDate = new LocalDate();
				currentDate.toDate();
				GlobalConfigurationProperty configurationProperty = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_DISCONNECT);
				List<OrderPrice> orderPrices=order.getPrice();
					for(OrderPrice price:orderPrices){
						price.updateDates(disconnectionDate);
					}
					Plan plan=this.planRepository.findOne(order.getPlanId());
					Long orderStatus=null;
						if(plan.getProvisionSystem().equalsIgnoreCase("None")){
							orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.DISCONNECTED).getId();
						}else{
							orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId();
						}
				if(configurationProperty.isEnabled()){
					if(plan.getBillRule() !=400 && plan.getBillRule() !=300){ 
						this.reverseInvoice.reverseInvoiceServices(orderId, order.getClientId(),disconnectionDate);
					}
				}
				order.update(command,orderStatus);
				order.setuserAction(UserActionStatusTypeEnum.DISCONNECTION.toString());
				this.orderRepository.saveAndFlush(order);


				//Update Client Status
				if(plan.getProvisionSystem().equalsIgnoreCase("None")){
					Long activeOrders=this.orderReadPlatformService.retrieveClientActiveOrderDetails(order.getClientId(), null);
						if(activeOrders == 0){
							Client client=this.clientRepository.findOne(order.getClientId());
							client.setStatus(ClientStatus.DEACTIVE.getValue());
							this.clientRepository.saveAndFlush(client);
						}
				}

				//for Prepare Request
				String requstStatus =UserActionStatusTypeEnum.DISCONNECTION.toString();
				Long processingResultId=new Long(0);
					if(plan.getProvisionSystem().equalsIgnoreCase(ProvisioningApiConstants.PROV_PACKETSPAN)){
						this.provisioningWritePlatformService.postOrderDetailsForProvisioning(order,plan.getPlanCode(),UserActionStatusTypeEnum.DISCONNECTION.toString(),
						processingResultId,null,null,order.getId());
					}else{
						CommandProcessingResult processingResult=this.prepareRequestWriteplatformService.prepareNewRequest(order,plan,requstStatus);
						processingResultId=processingResult.commandId();
					}

					//For Order History
					//	String requstStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.).getValue();
					Long userId=null;
					SecurityContext context = SecurityContextHolder.getContext();
						if (context.getAuthentication() != null) {
							AppUser appUser=this.context.authenticatedUser();
							userId=appUser.getId();
						}else{
							userId=new Long(0);
						}	
			
			//For Order History
			OrderHistory orderHistory=new OrderHistory(order.getId(),new LocalDate(),new LocalDate(),processingResultId,requstStatus,userId,null);
			this.orderHistoryRepository.save(orderHistory);
 
			//for TransactionHistory
			transactionHistoryWritePlatformService.saveTransactionHistory(order.getClientId(),"Order Disconnection", new Date(),
					"Price:"+order.getAllPriceAsString(),"PlanId:"+order.getPlanId(),"contarctPeriod:"+order.getContarctPeriod(),"Services:"+order.getAllServicesAsString(),"OrderID:"+order.getId(),"BillingAlign:"+order.getbillAlign());

		  return new CommandProcessingResult(Long.valueOf(order.getId()));	
		}catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(null,dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
		
	}

  
	@Override
	public CommandProcessingResult renewalClientOrder(JsonCommand command,Long orderId) {
		
		try{
			
			LocalDate newStartdate=null;
			String requstStatus=null;
			this.fromApiJsonDeserializer.validateForRenewalOrder(command.json());
			Order orderDetails=this.orderRepository.findOne(orderId);
				if(orderDetails == null){
					throw new NoOrdersFoundException(orderId);
				}
				List<OrderPrice>  orderPrices=orderDetails.getPrice();
				final Long contractPeriod = command.longValueOfParameterNamed("renewalPeriod");
				final String description=command.stringValueOfParameterNamed("description");
				Contract contractDetails=this.subscriptionRepository.findOne(contractPeriod);
				Plan plan=this.planRepository.findOne(orderDetails.getPlanId());
				
					if(orderDetails.getStatus().equals(StatusTypeEnum.ACTIVE.getValue().longValue())){
						newStartdate=new LocalDate(orderDetails.getEndDate()).plusDays(1);
						LocalDate renewalEndDate=calculateEndDate(newStartdate,contractDetails.getSubscriptionType(),contractDetails.getUnits());
						
						orderDetails.setEndDate(renewalEndDate);
							for(OrderPrice orderprice:orderPrices){
								
								if(plan.isPrepaid() == 'Y'){
									
									ServiceMaster service=this.serviceMasterRepository.findOne(orderprice.getServiceId()); 
								    Price price=this.priceRepository.findOneByPlanAndService(plan.getId(), service.getServiceCode(),contractDetails.getSubscriptionPeriod());
								    
								    if(price != null){
								    	ChargeCode chargeCode=this.chargeCodeRepository.findOneByChargeCode(price.getChargeCode());
								    	orderprice.setChargeCode(chargeCode.getChargeCode());
								    	orderprice.setChargeDuration(chargeCode.getChargeDuration().toString());
								    	orderprice.setChargeType(chargeCode.getChargeType());
								    	orderprice.setChargeDurationType(chargeCode.getDurationType());
								    	orderprice.setPrice(price.getPrice());
								    }
								
									
								}
								orderprice.setBillEndDate(renewalEndDate);
								// this.OrderPriceRepository.save(orderprice);
							}
							requstStatus=UserActionStatusEnumaration.OrderStatusType(UserActionStatusTypeEnum.RENEWAL_BEFORE_AUTOEXIPIRY).getValue();
							//Prepare Provisioning Req
							CodeValue codeValue=this.codeValueRepository.findOneByCodeValue(plan.getProvisionSystem());
							if(codeValue.position() == 1){
								this.prepareRequestWriteplatformService.prepareNewRequest(orderDetails,plan,"RENEWAL_BE");	
							}
							
					} else if(orderDetails.getStatus().equals(StatusTypeEnum.DISCONNECTED.getValue().longValue())){
		    	
						newStartdate=new LocalDate(); 
						LocalDate renewalEndDate=calculateEndDate(newStartdate,contractDetails.getSubscriptionType(),contractDetails.getUnits());
						orderDetails.setEndDate(renewalEndDate);
							
						for(OrderPrice orderprice:orderPrices){
                                 if(plan.isPrepaid() == 'Y'){
									
									ServiceMaster service=this.serviceMasterRepository.findOne(orderprice.getServiceId()); 
								    Price price=this.priceRepository.findOneByPlanAndService(plan.getId(), service.getServiceCode(),contractDetails.getSubscriptionPeriod());
								    
								    if(price != null){
								    	ChargeCode chargeCode=this.chargeCodeRepository.findOneByChargeCode(price.getChargeCode());
								    	orderprice.setChargeCode(chargeCode.getChargeCode());
								    	orderprice.setChargeDuration(chargeCode.getChargeDuration().toString());
								    	orderprice.setChargeType(chargeCode.getChargeType());
								    	orderprice.setChargeDurationType(chargeCode.getDurationType());
								    	orderprice.setPrice(price.getPrice());
								    }
								}
								orderprice.setBillStartDate(newStartdate);
								orderprice.setBillEndDate(renewalEndDate);
								orderprice.setNextBillableDay(null);
								orderprice.setInvoiceTillDate(null);
								this.OrderPriceRepository.save(orderprice);
							}
							requstStatus=UserActionStatusEnumaration.OrderStatusType(UserActionStatusTypeEnum.RENEWAL_AFTER_AUTOEXIPIRY).getValue();
							if(!plan.getProvisionSystem().equalsIgnoreCase("None")){
								orderDetails.setStatus(StatusTypeEnum.PENDING.getValue().longValue());
							}else{
								orderDetails.setStatus(StatusTypeEnum.ACTIVE.getValue().longValue());
							}
							CommandProcessingResult processingResult=   this.prepareRequestWriteplatformService.prepareNewRequest(orderDetails,plan,UserActionStatusTypeEnum.ACTIVATION.toString());
							if(plan.getProvisionSystem().equalsIgnoreCase(ProvisioningApiConstants.PROV_PACKETSPAN)){
								this.provisioningWritePlatformService.postOrderDetailsForProvisioning(orderDetails,plan.getPlanCode(),UserActionStatusTypeEnum.ACTIVATION.toString(),
           						processingResult.resourceId(),null,null,orderDetails.getId());
							}
							orderDetails.setNextBillableDay(null);
								if(!plan.getProvisionSystem().equalsIgnoreCase("None")){
									this.prepareRequestWriteplatformService.prepareNewRequest(orderDetails,plan,"RENEWAL_AE");	
								}
					}
                       orderDetails.setuserAction(requstStatus);
                       orderDetails.setRenewalDate(newStartdate.toDate());
         		       this.orderRepository.save(orderDetails);
         		     final boolean ispaymentEnable = command.booleanPrimitiveValueOfParameterNamed("ispaymentEnable");
         		     		if(ispaymentEnable){
         		     			JSONObject jsonobject = new JSONObject();
         		     			jsonobject.put("paymentDate",command.localDateValueOfParameterNamed("paymentDate").toString());
         		     			jsonobject.put("amountPaid", command.bigDecimalValueOfParameterNamed("amountPaid"));
         		     			jsonobject.put("remarks", command.stringValueOfParameterNamed("remarks"));
         		     			jsonobject.put("locale", "en");
         		     			jsonobject.put("dateFormat","yyyy-MM-dd");
         		     			jsonobject.put("paymentCode",command.longValueOfParameterNamed("paymentCode"));
         		     			jsonobject.put("recieptNo",command.longValueOfParameterNamed("recieptNo"));
         		     			paymentsApiResource.createPayment(orderDetails.getClientId(), jsonobject.toString());
         		     		}
         		     		// For Order History
         		     		Long userId=null;
         		     		SecurityContext context = SecurityContextHolder.getContext();
         		     			if(context.getAuthentication() != null){
         		     				AppUser appUser=this.context.authenticatedUser();
         		     				userId=appUser.getId();
         		     			}else {
         		     				userId=new Long(0);
         		     			}
         		     			//For Order History
         		     			OrderHistory orderHistory=new OrderHistory(orderDetails.getId(),new LocalDate(),newStartdate,null,requstStatus,userId,description);
         		     			this.orderHistoryRepository.save(orderHistory);

         		     			//for TransactionHistory
		   						transactionHistoryWritePlatformService.saveTransactionHistory(orderDetails.getClientId(),"Order Renewal", orderDetails.getStartDate(),
		   								"Order No:"+orderDetails.getOrderNo(),"Plan Id:"+orderDetails.getPlanId(),"Contarct Period:"+contractDetails.getSubscriptionPeriod(),"Billing Align:"+orderDetails.getbillAlign());
		   						
         		     			return new CommandProcessingResult(Long.valueOf(orderDetails.getClientId()));
			}catch (DataIntegrityViolationException dve) {
				handleCodeDataIntegrityIssues(null,dve);
			return new CommandProcessingResult(Long.valueOf(-1));
			} catch (JSONException e) {
				return new CommandProcessingResult(Long.valueOf(-1));
			}
	}
   
    @Override
	public CommandProcessingResult reconnectOrder(Long orderId) {
	  try{
		 
		  	Order order=this.orderRepository.findOne(orderId);
		  		if(order == null){
		  			throw new NoOrdersFoundException(orderId);
		  		}
		  		final LocalDate startDate=new LocalDate();
		  		List<SubscriptionData> subscriptionDatas=this.contractPeriodReadPlatformService.retrieveSubscriptionDatabyOrder(orderId);
		  		Contract contractPeriod=this.subscriptionRepository.findOne(subscriptionDatas.get(0).getId());
		  		LocalDate EndDate=calculateEndDate(startDate,contractPeriod.getSubscriptionType(),contractPeriod.getUnits());
		  		order.setStartDate(startDate);
		  		order.setEndDate(EndDate);
		  		order.setNextBillableDay(null);
		  		List<OrderPrice> orderPrices=order.getPrice();
		   			for(OrderPrice price:orderPrices){
		   				price.setBillStartDate(startDate);
		   				price.setBillEndDate(EndDate);
		   				price.setNextBillableDay(null);
		   				price.setInvoiceTillDate(null);
		   			}
		   		Plan plan=this.planRepository.findOne(order.getPlanId());
		   				if(plan.getProvisionSystem().equalsIgnoreCase("None")){
		   					order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE).getId());
		   					Client client=this.clientRepository.findOne(order.getClientId());
		   					client.setStatus(ClientStatus.ACTIVE.getValue());
		   					this.clientRepository.save(client);
		   				}else{
		   					//	Check For HardwareAssociation
		   					AssociationData associationData=this.hardwareAssociationReadplatformService.retrieveSingleDetails(orderId);
		   					if(associationData ==null && plan.isHardwareReq() == 'Y'){
		   						throw new HardwareDetailsNotFoundException(orderId.toString());
		   					}
		   					order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId());
		   				}
		   				order.setuserAction(UserActionStatusTypeEnum.RECONNECTION.toString());
		   				this.orderRepository.save(order);
		   				
		   				//for Prepare Request
		   				String requstStatus = UserActionStatusTypeEnum.RECONNECTION.toString().toString();
		   				CommandProcessingResult processingResult=this.prepareRequestWriteplatformService.prepareNewRequest(order,plan,requstStatus);
		   					if(plan.getProvisionSystem().equalsIgnoreCase(ProvisioningApiConstants.PROV_PACKETSPAN)){
		   						this.provisioningWritePlatformService.postOrderDetailsForProvisioning(order,plan.getPlanCode(), requstStatus,
		   								processingResult.resourceId(),null,null,order.getId());
		   					}
			
		   					//For Order History
		   					Long userId=null;
		   					SecurityContext context = SecurityContextHolder.getContext();
		   						if (context.getAuthentication() != null) {
		   								AppUser appUser=this.context.authenticatedUser();
		   								userId=appUser.getId();
		   						}else{
		   							userId=new Long(0);
		   						}
		   						OrderHistory orderHistory=new OrderHistory(order.getId(),new LocalDate(),new LocalDate(),processingResult.commandId(),requstStatus,userId,null);
		   						this.orderHistoryRepository.save(orderHistory);
		
		   						//for TransactionHistory
		   						transactionHistoryWritePlatformService.saveTransactionHistory(order.getClientId(),"Order Reconnection", order.getStartDate(),
		   								"PlanId:"+order.getPlanId(),"contarctPeriod:"+order.getContarctPeriod(),"Services:"+order.getAllServicesAsString(),"OrderID:"+order.getId(),"Billing Align:"+order.getbillAlign());
		   						return new CommandProcessingResult(order.getId());
	  	}catch(DataIntegrityViolationException dve){
	  		handleCodeDataIntegrityIssues(null, dve);
	  		return new CommandProcessingResult(Long.valueOf(-1));
	  	}
	}

	@Override
	public CommandProcessingResult retrackOsdMessage(JsonCommand command) {
		try {
			this.context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForRetrack(command.json());
			String requstStatus = null;
			String message = null;
			String commandName = command.stringValueOfParameterNamed("commandName");
			Order order = this.orderRepository.findOne(command.entityId());
				if (order == null) {
					throw new NoOrdersFoundException(command.entityId());
				}
				if (commandName.equalsIgnoreCase("RETRACK")) {
					String restrict = orderReadPlatformService.checkRetrackInterval(command.entityId());
						if (restrict != null && restrict.equalsIgnoreCase("yes")) {	
							requstStatus = UserActionStatusTypeEnum.RETRACK.toString();					 
						} else {
							throw new PlatformDataIntegrityException("retrack.already.done", "retrack.already.done","retrack.already.done");
						}
				} else if(commandName.equalsIgnoreCase("OSM")) {
					requstStatus = UserActionStatusTypeEnum.MESSAGE.toString();
					message = command.stringValueOfParameterNamed("message");
				}
				Plan plan = this.planRepository.findOne(order.getPlanId());
				if (plan == null) {
					throw new NoOrdersFoundException(command.entityId());
				}
				if (requstStatus != null && plan!=null) {
					GlobalConfigurationProperty configurationProperty=this.configurationRepository.findOneByName(ConfigurationConstants.CPE_TYPE);
					AllocationDetailsData detailsData = this.allocationReadPlatformService.getTheHardwareItemDetails(command.entityId(),configurationProperty.getValue());
					/*ProcessRequest processRequest = new ProcessRequest(order.getClientId(),
						order.getId(),plan.getProvisionSystem(), 'N', null, requstStatus,new Long(0));*/
					ProcessRequest processRequest=new ProcessRequest(Long.valueOf(0),order.getClientId(),order.getId(),plan.getProvisionSystem(),requstStatus
							,'N','N');
				  processRequest.setNotify();
				  List<OrderLine> orderLineData = order.getServices();
				  	for (OrderLine orderLine : orderLineData) {
				  		String HardWareId = null;
				  			if (detailsData != null) {
				  				HardWareId = detailsData.getSerialNo();
				  			}
				  			List<ProvisionServiceDetails> provisionServiceDetails = this.provisionServiceDetailsRepository.findOneByServiceId(orderLine.getServiceId());
				  			ServiceMaster service = this.serviceMasterRepository.findOne(orderLine.getServiceId());
				  			if (!provisionServiceDetails.isEmpty()) {
				  				if (message == null) {
				  					message = provisionServiceDetails.get(0).getServiceIdentification();
				  				}
				  				ProcessRequestDetails processRequestDetails = new ProcessRequestDetails(orderLine.getId(), orderLine.getServiceId(),message, "Recieved",
								HardWareId,order.getStartDate(), order.getEndDate(), null,null, 'N',requstStatus,service.getServiceType());
				  				processRequest.add(processRequestDetails);
				  			}
				  	}
				this.processRequestRepository.save(processRequest);
				this.orderRepository.save(order);
				AppUser appUser = this.context.authenticatedUser();
				Long userId = appUser.getId();
				OrderHistory orderHistory = new OrderHistory(order.getId(),new LocalDate(), new LocalDate(), command.entityId(),
						requstStatus, userId,null);
				this.orderHistoryRepository.save(orderHistory);
				transactionHistoryWritePlatformService.saveTransactionHistory(order.getClientId(), "OSD Message",order.getStartDate(),"PlanId:" + order.getPlanId(),
						"contarctPeriod:" + order.getContarctPeriod(), "OrderID:" + order.getId(),"BillingAlign:" + order.getbillAlign());
				
			}
			return new CommandProcessingResult(order.getId());
		} catch (EmptyResultDataAccessException dve) {
			throw new PlatformDataIntegrityException("retrack.already.done", "retrack.already.done", "retrack.already.done");
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(null, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}
	
@Transactional
@Override
public CommandProcessingResult changePlan(JsonCommand command, Long entityId) {
		
		try{
			Long userId=this.context.authenticatedUser().getId();
			Order order=this.orderRepository.findOne(entityId);
			order.updateDisconnectionstate();
			Date billEndDate=order.getPrice().get(0).getBillEndDate();
			
			Date invoicetillDate=order.getPrice().get(0).getInvoiceTillDate();
			this.orderRepository.save(order);
			//Plan oldPlan=this.planRepository.findOne(order.getPlanId());
			GlobalConfigurationProperty property = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_CHANGE_PLAN_ALIGN_DATES);
			if(!property.isEnabled()){
			GlobalConfigurationProperty dcConfiguration= this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_DISCONNECT);
				if(dcConfiguration.isEnabled()){
					//if(oldPlan.getBillRule() !=400 && oldPlan.getBillRule() !=300){ 
						this.reverseInvoice.reverseInvoiceServices(order.getId(), order.getClientId(),new LocalDate());
					//}
				}
		}
			CommandProcessingResult result=this.createOrder(order.getClientId(), command);
			Order newOrder=this.orderRepository.findOne(result.resourceId());
			newOrder.updateOrderNum(order.getOrderNo());
			newOrder.updateActivationDate(order.getActiveDate());
			
			
				if(property.isEnabled()){
					List<OrderPrice> orderPrices=newOrder.getPrice();
					for(OrderPrice orderPrice:orderPrices){
						if(billEndDate == null){
							orderPrice.setBillEndDate(null);	
						}else{
							orderPrice.setBillEndDate(new LocalDate(billEndDate));
						}
						orderPrice.setInvoiceTillDate(invoicetillDate);
						orderPrice.setNextBillableDay(order.getPrice().get(0).getNextBillableDay());
					}
				}
			newOrder.setuserAction(UserActionStatusTypeEnum.CHANGE_PLAN.toString());
			this.orderRepository.save(newOrder);
			Plan plan=this.planRepository.findOne(newOrder.getPlanId());
			Long id=hardwareAssociationReadplatformService.retrieveOrderAssociationDetails(order.getId(),order.getClientId());
			
				if(id != null  && id != new Long(0) && plan.isHardwareReq() == 'Y'){
					HardwareAssociation association=this.associationRepository.findOne(id);
				
					if(association != null){
							association.delete();
							this.associationRepository.save(association);
					}
				}
				
				Long processResuiltId=new Long(0);
					if(!plan.getProvisionSystem().equalsIgnoreCase("None") && plan.getProvisionSystem().equalsIgnoreCase(ProvisioningApiConstants.PROV_PACKETSPAN)){
						this.provisioningWritePlatformService.postOrderDetailsForProvisioning(newOrder, plan.getCode(), UserActionStatusTypeEnum.CHANGE_PLAN.toString(), 
						new Long(0), null, null,order.getId());
				
					}else if(!plan.getProvisionSystem().equalsIgnoreCase("None")){

					//Prepare a Requset For Order
					CommandProcessingResult processingResult=this.prepareRequestWriteplatformService.prepareNewRequest(newOrder,plan,UserActionStatusTypeEnum.CHANGE_PLAN.toString());
					processResuiltId=processingResult.commandId();
					}
		     
		   //For Order History
			OrderHistory orderHistory=new OrderHistory(order.getId(),new LocalDate(),new LocalDate(),processResuiltId,
					                               UserActionStatusTypeEnum.CHANGE_PLAN.toString(),userId,null);
			this.orderHistoryRepository.save(orderHistory);
			this.transactionHistoryWritePlatformService.saveTransactionHistory(order.getClientId(),"Change Order", new Date(),"Old Order :"+entityId,
					                               " New OrderId :"+result.resourceId());
			return new CommandProcessingResult(result.resourceId());
		}catch(DataIntegrityViolationException exception){
			handleCodeDataIntegrityIssues(command, exception);
			return new CommandProcessingResult(new Long(-1));
		}
		
	}
    
	@Transactional
	@Override
	public CommandProcessingResult applyPromo(JsonCommand command) {
		
		try{
			String username=this.context.authenticatedUser().getUsername();
			this.fromApiJsonDeserializer.validateForPromo(command.json());			
			final Long promoId=command.longValueOfParameterNamed("promoId");
			final LocalDate startDate=command.localDateValueOfParameterNamed("startDate");
			Promotion promotion=this.promotionRepository.findOne(promoId);
				if(promotion == null){
					throw new NoPromotionFoundException(promoId);
				}
				Order order=this.orderRepository.findOne(command.entityId());
				List<OrderDiscount> orderDiscounts=order.getOrderDiscount();
				LocalDate enddate=this.calculateEndDate(startDate,promotion.getDurationType(),promotion.getDuration());
					for(OrderDiscount orderDiscount:orderDiscounts){
						orderDiscount.updateDates(promotion.getDiscountRate(),promotion.getDiscountType(),enddate);
						this.orderDiscountRepository.save(orderDiscount);
					}
			this.transactionHistoryWritePlatformService.saveTransactionHistory(order.getClientId(),"Apply Promotion",new Date(), "User :"+username,
					"Promotion Code :" +promotion.getPromotionCode(),"Promotion Value" + promotion.getDiscountRate());
			return new CommandProcessingResult(command.entityId());
		
		}catch(DataIntegrityViolationException dve){
			handleCodeDataIntegrityIssues(command, dve);
			return null;
		}
		
	
	}

     @Transactional
	@Override
	public CommandProcessingResult scheduleOrderCreation(Long clientId,JsonCommand command) {
		
		try{
		
		this.fromApiJsonDeserializer.validateForCreate(command.json());
		LocalDate startDate=command.localDateValueOfParameterNamed("start_date");	
		
		//Check for Custome_Validation
		CustomValidationData customValidationData = this.orderDetailsReadPlatformServices.checkForCustomValidations(clientId,EventActionConstants.EVENT_CREATE_ORDER,command.json());
			
			if(customValidationData.getErrorCode() != 0 && customValidationData.getErrorMessage() != null){
				throw new ActivePlansFoundException(customValidationData.getErrorMessage()); 
				
			}
		//Check for Active Orders	
			 Long activeorderId=this.orderReadPlatformService.retrieveClientActiveOrderDetails(clientId,null);
			 if(activeorderId !=null && activeorderId !=0){
				 Order order=this.orderRepository.findOne(activeorderId);
				   	if(order.getEndDate() == null || !startDate.isAfter(new LocalDate(order.getEndDate()))){
					   throw new SchedulerOrderFoundException(activeorderId);				   
					   }
			 }
			JSONObject jsonObject=new JSONObject();
			   jsonObject.put("billAlign",command.booleanPrimitiveValueOfParameterNamed("billAlign"));
        	   jsonObject.put("contractPeriod",command.longValueOfParameterNamed("contractPeriod"));
        	   jsonObject.put("dateFormat","dd MMMM yyyy");
               jsonObject.put("locale","en");
               jsonObject.put("isNewPlan","true");
        	   jsonObject.put("paytermCode",command.stringValueOfParameterNamed("paytermCode"));
        	   jsonObject.put("planCode",command.longValueOfParameterNamed("planCode"));
        	   jsonObject.put("start_date",startDate.toDate());
        	   
        	  EventAction  eventAction=new EventAction(startDate.toDate(), "CREATE", "ORDER",EventActionConstants.ACTION_NEW,"/orders/"+clientId, 
        			  clientId,command.json(),null,clientId);
        	  this.eventActionRepository.save(eventAction);
			
			
        	  return  new CommandProcessingResult(command.entityId());
	}catch(DataIntegrityViolationException dve){
		handleCodeDataIntegrityIssues(command, null);
		return new CommandProcessingResult(Long.valueOf(-1));
	}catch(JSONException dve){
		
		return new CommandProcessingResult(Long.valueOf(-1));
		}
		
     }


	@Override
	public CommandProcessingResult deleteSchedulingOrder(Long entityId,JsonCommand command) {
		
		try{
			this.context.authenticatedUser();
			EventAction eventAction=this.eventActionRepository.findOne(entityId);
				if(eventAction.IsProcessed() == 'Y'){
					throw new PrepareRequestActivationException();	
				}else{
					eventAction.updateStatus('C');
					this.eventActionRepository.saveAndFlush(eventAction);
				}
			return new CommandProcessingResult(Long.valueOf(entityId));
		}catch(DataIntegrityViolationException dve){
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

    @Transactional
	@Override
	public CommandProcessingResult orderExtension(JsonCommand command,Long entityId) {
		
		try{
			
			Long userId=this.context.authenticatedUser().getId();
			Order order=this.orderRepository.findOne(entityId);
			String extensionperiod=command.stringValueOfParameterNamed("extensionPeriod");
			String extensionReason=command.stringValueOfParameterNamed("extensionReason");
			LocalDate newStartdate=new LocalDate(order.getEndDate());
			newStartdate=newStartdate.plusDays(1);
			String[] periodData=extensionperiod.split(" ");
			LocalDate endDate=calculateEndDate(newStartdate,periodData[1], new Long(periodData[0]));
			List<OrderPrice>  orderPrices=order.getPrice();
			Plan plan=this.planRepository.findOne(order.getPlanId());
					if(order.getStatus().intValue() == StatusTypeEnum.ACTIVE.getValue()){
						order.setEndDate(endDate);
							for(OrderPrice orderprice:orderPrices){
								orderprice.setBillEndDate(endDate);
								orderprice.setInvoiceTillDate(endDate.toDate());
								orderprice.setNextBillableDay(endDate.toDate());
								this.OrderPriceRepository.save(orderprice);
							}
					}else if(order.getStatus().intValue() == StatusTypeEnum.DISCONNECTED.getValue()){
						for(OrderPrice orderprice:orderPrices){
							orderprice.setBillStartDate(newStartdate);
				    		orderprice.setBillEndDate(endDate);
				    		orderprice.setNextBillableDay(null);
				    		orderprice.setInvoiceTillDate(null);
				    		this.OrderPriceRepository.save(orderprice);
						}
				if(plan.getProvisionSystem().equalsIgnoreCase("None")){
					order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE).getId());
					Client client=this.clientRepository.findOne(order.getClientId());
					client.setStatus(ClientStatus.ACTIVE.getValue());
					this.clientRepository.save(client);
				}else{
					 //Check For HardwareAssociation
					  AssociationData associationData=this.hardwareAssociationReadplatformService.retrieveSingleDetails(entityId);
					  	if(associationData ==null){
					  		throw new HardwareDetailsNotFoundException(entityId.toString());
					  	}
					  	order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId());
					}
					}		   
		      order.setEndDate(endDate);
		      order.setuserAction(UserActionStatusTypeEnum.RECONNECTION.toString());
		      this.orderRepository.save(order);

		      //for Prepare Request
			String requstStatus = UserActionStatusTypeEnum.RECONNECTION.toString().toString();
            this.prepareRequestWriteplatformService.prepareNewRequest(order,plan,requstStatus);

            //For Order History
			SecurityContext context = SecurityContextHolder.getContext();
	        	if (context.getAuthentication() != null) {
	        		AppUser appUser=this.context.authenticatedUser();
	        		userId=appUser.getId();
	        	}else{
	        		userId=new Long(0);
	        	}

	        	//For Order History
			OrderHistory orderHistory=new OrderHistory(order.getId(),new LocalDate(),new LocalDate(),entityId,
		    UserActionStatusTypeEnum.EXTENSION.toString(),userId,extensionReason);
			this.orderHistoryRepository.save(orderHistory);
			this.transactionHistoryWritePlatformService.saveTransactionHistory(order.getClientId(),"Extension Order", new Date(),"End Date"+endDate);
			return new CommandProcessingResult(entityId);

		}catch(DataIntegrityViolationException dve){
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(new Long(-1));
			
		}
	}

	@Override
	public CommandProcessingResult orderTermination(JsonCommand command,Long orderId) {
		
		try{
				AppUser appUser=this.context.authenticatedUser();
				Order order=this.orderRepository.findOne(orderId);
				Long resourceId=Long.valueOf(0);
					if(order == null){
						throw new OrderNotFoundException(orderId);
					}
					Long orderStatus=null;
					Plan plan=this.planRepository.findOne(order.getPlanId());
					
					if(plan.getProvisionSystem().equalsIgnoreCase("None") && !plan.getProvisionSystem().equalsIgnoreCase(ProvisioningApiConstants.PROV_PACKETSPAN)){
							orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.TERMINATED).getId();
					}else{
							orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId();
							CommandProcessingResult processingResult= this.prepareRequestWriteplatformService.prepareNewRequest(order,plan,UserActionStatusTypeEnum.TERMINATION.toString());
							resourceId=processingResult.resourceId();
					}
						order.setStatus(orderStatus);
						order.setuserAction(UserActionStatusTypeEnum.TERMINATION.toString());
						this.orderRepository.saveAndFlush(order);
							if(plan.getProvisionSystem().equalsIgnoreCase(ProvisioningApiConstants.PROV_PACKETSPAN)){
								this.provisioningWritePlatformService.postOrderDetailsForProvisioning(order, plan.getCode(), UserActionStatusTypeEnum.TERMINATION.toString(), 
										resourceId, null, null,order.getId());
							}
			OrderHistory orderHistory=new OrderHistory(order.getId(),new LocalDate(),new LocalDate(),resourceId,UserActionStatusTypeEnum.TERMINATION.toString(),
											appUser.getId(),null);
			this.orderHistoryRepository.save(orderHistory);	
		    transactionHistoryWritePlatformService.saveTransactionHistory(order.getClientId(),"Order Termination", new Date(),"User :"+appUser.getUsername(),
									"PlanId:"+order.getPlanId(),"contarctPeriod:"+order.getContarctPeriod(),"Services:"+order.getAllServicesAsString(),"OrderID:"+order.getId(),"BillingAlign:"+order.getbillAlign());
		    return new CommandProcessingResult(orderId);
		
		}catch(DataIntegrityViolationException exception){
			handleCodeDataIntegrityIssues(command, exception);
			return new CommandProcessingResult(new Long(-1));
		}
	}

	@Transactional
	@Override
	public CommandProcessingResult orderSuspention(JsonCommand command,Long entityId) {
		
		try{
				AppUser appUser= this.context.authenticatedUser();
				this.fromApiJsonDeserializer.validateForOrderSuspension(command.json());
				Order order=this.orderRepository.findOne(entityId);
				Long resourceId=Long.valueOf(0);
					if(order == null){
						throw new OrderNotFoundException(entityId);
					}
					
					Long pendingId=this.enumDomainServiceRepository.findOneByEnumMessageProperty(StatusTypeEnum.PENDING.toString()).getEnumId();	
				    Plan plan=this.planRepository.findOne(order.getPlanId());

				     if(!plan.getProvisionSystem().equalsIgnoreCase("None") && !plan.getProvisionSystem().equalsIgnoreCase(ProvisioningApiConstants.PROV_PACKETSPAN)){
								order.setStatus(pendingId);
                        CommandProcessingResult commandProcessingResult=this.prepareRequestWriteplatformService.prepareNewRequest(order,plan,UserActionStatusTypeEnum.SUSPENTATION.toString());
                        resourceId =commandProcessingResult.resourceId();
					}else{
						EnumDomainService enumDomainService=this.enumDomainServiceRepository.findOneByEnumMessageProperty(StatusTypeEnum.SUSPENDED.toString());
								order.setStatus(enumDomainService.getEnumId());
					}
					order.setuserAction(UserActionStatusTypeEnum.SUSPENTATION.toString());
				
					
					//Post Details in Payment followup
					PaymentFollowup paymentFollowup=PaymentFollowup.fromJson(command,order.getClientId(),order.getId(),
							StatusTypeEnum.ACTIVE.toString(),StatusTypeEnum.SUSPENDED.toString());
					this.paymentFollowupRepository.save(paymentFollowup);
					
					if(plan.getProvisionSystem().equalsIgnoreCase(ProvisioningApiConstants.PROV_PACKETSPAN)){
						order.setStatus(pendingId);
							this.provisioningWritePlatformService.postOrderDetailsForProvisioning(order, plan.getCode(), UserActionStatusTypeEnum.SUSPENTATION.toString(), 
											resourceId, null, null,order.getId());
					}
					this.orderRepository.save(order);
					OrderHistory orderHistory=new OrderHistory(order.getId(),new LocalDate(),new LocalDate(),resourceId,UserActionStatusTypeEnum.TERMINATION.toString(),
							appUser.getId(),null);
                     this.orderHistoryRepository.save(orderHistory);	
				    transactionHistoryWritePlatformService.saveTransactionHistory(order.getClientId(),"Order Suspentation", new Date(),
										"User :"+appUser.getUsername(),"PlanId:"+order.getPlanId(),"contarctPeriod:"+order.getContarctPeriod(),
										"Services:"+order.getAllServicesAsString(),"OrderID:"+order.getId(),"BillingAlign:"+order.getbillAlign());
			return new CommandProcessingResult(entityId);			
		}catch(DataIntegrityViolationException dve){
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
		
	}

	@Override
	public CommandProcessingResult reactiveOrder(JsonCommand command,Long entityId) {

		try{
			AppUser appUser=this.context.authenticatedUser();
			Order order=this.orderRepository.findOne(entityId);
			Long resourceId=new Long(0);
			if(order == null){
				throw new OrderNotFoundException(entityId);
			}
			
			 Long pendingId=this.enumDomainServiceRepository.findOneByEnumMessageProperty(StatusTypeEnum.PENDING.toString()).getEnumId();	
		    Plan plan=this.planRepository.findOne(order.getPlanId());

		    if(!plan.getProvisionSystem().equalsIgnoreCase("None") && !plan.getProvisionSystem().equalsIgnoreCase(ProvisioningApiConstants.PROV_PACKETSPAN)){
		    	
						
				CommandProcessingResult processingResult=this.prepareRequestWriteplatformService.prepareNewRequest(order,plan,UserActionStatusTypeEnum.REACTIVATION.toString());
				resourceId=processingResult.resourceId();
			}else{
				EnumDomainService enumDomainService=this.enumDomainServiceRepository.findOneByEnumMessageProperty(StatusTypeEnum.ACTIVE.toString());
						order.setStatus(enumDomainService.getEnumId());
			}
			order.setuserAction(UserActionStatusTypeEnum.REACTIVATION.toString());
			
			
			PaymentFollowup paymentFollowup=this.paymentFollowupRepository.findOneByorderId(order.getId());
			
			if(paymentFollowup != null){
				paymentFollowup.setReactiveDate(new Date());
				this.paymentFollowupRepository.save(paymentFollowup);
			}
			
		/*	//Post Details in Payment followup
			PaymentFollowup paymentFollowup=PaymentFollowup.fromJson(command,order.getClientId(),order.getId(),
					StatusTypeEnum.ACTIVE.toString(),StatusTypeEnum.REACTIVE.toString());
			this.paymentFollowupRepository.save(paymentFollowup);*/
			
			if(plan.getProvisionSystem().equalsIgnoreCase(ProvisioningApiConstants.PROV_PACKETSPAN)){
				order.setStatus(pendingId);
					this.provisioningWritePlatformService.postOrderDetailsForProvisioning(order, plan.getCode(), UserActionStatusTypeEnum.REACTIVATION.toString(), 
									resourceId, null, null,order.getId());
			}
			
			this.orderRepository.save(order);
			OrderHistory orderHistory=new OrderHistory(order.getId(),new LocalDate(),new LocalDate(),resourceId,UserActionStatusTypeEnum.REACTIVATION.toString(),
					appUser.getId(),null);
             this.orderHistoryRepository.save(orderHistory);	
		    transactionHistoryWritePlatformService.saveTransactionHistory(order.getClientId(),"Reactive Order", new Date(),
								"User :"+appUser.getUsername(),"PlanId:"+order.getPlanId(),"contarctPeriod:"+order.getContarctPeriod(),
								"Services:"+order.getAllServicesAsString(),"OrderID:"+order.getId(),"BillingAlign:"+order.getbillAlign());
		    return new CommandProcessingResult(entityId);	
			
		}catch(DataIntegrityViolationException dve){
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

	
 }
	

