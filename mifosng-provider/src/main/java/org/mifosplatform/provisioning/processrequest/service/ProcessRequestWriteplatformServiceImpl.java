package org.mifosplatform.provisioning.processrequest.service;

import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.mifosplatform.cms.eventmaster.domain.EventDetails;
import org.mifosplatform.cms.eventmaster.domain.EventMaster;
import org.mifosplatform.cms.eventmaster.domain.EventMasterRepository;
import org.mifosplatform.cms.eventorder.domain.EventOrder;
import org.mifosplatform.cms.eventorder.domain.EventOrderRepository;
import org.mifosplatform.cms.eventorder.domain.EventOrderdetials;
import org.mifosplatform.infrastructure.configuration.domain.EnumDomainService;
import org.mifosplatform.infrastructure.configuration.domain.EnumDomainServiceRepository;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.domain.MifosPlatformTenant;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.service.DataSourcePerTenantService;
import org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.security.service.TenantDetailsService;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.domain.ClientStatus;
import org.mifosplatform.portfolio.order.data.OrderStatusEnumaration;
import org.mifosplatform.portfolio.order.domain.Order;
import org.mifosplatform.portfolio.order.domain.OrderRepository;
import org.mifosplatform.portfolio.order.service.OrderReadPlatformService;
import org.mifosplatform.portfolio.plan.domain.Plan;
import org.mifosplatform.portfolio.plan.domain.PlanRepository;
import org.mifosplatform.portfolio.plan.domain.StatusTypeEnum;
import org.mifosplatform.portfolio.plan.domain.UserActionStatusTypeEnum;
import org.mifosplatform.provisioning.preparerequest.data.PrepareRequestData;
import org.mifosplatform.provisioning.preparerequest.domain.PrepareRequest;
import org.mifosplatform.provisioning.preparerequest.domain.PrepareRequsetRepository;
import org.mifosplatform.provisioning.preparerequest.service.PrepareRequestReadplatformService;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequest;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequestDetails;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequestRepository;
import org.mifosplatform.provisioning.provisioning.api.ProvisioningApiConstants;
import org.mifosplatform.workflow.eventaction.data.ActionDetaislData;
import org.mifosplatform.workflow.eventaction.service.ActionDetailsReadPlatformService;
import org.mifosplatform.workflow.eventaction.service.ActiondetailsWritePlatformService;
import org.mifosplatform.workflow.eventaction.service.EventActionConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;




@Service(value = "processRequestWriteplatformService")
public class ProcessRequestWriteplatformServiceImpl implements ProcessRequestWriteplatformService{

	  private static final Logger logger =LoggerFactory.getLogger(ProcessRequestReadplatformServiceImpl.class);
	  private final PlanRepository planRepository;
	  private final PlatformSecurityContext context;
	  private final OrderRepository orderRepository;
	  private final ClientRepository clientRepository;
	  private final EventOrderRepository eventOrderRepository;
	  private final TenantDetailsService tenantDetailsService;
	  private final EventMasterRepository eventMasterRepository;
	  private final OrderReadPlatformService orderReadPlatformService;
	  private final PrepareRequsetRepository prepareRequsetRepository;
	  private final ProcessRequestRepository processRequestRepository;
	  private final DataSourcePerTenantService dataSourcePerTenantService;
	  private final EnumDomainServiceRepository enumDomainServiceRepository;
	  private final ActionDetailsReadPlatformService actionDetailsReadPlatformService;
	  private final PrepareRequestReadplatformService prepareRequestReadplatformService;
	  private final ActiondetailsWritePlatformService actiondetailsWritePlatformService; 

	  
	  

