/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cultbay.zuzzu.utils;


import com.cultuzz.util.DBConnection;
import com.cultuzz.zuzzu.ebay.impl.CultbayAuctionDetailsRQImpl;
import com.cultuzz.zuzzu.ebay.impl.CultbayGeoTagsRequestImpl;
import com.cultuzz.zuzzu.ebay.impl.CultbayGeoTagsResponseTypeImpl;
import com.cultuzz.zuzzu.ebay.impl.CultbayRequestImpl;
import com.cultuzz.zuzzu.ebay.impl.CultbayRequestTypeImpl;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author kondalarao
 */
public class CulBayXMLParser {

        String errorCode = null;
        boolean parseError = false;
        int xmlFileType;
        // element of Cultbay_Request-XML
        private boolean locationSet = false;
        private boolean periodSet = false;
        private boolean facilitySet = false;
        private boolean categorySet = false;
        private boolean keywordSet = false;
        private boolean currencySet = false;
        private boolean languageSet = false;
        private boolean objectIDSet = false;
        private boolean auctionIDSet = false;
        private boolean periodEOASet = false;
        private boolean sellerIDSet = false;
        private boolean hotelNameSet = false;
        // Declare and initialize the element with values
        private String[] location = null;
        private String[] period = null;
        //private String[] hotelbrand = null;
        private List hotelbrand = null;
        private int starFrom = 0;
        private int starTo = 0;
        //private CultbayRequestTypeImpl.FacilityTypeImpl.FacilityInfosTypeImpl.FacilityInfoTypeImpl[] facilityInfos = null;
        private List<CultbayRequestTypeImpl.FacilityTypeImpl.FacilityInfosTypeImpl.FacilityInfoTypeImpl> facilityInfos = null;
        //private CultbayRequestTypeImpl.CategoryTypeImpl.CategoryInfoTypeImpl[] categoryInfos = null;
        private List<CultbayRequestTypeImpl.CategoryTypeImpl.CategoryInfoTypeImpl> categoryInfos = null;
        //private String[] keywords = null;
        private List keywords = null;
        private String currency = null;
        private String auctionID = null;
        private String language = "EN"; // default English
        private String objectID = null;
        private String periodEOAFrom = null;
        private String periodEOATo = null;
        private String sellerID = null;
        private String hotelName = null;
        private StringWriter sw = new StringWriter();
        private Marshaller m = null;
        private Unmarshaller un = null;

        public CulBayXMLParser() {
                DBConnection util = new DBConnection();
                JAXBContext jc = util.getJAXBContext();
        }

        /**
         * Initialize the arrays
         */
        public void initialize() {
                System.out.println("Initialize Arrays... ");
                this.location = new String[3];
                this.period = new String[4];
                location[0] = null;
                location[1] = null;
                location[2] = null;

                period[0] = null;
                period[1] = null;
                period[2] = null;
                period[3] = null;

                this.facilityInfos = null;
                this.categoryInfos = null;
                this.keywords = null;
                this.hotelbrand = null;
                this.sellerID = null;
                this.hotelName = null;
                starFrom = 0;
                starTo = 0;
                currency = null;
                auctionID = null;
                language = "EN"; // default English
                objectID = null;

                this.periodEOAFrom = null;
                this.periodEOATo = null;

                this.locationSet = false;
                this.periodSet = false;
                this.facilitySet = false;
                this.categorySet = false;
                this.keywordSet = false;
                this.currencySet = false;
                this.languageSet = false;
                this.objectIDSet = false;
                this.auctionIDSet = false;
                this.periodEOASet = false;
                this.sellerIDSet = false;
                this.hotelNameSet = false;
                System.out.println("after initilization");
        }

        /**
         * return the error code
         */
        public String getErrorCode() {
                return this.errorCode;

        }

        /**
         * return the XML File type
         */
        public int getXMLFileTyp() {
                return this.xmlFileType;
        }

        /**
         * return true if the location is set in the XML-String
         */
        public boolean getLocationSet() {
                return this.locationSet;
        }

        /**
         * return true if the period is set in the XML-String
         */
        public boolean getPeriodSet() {
                return this.periodSet;
        }

        /**
         * return true if the facility is set in the XML-String
         */
        public boolean getFacilitySet() {
                return this.facilitySet;
        }

        /**
         * return true if the category is set in the XML-String
         */
        public boolean getCategorySet() {
                return this.categorySet;
        }

        /**
         * return true if the Keyword is set in the XML-String
         */
        public boolean getKeywordSet() {
                return this.keywordSet;
        }

