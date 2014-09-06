package org.mifosplatform.billing.pricing.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.mifosplatform.billing.chargecode.data.ChargesData;
import org.mifosplatform.billing.pricing.data.PricingData;
import org.mifosplatform.billing.pricing.data.SavingChargeVaraint;
import org.mifosplatform.finance.data.DiscountMasterData;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.contract.data.SubscriptionData;
import org.mifosplatform.portfolio.plan.data.ServiceData;
import org.mifosplatform.portfolio.plan.service.ChargeVariant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;


@Service
public class PriceReadPlatformServiceImpl implements PriceReadPlatformService{


	 private final JdbcTemplate jdbcTemplate;
	    private final PlatformSecurityContext context;

	    @Autowired
	    public PriceReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource) {
	        this.context = context;
	        this.jdbcTemplate = new JdbcTemplate(dataSource);
	    }


		@Override
		public List<SubscriptionData> retrievePaytermData() {

			context.authenticatedUser();
			SubscriptionDataMapper mapper = new SubscriptionDataMapper();
			String sql = "select " + mapper.schema();
			return this.jdbcTemplate.query(sql, mapper, new Object[] {});
		}

		private static final class SubscriptionDataMapper implements RowMapper<SubscriptionData> {

			public String schema() {
				return " sb.id as id,sb.payterm_type as paytermType,sb.units as units "
						+ " from b_payments sb ";
			}

			@Override
			public SubscriptionData mapRow(ResultSet rs, int rowNum)throws SQLException {

				Long id = rs.getLong("id");
				String payterm_type = rs.getString("paytermType");
				String units = rs.getString("units");
				String contractPeriod = units.concat(payterm_type);
				SubscriptionData data = new SubscriptionData(id, contractPeriod,null);
				return data;
			}
		}

	    @Override
		public List<ServiceData> retrievePrcingDetails(Long planId) {

			  context.authenticatedUser();
		        String sql = "SELECT sm.id AS id,sm.service_description AS service_description,p.plan_code AS planCode,pm.service_code AS service_code,p.is_prepaid as isprepaid," +
		        		" c.billfrequency_code as billingfreq FROM b_plan_detail pm, b_service sm, b_plan_master p left join b_plan_pricing pr on pr.plan_id = p.id" +
		        		" left join b_charge_codes c ON c.charge_code = pr.charge_code WHERE pm.service_code = sm.service_code AND p.id = pm.plan_id " +
		        		" AND sm.is_deleted = 'n' AND pm.plan_id = ? group by pm.service_code";

		        RowMapper<ServiceData> rm = new PeriodMapper();
		        return this.jdbcTemplate.query(sql, rm, new Object[] { planId });
		}

		 private static final class PeriodMapper implements RowMapper<ServiceData> {

		        @Override
		        public ServiceData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
		            Long id = rs.getLong("id");
		            String planCode = rs.getString("planCode");
		            String serviceCode = rs.getString("service_code");
		            String serviceDescription = rs.getString("service_description");
		            String billingfreq = rs.getString("billingfreq");
		            String isprepaid = rs.getString("isprepaid");
		            ServiceData serviceData = new  ServiceData(id, null, planCode,billingfreq, serviceCode, serviceDescription,null);
		            serviceData.setIsPrepaid(isprepaid);
		            return serviceData;
		        }
	}

		    @Override
			public List<ServiceData> retrievePriceDetails(String planId, String region) {

				  context.authenticatedUser();
			        String sql = "SELECT p.plan_code AS plan_code, cp.id as contractId,pm.id AS id,pm.service_code AS serviceCode," +
			        		"se.service_description AS serviceDescription, pm.duration as contract,c.charge_description AS chargeDescription," +
			        		"pm.charge_code AS charge_code,pm.charging_variant AS chargingVariant,pm.price AS price,c.billfrequency_code as billingFrequency," +
			        		"pr.priceregion_name AS priceregion FROM b_plan_master p,b_service se,b_charge_codes c,b_plan_pricing pm  left join b_priceregion_master " +
			        		"pr on  pm.price_region_id=pr.id  LEFT JOIN b_contract_period cp ON cp.contract_period = pm.duration WHERE p.id = pm.plan_id  AND pm.charge_code=c.charge_code and " +
			        		" (pm.service_code = se.service_code or pm.service_code ='None') and pm.is_deleted='n' and se.is_deleted='n' and  pm.plan_id =? group by pm.id";
			       
			        if(region != null){
			        	sql=" SELECT p.plan_code AS plan_code,cp.id AS contractId,pm.id AS id,pm.service_code AS serviceCode,se.service_description AS serviceDescription," +
			        		" pm.duration AS contract,c.charge_description AS chargeDescription,pm.charge_code AS charge_code,pm.charging_variant AS chargingVariant," +
			        		" pm.price AS price,c.billfrequency_code AS billingFrequency,pr.priceregion_name AS priceregion FROM b_plan_master p,b_service se, b_charge_codes c," +
			        		" b_priceregion_detail pd, b_state s,b_plan_pricing pm LEFT JOIN b_priceregion_master pr ON pm.price_region_id = pr.id LEFT JOIN " +
			        		" b_contract_period cp ON cp.contract_period = pm.duration WHERE p.id = pm.plan_id AND pm.charge_code = c.charge_code AND " +
			        		" (pm.service_code = se.service_code OR pm.service_code = 'None') AND pm.is_deleted = 'n' AND se.is_deleted = 'n' and  " +
			        		" pm.price_region_id = pd.priceregion_id AND ( s.id = pd.state_id OR (pd.state_id = 0 AND pd.country_id = s.parent_code)) AND " +
			        		" s.state_name = '"+region+"' AND pm.plan_id =? GROUP BY pm.id";
			        }


			        RowMapper<ServiceData> rm = new PriceMapper();
			        return this.jdbcTemplate.query(sql, rm, new Object[] {planId });
			}

			 private static final class PriceMapper implements RowMapper<ServiceData> {

			        @Override
			        public ServiceData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

			             Long id = rs.getLong("id");
			             Long contractId = rs.getLong("contractId");
			             String planCode = rs.getString("plan_code");
			             String duration = rs.getString("contract");
			             String planDescription=null;
			             String serviceCode = rs.getString("serviceCode");
			             String chargeCode = rs.getString("chargeDescription");
			             String billingFrequency = rs.getString("billingFrequency");
			            String chargingVariant=rs.getString("chargingVariant");
			            String priceregion=rs.getString("priceregion");
			            BigDecimal price=rs.getBigDecimal("price");
			            int chargingVariant1 = new Integer(chargingVariant);
			           EnumOptionData chargingvariant = SavingChargeVaraint.interestCompoundingPeriodType(chargingVariant1);
			           String chargeValue=chargingvariant.getValue();
			            return new ServiceData(id,planCode,serviceCode,planDescription,chargeCode,chargeValue,price,priceregion,contractId,duration,billingFrequency);
			        }
		}


	@Override
	public List<DiscountMasterData> retrieveDiscountDetails() {

		  context.authenticatedUser();
	        String sql = "select s.id as id,s.discount_code as discountcode,s.discount_description as discount_description from b_discount_master s" +
	        		     " where s.is_delete='N'";

	        RowMapper<DiscountMasterData> rm = new DiscountMapper();
	        return this.jdbcTemplate.query(sql, rm, new Object[] {});
	}


	 private static final class DiscountMapper implements RowMapper<DiscountMasterData> {

	        @Override
	        public DiscountMasterData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

	            Long id = rs.getLong("id");
	            String discountcode = rs.getString("discountcode");
	            String discountdesc = rs.getString("discount_description");
	            return new DiscountMasterData(id,discountcode,discountdesc,null,null,null,null);
	        }
	 }

	@Override
	public List<ChargesData> retrieveChargeCode() {
		 
		String sql = "select s.id as id,s.charge_code as charge_code,s.charge_description as charge_description from b_charge_codes s";
		 RowMapper<ChargesData> rm = new ChargeMapper();
        return this.jdbcTemplate.query(sql, rm, new Object[] {});
}

 private static final class ChargeMapper implements RowMapper<ChargesData> {

        @Override
        public ChargesData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
        Long id = rs.getLong("id");
            String chargeCode = rs.getString("charge_code");
            String chargeDesc= rs.getString("charge_description");
            return new ChargesData(id,chargeCode,chargeDesc);
        }
}


