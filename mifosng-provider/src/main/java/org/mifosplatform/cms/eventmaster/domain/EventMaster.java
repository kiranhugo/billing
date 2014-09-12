package org.mifosplatform.cms.eventmaster.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.LocalDate;
import org.mifosplatform.annotation.ComparableFields;
import org.mifosplatform.cms.eventpricing.domain.EventPricing;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;


/**
 * Domian for {@link EventMaster} 
 * @author pavani
 *
 */
@ComparableFields(on={"eventName","eventDescription","status","eventStartDate","eventEndDate","eventValidity","eventCategory"})
@Entity
@Table(name = "b_event_master")
public class EventMaster extends AbstractPersistable<Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Column(name = "event_name")
	private String eventName;
	
	@Column(name = "event_description")
	private String eventDescription;
	
	@Column(name = "status")
	private Integer status;
	
	@Column(name = "event_start_date")
	private Date eventStartDate;
	
	@Column(name = "event_end_date")
	private Date eventEndDate;
	
	/*@Column(name = "allow_cancellation")
	private boolean allowCancellation;*/
	
	@Column(name = "event_validity")
	private Date eventValidity;
	
	@Column(name = "createdby_id")
	private Long createdbyId;
	
	@Column(name = "created_date")
	private Date createdDate;
	
	@Column(name = "charge_code")
	private String chargeCode;
	
	@Column(name = "event_category")
	private String eventCategory;
	
	
	
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL , mappedBy = "event" , orphanRemoval = true)
	private List<EventDetails> details = new ArrayList<EventDetails>();
	
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL , mappedBy = "eventId" , orphanRemoval = true)
	private List<EventPricing> eventPricings = new ArrayList<EventPricing>();
    
	
	
	public static EventMaster fromJsom(final JsonCommand command) {
		
		String eventName = command.stringValueOfParameterNamed("eventName");
		String eventDescription = command.stringValueOfParameterNamed("eventDescription");
		Integer status = command.integerValueOfParameterNamed("status");
		LocalDate eventStartDate = command.localDateValueOfParameterNamed("eventStartDate"); 
		LocalDate eventEndDate = command.localDateValueOfParameterNamed("eventEndDate");
		String chargeCode = command.stringValueOfParameterNamed("chargeCode");
		/*boolean allowCancellation = command.booleanPrimitiveValueOfParameterNamed("allowCancellation");*/
		LocalDate eventValidity = command.localDateValueOfParameterNamed("eventValidity");
		String eventCategory = command.stringValueOfParameterNamed("eventCategory");

		return new EventMaster(eventName,eventDescription,status,eventStartDate,eventEndDate,eventValidity,eventCategory,chargeCode);
	}
	
	public EventMaster (String eventName, String eventDescription, Integer status, LocalDate eventStartDate,LocalDate eventEndDate,LocalDate eventValidity,
			String eventCategory,String chargeCode) {

		
		this.eventName = eventName;
		this.eventDescription = eventDescription;
		this.status = status;
		this.eventStartDate = eventStartDate.toDate();
		this.eventEndDate =eventEndDate!=null?eventEndDate.toDate():null;
		this.eventValidity = eventValidity.toDate();
		this.createdDate = new Date();
		this.eventCategory=eventCategory;
		this.chargeCode=chargeCode;
	}
	
	public EventMaster() {
		
	}
	
	
	

	public List<EventDetails> getDetails() {
		return details;
	}

	public void addMediaDetails(EventDetails details){
		details.update(this);
		this.details.add(details);
	}
	
	public void delete() {
		this.eventEndDate = new Date();
	}
	
	
	/**
	 * @return the eventName
	 */
	public String getEventName() {
		return eventName;
	}
	
	

	public List<EventPricing> getEventPricings() {
		return eventPricings;
	}

	/**
	 * @param eventName the eventName to set
	 */
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	/**
	 * @return the eventDescription
	 */
	public String getEventDescription() {
		return eventDescription;
	}

	/**
	 * @param eventDescription the eventDescription to set
	 */
	public void setEventDescription(String eventDescription) {
		this.eventDescription = eventDescription;
	}

	/**
	 * @return the status
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}

	/**
	 * @return the eventStartDate
	 */
	public Date getEventStartDate() {
		return eventStartDate;
	}

	/**
	 * @param eventStartDate the eventStartDate to set
	 */
	public void setEventStartDate(Date eventStartDate) {
		this.eventStartDate = eventStartDate;
	}

	/**
	 * @return the eventEndDate
	 */
	public Date getEventEndDate() {
		return eventEndDate;
	}

	/**
	 * @param eventEndDate the eventEndDate to set
	 */
	public void setEventEndDate(Date eventEndDate) {
		this.eventEndDate = eventEndDate;
	}
