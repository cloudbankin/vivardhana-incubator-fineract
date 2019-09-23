
	
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
	public class Bulk {

	   
	    private final String mobileno;
	    private final String response;
	    private final String name;
	    
	    
		public Bulk(String name,String mobileno,String response) {
			
			this.mobileno = mobileno;
			this.response = response;
			this.name = name;
		}
		
		public String getName() {
			return name;
		}

		public String getMobileno() {
			return mobileno;
		}
		public String getResponse() {
			return response;
		}

  
	    


	   
	    
	    
	}


