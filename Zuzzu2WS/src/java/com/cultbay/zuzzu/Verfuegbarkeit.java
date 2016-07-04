/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cultbay.zuzzu;

import java.util.Date;

public class Verfuegbarkeit {

    private Integer id;
    private Integer cusebedaObjektId;
    private int hofesodaZiwekatId;
    private Date datum;
    private int anzahl;
    private Integer sonderpreis;
    private Integer channelPrice;
    private int hofesodaSaisonId;
    private int manuell;
    private int hofesodaGarantietypId;
    private double yieldingUtilisation;
    private Integer requestedQuantity;

    public Verfuegbarkeit(Integer paramInteger1, Integer paramInteger2, int paramInt1, Date paramDate, int paramInt2, Integer paramInteger3, int paramInt3, int paramInt4, int paramInt5) {
        this.id = paramInteger1;
        this.cusebedaObjektId = paramInteger2;
        this.hofesodaZiwekatId = paramInt1;
        this.datum = paramDate;
        this.anzahl = paramInt2;
        this.sonderpreis = paramInteger3;
        this.hofesodaSaisonId = paramInt3;
        this.manuell = paramInt4;
        this.hofesodaGarantietypId = paramInt5;
        this.channelPrice = Integer.valueOf(0);
        this.yieldingUtilisation = 0.0D;
        this.requestedQuantity = Integer.valueOf(1);
    }

    public Verfuegbarkeit() {
        this.channelPrice = Integer.valueOf(0);
        this.requestedQuantity = Integer.valueOf(1);
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer paramInteger) {
        this.id = paramInteger;
    }

    public Integer getCusebedaObjektId() {
        return this.cusebedaObjektId;
    }

    public void setCusebedaObjektId(Integer paramInteger) {
        this.cusebedaObjektId = paramInteger;
    }

    public int getHofesodaZiwekatId() {
        return this.hofesodaZiwekatId;
    }

    public void setHofesodaZiwekatId(int paramInt) {
        this.hofesodaZiwekatId = paramInt;
    }

    public Date getDatum() {
        return this.datum;
    }

    public void setDatum(Date paramDate) {
        this.datum = paramDate;
    }

    public int getAnzahl() {
        return this.anzahl;
    }

    public void setAnzahl(int paramInt) {
        this.anzahl = paramInt;
    }

    public Integer getSonderpreis() {
        return this.sonderpreis;
    }

    public void setSonderpreis(Integer paramInteger) {
        this.sonderpreis = paramInteger;
    }

    public Integer getChannelPrice() {
        return this.channelPrice;
    }

    public void setChannelPrice(Integer paramInteger) {
        this.channelPrice = paramInteger;
    }

    public int getHofesodaSaisonId() {
        return this.hofesodaSaisonId;
    }

    public void setHofesodaSaisonId(int paramInt) {
        this.hofesodaSaisonId = paramInt;
    }

    public int getManuell() {
        return this.manuell;
    }

    public void setManuell(int paramInt) {
        this.manuell = paramInt;
    }

    public int getHofesodaGarantietypId() {
        return this.hofesodaGarantietypId;
    }

    public void setHofesodaGarantietypId(int paramInt) {
        this.hofesodaGarantietypId = paramInt;
    }

    public double getYieldingUtilisation() {
        return this.yieldingUtilisation;
    }

    public void setYieldingUtilisation(double paramDouble) {
        this.yieldingUtilisation = paramDouble;
    }

    public Integer getRequestedQuantity() {
        return this.requestedQuantity;
    }

    public void setRequestedQuantity(Integer paramInteger) {
        this.requestedQuantity = paramInteger;
    }
}