@Override
public List<EnumOptionData> retrieveChargeVariantData() {

	EnumOptionData base = SavingChargeVaraint.interestCompoundingPeriodType(ChargeVariant.BASE);
	List<EnumOptionData> categotyType = Arrays.asList(base);
	return categotyType;
}

@Override

public List<ServiceData> retrieveServiceCodeDetails(Long planCode) {

	  context.authenticatedUser();
        String sql = "SELECT p.id AS planId, pm.id AS id,ch.charge_description AS chargeDescription, pm.plan_id AS plan_code,"
			+"pm.service_code AS service_code,pm.charge_code AS charge_code,pm.price_region_id as priceregion " +
			" FROM b_plan_master p, b_plan_pricing pm,b_charge_codes ch"
           +" WHERE p.id = pm.plan_id AND  ch.charge_code = pm.charge_code and pm.is_deleted='n' and pm.plan_id="+planCode;

        RowMapper<ServiceData> rm = new ServiceMapper();
        return this.jdbcTemplate.query(sql, rm, new Object[] {  });
}

 private static final class ServiceMapper implements RowMapper<ServiceData> {

        @Override
        public ServiceData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

               Long id = rs.getLong("id");
               Long planId = rs.getLong("planId");
               String planCode = rs.getString("plan_code");
               String serviceCode = rs.getString("service_code");
               String chargeCode = rs.getString("charge_code");
               String chargeDescription = rs.getString("chargeDescription");
               String priceRegion = rs.getString("priceregion");

            return new ServiceData(id,planId,planCode,chargeCode,serviceCode,chargeDescription,priceRegion);
        }
}


