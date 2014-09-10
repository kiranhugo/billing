package org.mifosplatform.cms.media.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.billing.paymode.data.McodeData;
import org.mifosplatform.cms.mediadetails.data.MediaLocationData;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.data.MediaEnumoptionData;
import org.mifosplatform.organisation.mcodevalues.data.MCodeData;

public class MediaAssetData {


	private final Long mediaId;
	private final String mediaTitle;
	private final String mediaImage;
	private final BigDecimal mediaRating;
	private final Long eventId;
	private Long noOfPages;
	private Long pageNo;
	private String assetTag;
	private List<EnumOptionData> mediaStatus;
	private List<MediaassetAttribute> mediaAttributes;
	private List<MediaassetAttribute> mediaFormat;
	private String status;
	private LocalDate releaseDate;
	private List<MediaEnumoptionData> mediaTypeData;
	private List<McodeData> mediaCategeorydata;
	private List<McodeData> mediaLanguageData;
	private MediaAssetData mediaAssetData;
	private List<MediaLocationData> mediaLocationData;
	private List<MediaassetAttributeData> mediaassetAttributes;
	private List<MediaAssetData> mediaDetails;
	private String mediatype;
	private String genre;
	private Long catageoryId;
	private String subject;
	private String overview;
	private Long contentProvider;
	private String rated;
	private BigDecimal rating;
	private String duration;
	private Long ratingCount;
	private List<McodeData> contentProviderData;
	private BigDecimal cpShareValue;
	private String quality;
	private String optType;
	private BigDecimal price;
	private Collection<MCodeData> eventCategeorydata;
	private String EventCategory;
	private String mediaCategory;
	private String contentProviderValue;
	
public MediaAssetData(final Long mediaId,final String mediaTitle,final String image,final BigDecimal rating, Long eventId, 
		               String assetTag, String quality, String optType, BigDecimal price){
	this.mediaId=mediaId;
	this.mediaTitle=mediaTitle;
	this.mediaImage=image;
	this.mediaRating=rating;
	this.eventId=eventId;
	this.assetTag=assetTag;
	this.quality=quality;
	this.optType=optType;
	this.price=price;
}
public MediaAssetData(List<MediaAssetData> data, Long noOfPages, Long pageNo) {
	this.mediaId=null;
	this.mediaTitle=null;
	this.mediaImage=null;
	this.mediaRating=null;
	this.eventId=null;
	this.noOfPages=noOfPages;
	this.mediaDetails=data;
	this.pageNo=pageNo;
}
public MediaAssetData(MediaAssetData mediaAssetData, List<MediaassetAttributeData> mediaassetAttributes, List<MediaLocationData> mediaLocationData, List<EnumOptionData> status,List<MediaassetAttribute> data, List<MediaassetAttribute> mediaFormat,
		Collection<MCodeData> eventCategeorydata, List<McodeData> mediaCategeorydata,List<McodeData> mediaLangauagedata,List<McodeData> contentProviderData,List<MediaEnumoptionData> mediaTypeData) {

	this.mediaAssetData=mediaAssetData;
	this.mediaStatus=status;
	this.mediaAttributes=data;
	this.mediaFormat=mediaFormat;
	this.mediaId=null;
	this.mediaTitle=null;
	this.mediaImage=null;
	this.mediaRating=null;
	this.eventId=null;
	this.eventCategeorydata=eventCategeorydata;
	this.mediaCategeorydata=mediaCategeorydata;
	this.mediaLanguageData=mediaLangauagedata;
	this.mediaLocationData=mediaLocationData;
	this.mediaassetAttributes=mediaassetAttributes;
	this.contentProviderData=contentProviderData;
	this.mediaTypeData=mediaTypeData;
	
}
public MediaAssetData(Long mediaId, String mediaTitle, String status,
		LocalDate releaseDate, BigDecimal share,String EventCategory,String mediaCategory,String contentProviderValue) {
          this.mediaId=mediaId;
          this.mediaTitle=mediaTitle;
          this.status=status;
          this.releaseDate=releaseDate;
          this.cpShareValue=share;
          this.EventCategory=EventCategory;
          this.mediaCategory=mediaCategory;
          this.contentProviderValue=contentProviderValue;
      	  this.mediaImage=null;
      	  this.eventId=null;
      	  this.mediaRating=null;
}

public MediaAssetData(Long mediaId, String mediatitle, String type,
		String genre, Long catageoryId, LocalDate releaseDate, String subject,
		String overview, String image, Long contentProvider, String rated,
		BigDecimal rating, Long ratingCount, String status, String duration,BigDecimal cpShareValue) {
	// TODO Auto-generated constructor stub
	 this.mediaId=mediaId;
     this.mediaTitle=mediatitle;
     this.mediatype=type;
     this.genre=genre;
     this.catageoryId=catageoryId;
     this.releaseDate=releaseDate;
     this.subject=subject;
     this.overview=overview;
     this.mediaImage=image;
     this.contentProvider=contentProvider;
     this.rated=rated;
     this.mediaRating=rating;
     this.rating=rating;
     this.ratingCount=ratingCount;
     this.duration=duration;
     this.status=status;
     this.cpShareValue=cpShareValue;
 	 this.eventId=null;
}
public Long getMediaId() {
	return mediaId;
}
public String getMediaTitle() {
	return mediaTitle;
}
public String getMediaImage() {
	return mediaImage;
}
public BigDecimal getMediaRating() {
	return mediaRating;
}
public Long getEventId() {
	return eventId;
}
	

}
