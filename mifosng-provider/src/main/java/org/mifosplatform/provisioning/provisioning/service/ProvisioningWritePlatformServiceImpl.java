package org.mifosplatform.provisioning.provisioning.service;


import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.logistics.itemdetails.domain.InventoryItemDetails;
import org.mifosplatform.logistics.itemdetails.domain.InventoryItemDetailsRepository;
import org.mifosplatform.logistics.itemdetails.exception.ActivePlansFoundException;
import org.mifosplatform.organisation.ippool.data.IpGeneration;
import org.mifosplatform.organisation.ippool.domain.IpPoolManagementDetail;
import org.mifosplatform.organisation.ippool.domain.IpPoolManagementJpaRepository;
import org.mifosplatform.organisation.ippool.exception.IpAddresAllocatedException;
import org.mifosplatform.organisation.ippool.exception.IpNotAvailableException;
import org.mifosplatform.organisation.ippool.service.IpPoolManagementReadPlatformService;
import org.mifosplatform.portfolio.association.domain.HardwareAssociation;
import org.mifosplatform.portfolio.association.exception.PairingNotExistException;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.service.GroupData;
import org.mifosplatform.portfolio.group.service.GroupReadPlatformService;
import org.mifosplatform.portfolio.order.domain.HardwareAssociationRepository;
import org.mifosplatform.portfolio.order.domain.Order;
import org.mifosplatform.portfolio.order.domain.OrderLine;
import org.mifosplatform.portfolio.order.domain.OrderRepository;
import org.mifosplatform.portfolio.order.service.OrderReadPlatformService;
import org.mifosplatform.portfolio.plan.domain.Plan;
import org.mifosplatform.portfolio.plan.domain.PlanRepository;
import org.mifosplatform.portfolio.plan.domain.UserActionStatusTypeEnum;
import org.mifosplatform.portfolio.service.domain.ServiceMaster;
import org.mifosplatform.portfolio.service.domain.ServiceMasterRepository;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequest;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequestDetails;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequestRepository;
import org.mifosplatform.provisioning.processrequest.service.ProcessRequestReadplatformService;
import org.mifosplatform.provisioning.processrequest.service.ProcessRequestWriteplatformService;
import org.mifosplatform.provisioning.provisioning.api.ProvisioningApiConstants;
import org.mifosplatform.provisioning.provisioning.data.ServiceParameterData;
import org.mifosplatform.provisioning.provisioning.domain.ProvisioningCommand;
import org.mifosplatform.provisioning.provisioning.domain.ProvisioningCommandParameters;
import org.mifosplatform.provisioning.provisioning.domain.ProvisioningCommandRepository;
import org.mifosplatform.provisioning.provisioning.domain.ServiceParameters;
import org.mifosplatform.provisioning.provisioning.domain.ServiceParametersRepository;
import org.mifosplatform.provisioning.provisioning.exceptions.ProvisioningRequestNotFoundException;
import org.mifosplatform.provisioning.provisioning.serialization.ProvisioningCommandFromApiJsonDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

@Service
public class ProvisioningWritePlatformServiceImpl implements ProvisioningWritePlatformService {

	

	
	private final FromJsonHelper fromJsonHelper;
	private final PlatformSecurityContext context;
    private final OrderRepository orderRepository;
    private final FromJsonHelper fromApiJsonHelper;
    private final ClientRepository clientRepository;
    private final ServiceMasterRepository serviceMasterRepository;
    private final ProcessRequestRepository processRequestRepository;
    private final OrderReadPlatformService orderReadPlatformService;
    private final HardwareAssociationRepository associationRepository;
    private final ServiceParametersRepository serviceParametersRepository;
    private final ProvisioningCommandRepository provisioningCommandRepository;
    private final IpPoolManagementJpaRepository ipPoolManagementJpaRepository;
	private final InventoryItemDetailsRepository inventoryItemDetailsRepository;
	private final ProvisioningReadPlatformService provisioningReadPlatformService;
    private final ProvisioningCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	private final ProcessRequestReadplatformService processRequestReadplatformService;
	private final ProcessRequestWriteplatformService processRequestWriteplatformService;
	private final IpPoolManagementReadPlatformService ipPoolManagementReadPlatformService;
	private final PlanRepository planRepository;
	private final GroupReadPlatformService groupReadPlatformService;
	
