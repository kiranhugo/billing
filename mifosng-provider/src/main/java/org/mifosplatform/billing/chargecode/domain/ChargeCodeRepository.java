package org.mifosplatform.billing.chargecode.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChargeCodeRepository extends JpaRepository<ChargeCode, Long>,JpaSpecificationExecutor<ChargeCode>{

	@Query("from ChargeCode charge where charge.chargeCode =:chargeCode")
	ChargeCode findOneByChargeCode(@Param("chargeCode")String chargeCode);

}
