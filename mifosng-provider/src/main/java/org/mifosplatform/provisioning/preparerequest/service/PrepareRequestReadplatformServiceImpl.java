package org.mifosplatform.provisioning.preparerequest.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mifosplatform.infrastructure.core.service.DataSourcePerTenantService;
import org.mifosplatform.logistics.onetimesale.data.AllocationDetailsData;
import org.mifosplatform.portfolio.allocation.service.AllocationReadPlatformService;
import org.mifosplatform.portfolio.order.data.OrderStatusEnumaration;
import org.mifosplatform.portfolio.order.domain.Order;
import org.mifosplatform.portfolio.order.domain.OrderLine;
import org.mifosplatform.portfolio.order.domain.OrderRepository;
import org.mifosplatform.portfolio.plan.domain.StatusTypeEnum;
import org.mifosplatform.portfolio.plan.domain.UserActionStatusTypeEnum;
import org.mifosplatform.portfolio.planmapping.domain.PlanMapping;
import org.mifosplatform.portfolio.planmapping.domain.PlanMappingRepository;
import org.mifosplatform.portfolio.service.domain.ProvisionServiceDetails;
import org.mifosplatform.portfolio.service.domain.ProvisionServiceDetailsRepository;
import org.mifosplatform.portfolio.service.domain.ServiceMaster;
import org.mifosplatform.portfolio.service.domain.ServiceMasterRepository;
import org.mifosplatform.provisioning.preparerequest.data.PrepareRequestData;
import org.mifosplatform.provisioning.preparerequest.domain.PrepareRequest;
import org.mifosplatform.provisioning.preparerequest.domain.PrepareRequsetRepository;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequest;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequestDetails;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequestRepository;
import org.mifosplatform.provisioning.provisioning.api.ProvisioningApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Service
public class PrepareRequestReadplatformServiceImpl  implements PrepareRequestReadplatformService{

	
	  private final DataSourcePerTenantService dataSourcePerTenantService;
	  private final OrderRepository orderRepository;
	  private final ProcessRequestRepository processRequestRepository;
	  private final PrepareRequsetRepository prepareRequsetRepository;
	  private final AllocationReadPlatformService allocationReadPlatformService;
	  private final ProvisionServiceDetailsRepository provisionServiceDetailsRepository;
	  private final ServiceMasterRepository serviceMasterRepository;
	  public final static String PROVISIONGSYS_COMVENIENT="Comvenient";
	  private final PlanMappingRepository planMappingRepository;
	