    @Autowired
	public ProvisioningWritePlatformServiceImpl(final PlatformSecurityContext context,final InventoryItemDetailsRepository inventoryItemDetailsRepository,
			final ProvisioningCommandFromApiJsonDeserializer fromApiJsonDeserializer,final FromJsonHelper fromApiJsonHelper,final OrderReadPlatformService orderReadPlatformService,
			final ProvisioningCommandRepository provisioningCommandRepository,final ServiceParametersRepository parametersRepository,
			final ProcessRequestRepository processRequestRepository,final OrderRepository orderRepository,final FromJsonHelper fromJsonHelper,
			final HardwareAssociationRepository associationRepository,final ServiceMasterRepository serviceMasterRepository,final ClientRepository clientRepository,
			final ProcessRequestReadplatformService processRequestReadplatformService,final IpPoolManagementJpaRepository ipPoolManagementJpaRepository,
			final IpPoolManagementReadPlatformService ipPoolManagementReadPlatformService,final ProvisioningReadPlatformService provisioningReadPlatformService,
			final ProcessRequestWriteplatformService processRequestWriteplatformService,final PlanRepository planRepository,
			final GroupReadPlatformService groupReadPlatformService) {

    	this.context = context;
    	this.fromJsonHelper=fromJsonHelper;
		this.orderRepository=orderRepository;
		this.clientRepository=clientRepository;
		this.fromApiJsonHelper=fromApiJsonHelper;
		this.associationRepository=associationRepository;
		this.fromApiJsonDeserializer=fromApiJsonDeserializer;
		this.serviceMasterRepository=serviceMasterRepository;
		this.serviceParametersRepository=parametersRepository;
		this.processRequestRepository=processRequestRepository;
		this.orderReadPlatformService=orderReadPlatformService;
		this.provisioningCommandRepository=provisioningCommandRepository;
		this.ipPoolManagementJpaRepository=ipPoolManagementJpaRepository;
		this.inventoryItemDetailsRepository=inventoryItemDetailsRepository;
		this.provisioningReadPlatformService=provisioningReadPlatformService;
		this.processRequestReadplatformService=processRequestReadplatformService;
		this.processRequestWriteplatformService=processRequestWriteplatformService;
		this.ipPoolManagementReadPlatformService=ipPoolManagementReadPlatformService;
		this.planRepository=planRepository;
		this.groupReadPlatformService=groupReadPlatformService;
	}

	@Override
	public CommandProcessingResult createProvisioning(JsonCommand command) {
		
			try{	
				 this.context.authenticatedUser();
				 this.fromApiJsonDeserializer.validateForProvisioning(command.json());
				 ProvisioningCommand provisioningCommand=ProvisioningCommand.from(command);
				 final JsonElement element = fromApiJsonHelper.parse(command.json());
				 final JsonArray commandArray=fromApiJsonHelper.extractJsonArrayNamed("commandParameters",element);
				 if(commandArray!=null){
			     for (JsonElement jsonelement : commandArray) {	
				          String commandParam = fromApiJsonHelper.extractStringNamed("commandParam", jsonelement);		    
				          String paramType = fromApiJsonHelper.extractStringNamed("paramType", jsonelement);	
				          String paramDefault=null;
				          if(fromApiJsonHelper.parameterExists("paramDefault", jsonelement)){
				        	  paramDefault = fromApiJsonHelper.extractStringNamed("paramDefault", jsonelement);	
				          }
				          ProvisioningCommandParameters data=new ProvisioningCommandParameters(commandParam,paramType,paramDefault);
				          provisioningCommand.addCommandParameters(data);
			     }
			     }
			     
			     this.provisioningCommandRepository.save(provisioningCommand);
			     
			     return new CommandProcessingResult(provisioningCommand.getId());	
			     
			}catch (DataIntegrityViolationException dve) {
				handleCodeDataIntegrityIssues(command, dve);
				return new CommandProcessingResult(Long.valueOf(-1));
			}
			
		
	}

	private void handleCodeDataIntegrityIssues(JsonCommand command,
			DataIntegrityViolationException dve) {
	}

