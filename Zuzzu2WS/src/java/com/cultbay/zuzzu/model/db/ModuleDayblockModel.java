/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author sravan
 */

package com.cultbay.zuzzu.model.db;

import java.sql.Date;

public class ModuleDayblockModel {

    private Integer cusebeda_objekt_id;
    private Integer distributor_id;
    private Date blocking_from;
    private Date blocking_till;
    private Integer hofesoda_ziwekat_id;
    private Integer ext_dist_type;

    public ModuleDayblockModel() {
    }

    public ModuleDayblockModel(Integer paramInteger1, Integer paramInteger2, Date paramDate1, Date paramDate2, Integer paramInteger3, Integer paramInteger4) {
        this.cusebeda_objekt_id = paramInteger1;
        this.distributor_id = paramInteger2;
        this.blocking_from = paramDate1;
        this.blocking_till = paramDate2;
        this.hofesoda_ziwekat_id = paramInteger3;
        this.ext_dist_type = paramInteger4;
    }

    public Date getBlocking_from() {
        return this.blocking_from;
    }

    public void setBlocking_from(Date paramDate) {
        this.blocking_from = paramDate;
    }

    public Date getBlocking_till() {
        return this.blocking_till;
    }

    public void setBlocking_till(Date paramDate) {
        this.blocking_till = paramDate;
    }

    public Integer getCusebeda_objekt_id() {
        return this.cusebeda_objekt_id;
    }

    public void setCusebeda_objekt_id(Integer paramInteger) {
        this.cusebeda_objekt_id = paramInteger;
    }

    public Integer getDistributor_id() {
        return this.distributor_id;
    }

    public void setDistributor_id(Integer paramInteger) {
        this.distributor_id = paramInteger;
    }

    public Integer getExt_dist_type() {
        return this.ext_dist_type;
    }

    public void setExt_dist_type(Integer paramInteger) {
        this.ext_dist_type = paramInteger;
    }

    public Integer getHofesoda_ziwekat_id() {
        return this.hofesoda_ziwekat_id;
    }

    public void setHofesoda_ziwekat_id(Integer paramInteger) {
        this.hofesoda_ziwekat_id = paramInteger;
    }
}