@Override
public PricingData retrieveSinglePriceDetails(String priceId) {
	 
try{
	context.authenticatedUser();
	PricingMapper rm = new PricingMapper();
	String sql = "SELECT "+rm.schema()+" AND p.id =? group by p.id";

	return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { priceId });

}catch(EmptyResultDataAccessException e){
	return null;
}
}


private static final class PricingMapper implements RowMapper<PricingData> {
	
	public String schema(){
		
		return 	" p.plan_id AS planId,pm.plan_code AS planCode,p.id AS priceId,p.service_code AS serviceCode,c.charge_code AS chargeCode," +
				" p.charging_variant AS chargeVariant,p.price AS price,p.discount_id AS discountId,p.duration as contractperiod,p.price_region_id AS priceregion " +
				" FROM b_plan_pricing p,b_service s,b_charge_codes c,b_plan_master pm  WHERE p.charge_code = c.charge_code  AND (p.service_code = s.service_code or " +
				" p.service_code ='None') AND pm.id = p.plan_id ";
		
	}

     @Override
     public PricingData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

	  Long planId = rs.getLong("planId");
	  Long priceId = rs.getLong("priceId");
	  String serviceCode = rs.getString("serviceCode");
	  String chargeCode = rs.getString("chargeCode");
        BigDecimal price = rs.getBigDecimal("price");
         Long discountId = rs.getLong("discountId");
         String chargeVariant = rs.getString("chargeVariant");
         int chargeVariantId=new Integer(chargeVariant);
         Long priceregion = rs.getLong("priceregion");
         String planCode = rs.getString("planCode");
         String contractperiod = rs.getString("contractperiod");
         return new PricingData(planId,serviceCode,chargeCode,price,discountId,chargeVariantId,priceregion,planCode,priceId,contractperiod);
     }
}

@Override
public List<PricingData> retrievePlanAndPriceDetails(String region) {
	
	try{
		this.context.authenticatedUser();
		PlanAndPricingMapper mapper=new PlanAndPricingMapper(this,region);  
		String sql="SELECT pm.plan_code AS planCode, pm.id AS planId, pm.is_prepaid as isPrepaid FROM b_plan_master pm where pm.is_deleted='N'";
		
		 return this.jdbcTemplate.query(sql, mapper, new Object[] {  });
		
	}catch(EmptyResultDataAccessException e){
		return null;
	}
}

private static final class PlanAndPricingMapper implements RowMapper<PricingData> {
	
	PriceReadPlatformServiceImpl priceReadPlatformServiceImp=null;
	private String region;
    public PlanAndPricingMapper(PriceReadPlatformServiceImpl priceReadPlatformServiceImpl,String region) {
    	this.priceReadPlatformServiceImp=priceReadPlatformServiceImpl;
    	this.region=region;
		
	}

	@Override
    public PricingData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

	  Long planId = rs.getLong("planId");
	  String planCode = rs.getString("planCode");
	  String isPrepaid = rs.getString("isPrepaid");
      List<ServiceData> pricingData=this.priceReadPlatformServiceImp.retrievePriceDetails(planId.toString(),region);
      
        return new PricingData(planId,planCode,isPrepaid,pricingData);
    }
}


}








