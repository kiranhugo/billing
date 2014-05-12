package org.mifosplatform.organisation.hardwaremapping.service;

import java.util.List;

import org.mifosplatform.organisation.hardwaremapping.data.HardwareMappingDetailsData;

public interface HardwareMappingReadPlatformService {

	List<HardwareMappingDetailsData> getPlanDetailsByItemCode(String itemCode, Long clientId);

}