	    @Autowired
	    public ProcessRequestWriteplatformServiceImpl(final DataSourcePerTenantService dataSourcePerTenantService,final TenantDetailsService tenantDetailsService,
	    		final PrepareRequestReadplatformService prepareRequestReadplatformService,final OrderReadPlatformService orderReadPlatformService,
	    		final OrderRepository orderRepository,final ProcessRequestRepository processRequestRepository,final PrepareRequsetRepository prepareRequsetRepository,
	    		final ClientRepository clientRepository,final PlanRepository planRepository,final ActionDetailsReadPlatformService actionDetailsReadPlatformService,
	    		final ActiondetailsWritePlatformService actiondetailsWritePlatformService,final PlatformSecurityContext context,
	    		final EnumDomainServiceRepository enumDomainServiceRepository,final EventOrderRepository eventOrderRepository,
	    		final EventMasterRepository eventMasterRepository) {
	    	
	    	    this.context = context;
	    	    this.planRepository=planRepository;
	    	    this.orderRepository=orderRepository;
	    	    this.clientRepository=clientRepository;
	    	    this.eventOrderRepository=eventOrderRepository;
	    	    this.tenantDetailsService = tenantDetailsService;
	    	    this.eventMasterRepository=eventMasterRepository;
	    	    this.prepareRequsetRepository=prepareRequsetRepository;
	    	    this.processRequestRepository=processRequestRepository;
	    	    this.orderReadPlatformService=orderReadPlatformService;
	    	    this.enumDomainServiceRepository=enumDomainServiceRepository;
	            this.dataSourcePerTenantService = dataSourcePerTenantService;
	            this.actionDetailsReadPlatformService=actionDetailsReadPlatformService;
	            this.prepareRequestReadplatformService=prepareRequestReadplatformService;
	            this.actiondetailsWritePlatformService=actiondetailsWritePlatformService;
	             
	    }

