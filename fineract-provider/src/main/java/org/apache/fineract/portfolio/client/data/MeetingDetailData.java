package org.apache.fineract.portfolio.client.data;

import java.util.Date;

public class MeetingDetailData 
{
	private final Date meetingDate ;
	
	public MeetingDetailData(Date meetingDate)
	{
		this.meetingDate = meetingDate;
	}

	public Date getMeetingDate() 
	{
		return meetingDate;
	}
	
	
}