        /**
         * return true if the Keyword is set in the XML-String
         */
        public boolean getLanguageSet() {
                return this.languageSet;
        }

        /**
         * return true if the Keyword is set in the XML-String
         */
        public boolean getObjectIDSet() {
                return this.objectIDSet;
        }

        /**
         * return true if the currency is set in the XML-String
         */
        public boolean getCurrencySet() {
                return this.currencySet;
        }

        /**
         * return true if the auctionID is set in the XML-String
         */
        public boolean getAuctionIDSet() {
                return this.auctionIDSet;
        }

        /**
         * return true if the auctionID is set in the XML-String
         */
        public boolean getHotelNameSet() {
                return this.hotelNameSet;
        }

        /**
         * return true if the auctionID is set in the XML-String
         */
        public boolean getSellerIDSet() {
                return this.sellerIDSet;
        }

        /**
         * return the location-values from the XML-String
         */
        public String[] getLocation() {
                return this.location;
        }

        /**
         * return the Period-values from the XML-String
         */
        public String[] getPeriod() {
                return this.period;
        }

        /**
         * return the Hotel Brand value from the XML-String
         */
        public List getHotelBrand() {
                return this.hotelbrand;
        }

        /**
         * return the Star value from the XML-String
         */
        public int getStarFrom() {
                return this.starFrom;
        }

        /**
         * return the Star value from the XML-String
         */
        public int getStarTo() {
                return this.starTo;
        }

        /**
         * return the facilities-values from the XML-String
         */
        public List<CultbayRequestTypeImpl.FacilityTypeImpl.FacilityInfosTypeImpl.FacilityInfoTypeImpl> getFacilityInfos() {
                return facilityInfos;
        }

        /**
         * return the category-values from the XML-String
         */
        public List<CultbayRequestTypeImpl.CategoryTypeImpl.CategoryInfoTypeImpl> getCategoryInfos() {
                return categoryInfos;
        }

        /**
         * return the keywords-values from the XML-String
         */
        public List getKeywords() {
                return this.keywords;
        }

        /**
         * return the currency-value from the XML-String
         */
        public String getCurrency() {
                return this.currency;
        }

        /**
         * return the auctionID-value from the XML-String
         */
        public String getAuctionID() {
                return this.auctionID;
        }

        /**
         * return the language-values from the XML-String
         */
        public String getLanguage() {
                return this.language;
        }

        /**
         * return the object-ID-values from the XML-String
         */
        public String getObjectID() {
                return this.objectID;
        }

        /**
         * return true if the period for the set for ended is set in the
         * XML-String
         */
        public boolean getperiodEOASet() {
                return this.periodEOASet;
        }

        /**
         * return the From-Period-time from the XML-String
         */
        public String getPeriodEOAFrom() {
                return this.periodEOAFrom;
        }

        /**
         * return the To-period-time from the XML-String
         */
        public String getPeriodEOATo() {
                return this.periodEOATo;
        }

        /**
         * return the sellerID from the XML-String
         */
        public String getSellerID() {
                return this.sellerID;
        }

        /**
         * return the HotelName from the XML-String
         */
        public String getHotelName() {
                return this.hotelName;
        }