	    @Transactional
	    @Override
		public void ProcessingRequestDetails() {
	        
	        final MifosPlatformTenant tenant = this.tenantDetailsService.loadTenantById("default");
	        ThreadLocalContextUtil.setTenant(tenant);
            List<PrepareRequestData> data=this.prepareRequestReadplatformService.retrieveDataForProcessing();
            	for(PrepareRequestData requestData:data){
                       //Get the Order details
                     final List<Long> clientOrderIds = this.prepareRequestReadplatformService.retrieveRequestClientOrderDetails(requestData.getClientId());
                     	//Processing the request
                     	if(clientOrderIds!=null){
                                     this.processingClientDetails(clientOrderIds,requestData);
                                    //Update RequestData
                                     PrepareRequest prepareRequest=this.prepareRequsetRepository.findOne(requestData.getRequestId());
                                     prepareRequest.updateProvisioning('Y');
                                     this.prepareRequsetRepository.save(prepareRequest);
                     	}
            	}
	    	}
                    
		
	    private void processingClientDetails(List<Long> clientOrderIds,PrepareRequestData requestData) {
	    		for(Long orderId:clientOrderIds){
	    			final MifosPlatformTenant tenant = this.tenantDetailsService.loadTenantById("default");
			        ThreadLocalContextUtil.setTenant(tenant);
			        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSourcePerTenantService.retrieveDataSource());
	    		}
			}

		@Override
		public void notifyProcessingDetails(ProcessRequest detailsData,char status) {
				try{
					if(detailsData!=null && !(detailsData.getRequestType().equalsIgnoreCase(ProvisioningApiConstants.REQUEST_TERMINATE)) && status != 'F'){
						Order order=this.orderRepository.findOne(detailsData.getOrderId());
						Client client=this.clientRepository.findOne(order.getClientId());
							
							if(detailsData.getRequestType().equalsIgnoreCase(UserActionStatusTypeEnum.ACTIVATION.toString())){
								
								order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE).getId());
								client.setStatus(ClientStatus.ACTIVE.getValue());
								Plan plan=this.planRepository.findOne(order.getPlanId());
								this.orderRepository.saveAndFlush(order);
								
									if(plan.isPrepaid() == 'Y'){
										List<ActionDetaislData> actionDetaislDatas=this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_ACTIVE_ORDER);
										if(actionDetaislDatas.size() != 0){
											this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas,order.getClientId(), order.getId().toString());
										}
									}
					 
							}else if(detailsData.getRequestType().equalsIgnoreCase(UserActionStatusTypeEnum.DISCONNECTION.toString())){
					 
								order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.DISCONNECTED).getId());
								Long activeOrders=this.orderReadPlatformService.retrieveClientActiveOrderDetails(order.getClientId(), null);
								if(activeOrders == 0){
									client.setStatus(ClientStatus.DEACTIVE.getValue());
								}
								this.orderRepository.saveAndFlush(order);
							
							}else if(detailsData.getRequestType().equalsIgnoreCase(UserActionStatusTypeEnum.TERMINATION.toString())){
								order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.TERMINATED).getId());
								this.orderRepository.saveAndFlush(order);
									/*if(plan.getProvisionSystem().equalsIgnoreCase(ProvisioningApiConstants.PROV_PACKETSPAN)){
										
										List<ServiceParameters> parameters=this.serviceParametersRepository.findDataByOrderId(order.getId());
											
											for(ServiceParameters serviceParameter:parameters){
												if(serviceParameter.getParameterName().equalsIgnoreCase(ProvisioningApiConstants.PROV_DATA_IPADDRESS)){
													this.ipPoolManagementWritePlatformService.updateIpAddressStatus(serviceParameter.getParameterValue(),'F');
													serviceParameter.setStatus("INACTIVE");
													this.serviceParametersRepository.saveAndFlush(serviceParameter);
												}
											}
									}*/
							}else if(detailsData.getRequestType().equalsIgnoreCase(UserActionStatusTypeEnum.SUSPENTATION.toString())){
								EnumDomainService enumDomainService=this.enumDomainServiceRepository.findOneByEnumMessageProperty(StatusTypeEnum.SUSPENDED.toString());
								order.setStatus(enumDomainService.getEnumId());
								this.orderRepository.saveAndFlush(order);
								
							}else if(detailsData.getRequestType().equalsIgnoreCase(UserActionStatusTypeEnum.REACTIVATION.toString())){
								
								EnumDomainService enumDomainService=this.enumDomainServiceRepository.findOneByEnumMessageProperty(StatusTypeEnum.ACTIVE.toString());
								order.setStatus(enumDomainService.getEnumId());
								this.orderRepository.saveAndFlush(order);
							}else{
								order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE).getId());
								client.setStatus(ClientStatus.ACTIVE.getValue());
								this.clientRepository.saveAndFlush(client);
								this.orderRepository.saveAndFlush(order);
							}	
						//	this.orderRepository.saveAndFlush(order);
							this.clientRepository.saveAndFlush(client);
							detailsData.setNotify();
					}
						this.processRequestRepository.saveAndFlush(detailsData);
				}catch(Exception exception){
					exception.printStackTrace();
				}
		}
		
		@Transactional
		@Override
		public CommandProcessingResult addProcessRequest(JsonCommand command){
			
			try{
				this.context.authenticatedUser();
				ProcessRequest processRequest = ProcessRequest.fromJson(command);
				ProcessRequestDetails processRequestDetails = ProcessRequestDetails.fromJson(processRequest,command);	
				processRequest.add(processRequestDetails);
				this.processRequestRepository.save(processRequest);
				return	new CommandProcessingResult(Long.valueOf(processRequest.getPrepareRequestId()));

			}catch(DataIntegrityViolationException dve){
				handleCodeDataIntegrityIssues(command,dve);
				return CommandProcessingResult.empty();
			}
			
		}
		
		 private void handleCodeDataIntegrityIssues(JsonCommand command,DataIntegrityViolationException dve) {
				 Throwable realCause = dve.getMostSpecificCause();
			        logger.error(dve.getMessage(), dve);
			        throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
			                "Unknown data integrity issue with resource: " + realCause.getMessage());
			}

		
	/*	@Transactional
		@Override
		public void postProvisioningdetails(Client client,EventOrder eventOrder,String requestType,String provSystem, String response) {
			try{
				
				
				this.context.authenticatedUser();
				ProcessRequest processRequest=new ProcessRequest(Long.valueOf(0), eventOrder.getClientId(),eventOrder.getId(),ProvisioningApiConstants.PROV_BEENIUS,
						ProvisioningApiConstants.REQUEST_ACTIVATION_VOD);
				List<EventOrderdetials> eventDetails=eventOrder.getEventOrderdetials();
				EventMaster eventMaster=this.eventMasterRepository.findOne(eventOrder.getEventId());
				JSONObject jsonObject=new JSONObject();
				jsonObject.put("officeUid",client.getOffice().getExternalId());
				jsonObject.put("subscriberUid",client.getAccountNo());
				jsonObject.put("vodUid",eventMaster.getEventName());
						
					for(EventOrderdetials details:eventDetails){
						ProcessRequestDetails processRequestDetails=new ProcessRequestDetails(details.getId(),details.getEventDetails().getId(),jsonObject.toString(),
								response,null,eventMaster.getEventStartDate(), eventMaster.getEventEndDate(),new Date(),new Date(),'N',
								ProvisioningApiConstants.REQUEST_ACTIVATION_VOD,null);
						processRequest.add(processRequestDetails);
					}
				this.processRequestRepository.save(processRequest);
			}catch(DataIntegrityViolationException dve){
				handleCodeDataIntegrityIssues(null, dve);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}*/
}
