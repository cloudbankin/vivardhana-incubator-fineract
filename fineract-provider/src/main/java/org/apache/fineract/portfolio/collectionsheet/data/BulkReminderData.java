
	
	/**
	 * Licensed to the Apache Software Foundation (ASF) under one
	 * or more contributor license agreements. See the NOTICE file
	 * distributed with this work for additional information
	 * regarding copyright ownership. The ASF licenses this file
	 * to you under the Apache License, Version 2.0 (the
	 * "License"); you may not use this file except in compliance
	 * with the License. You may obtain a copy of the License at
	 *
	 * http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing,
	 * software distributed under the License is distributed on an
	 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
	 * KIND, either express or implied. See the License for the
	 * specific language governing permissions and limitations
	 * under the License.
	 */
	package org.apache.fineract.portfolio.collectionsheet.data;

	import java.math.BigDecimal;

	import org.apache.fineract.infrastructure.core.data.EnumOptionData;
	import org.apache.fineract.organisation.monetary.data.CurrencyData;
	import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;

	/**
	 * Immutable data object for extracting flat data for joint liability group's
	 * collection sheet.
	 */
	public class BulkReminderData {

	    private final String loanAccountNumber;
	    private final String installmentNumber;
	    private final BigDecimal amount;
	    private final String mobileno;
	    private final String duedate;
	    private final String name;
	    private final Integer id;
	    
	  

		public BulkReminderData(String loanAccountNumber,String name,String mobileno,String duedate, String installmentNumber, BigDecimal amount
				,Integer id) {
			this.loanAccountNumber = loanAccountNumber;
			this.installmentNumber = installmentNumber;
			this.amount = amount;
			this.mobileno = mobileno;
			this.duedate = duedate;
			this.name = name;
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public String getLoanAccountNumber() {
			return loanAccountNumber;
		}
		public String getInstallmentNumber() {
			return installmentNumber;
		}
		public BigDecimal getAmount() {
			return amount;
		}
		public String getMobileno() {
			return mobileno;
		}
		public String getDuedate() {
			return duedate;
		}
		public Integer getId() {
			return id;
		}
		
  
	    


	   
	    
	    
	}