/*
	*//**
	 * @return the allowCancellation
	 *//*
	public boolean getAllowCancellation() {
		return allowCancellation;
	}

	*//**
	 * @param allowCancellation the allowCancellation to set
	 *//*
	public void setAllowCancellation(boolean allowCancellation) {
		this.allowCancellation = allowCancellation;
	}*/

	/**
	 * @return the eventValidity
	 */
	public Date getEventValidity() {
		return eventValidity;
	}

	/**
	 * @param eventValidity the eventValidity to set
	 */
	public void setEventValidity(Date eventValidity) {
		this.eventValidity = eventValidity;
	}

	/**
	 * @return the details
	 */
	public List<EventDetails> getEventDetails() {
		return details;
	}

	/**
	 * @param details the details to set
	 */
	public void setDetails(List<EventDetails> details) {
		this.details = details;
	}

	/**
	 * @return the createdbyId
	 */
	public Long getCreatedbyId() {
		return createdbyId;
	}

	/**
	 * @param createdbyId the createdbyId to set
	 */
	public void setCreatedbyId(Long createdbyId) {
		this.createdbyId = createdbyId;
	}

	/**
	 * @return the createdDate
	 */
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * @param createdDate the createdDate to set
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getChargeCode() {
		return chargeCode;
	}

	public java.util.Map<String, Object> updateEventDetails(JsonCommand command) {

		final LinkedHashMap<String, Object> actualChanges = new LinkedHashMap<String, Object>(1);
		
		
		final String eventNameNamedParamName = "eventName";
		final String eventDescriptionNamedParamName = "eventDescription"; 
		final String statusNamedParamName = "status";  
		final String eventStartDateNamedParamName = "eventStartDate";
		final String eventEndDateNamedParamName = "eventEndDate";
		final String eventValidityNamedParamName = "eventValidity";
		final String eventCategoryNamedParamName = "eventCategory";
		final String chargeCodeNamedParamName = "chargeCode";
		
		if(command.isChangeInStringParameterNamed(eventNameNamedParamName, this.eventName)){
			final String newValue = command.stringValueOfParameterNamed(eventNameNamedParamName);
			actualChanges.put(eventNameNamedParamName, newValue);
			this.eventName = StringUtils.defaultIfEmpty(newValue,null);
		}
		if(command.isChangeInStringParameterNamed(eventDescriptionNamedParamName, this.eventDescription)){
			final String newValue = command.stringValueOfParameterNamed(eventDescriptionNamedParamName);
			actualChanges.put(eventDescriptionNamedParamName, newValue);
			this.eventDescription = StringUtils.defaultIfEmpty(newValue, null);
		}
		if(command.isChangeInIntegerParameterNamed(statusNamedParamName, this.status)){
			
			final Integer newValue= command.integerValueOfParameterNamed(statusNamedParamName);
			actualChanges.put(statusNamedParamName, newValue);
			this.status=newValue;
		}
		if(command.isChangeInDateParameterNamed(eventStartDateNamedParamName, this.eventStartDate)){
			final Date newValue=command.DateValueOfParameterNamed(eventStartDateNamedParamName);
			actualChanges.put(eventStartDateNamedParamName, newValue);
			this.eventStartDate=newValue;
		}
		if(command.isChangeInDateParameterNamed(eventEndDateNamedParamName, this.eventEndDate)){
			final Date newValue=command.DateValueOfParameterNamed(eventEndDateNamedParamName);
			actualChanges.put(eventEndDateNamedParamName, newValue);
			this.eventEndDate=newValue;
		}
		if(command.isChangeInDateParameterNamed(eventValidityNamedParamName, this.eventValidity)){
			final Date newValue=command.DateValueOfParameterNamed(eventValidityNamedParamName);
			actualChanges.put(eventValidityNamedParamName, newValue);
			this.eventValidity=newValue;
		}
		if(command.isChangeInStringParameterNamed(eventCategoryNamedParamName, this.eventCategory)){
			final String newValue = command.stringValueOfParameterNamed(eventCategoryNamedParamName);
			actualChanges.put(eventCategoryNamedParamName, newValue);
			this.eventCategory = StringUtils.defaultIfEmpty(newValue,null);
		}
		if(command.isChangeInStringParameterNamed(chargeCodeNamedParamName, this.chargeCode)){
			final String newValue = command.stringValueOfParameterNamed(chargeCodeNamedParamName);
			actualChanges.put(chargeCodeNamedParamName, newValue);
			this.chargeCode = StringUtils.defaultIfEmpty(newValue,null);
		}
		
		return actualChanges;
	}

	
}
