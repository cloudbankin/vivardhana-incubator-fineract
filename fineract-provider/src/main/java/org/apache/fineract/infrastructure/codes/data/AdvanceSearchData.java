package org.apache.fineract.infrastructure.codes.data;

import java.io.Serializable;

public class AdvanceSearchData implements Serializable 
{
	 private final Long id;
	 private final String name;
	 
	 public AdvanceSearchData(final Long id,final String name)
	 {
		 this.id = id;
		 this.name = name;
	 }
	 
	 public static AdvanceSearchData instance(final Long id,final String name)
	 {
		 return new AdvanceSearchData(id, name);
	 }
	 
	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

}
