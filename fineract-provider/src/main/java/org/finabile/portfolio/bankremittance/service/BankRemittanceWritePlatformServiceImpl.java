package org.finabile.portfolio.bankremittance.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.fineract.infrastructure.codes.domain.CodeRepository;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.portfolio.client.domain.ClientAddress;
import org.apache.fineract.portfolio.client.domain.ClientAddressRepository;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.finabile.portfolio.bankremittance.data.BankRemittanceData;
import org.finabile.portfolio.bankremittance.domain.BankRemittanceDetails;
import org.finabile.portfolio.bankremittance.domain.BankRemittanceRepository;
import org.finabile.portfolio.bankremittance.domain.BankRemittanceRepositoryWrapper;
import org.finabile.portfolio.bankremittance.exception.BankFormatMissingException;
import org.finabile.portfolio.bankremittance.exception.loanAlreadyMarkAnotherBankRemittanceException;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.minidev.json.JSONObject;

@Service
public class BankRemittanceWritePlatformServiceImpl implements BankRemittanceWritePlatformService {

	private final LoanRepository loanRepository;
	private final CodeValueRepository codeValueRepository;
	private final ClientAddressRepository clientAddressRepository;
	private final BankRemittanceReadPlatformService bankRemittanceReadPlatformService;
	private final BankRemittanceRepositoryWrapper bankRemittanceRepositoryWrapper;

	@Autowired
	public BankRemittanceWritePlatformServiceImpl(final LoanRepository loanRepository,
			final CodeValueRepository codeValueRepository, final ClientAddressRepository clientAddressRepository,
			final BankRemittanceReadPlatformService bankRemittanceReadPlatformService,
			final BankRemittanceRepositoryWrapper bankRemittanceRepositoryWrapper) {
		this.loanRepository = loanRepository;
		this.codeValueRepository = codeValueRepository;
		this.clientAddressRepository = clientAddressRepository;
		this.bankRemittanceReadPlatformService = bankRemittanceReadPlatformService;
		this.bankRemittanceRepositoryWrapper = bankRemittanceRepositoryWrapper;

	}

	@Override
	public File fileWriterForApprovedLoansDetails(Long bankId, List<String> downloadedData) {
		CodeValue codeValueBAnkDetails = null;
		File fileData = null;
		if (!bankId.equals(null)) {
			codeValueBAnkDetails = this.codeValueRepository.findOne(bankId);
		} else {
			throw new BankFormatMissingException(
					"error.bankRemittance.filegenerator.bank.name.should.not.be.null.or.empty",
					"Select any one bank name");
		}
		if (codeValueBAnkDetails.getLabel().equals("CUB")) {
			fileData = bankRemittanceFileGeneratorForCUBBank(codeValueBAnkDetails, downloadedData);

		} else if (codeValueBAnkDetails.getLabel().equals("INDIAN BANK")) {
			fileData = bankRemittanceFileGeneratorForIndianBank(codeValueBAnkDetails, downloadedData);
		}
		return fileData;

	}

