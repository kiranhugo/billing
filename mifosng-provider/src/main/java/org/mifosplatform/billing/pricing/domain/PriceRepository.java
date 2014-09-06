package org.mifosplatform.billing.pricing.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PriceRepository extends JpaRepository<Price, Long>,
JpaSpecificationExecutor<Price>{

	@Query("from Price price where price.planCode =:planId and price.serviceCode =:serviceCode and price.contractPeriod =:duration")
	Price findOneByPlanAndService(@Param("planId")Long planId,@Param("serviceCode") String serviceCode,@Param("duration") String duration);




}