        /**
         * function for parsing the incoming XML-Request
         *
         * @return boolean true if parsing was successful
         */
        public boolean parse(String request) {
                System.out.println("Input Data");
                this.initialize();
                boolean result = false;
                DBConnection util = new DBConnection();
                JAXBContext jc = util.getJAXBContext();

                // Bind the incoming XML to an XMLBeans type.
                try {
                    System.out.println("before parsing");
                        m = jc.createMarshaller();
                        un = jc.createUnmarshaller();
                        // first try to parse the file, because we don't known the type of the XML
                    System.out.println("after creating marshaller and unmarshaller objects");
                        ByteArrayInputStream bis = new ByteArrayInputStream(request.getBytes("UTF-8"));
                        System.out.println("Request==============>\n" + request);
                        if (request.contains("<Cultbay_Request")) {
                                try {
                                        System.out.println("in CultbayRequest XML Block");
                                        CultbayRequestImpl cri = (CultbayRequestImpl) un.unmarshal(bis);
                                        parseCultbayRequest(cri);
                                        xmlFileType = 1; // set the file type
                                        return true;
                                } catch (Exception ex) {
                                        System.out.println("" + ex.getMessage());
                                        errorCode = "Not a valid CultbayRequest document";
                                        return false;
                                }
                        } else if (request.contains("<Cultbay_GeoTagsRequest")) {
                                try {
                                        CultbayGeoTagsRequestImpl cri = (CultbayGeoTagsRequestImpl) un.unmarshal(bis);
                                        System.out.println("in Cultbay_GeoTagsRequest ");
                                        parseCultbayGeoTagsRequestDocument(cri);
                                        xmlFileType = 2; // set the file type
                                        return true;
                                } catch (UnmarshalException ex) {
                                        System.out.println("Exceptino in Cultbay_GeoTagsRequest");
                                        ex.printStackTrace();
                                        errorCode = "Not a valid CultbayGeoTagsRequest document";
                                        return false;
                                }
                        } else if (request.contains("<Cultbay_AuctionDetailsRQ")) {
                                try {
                                        System.out.println("in Cultbay_Details_Request");
                                        CultbayAuctionDetailsRQImpl cri = (CultbayAuctionDetailsRQImpl) un.unmarshal(bis);
                                        parseCultbayDetailsRequestDocument(cri);
                                        xmlFileType = 3; // set the file type
                                        return true;
                                } catch (UnmarshalException ex) {
                                        System.out.println("error in Cultbay_Details_Request");
                                        ex.printStackTrace();
                                        errorCode = "Not a valid CultbayAuctionDetailsRQ document";
                                        return false;
                                }
                        } else {
                                errorCode = "Unknown Document Type";
                                result = false;
                        }
                } catch (Exception e) {
                        errorCode = e.getMessage();
                        System.out.println("Error In parsing Error Code " + errorCode);
                        System.out.println(" " + e.getMessage());
                }
                return result;
        }

        /**
         * parser for the first XML-document type(Cultbay-Request) all data in
         * the XML-Request will be saved in this class and can be retrieved
         * later by using getter-functions
         *
         * @param doc
         * @return boolean true, if parsing was successful else false.
         */
        public boolean parseCultbayRequest(CultbayRequestImpl cultbayRequest) {
                System.out.println("parse CultbayRequest Document");

                // set the travels location
                locationSet = cultbayRequest.isSetLocation();
                if (locationSet) {

                        location[0] = cultbayRequest.getLocation().getCountry();
                        System.out.println("Location=====>" + location[0] + "\n");
                        if (cultbayRequest.getLocation().isSetRegion()) {
                                location[1] = cultbayRequest.getLocation().getRegion();
                        } else {
                                location[1] = "";
                        }
                        if (cultbayRequest.getLocation().isSetPlace()) {
                                location[2] = cultbayRequest.getLocation().getPlace();
                        } else {
                                location[2] = "";
                        }
                }
                // set the travels period
                periodSet = cultbayRequest.isSetPeriod();
                if (periodSet) {
                        System.out.println("periodSet " + periodSet);
                        boolean earliestArrival = cultbayRequest.getPeriod().isSetEarliestArrival();
                        if (earliestArrival) {
                                System.out.println("EarliestArrival" + cultbayRequest.getPeriod().getEarliestArrival());
                                period[0] = cultbayRequest.getPeriod().getEarliestArrival();
                        }
                        boolean latestDepartureSet = cultbayRequest.getPeriod().isSetLatestDeparture();
                        if (latestDepartureSet) {
                                period[1] = cultbayRequest.getPeriod().getLatestDeparture();
                        }
                        // set the number of persons
                        boolean personSet = cultbayRequest.getPeriod().isSetPersons();
                        //System.out.println("personSet "+personSet);
                        if (personSet) {
                                period[2] = cultbayRequest.getPeriod().getPersons().toString();
                        }
                        // set the number of nights
                        boolean accomodationSet = cultbayRequest.getPeriod().isSetAccommodations();
                        //System.out.println("accomodationSet "+accomodationSet);
                        if (accomodationSet) {
                                period[3] = cultbayRequest.getPeriod().getAccommodations().toString();
                        }
                        //System.out.println("Period Data "+period[0]+" "+period[1]+" "+period[2]+" "+period[3]);
                }
                // set the requested facilities 
                facilitySet = cultbayRequest.isSetFacility();
                if (facilitySet) {
                        // set the hotel brand 
                        boolean hotelBrand = cultbayRequest.getFacility().isSetHotelBrands();
                        if (hotelBrand) {
                                hotelbrand = cultbayRequest.getFacility().getHotelBrands().getHotelBrand();
                        }
                        // set the number of stars
                        boolean starFromSet = cultbayRequest.getFacility().isSetStarsFrom();
                        if (starFromSet) {
                                starFrom = cultbayRequest.getFacility().getStarsFrom().intValue();
                        }
                        // set the number of stars
                        boolean starToSet = cultbayRequest.getFacility().isSetStarsTo();
                        if (starToSet) {
                                starTo = cultbayRequest.getFacility().getStarsTo().intValue();
                        }
                        // set the facilities
                        boolean facilityInfosSet = cultbayRequest.getFacility().isSetFacilityInfos();
                        if (facilityInfosSet) {
                                facilityInfos = cultbayRequest.getFacility().getFacilityInfos().getFacilityInfo();
                        }
                }
                // set the requested categories
                categorySet = cultbayRequest.isSetCategory();
                if (categorySet) {
                        categoryInfos = cultbayRequest.getCategory().getCategoryInfo();
                }
                // set the keyWord keywordSet currencySet
                if (cultbayRequest.isSetKeyword()) {
                        keywordSet = true;
                        keywords = cultbayRequest.getKeyword();

                }
                // set the requested currency
                currencySet = cultbayRequest.isSetCurrency();
                if (currencySet) {
                        currency = cultbayRequest.getCurrency();
                }
                // set the requested language
                languageSet = cultbayRequest.isSetLanguage();
                if (languageSet) {
                        language = cultbayRequest.getLanguage();
                }

                // set the requested language
                objectIDSet = cultbayRequest.isSetObjectId();
                if (objectIDSet) {
                        objectID = cultbayRequest.getObjectId().toString();
                }

                // set the requested language
                sellerIDSet = cultbayRequest.isSetSellerId();
                if (sellerIDSet) {
                        sellerID = cultbayRequest.getSellerId().toString();
                        System.out.println("Seller ID Set: " + sellerID);
                }

                // set the requested language
                hotelNameSet = cultbayRequest.isSetHotelName();
                if (hotelNameSet) {
                        hotelName = cultbayRequest.getHotelName().toString();
                        System.out.println("Hotel Name Set: " + hotelName);
                }

                return true;
        }
        /* parser for the second XML

         -document type(CultbayGeoTags

         -Request) all data
         * in the XML-Request will be saved in this class and 

         can be retrieved later
         * by using getter-functions
         *
         * @param
         doc
         * @
         return boolean true, if parsing was successful

         else false.
         */