	public File bankRemittanceFileGeneratorForIndianBank(CodeValue codeValueBAnkDetails, List<String> downloadedData) {
		File fileReturnSame = null;
		try {
			Date date = new Date();
			String strDateFormat = "hh:mm:ss_a";
			DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
			String formattedDate = dateFormat.format(date);
			List<Loan> loanDetails = new ArrayList();
			LocalDate currentDate = new LocalDate();
			String fileName = "IBNK" + currentDate + "_" + formattedDate;
			File path = new File("C:\\vivardhana\\apache-tomcat-7.0.84\\bankRemittanceDetails");

			File[] files = path.listFiles();

			if (files != null) {
				for (File file : files) {
					file.delete();

				}
			}

			FileWriter fw = new FileWriter(
					"C:\\vivardhana\\apache-tomcat-7.0.84\\bankRemittanceDetails\\IBNK_0001.txt");

			FileWriter fwd = new FileWriter(
					"C:\\vivardhana\\apache-tomcat-7.0.84\\bankRemittanceDetails\\IBNK_0002.txt");
			Iterator itr = downloadedData.iterator();
			while (itr.hasNext()) {
				Long longId = Long.parseLong(itr.next().toString());

				Loan loanDetail = this.loanRepository.findOne(longId);
				loanDetails.add(loanDetail);
			}
			if (!loanDetails.isEmpty()) {
				int i = 1;

				for (Loan loan : loanDetails) {
					// bank remittance validations
					if (loan != null) {
						try {
							BankRemittanceDetails bank = this.bankRemittanceRepositoryWrapper
									.findOneWithNotFoundDetection(loan.getId());
							if (bank != null) {
								if (!bank.getBankDetails().getId().equals(codeValueBAnkDetails.getId())) {

									CodeValue errorSpot = bank.getBankDetails();
									throw new loanAlreadyMarkAnotherBankRemittanceException(errorSpot.getLabel(),
											loan.getAccountNumber());

								}
							} else {
								BankRemittanceDetails bankRemittanceDetails = BankRemittanceDetails
										.addingRefference(loan, codeValueBAnkDetails);
								this.bankRemittanceRepositoryWrapper.save(bankRemittanceDetails);

							}
						} catch (NullPointerException e) {
							e.printStackTrace();
						}
					}
					if (i == 1) {
						fw.write("SNO|");
						fw.write("CUSTOMER_NAME|");
						fw.write("CITY|");
						fw.write("ACCOUNT_NO|");
						fw.write("AMOUNT|");
						fw.write("DESCRIPTION|");
						fw.write("IFSC_CODE|");
						fw.write("BANK_NAME|");
						fw.write("BENEFICIARY_EMAIL_ID");
						fw.write("\n");
						fwd.write("SNO|");
						fwd.write("CUSTOMER_NAME|");
						fwd.write("CITY|");
						fwd.write("ACCOUNT_NO|");
						fwd.write("AMOUNT|");
						fwd.write("DESCRIPTION|");
						fwd.write("IFSC_CODE|");
						fwd.write("BANK_NAME|");
						fwd.write("BENEFICIARY_EMAIL_ID");
						fwd.write("\n");

					}
					BankRemittanceData bankDetails = this.bankRemittanceReadPlatformService
							.readBankDetails(loan.getClient().getId());
					if (bankDetails.getiFSCCode().startsWith("IDIB")) {

						fw.write(i + "|");
						fw.write(loan.getClient().getDisplayName() + "|");
						// ClientAddress clientAddress =
						// this.clientAddressRepository.findOne(loan.getClientId());
						ClientAddress clientAddress = this.clientAddressRepository.getData(loan.getClient().getId());
						fw.write(clientAddress.getAddress().getCity() + "|");

						fw.write(bankDetails.getAccountNumber() + "|");
						fw.write(loan.getApprovedPrincipal()
								.subtract(loan.getLoanSummary().getTotalFeeChargesDueAtDisbursement()) + "|");
						fw.write(loan.getLoanProduct().getShortName() + "|");
						fw.write(bankDetails.getiFSCCode() + "|");
						fw.write(bankDetails.getBankName() + "|");
						fw.write("accounts@vivardhanamfl.co.in");
						fw.write("\n");
						i++;
					} else {
						fwd.write(i + "|");
						fwd.write(loan.getClient().getDisplayName() + "|");
						// ClientAddress clientAddress =
						// this.clientAddressRepository.findOne(loan.getClientId());
						ClientAddress clientAddress = this.clientAddressRepository.getData(loan.getClient().getId());
						fwd.write(clientAddress.getAddress().getCity() + "|");

						fwd.write(bankDetails.getAccountNumber() + "|");
						fwd.write(loan.getApprovedPrincipal()
								.subtract(loan.getLoanSummary().getTotalFeeChargesDueAtDisbursement()) + "|");
						fwd.write(loan.getLoanProduct().getShortName() + "|");
						fwd.write(bankDetails.getiFSCCode() + "|");
						fwd.write(bankDetails.getBankName() + "|");
						fwd.write("accounts@vivardhanamfl.co.in");
						fwd.write("\n");
						i++;
					}
				}
			}

			fw.flush();
			fw.close();
			fwd.flush();
			fwd.close();
			fileReturnSame = new File("C:\\vivardhana\\apache-tomcat-7.0.84\\bankRemittanceDetails\\IBNK_0001.txt");

		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileReturnSame;

	}

	public File bankRemittanceFileGeneratorForCUBBank(CodeValue codeValueBAnkDetails, List<String> downloadedData) {
		File fileReturnSame = null;
		try {
			Date date = new Date();
			String strDateFormat = "hh:mm:ss_a";
			DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
			String formattedDate = dateFormat.format(date);
			List<Loan> loanDetails = new ArrayList();
			LocalDate currentDate = new LocalDate();
			String fileName = "CUB" + currentDate + "_" + formattedDate;

			File path = new File("C:\\vivardhana\\apache-tomcat-7.0.84\\bankRemittanceDetails");

			File[] files = path.listFiles();
			if (files != null) {
				for (File file : files) {
					file.delete();

				}
			}

			FileWriter fw = new FileWriter("C:\\vivardhana\\apache-tomcat-7.0.84\\bankRemittanceDetails\\CUB_0001.txt");

			FileWriter fwd = new FileWriter(
					"C:\\vivardhana\\apache-tomcat-7.0.84\\bankRemittanceDetails\\CUB_0002.txt");
			Iterator itr = downloadedData.iterator();
			while (itr.hasNext()) {
				Long longId = Long.parseLong(itr.next().toString());

				Loan loanDetail = this.loanRepository.findOne(longId);
				loanDetails.add(loanDetail);
			}
			if (!loanDetails.isEmpty()) {
				// int i = 1;
				for (Loan loan : loanDetails) {
					// bank remittance validations
					if (loan != null) {
						try {
							BankRemittanceDetails bank = this.bankRemittanceRepositoryWrapper
									.findOneWithNotFoundDetection(loan.getId());
							if (bank != null) {

								if (!bank.getBankDetails().getId().equals(codeValueBAnkDetails.getId())) {

									CodeValue errorSpot = bank.getBankDetails();
									throw new loanAlreadyMarkAnotherBankRemittanceException(errorSpot.getLabel(),
											loan.getAccountNumber());

								}
							} else {
								BankRemittanceDetails bankRemittanceDetails = BankRemittanceDetails
										.addingRefference(loan, codeValueBAnkDetails);
								this.bankRemittanceRepositoryWrapper.save(bankRemittanceDetails);
							}
						} catch (NullPointerException e) {
							e.printStackTrace();
						}
					}

					BankRemittanceData bankDetails = this.bankRemittanceReadPlatformService
							.readBankDetails(loan.getClient().getId());
					if (bankDetails.getiFSCCode().startsWith("CIUB")) {
						// fw.write(i + "|");
						fw.write("NEFT,");
						fw.write("510909010069422,");
						fw.write("Vivardhana Microfinance Limited,");
						fw.write(bankDetails.getiFSCCode() + ",");
						fw.write(loan.getApprovedPrincipal()
								.subtract(loan.getLoanSummary().getTotalFeeChargesDueAtDisbursement()) + ",");
						fw.write(bankDetails.getAccountNumber() + ",");
						fw.write(loan.getClient().getDisplayName());

						/*
						 * ClientAddress clientAddress =
						 * this.clientAddressRepository.getData(loan.getClient().getId());
						 * fw.write(clientAddress.getAddress().getCity() + "|");
						 * 
						 * fw.write(loan.getLoanProduct().getShortName() + "|");
						 * 
						 * fw.write(bankDetails.getBankName() + "|"); fw.write("abc@gmail.com");
						 */
						fw.write("\n");
						// i++;
					} else {
						fwd.write("NEFT,");
						fwd.write("510909010069422,");
						fwd.write("Vivardhana Microfinance Limited,");
						fwd.write(bankDetails.getiFSCCode() + ",");
						fwd.write(loan.getApprovedPrincipal()
								.subtract(loan.getLoanSummary().getTotalFeeChargesDueAtDisbursement()) + ",");
						fwd.write(bankDetails.getAccountNumber() + ",");
						fwd.write(loan.getClient().getDisplayName());
						fwd.write("\n");

					}
				}
			}

			fw.flush();
			fw.close();
			fwd.flush();
			fwd.close();
			fileReturnSame = new File("C:\\vivardhana\\apache-tomcat-7.0.84\\bankRemittanceDetails\\CUB_0001.txt");

		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return fileReturnSame;
	}

}
