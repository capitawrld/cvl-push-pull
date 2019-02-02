package com.capitaworld.service.loans.model;

import java.io.Serializable;

public class CMADetailResponse implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	 //asset	
		private Double receivableOtherThanDefferred; //D16
		private Double exportReceivables; //D18
		private Double inventory; //D22
		private Double advanceToSupplierRawMaterials; //D37
		//liabilities
		private Double sundryCreditors; //D19
		private Double advancePaymentsFromCustomers;//D21
		
		private Double domesticSales; //D8
		private Double exportSales; //D9
		private Double netProfitOrLoss; //D78
		private Double depreciation; //D34
		private Double interest;  //D62
		private Double provisionForDeferredTax; //D77
		private Double grossBlock; //D45
		private Double totalCurrentAssets ; //D43 Assets
		private Double subTotalA;  // D15 Liabilities
		private Double totalCurrentLiabilities; // D37 Liabilities

		private Double totalOutsideLiabilities; // D63 Liabilities
		private Double tangibleNetWorth ; // Assessts D91 same as Total Net-worth

		private Double opProfitBeforeIntrest; 	// D60 operating stmt.

		public Double getReceivableOtherThanDefferred() {
			return receivableOtherThanDefferred;
		}
		public void setReceivableOtherThanDefferred(Double receivableOtherThanDefferred) {
			this.receivableOtherThanDefferred = receivableOtherThanDefferred;
		}
		public Double getExportReceivables() {
			return exportReceivables;
		}
		public void setExportReceivables(Double exportReceivables) {
			this.exportReceivables = exportReceivables;
		}
		public Double getInventory() {
			return inventory;
		}
		public void setInventory(Double inventory) {
			this.inventory = inventory;
		}
		public Double getAdvanceToSupplierRawMaterials() {
			return advanceToSupplierRawMaterials;
		}
		public void setAdvanceToSupplierRawMaterials(Double advanceToSupplierRawMaterials) {
			this.advanceToSupplierRawMaterials = advanceToSupplierRawMaterials;
		}
		public Double getSundryCreditors() {
			return sundryCreditors;
		}
		public void setSundryCreditors(Double sundryCreditors) {
			this.sundryCreditors = sundryCreditors;
		}
		public Double getAdvancePaymentsFromCustomers() {
			return advancePaymentsFromCustomers;
		}
		public void setAdvancePaymentsFromCustomers(Double advancePaymentsFromCustomers) {
			this.advancePaymentsFromCustomers = advancePaymentsFromCustomers;
		}
		public Double getDomesticSales() {
			return domesticSales;
		}
		public void setDomesticSales(Double domesticSales) {
			this.domesticSales = domesticSales;
		}
		public Double getExportSales() {
			return exportSales;
		}
		public void setExportSales(Double exportSales) {
			this.exportSales = exportSales;
		}
		public Double getNetProfitOrLoss() {
			return netProfitOrLoss;
		}
		public void setNetProfitOrLoss(Double netProfitOrLoss) {
			this.netProfitOrLoss = netProfitOrLoss;
		}
		public Double getDepreciation() {
			return depreciation;
		}
		public void setDepreciation(Double depreciation) {
			this.depreciation = depreciation;
		}
		
		public Double getInterest() {
			return interest;
		}
		public void setInterest(Double interest) {
			this.interest = interest;
		}
		
		public Double getProvisionForDeferredTax() {
			return provisionForDeferredTax;
		}
		public void setProvisionForDeferredTax(Double provisionForDeferredTax) {
			this.provisionForDeferredTax = provisionForDeferredTax;
		}
		
		public Double getGrossBlock() {
			return grossBlock;
		}
		public void setGrossBlock(Double grossBlock) {
			this.grossBlock = grossBlock;
		}
		
		public Double getTotalCurrentAssets() {
			return totalCurrentAssets;
		}
		public void setTotalCurrentAssets(Double totalCurrentAssets) {
			this.totalCurrentAssets = totalCurrentAssets;
		}
		public Double getSubTotalA() {
			return subTotalA;
		}
		public void setSubTotalA(Double subTotalA) {
			this.subTotalA = subTotalA;
		}
		
		public Double getTotalCurrentLiabilities() {
			return totalCurrentLiabilities;
		}
		public void setTotalCurrentLiabilities(Double totalCurrentLiabilities) {
			this.totalCurrentLiabilities = totalCurrentLiabilities;
		}

		public Double getTotalOutsideLiabilities() {
			return totalOutsideLiabilities;
		}
		public void setTotalOutsideLiabilities(Double totalOutsideLiabilities) {
			this.totalOutsideLiabilities = totalOutsideLiabilities;
		}
		public Double getTangibleNetWorth() {
			return tangibleNetWorth;
		}
		public void setTangibleNetWorth(Double tangibleNetWorth) {
			this.tangibleNetWorth = tangibleNetWorth;
		}

		public Double getOpProfitBeforeIntrest() {
			return opProfitBeforeIntrest;
		}
		public void setOpProfitBeforeIntrest(Double opProfitBeforeIntrest) {
			this.opProfitBeforeIntrest = opProfitBeforeIntrest;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "CMADetailResponse [receivableOtherThanDefferred=" + receivableOtherThanDefferred
					+ ", exportReceivables=" + exportReceivables + ", inventory=" + inventory
					+ ", advanceToSupplierRawMaterials=" + advanceToSupplierRawMaterials + ", sundryCreditors="
					+ sundryCreditors + ", advancePaymentsFromCustomers=" + advancePaymentsFromCustomers
					+ ", domesticSales=" + domesticSales + ", exportSales=" + exportSales + ", netProfitOrLoss="
					+ netProfitOrLoss + ", depreciation=" + depreciation + ", interest=" + interest
					+ ", provisionForDeferredTax=" + provisionForDeferredTax + ", grossBlock=" + grossBlock
					+ ", totalCurrentAssets=" + totalCurrentAssets + ", subTotalA=" + subTotalA
					+ ", totalCurrentLiabilities=" + totalCurrentLiabilities + ", totalOutsideLiabilities="
					+ totalOutsideLiabilities + ", tangibleNetWorth=" + tangibleNetWorth + ", opProfitBeforeIntrest="
					+ opProfitBeforeIntrest + "]";
		}
		
}