        public boolean parseCultbayGeoTagsRequestDocument(CultbayGeoTagsRequestImpl doc) {
                System.out.println("parse CultbayGeoTagsRequest Document");

                languageSet = doc.isSetLanguage();
                if (languageSet) {
                        language = doc.getLanguage();
                }
                return true;
        }

        public boolean parseCultbayDetailsRequestDocument(CultbayAuctionDetailsRQImpl cultbayDetailsRequest) {
                System.out.println("parse CultbayDetailsRequest Document");
                auctionIDSet = true;
                auctionID = cultbayDetailsRequest.getAuctionID();
                return true;
        }
        /**
         * parser for the third XML-document type(cultbayDetailsRequest) all
         * data in the XML-Request will be saved in this class and can be
         * retrieved later by using getter-functions
         *
         * @param doc
         * @return boolean true, if parsing was successful else false.
         *
         * public boolean
         * parseCultbayDetailsRequestDocument(XmlCultbayAuctionDetailsRQDocumentBean
         * doc) { System.out.println("parse CultbayDetailsRequest Document");
         * XmlCultbayAuctionDetailsRQDocumentBean.CultbayAuctionDetailsRQ
         * cultbayDetailsRequest = doc.getCultbayAuctionDetailsRQ(); // set the
         * requested language auctionIDSet = true; auctionID =
         * cultbayDetailsRequest.getAuctionID(); return true; }
         *
         * /**
         * parser for the fourth XML-document type(cultbayDetailsRequest) all
         * data in the XML-Request will be saved in this class and can be
         * retrieved later by using getter-functions
         *
         * @param doc
         * @return boolean true, if parsing was successful else false.
         *
         * public boolean
         * parseCultbayEndedAuctionsRequestDocument(XmlCultbayEndedAuctionsRequestDocumentBean
         * doc) { System.out.println("parse CultbayEndedAuctionsRequest
         * Document");
         * XmlCultbayEndedAuctionsRequestDocumentBean.CultbayEndedAuctionsRequest
         * cultbayEndedAuctionsRequest = doc.getCultbayEndedAuctionsRequest();
         * periodEOASet = cultbayEndedAuctionsRequest.isSetPeriod(); if
         * (periodEOASet) { periodEOAFrom =
         * cultbayEndedAuctionsRequest.getPeriod().getFromDate().toString();
         * periodEOATo =
         * cultbayEndedAuctionsRequest.getPeriod().getToDate().toString();
         * return true; } else { return false; } }
         */
}
