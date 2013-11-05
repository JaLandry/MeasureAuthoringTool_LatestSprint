package mat.model;

import java.sql.Timestamp;
import java.util.Date;

import mat.model.clause.Measure;

/**
 * The Class MeasureAuditLog.
 */
public class MeasureAuditLog {
	
	/** The id. */
	private String id;
	
	/** The activity type. */
	private String activityType;	
	
	/** The time. */
	private Timestamp time;
	
	/** The user id. */
	private String userId;
	
	/** The measure. */
	private Measure measure;
	
	/** The additional info. */
	private String additionalInfo;

	/**
	 * Gets the time.
	 * 
	 * @return the time
	 */
	public Date getTime() {
		return (Date)time;
	}

	/**
	 * Sets the time.
	 * 
	 * @param created
	 *            the new time
	 */
	public void setTime(Date created) {
		this.time = new Timestamp(created.getTime());
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the id.
	 * 
	 * @param id
	 *            the new id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Gets the measure.
	 * 
	 * @return the measure
	 */
	public Measure getMeasure() {
		return measure;
	}

	/**
	 * Sets the measure.
	 * 
	 * @param measure
	 *            the new measure
	 */
	public void setMeasure(Measure measure) {
		this.measure = measure;
	}

	/**
	 * Gets the activity type.
	 * 
	 * @return the activity type
	 */
	public String getActivityType() {
		return activityType;
	}

	/**
	 * Sets the activity type.
	 * 
	 * @param activityType
	 *            the new activity type
	 */
	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}

	/**
	 * Gets the user id.
	 * 
	 * @return the user id
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Sets the user id.
	 * 
	 * @param userId
	 *            the new user id
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * Gets the additional info.
	 * 
	 * @return the additional info
	 */
	public String getAdditionalInfo() {
		return additionalInfo;
	}

	/**
	 * Sets the additional info.
	 * 
	 * @param additionalInfo
	 *            the new additional info
	 */
	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}
}
