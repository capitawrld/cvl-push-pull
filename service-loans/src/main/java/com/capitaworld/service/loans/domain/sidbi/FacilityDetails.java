package com.capitaworld.service.loans.domain.sidbi;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by pooja.patel on 19-06-2019.
 */

@Entity
@Table(name="fs_sidbi_facility_details")
public class FacilityDetails {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="application_id")
    private Long applicationId;

    @Column(name="rupee_term_loan")
    private Long rupeeTermLoan;

        @Column(name="foreign_currency")
    private Long foreignCurrency;

    @Column(name="working_capital_fund")
    private Double workingCapitalFund;

    @Column(name="working_capital_non_fund")
    private Double workingCapitalNonFund;

    @Column(name="total")
    private Double total;

    @Column(name="is_active")
    private Boolean isActive;

    @Column(name="created_by")
    private Long createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="created_date")
    private Date createdDate;

    @Column(name="modified_by")
    private Long modifiedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="modified_date")
    private Date modifiedDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Long getRupeeTermLoan() {
        return rupeeTermLoan;
    }

    public void setRupeeTermLoan(Long rupeeTermLoan) {
        this.rupeeTermLoan = rupeeTermLoan;
    }

    public Long getForeignCurrency() {
        return foreignCurrency;
    }

    public void setForeignCurrency(Long foreignCurrency) {
        this.foreignCurrency = foreignCurrency;
    }

    public Double getWorkingCapitalFund() {
        return workingCapitalFund;
    }

    public void setWorkingCapitalFund(Double workingCapitalFund) {
        this.workingCapitalFund = workingCapitalFund;
    }

    public Double getWorkingCapitalNonFund() {
        return workingCapitalNonFund;
    }

    public void setWorkingCapitalNonFund(Double workingCapitalNonFund) {
        this.workingCapitalNonFund = workingCapitalNonFund;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Long getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Long modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
