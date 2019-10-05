package com.capitaworld.service.loans.domain;


import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="csp_code")
public class CspCodes implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="product_code")
    private String productCode;

    @Column(name="description")
    private String description;

    @Column(name="variant")
    private String variant;

    @Column(name="category")
    private String category;

    @Column(name="rsm_occupation")
    private String rsmOccupation;

    @Column(name="rsm_relationship_with_bank")
    private String rsmRelationshipWithBank;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRsmOccupation() {
        return rsmOccupation;
    }

    public void setRsmOccupation(String rsmOccupation) {
        this.rsmOccupation = rsmOccupation;
    }

    public String getRsmRelationshipWithBank() {
        return rsmRelationshipWithBank;
    }

    public void setRsmRelationshipWithBank(String rsmRelationshipWithBank) {
        this.rsmRelationshipWithBank = rsmRelationshipWithBank;
    }

}