	@Override
	public CommandProcessingResult updateProvisioning(JsonCommand command) {

		try{	
			 this.context.authenticatedUser();
			 this.fromApiJsonDeserializer.validateForProvisioning(command.json());
			 ProvisioningCommand provisioningCommand= this.provisioningCommandRepository.findOne(command.entityId());
			 provisioningCommand.getCommandparameters().clear();
			 final Map<String, Object> changes = provisioningCommand.UpdateProvisioning(command);
			 final JsonElement element = fromApiJsonHelper.parse(command.json());
			 final JsonArray commandArray=fromApiJsonHelper.extractJsonArrayNamed("commandParameters",element);
			 	if(commandArray!=null){
			 		for (JsonElement jsonelement : commandArray) {	
			          String commandParam = fromApiJsonHelper.extractStringNamed("commandParam", jsonelement);		    
			          String paramType = fromApiJsonHelper.extractStringNamed("paramType", jsonelement);	
			          String paramDefault=null;
			          	if(fromApiJsonHelper.parameterExists("paramDefault", jsonelement)){
			          		paramDefault = fromApiJsonHelper.extractStringNamed("paramDefault", jsonelement);	
			          	}
			          	ProvisioningCommandParameters data=new ProvisioningCommandParameters(commandParam,paramType,paramDefault);
			          	provisioningCommand.addCommandParameters(data);
			 		}
			 	}
		     this.provisioningCommandRepository.save(provisioningCommand);
		     return new CommandProcessingResult(provisioningCommand.getId());	
			}catch (DataIntegrityViolationException dve) {
				handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
			}
		}