	    @Autowired
	    public PrepareRequestReadplatformServiceImpl(final DataSourcePerTenantService dataSourcePerTenantService,final OrderRepository orderRepository,
	    		final ServiceMasterRepository serviceMasterRepository,final ProcessRequestRepository processRequestRepository,
	    		final AllocationReadPlatformService allocationReadPlatformService,final PrepareRequsetRepository prepareRequsetRepository,
	    		final ProvisionServiceDetailsRepository provisionServiceDetailsRepository,final PlanMappingRepository planMappingRepository) {
	            
	    	    
	    	    this.orderRepository=orderRepository;
	    	    this.serviceMasterRepository=serviceMasterRepository;
	            this.processRequestRepository=processRequestRepository;
	            this.prepareRequsetRepository=prepareRequsetRepository;
	            this.dataSourcePerTenantService = dataSourcePerTenantService;
	            this.allocationReadPlatformService=allocationReadPlatformService;
	            this.provisionServiceDetailsRepository=provisionServiceDetailsRepository;
	            this.planMappingRepository=planMappingRepository;
	        
	    }

	
	@Override
	public List<PrepareRequestData> retrieveDataForProcessing() {
			
		try {
				JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSourcePerTenantService.retrieveDataSource());
				final ProvisioningMapper mapper = new ProvisioningMapper();
				final String sql = "select " + mapper.provisioningLookupSchema();
				return jdbcTemplate.query(sql, mapper, new Object[] { });
			} catch (EmptyResultDataAccessException e) {
				return null;
			}
			}

	private static final class ProvisioningMapper implements RowMapper<PrepareRequestData> {

			public String provisioningLookupSchema() {
				/*return  " pr.id AS id,pr.client_id AS clientId,pr.order_id AS orderId,pr.provisioning_sys AS provisioningSystem,c.firstname AS userName," +
						" p.is_hw_req AS hwRequired,pw.plan_code AS planName,pr.request_type AS requestType FROM b_prepare_request pr,m_client c,b_hw_plan_mapping pw," +
						" b_orders o,b_association asoc,b_plan_master p  WHERE pr.client_id = c.id  AND o.plan_id = p.id  AND pr.provisioning_sys !='packetspan' " +
						" AND p.plan_code = pw.plan_code  AND pr.order_id = o.id" +
						" AND o.id=asoc.order_id  AND (pr.is_provisioning = 'N' OR pr.status = 'PENDING') group by pr.order_id desc ";*/
				
				return "  pr.id AS id,pr.client_id AS clientId,pr.order_id AS orderId,pr.provisioning_sys AS provisioningSystem,c.firstname AS userName," +
						" p.is_hw_req AS hwRequired,p.plan_code AS planName,pr.request_type AS requestType FROM b_prepare_request pr,m_client c,b_orders o," +
						" b_plan_master p WHERE pr.client_id = c.id AND o.plan_id = p.id AND pr.provisioning_sys != 'packetspan' AND pr.order_id = o.id" +
						" AND (pr.is_provisioning = 'N' OR pr.status = 'PENDING') GROUP BY pr.order_id DESC";
			}

			@Override
			public PrepareRequestData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final Long clientId = rs.getLong("clientId");
			final Long orderId = rs.getLong("orderId");
			final String requestType = rs.getString("requestType");
		    final String planName=rs.getString("planName");	
			final String userName=rs.getString("userName");
			final String ishwReq=rs.getString("hwRequired");
			final String provisioningSys=rs.getString("provisioningSystem");
			
			return new PrepareRequestData(id, clientId,orderId, requestType,null,userName,provisioningSys,planName,ishwReq);
				}
			}	
			
			@Override
			public List<Long> retrieveRequestClientOrderDetails(Long clientId) {
				try {
				
			        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSourcePerTenantService.retrieveDataSource());
				   OrderIdMapper planIdMapper = new OrderIdMapper();
				   String sql = "select" + planIdMapper.planIdSchema();
				  return jdbcTemplate.query(sql, planIdMapper,new Object[] { clientId });
				
			
			} catch (EmptyResultDataAccessException e) {
				return null;
				}
			}
			private static final class OrderIdMapper implements RowMapper<Long> {

				@Override
				public Long  mapRow(ResultSet resultSet, int rowNum) throws SQLException {
//					Long orderId = resultSet.getLong("orderId");
//					String durationType = resultSet.getString("durationType");
//					Date billStartDate = resultSet.getDate("billStartDate");
					return resultSet.getLong("orderId");
				}
				

				public String planIdSchema() {
					return "  os.id as orderId FROM b_orders os where os.client_id=?";
							
				}
				
				
			}

			@Override
			public void processingClientDetails(PrepareRequestData requestData,String configProp) {
				
				
				try{
					String requestType=null,sentMessage=null;			        
					 Order order=this.orderRepository.findOne(requestData.getOrderId());
					 AllocationDetailsData detailsData=this.allocationReadPlatformService.getTheHardwareItemDetails(requestData.getOrderId(),configProp);
					 requestType=requestData.getRequestType();
					 
					  PrepareRequest prepareRequest=this.prepareRequsetRepository.findOne(requestData.getRequestId());
					  
					 if(requestData.getIshardwareReq().equalsIgnoreCase("Y") && detailsData == null){
						 
						    
						      String status=OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getValue().toString();
         	                  prepareRequest.setStatus(status);
	                          this.prepareRequsetRepository.save(prepareRequest);
	                         
	                         //Update Order Status
	                         order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId());
	                         this.orderRepository.saveAndFlush(order);
	                                        
					}else {

/*<<<<<<< HEAD
						String requestType=null,sentMessage=null;			        
						Order order=this.orderRepository.findOne(requestData.getOrderId());
						AllocationDetailsData detailsData=this.allocationReadPlatformService.getTheHardwareItemDetails(requestData.getOrderId(),configProp);
						requestType=requestData.getRequestType();
						PrepareRequest prepareRequest=this.prepareRequsetRepository.findOne(requestData.getRequestId());

						if(requestData.getIshardwareReq().equalsIgnoreCase("Y") && detailsData == null){
							String status=OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getValue().toString();
							prepareRequest.setStatus(status);
							this.prepareRequsetRepository.save(prepareRequest);
								
							//Update Order Status
							order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId());
							this.orderRepository.saveAndFlush(order);
						
						}else {
							ProcessRequest processRequest=new ProcessRequest(requestData.getRequestId(),order.getClientId(),order.getId(),
									                       requestData.getProvisioningSystem(),requestType);
								List<OrderLine> orderLineData=order.getServices();
								for(OrderLine orderLine:orderLineData){
									String HardWareId=null;
									
									if(detailsData!=null){
										HardWareId=detailsData.getSerialNo();
									}
=======*/
						 
						 ProcessRequest processRequest=new ProcessRequest(requestData.getRequestId(),order.getClientId(), order.getId(),
								 requestData.getProvisioningSystem(),requestType,'N','N');
						 List<OrderLine> orderLineData=order.getServices();
						 
						 JSONObject jobject = new JSONObject();
						 PlanMapping planMapping= this.planMappingRepository.findOneByPlanId(order.getPlanId());
						 
						 if(planMapping != null){
						 jobject.put("planIdentification", planMapping.getPlanIdentification());
						 }
						 
						 
						 if(requestData.getProvisioningSystem().equalsIgnoreCase(ProvisioningApiConstants.PROV_BEENIUS)){
							 
							 JSONObject subjson = new JSONObject();
							 JSONArray serviceArray = new JSONArray();
							 if(requestData.getRequestType().equalsIgnoreCase(UserActionStatusTypeEnum.CHANGE_PLAN.toString())){
								 
								
								 Order oldOrder=this.orderRepository.findOldOrderByOrderNO(order.getOrderNo());
								 List<OrderLine> orderdetails=oldOrder.getServices();
								
								 for(OrderLine orderLine:orderdetails){
									 
									 JSONObject oldsubjson = new JSONObject();
									
									 List<ProvisionServiceDetails> provisionServiceDetails=this.provisionServiceDetailsRepository.findOneByServiceId(orderLine.getServiceId());
									 ServiceMaster service=this.serviceMasterRepository.findOne(orderLine.getServiceId()); 
									 oldsubjson.put("oldServiceIdentification", provisionServiceDetails.get(0).getServiceIdentification());
									 oldsubjson.put("oldServiceType", service.getServiceType());
									serviceArray.put(oldsubjson.toString());
								 }
								 jobject.put("oldServices", serviceArray);
								 
							 }
							 
								 JSONArray newServiceArray = new JSONArray();
								 for(OrderLine orderLine:orderLineData){
									// JSONObject subjson = new JSONObject();
									 List<ProvisionServiceDetails> provisionServiceDetails=this.provisionServiceDetailsRepository.findOneByServiceId(orderLine.getServiceId());
									 ServiceMaster service=this.serviceMasterRepository.findOne(orderLine.getServiceId()); 
									 subjson.put("serviceIdentification", provisionServiceDetails.get(0).getServiceIdentification());
									 subjson.put("serviceType", service.getServiceType());
									 newServiceArray.put(subjson.toString());	 
								 }
								 jobject.put("services", newServiceArray);
								 ProcessRequestDetails processRequestDetails=new ProcessRequestDetails(orderLineData.get(0).getId(),
									orderLineData.get(0).getServiceId(),jobject.toString(),"Recieved",
									null,order.getStartDate(),order.getEndDate(),null,null,'N',requestType,null);
								 processRequest.add(processRequestDetails);
							
						 
						 }else{
							 for(OrderLine orderLine:orderLineData){
								    String HardWareId=null;
								  if(detailsData!=null){
									  HardWareId=detailsData.getSerialNo();
								  }
								  
								  List<ProvisionServiceDetails> provisionServiceDetails=this.provisionServiceDetailsRepository.findOneByServiceId(orderLine.getServiceId());

								  ServiceMaster service=this.serviceMasterRepository.findOne(orderLine.getServiceId());
								
								  if(!provisionServiceDetails.isEmpty()){
									  /*Ashok adding
									   * 
									   */
									  sentMessage = provisionServiceDetails.get(0).getServiceIdentification();
									  
		                              if(requestData.getRequestType().equalsIgnoreCase(UserActionStatusTypeEnum.DEVICE_SWAP.toString())){
		                            	  
		                            	/*   requestType=UserActionStatusTypeEnum.ACTIVATION.toString();*/
		                            		 AllocationDetailsData allocationDetailsData=this.allocationReadPlatformService.getDisconnectedHardwareItemDetails(requestData.getOrderId(),requestData.getClientId(),configProp);
		                            		 JSONObject object = new JSONObject();
		                            		 object.put("clientId", order.getClientId());
		                            		 object.put("service", provisionServiceDetails.get(0).getServiceIdentification());
		                            		 object.put("OldHWId", allocationDetailsData.getSerialNo());
		                            		 object.put("NewHWId", HardWareId);
		                            		 
		                            		 sentMessage = object.toString();
		                            		 
		                            		/* ProcessRequestDetails processRequestDetails=new ProcessRequestDetails(orderLine.getId(),orderLine.getServiceId(),provisionServiceDetails.get(0).getServiceIdentification(),"Recieved",
		                            				 allocationDetailsData.getSerialNo(),order.getStartDate(),order.getEndDate(),null,null,'N',UserActionStatusTypeEnum.DISCONNECTION.toString());
		                            		 processRequest.add(processRequestDetails);*/
		                            		 
		                              }
		                           
								  ProcessRequestDetails processRequestDetails=new ProcessRequestDetails(orderLine.getId(),orderLine.getServiceId(),sentMessage,"Recieved",
										  HardWareId,order.getStartDate(),order.getEndDate(),null,null,'N',requestType,service.getServiceType());
								  processRequest.add(processRequestDetails);
								  
								/*  this.transactionHistoryWritePlatformService.saveTransactionHistory(order.getClientId(),"Provisioning",new Date(),"Order No:"+order.getOrderNo(),
										  "Request Type :"+prepareRequest.getRequestType(), "Request Id :"+prepareRequest.getId(),"Status:Sent For activation",ft.format(new Date())); */
								  }
							  }
						 }
						 
					     
					          
					       
	                       this.processRequestRepository.save(processRequest);				
	                       //PrepareRequest prepareRequest=this.prepareRequsetRepository.findOne(requestData.getRequestId());
                           prepareRequest.setIsProvisioning('Y');
                           String status=OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE).getValue().toString();
      	                  prepareRequest.setStatus(status);
                           this.prepareRequsetRepository.save(prepareRequest);
                         
                           
				  }
					 if(requestData.getProvisioningSystem().equalsIgnoreCase("None")){
						 order.setStatus(new Long(1));
						 this.orderRepository.save(order);
					 }
				}catch(Exception exception){
					exception.printStackTrace();
					}
			}

			@Override
			public List<Long> getPrepareRequestDetails(Long id) {

				try {
				
			        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSourcePerTenantService.retrieveDataSource());
				   PrepareRequestIdMapper planIdMapper = new PrepareRequestIdMapper();
				   String sql = "select" + planIdMapper.planIdSchema();
				  return jdbcTemplate.query(sql, planIdMapper,new Object[] { id });
				
			
			} catch (EmptyResultDataAccessException e) {
				return null;
				}
			}
			private static final class PrepareRequestIdMapper implements RowMapper<Long> {

				@Override
				public Long  mapRow(ResultSet resultSet, int rowNum) throws SQLException {
//					
					return resultSet.getLong("id");
				}
				

				public String planIdSchema() {
					return " pr.id as id from b_prepare_request pr " +
							" where pr.order_id=? and pr.is_provisioning ='N' and pr.request_type='ACTIVATION'";
							
				}
				
				
			}

			@Override
			public int getLastPrepareId(Long orderId) {
			try {

			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSourcePerTenantService.retrieveDataSource());
			String sql = "select max(id)from b_prepare_request where order_id=? and request_type='ACTIVATION'";

			return jdbcTemplate.queryForInt(sql, new Object[] { orderId });
			} catch (EmptyResultDataAccessException e) {
			return 0;
			}
			}		

}
