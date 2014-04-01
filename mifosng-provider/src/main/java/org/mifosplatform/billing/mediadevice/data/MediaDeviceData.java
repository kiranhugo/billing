package org.mifosplatform.billing.mediadevice.data;

import java.math.BigDecimal;
import java.util.List;

public class MediaDeviceData {
	
	
	private  Long deviceId;
	private  Long clientId;
	private  String clientType;
	private Long clientTypeId;
	private BigDecimal balanceAmount;
	
	private List<MediaDeviceData> data;

	
	
	public MediaDeviceData(Long deviceId, Long clientId, String clientType, Long clientTypeId, BigDecimal balanceAmount) {
           this.deviceId=deviceId;
           this.clientId=clientId;
           this.clientType=clientType;
           this.clientTypeId=clientTypeId;
           if(balanceAmount == null)
           this.balanceAmount=BigDecimal.ZERO;
           else
        	   this.balanceAmount=balanceAmount;
	}
	public MediaDeviceData(List<MediaDeviceData> data){
		this.data = data;
	}
	

	public Long getDeviceId() {
		return deviceId;
	}

	public Long getClientId() {
		return clientId;
	}

	public String getClientType() {
		return clientType;
	}

	public Long getClientTypeId() {
		return clientTypeId;
	}



	public List<MediaDeviceData> getData() {
		return data;
	}



	public void setData(List<MediaDeviceData> data) {
		this.data = data;
	}



	public void setClientTypeId(Long clientTypeId) {
		this.clientTypeId = clientTypeId;
	}
	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}
	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}
	public void setClientType(String clientType) {
		this.clientType = clientType;
	}

	
	
}
