package org.mifosplatform.logistics.agent.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.mifosplatform.logistics.item.data.ChargesData;
import org.mifosplatform.logistics.item.data.ItemData;
import org.mifosplatform.organisation.office.data.OfficeData;

public class AgentItemSaleData {

	private final Collection<OfficeData> officeDatas;
	private final List<ItemData> itemDatas;
	private final Long id;
	private final Long itemId;
	private final Long agentId;
	private final String agentName;
	private final String itemName;
	private final Long orderQunatity;
	private final BigDecimal chargeAmount;
	private final BigDecimal invoiceAmount;
	private final BigDecimal tax;
	private final List<ChargesData> chargesDatas;
	
	
	public static AgentItemSaleData instance(AgentItemSaleData itemSaleData, Collection<OfficeData> officeDatas,List<ItemData> itemDatas, List<ChargesData> chargesDatas) {

		return new AgentItemSaleData(itemSaleData.id,itemSaleData.itemId,itemSaleData.agentId,itemSaleData.itemName,itemSaleData.agentName,
				itemSaleData.orderQunatity,itemSaleData.chargeAmount,itemSaleData.tax,itemSaleData.invoiceAmount,officeDatas,itemDatas,chargesDatas);
	}

	public static AgentItemSaleData withTemplateData(Collection<OfficeData> officeDatas, List<ItemData> itemDatas, List<ChargesData> chargesDatas) {

		return new AgentItemSaleData(null,null,null,null,null,null,null,null,null,officeDatas,itemDatas,chargesDatas);
	}

	public AgentItemSaleData(Long id, Long itemId, Long agentId,String itemName, String agentName,Long orderQunatity,
			BigDecimal chargeAmount, BigDecimal tax, BigDecimal invoiceAmount,Collection<OfficeData> officeDatas, List<ItemData> itemDatas, List<ChargesData> chargesDatas) {
		
		this.id=id;
		this.itemId=itemId;
		this.itemName=itemName;
		this.agentId=agentId;
		this.agentName=agentName;
		this.orderQunatity=orderQunatity;
		this.chargeAmount=chargeAmount;
		this.invoiceAmount=invoiceAmount;
		this.tax=tax;
		this.officeDatas=officeDatas;
		this.chargesDatas=chargesDatas;
		this.itemDatas=itemDatas;

	
	
	}

	public Collection<OfficeData> getOfficeDatas() {
		return officeDatas;
	}

	public List<ItemData> getItemDatas() {
		return itemDatas;
	}

	public Long getId() {
		return id;
	}

	public Long getItemId() {
		return itemId;
	}

	public Long getAgentId() {
		return agentId;
	}

	public String getAgentName() {
		return agentName;
	}

	public String getItemName() {
		return itemName;
	}

	public Long getOrderQunatity() {
		return orderQunatity;
	}

	public BigDecimal getChargeAmount() {
		return chargeAmount;
	}

	public BigDecimal getInvoiceAmount() {
		return invoiceAmount;
	}

	public BigDecimal getTax() {
		return tax;
	}

	

	
	
}