	@Override
	public CommandProcessingResult deleteProvisioningSystem(JsonCommand command) {
		try{	
			 	this.context.authenticatedUser();
			 	ProvisioningCommand provisioningCommand= this.provisioningCommandRepository.findOne(command.entityId());
			 		if(provisioningCommand.getIsDeleted()!='Y'){
			 			provisioningCommand.setIsDeleted('Y');
			 		}
			 		this.provisioningCommandRepository.save(provisioningCommand);
			 		return new CommandProcessingResult(provisioningCommand.getId());	
		}catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
			}
		}


	@Transactional
	@Override
	public CommandProcessingResult createNewProvisioningSystem(JsonCommand command, Long entityId) {
		
		try{
				this.context.authenticatedUser();
				this.fromApiJsonDeserializer.validateForAddProvisioning(command.json());
				final Long orderId=command.longValueOfParameterNamed("orderId");
				final Long clientId=command.longValueOfParameterNamed("clientId");
				final String planName=command.stringValueOfParameterNamed("planName");
				final String macId=command.stringValueOfParameterNamed("macId");
				final String ipType=command.stringValueOfParameterNamed("ipType");
				final String iprange=command.stringValueOfParameterNamed("ipRange");
				final Long subnet=command.longValueOfParameterNamed("subnet");
				String[] ipAddressArray =null;
			
				InventoryItemDetails inventoryItemDetails=this.inventoryItemDetailsRepository.getInventoryItemDetailBySerialNum(macId);
					if(inventoryItemDetails == null){
						throw new PairingNotExistException(orderId);
					}
			
					final JsonElement element = fromJsonHelper.parse(command.json());
					JsonArray serviceParameters = fromJsonHelper.extractJsonArrayNamed("serviceParameters", element);
					JSONObject jsonObject=new JSONObject();
	        	
				for(JsonElement j:serviceParameters){
					ServiceParameters serviceParameter=ServiceParameters.fromJson(j,fromJsonHelper,clientId,orderId,planName,"ACTIVE",iprange,subnet);
					this.serviceParametersRepository.saveAndFlush(serviceParameter);
				
					//	ip_pool_data status updation
					String paramName = fromJsonHelper.extractStringNamed("paramName", j);
					
						if(paramName.equalsIgnoreCase(ProvisioningApiConstants.PROV_DATA_IPADDRESS)){
							
								if(iprange.equalsIgnoreCase(ProvisioningApiConstants.PROV_DATA_SUBNET)){
									String ipAddress=fromJsonHelper.extractStringNamed("paramValue",j);
									String ipData=ipAddress+"/"+subnet;
									IpGeneration ipGeneration=new IpGeneration(ipData,this.ipPoolManagementReadPlatformService);
									ipAddressArray=ipGeneration.getInfo().getsubnetAddresses();
										for(int i=0;i<ipAddressArray.length;i++){
											IpPoolManagementDetail ipPoolManagementDetail= this.ipPoolManagementJpaRepository.findIpAddressData(ipAddressArray[i]);
												if(ipPoolManagementDetail == null){
													throw new IpAddresAllocatedException(ipAddressArray[i]);
												}
										}
										jsonObject.put(ProvisioningApiConstants.PROV_DATA_SUBNET,subnet);
										
								}else{
									ipAddressArray = fromJsonHelper.extractArrayNamed("paramValue", j);
								}
								
								for(String ipAddress:ipAddressArray){
									IpPoolManagementDetail ipPoolManagementDetail= this.ipPoolManagementJpaRepository.findIpAddressData(ipAddress);
										if(ipPoolManagementDetail == null){
											throw new IpNotAvailableException(ipAddress);
										}
										ipPoolManagementDetail.setStatus('A');
										ipPoolManagementDetail.setClientId(clientId);
										this.ipPoolManagementJpaRepository.save(ipPoolManagementDetail);
								}
						}
				jsonObject.put(serviceParameter.getParameterName(),serviceParameter.getParameterValue());
				
			    }
				Client client=this.clientRepository.findOne(clientId);
				jsonObject.put(ProvisioningApiConstants.PROV_DATA_CLIENTID,client.getAccountNo());
				jsonObject.put(ProvisioningApiConstants.PROV_DATA_CLIENTNAME,client.getFirstname());
				jsonObject.put(ProvisioningApiConstants.PROV_DATA_ORDERID,orderId);
				jsonObject.put(ProvisioningApiConstants.PROV_DATA_PLANNAME,planName);
				jsonObject.put(ProvisioningApiConstants.PROV_DATA_MACID,macId);
				jsonObject.put(ProvisioningApiConstants.PROV_DATA_IPTYPE,ipType);
				
				ProcessRequest processRequest=new ProcessRequest(Long.valueOf(0),clientId,orderId,ProvisioningApiConstants.PROV_PACKETSPAN,
						                       UserActionStatusTypeEnum.ACTIVATION.toString(),'N','N');
				Order order=this.orderRepository.findOne(orderId);
				List<OrderLine> orderLines=order.getServices();
				
					for(OrderLine orderLine:orderLines){
						ServiceMaster service=this.serviceMasterRepository.findOne(orderLine.getServiceId());
						jsonObject.put(ProvisioningApiConstants.PROV_DATA_SERVICETYPE,service.getServiceType());
						ProcessRequestDetails processRequestDetails=new ProcessRequestDetails(orderLine.getId(),orderLine.getServiceId(),
						jsonObject.toString(),"Recieved",inventoryItemDetails.getProvisioningSerialNumber(),order.getStartDate(),
						order.getEndDate(),null,null,'N',UserActionStatusTypeEnum.ACTIVATION.toString(),service.getServiceType());
						processRequest.add(processRequestDetails);
					}
					this.processRequestRepository.saveAndFlush(processRequest);
					//Update Prepare Request table
					//prepareRequest.updateProvisioning('Y');
					//this.prepareRequsetRepository.save(prepareRequest);
					return new CommandProcessingResult(Long.valueOf(processRequest.getId()));
		}catch(DataIntegrityViolationException dve){
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

    @Transactional
	@Override
	public CommandProcessingResult updateProvisioningDetails(Long entityId) {
		
	   try{
				this.context.authenticatedUser();
				ProcessRequest processRequest=this.processRequestRepository.findOne(entityId);
					if(processRequest != null){
						processRequest.update();
						this.processRequestRepository.saveAndFlush(processRequest);
					}
		return new CommandProcessingResult(entityId);	
	   }catch(DataIntegrityViolationException dve){
			handleCodeDataIntegrityIssues(null, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
	   }
    }

    @Transactional
	@Override
	public void updateHardwareDetails(Long clientId, String serialNumber,String oldSerialnumber,String provSerilaNum,String oldHardware) {
			Long activeorders=this.orderReadPlatformService.retrieveClientActiveOrderDetails(clientId,oldSerialnumber);
				if(activeorders!= 0){
					throw new ActivePlansFoundException(serialNumber);
				}
				//Update in Association table if Exist
				List<HardwareAssociation> hardwareAssociations=this.associationRepository.findOneByserialNo(oldSerialnumber);
				if(!hardwareAssociations.isEmpty()){
					for(HardwareAssociation hardwareAssociation:hardwareAssociations){
						hardwareAssociation.updateserailNum(serialNumber);
						this.associationRepository.saveAndFlush(hardwareAssociation);
					}
				}
				//Update ProcessRequest
				final Long ProcessReqId=this.processRequestReadplatformService.retrievelatestReqId(clientId,oldHardware);
				if(ProcessReqId != null && !ProcessReqId.equals(new Long(0))){
					ProcessRequest processRequest=this.processRequestRepository.findOne(ProcessReqId);
					List<ProcessRequestDetails> processRequestDetails=processRequest.getProcessRequestDetails();
					for(ProcessRequestDetails details:processRequestDetails){
						details.update(provSerilaNum);
					}
					this.processRequestRepository.saveAndFlush(processRequest);
				}
		}
    
	@Transactional
    @Override
	public void postOrderDetailsForProvisioning(Order order,String planName,String requestType,Long prepareId,String groupname,String serialNo,Long orderId) {
		try{
			
			this.context.authenticatedUser();
			List<ServiceParameters> parameters=this.serviceParametersRepository.findDataByOrderId(orderId);
			
			if(!parameters.isEmpty()){
			    ProcessRequest processRequest=new ProcessRequest(prepareId,order.getClientId(),order.getId(),ProvisioningApiConstants.PROV_PACKETSPAN,
			    		requestType,'N','N');
			    List<OrderLine> orderLines=order.getServices();
			    HardwareAssociation hardwareAssociation=this.associationRepository.findOneByOrderId(order.getId());
			   
			    if(hardwareAssociation == null){
			    		throw new PairingNotExistException(order.getId());
			    }
			    
			    InventoryItemDetails inventoryItemDetails=this.inventoryItemDetailsRepository.getInventoryItemDetailBySerialNum(hardwareAssociation.getSerialNo());
			    
			    	if(inventoryItemDetails == null){
			    		throw new PairingNotExistException(order.getId());
			    	}
			    	Client client=this.clientRepository.findOne(order.getClientId());
			    	JSONObject jsonObject=new JSONObject();
			    	jsonObject.put(ProvisioningApiConstants.PROV_DATA_CLIENTID,client.getAccountNo());
			    	jsonObject.put(ProvisioningApiConstants.PROV_DATA_CLIENTNAME,client.getFirstname());
			    	jsonObject.put(ProvisioningApiConstants.PROV_DATA_ORDERID,order.getId());
			    	jsonObject.put(ProvisioningApiConstants.PROV_DATA_MACID,inventoryItemDetails.getSerialNumber());

			    	if(requestType.equalsIgnoreCase(UserActionStatusTypeEnum.CHANGE_PLAN.toString())){
			    		
			    		jsonObject.put("New_"+ProvisioningApiConstants.PROV_DATA_PLANNAME,planName);
			    		Order Oldorder=this.orderRepository.findOne(orderId);
				    	Plan plan=this.planRepository.findOne(Oldorder.getPlanId());
				    	jsonObject.put("Old_"+ProvisioningApiConstants.PROV_DATA_PLANNAME,plan.getCode());
			    	
			    	}else{
			    	
			    		jsonObject.put(ProvisioningApiConstants.PROV_DATA_PLANNAME,planName);
			    	}
			    	
			    		if(groupname != null){
			    			jsonObject.put(ProvisioningApiConstants.PROV_DATA_OLD_GROUPNAME,groupname);
			    		}
			    		if(serialNo !=null){
			    			jsonObject.put(ProvisioningApiConstants.PROV_DATA_OLD_SERIALNO,serialNo);
			    			jsonObject.put(ProvisioningApiConstants.PROV_DATA_NEW_SERIALNO,inventoryItemDetails.getSerialNumber());
			    		}
			    		if(requestType.equalsIgnoreCase(UserActionStatusTypeEnum.TERMINATION.toString())){
			    			jsonObject.put("perminateDelete","true");
			    		}
			    		
			    		for(ServiceParameters serviceParameters:parameters){
			    			
			    			String newParamName=null; 
			    			String newParamValue=null;
			    			
			    			if(serviceParameters.getParameterName().equalsIgnoreCase(ProvisioningApiConstants.PROV_DATA_IPADDRESS)){
			    				
			    				if(serviceParameters.getParameterValue().contains("/")){
			    					jsonObject.put(ProvisioningApiConstants.PROV_DATA_IPTYPE,"Subnet");
		        		
			    				}else if(serviceParameters.getParameterValue().contains("[")){
			    					JSONArray jsonArray=new JSONArray(serviceParameters.getParameterValue());
		        				if(jsonArray.length() > 1)
		        					jsonObject.put(ProvisioningApiConstants.PROV_DATA_IPTYPE,"Multiple");
			    				}else{
		        				jsonObject.put(ProvisioningApiConstants.PROV_DATA_IPTYPE,"Single");
			    				}
			    				newParamName=serviceParameters.getParameterName();
			    				newParamValue=serviceParameters.getParameterValue();
			    			}
		        		
		        	if(serviceParameters.getParameterName().equalsIgnoreCase(ProvisioningApiConstants.PROV_DATA_GROUPNAME) && groupname != null){
		        			jsonObject.put("NEW_"+serviceParameters.getParameterName(),serviceParameters.getParameterValue());
		        			newParamName=serviceParameters.getParameterName();
			        		newParamValue=serviceParameters.getParameterValue();
		        	
		        	}else{
		        			
		        		if(serviceParameters.getParameterName().equalsIgnoreCase(ProvisioningApiConstants.PROV_DATA_SERVICE) && 
			        				requestType.equalsIgnoreCase(UserActionStatusTypeEnum.CHANGE_PLAN.toString())){
		        			
			        			List<ServiceParameterData> serviceDatas=this.provisioningReadPlatformService.getSerivceParameters(order.getId());
			        			List<ServiceParameterData> oldServiceDatas=this.provisioningReadPlatformService.getSerivceParameters(orderId);
			        			jsonObject.put("NEW_"+serviceParameters.getParameterName(),serviceDatas.get(0).getParamValue());
			        			jsonObject.put("OLD_"+serviceParameters.getParameterName(),oldServiceDatas.get(0).getParamValue());
			        			jsonObject.put(ProvisioningApiConstants.PROV_DATA_OLD_ORDERID,orderId);
			        			newParamName=serviceParameters.getParameterName();
				        		newParamValue=serviceDatas.get(0).getParamValue();
				        		
		        		}else if(serviceParameters.getParameterName().equalsIgnoreCase(ProvisioningApiConstants.PROV_DATA_GROUPNAME) &&
			        				requestType.equalsIgnoreCase(UserActionStatusTypeEnum.CHANGE_PLAN.toString())){
		        			
			        			Collection<GroupData> groupDatas = this.groupReadPlatformService.retrieveGroupServiceDetails(order.getId());
			        			
			        			for(GroupData groupData:groupDatas){
			        				jsonObject.put("NEW_"+serviceParameters.getParameterName(),groupData.getGroupName());
			        				newParamValue=groupData.getGroupName();
			        			}
			        			jsonObject.put("OLD_"+serviceParameters.getParameterName(),serviceParameters.getParameterValue());
			        			newParamName=serviceParameters.getParameterName();
				        		
			        		
		        		}else{
			        			jsonObject.put(serviceParameters.getParameterName(),serviceParameters.getParameterValue());
			        			newParamName=serviceParameters.getParameterName();
			        			newParamValue=serviceParameters.getParameterValue();
			        		}
		        		}
		        	
		        	serviceParameters.setStatus("INACTIVE");
		        	this.serviceParametersRepository.save(serviceParameters);
		          ServiceParameters newServiceParameters=new ServiceParameters(order.getClientId(), order.getId(), planName, newParamName, newParamValue, "ACTIVE");
		          this.serviceParametersRepository.save(newServiceParameters);
		        	}
			    	
		        for(OrderLine orderLine:orderLines){
		        	
			    		ServiceMaster service=this.serviceMasterRepository.findOne(orderLine.getServiceId());
			    		jsonObject.put(ProvisioningApiConstants.PROV_DATA_SERVICETYPE,service.getServiceType());
			    		ProcessRequestDetails processRequestDetails=new ProcessRequestDetails(orderLine.getId(),orderLine.getServiceId(),
								jsonObject.toString(),"Recieved",inventoryItemDetails.getProvisioningSerialNumber(),order.getStartDate(),
								order.getEndDate(),null,null,'N',requestType,service.getServiceType());
						  processRequest.add(processRequestDetails);
			    	}
			    	
		        this.processRequestRepository.save(processRequest);
				}
			}catch(DataIntegrityViolationException dve){
			handleCodeDataIntegrityIssues(null, dve);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	@Transactional
	@Override
	public CommandProcessingResult confirmProvisioningDetails(Long entityId) {
		
		try{
             this.context.authenticatedUser();
             ProcessRequest processRequest=this.processRequestRepository.findOne(entityId);
             
             	if(processRequest == null){
             		throw new ProvisioningRequestNotFoundException(entityId);
             	}
             	processRequest.setProcessStatus('C');
             	processRequest.setNotify();
             	List<ProcessRequestDetails> details=processRequest.getProcessRequestDetails();
             		
             		for(ProcessRequestDetails processRequestDetails:details){
             			processRequestDetails.setRecievedMessage("Manually Confirmed");
             		}
             		
             		this.processRequestRepository.save(processRequest);
             	    this.processRequestWriteplatformService.notifyProcessingDetails(processRequest, 'Y');	
             	
			return new CommandProcessingResult(entityId);
		
		}catch(DataIntegrityViolationException dve){
			handleCodeDataIntegrityIssues(null, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

	
	@Transactional
	@Override
	public CommandProcessingResult updateIpDetails(Long orderId,JsonCommand command) {
		
		try{
			this.context.authenticatedUser();
			//this.fromApiJsonDeserializer.validateForUpDateIpDetails(command.json());
			final Long clientId=command.longValueOfParameterNamed("clientId");
			final JsonElement element = fromJsonHelper.parse(command.json());


			final String[] exitIpsArray=fromApiJsonHelper.extractArrayNamed("existIps",element);

			final String[] removeIpsArray=fromApiJsonHelper.extractArrayNamed("removeIps",element);

			final String[] newIpsArray=fromApiJsonHelper.extractArrayNamed("newIps",element);
			List<String> tmpList = Arrays.asList(newIpsArray);
			Set<String> uniqueList = new HashSet<String>(tmpList);
			if(uniqueList.size()<tmpList.size()){
				 throw new IpNotAvailableException(orderId);
			}
			IpPoolManagementDetail ipPoolManagement=null;
			JSONArray array=new JSONArray();
			List<ServiceParameters> parameters=this.serviceParametersRepository.findDataByOrderId(orderId);
			
			for(ServiceParameters serviceData:parameters){
				if(serviceData.getParameterName().equalsIgnoreCase(ProvisioningApiConstants.PROV_DATA_IPADDRESS)){

					for(String newIp:newIpsArray){
						array.put(newIp);
					}
					serviceData.setParameterValue(array.toString());

					 if(removeIpsArray.length >= 1){
					      for (int i=0;i<removeIpsArray.length; i++){
					    	  ipPoolManagement= this.ipPoolManagementJpaRepository.findByIpAddress(removeIpsArray[i]);
					    	  if(ipPoolManagement == null){
									throw new IpNotAvailableException(removeIpsArray[i]); }
					    	  ipPoolManagement.setStatus('F');
					    	  ipPoolManagement.setClientId(null);
					    	  ipPoolManagement.setSubnet(null);
					    	  this.ipPoolManagementJpaRepository.save(ipPoolManagement);
					      }
					     
					   }

					if(newIpsArray.length >= 1){

					      for (int i=0;i<newIpsArray.length; i++){
					    	  ipPoolManagement= this.ipPoolManagementJpaRepository.findByIpAddress(newIpsArray[i]);
					    	  if(ipPoolManagement == null){
									throw new IpNotAvailableException(newIpsArray[i]); }
					    	  ipPoolManagement.setStatus('A');
					    	  ipPoolManagement.setClientId(clientId);
					    	 // ipPoolManagement.setSubnet(null);
					    	  this.ipPoolManagementJpaRepository.save(ipPoolManagement);
					      }
					   }	
					this.serviceParametersRepository.save(serviceData);
				}
		}
		}catch(DataIntegrityViolationException dve){
			handleCodeDataIntegrityIssues(null, dve);
		}
		return new CommandProcessingResult(orderId);	
		
	}
}	
