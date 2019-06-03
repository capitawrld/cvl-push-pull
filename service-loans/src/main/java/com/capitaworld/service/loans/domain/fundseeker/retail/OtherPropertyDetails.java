package com.capitaworld.service.loans.domain.fundseeker.retail;

import com.capitaworld.service.loans.domain.fundseeker.ApplicationProposalMapping;
import com.capitaworld.service.loans.domain.fundseeker.LoanApplicationMaster;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "fs_other_property_details")
public class OtherPropertyDetails implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private LoanApplicationMaster applicationId;

    @OneToOne
    @JoinColumn(name = "proposal_mapping_id")
    private ApplicationProposalMapping proposalId;

    @Column(name="property_type")
    private Integer propertyType;

    @Column(name="total_cost_of_land")
    private Integer totalCostOfLand;

    @Column(name="total_cost_of_construction")
    private Integer totalCostOfConstruction;

    @Column(name="time_for_completion")
    private Integer timeForCompletion;

    @Column(name="created_date")
    private Date createdDate;

    @Column(name="modified_date")
    private Date modifiedDate;

    @Column(name="is_active")
    private Boolean isActive;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LoanApplicationMaster getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(LoanApplicationMaster applicationId) {
        this.applicationId = applicationId;
    }

    public Integer getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(Integer propertyType) {
        this.propertyType = propertyType;
    }

    public Integer getTotalCostOfLand() {
        return totalCostOfLand;
    }

    public void setTotalCostOfLand(Integer totalCostOfLand) {
        this.totalCostOfLand = totalCostOfLand;
    }

    public Integer getTotalCostOfConstruction() {
        return totalCostOfConstruction;
    }

    public void setTotalCostOfConstruction(Integer totalCostOfConstruction) {
        this.totalCostOfConstruction = totalCostOfConstruction;
    }

    public Integer getTimeForCompletion() {
        return timeForCompletion;
    }

    public void setTimeForCompletion(Integer timeForCompletion) {
        this.timeForCompletion = timeForCompletion;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public ApplicationProposalMapping getProposalId() {
        return proposalId;
    }

    public void setProposalId(ApplicationProposalMapping proposalId) {
        this.proposalId = proposalId;
    }
}
