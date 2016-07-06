/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cultbay.zuzzu;

import com.cultbay.zuzzu.utils.CulBayXMLParser;
import com.cultbay.zuzzu.utils.CultBayXMLBuilder;
import com.cultuzz.util.DBConnection;
import com.cultuzz.zuzzu.ebay.impl.CultbayRequestTypeImpl;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author kondalarao
 */
public class CultBayRequestHandler {

    private CulBayXMLParser parseCulBayXML = null;
    public ArrayList<String> object_ids = null;
    public ArrayList<String> arrangement_ids = null;
    public ArrayList<String> vorlage_ids = null;
    public HashMap<String, String> objectGeoTags = null;
    // this variable is used to control the auction search process
    // because, if one of the search criteria could not be satisfied,
    // we must stop the search and return null
    public boolean stopSearch = false;
    // language
    public String language = "EN";
    public int language_id = 2;
    public CultBayXMLBuilder cultBayXMLBuilder;
    public DBConnection dbConnection;

    public CultBayRequestHandler() {
        //parseCulBayXML = new CulBayXMLParser();
    }

    /**
     * return the interface for providing access to the methods of this
     * component
     */
    public void initialize() {
        this.object_ids = null;
        this.arrangement_ids = null;
        this.vorlage_ids = null;
        this.objectGeoTags = null;
    }

    /**
     * function to handle the Cultbay-Requests
     *
     */
    public String handleRequest(int xmlFileType, CulBayXMLParser parseCulBayXML) {
        // initialize the values 
        this.initialize();
        this.object_ids = new ArrayList<String>();
        this.arrangement_ids = new ArrayList<String>();
        this.vorlage_ids = new ArrayList<String>();
        this.objectGeoTags = new HashMap<String, String>();
        this.stopSearch = false;
        this.parseCulBayXML = parseCulBayXML;
        //this.javaMySQLConnectionEbay = this.getJavaMySQLConnectionEbay();
 
        // retrieve the language 
        this.language = parseCulBayXML.getLanguage();
        try {
            System.out.println("languageeeeeeeee" + parseCulBayXML.getLanguage());
        } catch (Exception e) {
        }
        if (parseCulBayXML.getLanguage().compareToIgnoreCase("DE") == 0) {

            this.language_id = 1;
        } else {
            this.language_id = 2;
        }
        //this.cultbay2ndWSXMLBuilder.setLanguageID(this.language_id);
        String output = "";
        switch (xmlFileType) {
            case 1:
                output += handleCultbayRequest();
                break;
            case 2:
                output += handleCultbayGeoTagsRequest();
                break;
            case 3:
                output += handleCultbayDetailsRequest();
                break;
            case 4:
                output += handleCultbayEndedAuctionsRequest();
                break;
        }
        return output;
    }

    /**
     * handler for the first XML-document type(Cultbay-Request) the auction will
     * be retrieved against the input fields (search-criteria)in the
     * XML-Document.
     *
     * @return XML-Response.
     */
public String handleCultbayRequest() {
        String result = "";
        System.out.println("in Handle cultbay Request");
        cultBayXMLBuilder = new CultBayXMLBuilder();
        System.out.println("\n\nObject of parseCulBayXML" + parseCulBayXML + "\nStopSerach " + this.stopSearch);
        System.out.println("\nCountry" + parseCulBayXML.getLocation());
        if (parseCulBayXML.getObjectIDSet() == true) {
            System.out.println("in getObjectIDSet");
            this.stopSearch = false;
            this.object_ids.add(parseCulBayXML.getObjectID());
        } else {
            // open the SQLs connection

            if (parseCulBayXML.getPeriodSet() == true && this.stopSearch == false) {
                String[] period = parseCulBayXML.getPeriod();
                System.out.println("Perod Set......." + period[0]);
                this.getPeriodBasedAuctions(period[0], period[1], period[2], period[3]);
                System.out.println(" getPeriodSet Nbr of Objects found: " + this.object_ids.size());
            }
            System.out.println("Location set " + parseCulBayXML.getLocationSet());
            if (parseCulBayXML.getLocationSet() == true && this.stopSearch == false) {
                String[] location = parseCulBayXML.getLocation();
                System.out.println("before calling getLocationBasedAuctions\n" + location[0] + "   " + location[1] + "   " + location[2]);
                this.getLocationBasedAuctions(location[0], location[1], location[2]);

                System.out.println(" getLocationSet Nbr of Objects found: " + this.object_ids.size());
            }

            if (parseCulBayXML.getFacilitySet() == true && this.stopSearch == false) {
                List hotelBrand = parseCulBayXML.getHotelBrand();
                int starFrom = parseCulBayXML.getStarFrom();
                int starTo = parseCulBayXML.getStarTo();
                List<CultbayRequestTypeImpl.FacilityTypeImpl.FacilityInfosTypeImpl.FacilityInfoTypeImpl> facilityInfos = parseCulBayXML.getFacilityInfos();
                this.getFaciltyBasedAuctions(hotelBrand, starFrom, starTo, facilityInfos);
                System.out.println(" getFacilitySet Nbr of Objects found: " + this.object_ids.size());
            }

            if (parseCulBayXML.getCategorySet() == true && this.stopSearch == false) {
                List<CultbayRequestTypeImpl.CategoryTypeImpl.CategoryInfoTypeImpl> categoryInfos = parseCulBayXML.getCategoryInfos();
                this.getCategoryBasedAuctions(categoryInfos);
                System.out.println(" getCategorySet Nbr of Objects found: " + this.object_ids.size());
            }

            if (parseCulBayXML.getKeywordSet() == true && this.stopSearch == false) {
                List keywords = parseCulBayXML.getKeywords();
                this.getKeywordsBasedAuctions(keywords);
                System.out.println(" getKeywordSet Nbr of Objects found: " + this.object_ids.size());
            }

            if (parseCulBayXML.getSellerIDSet() == true && this.stopSearch == false) {
                String sellerID = parseCulBayXML.getSellerID();
                this.getSellerIDBasedAuctions(sellerID);
                System.out.println(" getSellerIDBasedAuctions Nbr of Objects found: " + this.object_ids.size());
            }

            if (parseCulBayXML.getHotelNameSet() == true && this.stopSearch == false) {
                String hotelName = parseCulBayXML.getHotelName();
                this.getHotelNameBasedAuctions(hotelName);
                System.out.println(" getHotelNameBasedAuctions Nbr of Objects found: " + this.object_ids.size());
            }

            // close the connections
            //this.javaMySQLConnectionEbay.closeEbay();
        }

        if (!stopSearch) {
            if (this.arrangement_ids.size() == 0 && this.object_ids.size() == 0 && !this.stopSearch) {
                this.getAllAuctions();
            }
            /*"before buildXML \n";*/
            if (parseCulBayXML.getPeriodSet() == true) {
                System.out.println("before bulidXML " + parseCulBayXML.getPeriod());
                String[] period = parseCulBayXML.getPeriod();
                System.out.println("VorlageID Array SIze before buildXml : "+this.vorlage_ids.size());
                result = cultBayXMLBuilder.buildXML(this.arrangement_ids, this.object_ids, this.vorlage_ids, period[2], period[3]);
            } else {
                result = cultBayXMLBuilder.buildXML(this.arrangement_ids, this.object_ids, this.vorlage_ids, null, null);
            }

            /*"returned by buildXML : \n";
             result*/
            return result;
        } else {
            result += "<error>handleCultbayRequest, No result found!</error>";
            return result;
        }
    }

    /**
     * Handler for the second XML-document type(CultbayGeoTag-Request) the
     * geoTags of an hotel will be send back, if this hotel has a auction
     * running on eBay.
     *
     * @return XML-Response.
     */
    public String handleCultbayGeoTagsRequest() {
        // Set the eBay-DB SQL Connection
        //this.javaMySQLConnectionEbay = this.getJavaMySQLConnectionEbay();
        System.out.println("in handleCultbayGeoTagsRequest()");
        dbConnection = new DBConnection();
        Connection regorConnection = dbConnection.getRegorConnection();

        cultBayXMLBuilder = new CultBayXMLBuilder();
        // SQL-Query for get all auction actually running on eBay(status = 1)
        // Auktion startdatum muss nicht zu lange in der vergangenheit liegen			
        String SQL_Query = "SELECT count(*) as number, auktion.cusebeda_objekt_id AS objectID"
                + "		   FROM ebay.auktion"
                + "			 WHERE auktion.status = 1 "
                + "               AND auktion.startdatum > date_sub(now(), interval 31 day)"
                + "				GROUP BY objectID";
        // Compute the Query 
        Statement stmt;
        ResultSet result;
        try {
            stmt = regorConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);
            while (result.next()) {
                System.out.println("==============GeoTagsObjects==================" + result.getString("objectID"));
                this.objectGeoTags.put(result.getString("objectID"), result.getString("number"));
            }
            result.close(); // close the resultSet
            stmt.close();
            regorConnection.close();
        } catch (SQLException sqlfehler) {
            System.out.println("Error: handleCultbayGeoTagsRequest Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        }

        if (this.objectGeoTags.size() > 0) {
            return cultBayXMLBuilder.buildXMLGeoTags(this.objectGeoTags);
        } else {
            return "<error>handleCultbayGeoTagsRequest, No result found!</error>";
        }
    }

    /**
     * Handler for the third XML-document type(CultbayDetail-Request) this
     * function return details information about a specified auction running on
     * eBay.
     *
     * @return XML-Response.
     */
    public String handleCultbayDetailsRequest() {
        String output = "";
        cultBayXMLBuilder = new CultBayXMLBuilder();
        System.out.println("in handleCultbayDetailsRequest");
        String auctionID = parseCulBayXML.getAuctionID();
          String zuzzuId=parseCulBayXML.getZuzzuItemId();
        boolean zuzzuCheck=false;
        if(zuzzuId!=null){
        boolean zuzzuIdChecking=this.getZuzzuIdChecking();
                zuzzuCheck=zuzzuIdChecking;
        }else
            zuzzuCheck=true;
        
        boolean auctionCheck=false;
        
        auctionCheck=cultBayXMLBuilder.checkIfRunningAuction(auctionID);
        if(!auctionCheck){
            auctionCheck=cultBayXMLBuilder.checkIfPastAuction(auctionID);
        }
        
        
        if (auctionID != null && !auctionID.equals("") && zuzzuCheck && auctionCheck) {
            // System.out.println("handleCultbayDetailsRequest...");
            output = cultBayXMLBuilder.buildXMLAuctionDetails(auctionID);
        }
        return output;
    }

    /**
     * Handler for the fourth XML-document type(CultbayGeoTag-Request)
     *
     * @return
     */
    public String handleCultbayEndedAuctionsRequest() {
        String output = "";
        String periodEOAFrom = parseCulBayXML.getPeriodEOAFrom();
        String periodEOATo = parseCulBayXML.getPeriodEOATo();
        cultBayXMLBuilder = new CultBayXMLBuilder();
        if (periodEOAFrom != null && periodEOAFrom != null) {
            System.out.println("handleCultbayEndedAuctionsRequest...");
            System.out.println("Period From " + periodEOAFrom + " Period To " + periodEOATo);
            //output = cultBayXMLBuilder.buildXMLEndedAuctions(periodEOAFrom, periodEOATo);
        }
        return output;
    }

    /*
     * Handler for the fourth XML-document type(CultbayGeoTag-Request)
     * @return
     */
    public void getPeriodBasedAuctions(String earliest_arrival, String latest_departure, String person, String accommodation) {
        String arrang_arr = "";
        // first select all auctions having a arrangement
        String SQL_QueryE = " SELECT DISTINCT vorlage.arrangement_id"
                + "		FROM ebay.auktion, ebay.vorlage"
                + "			WHERE auktion.vorlage_id = vorlage.id"
                + "				AND auktion.status = 1 "
                + "               AND auktion.startdatum > date_sub(now(), interval 31 day)"
                + "				AND vorlage.arrangement_id > '0'";

        // Query 

        dbConnection = new DBConnection();
        Connection regorConnection = dbConnection.getRegorConnection();
        Connection vegaConnection = dbConnection.getVegaConnection();
        Statement stmte;
        ResultSet resultE;
        try {
            stmte = regorConnection.createStatement();
            resultE = stmte.executeQuery(SQL_QueryE);
            int anz_ar = 0;
            // this array is helpful for the SQL-query for getting the Data once
            arrang_arr = "(";
            // go over all data and build the arrangement Array
            while (resultE.next()) {
                anz_ar++;
                if (resultE.isLast()) {
                    arrang_arr += "'" + resultE.getString(1) + "'";
                } else {
                    arrang_arr += "'" + resultE.getString(1) + "',";
                }
            }
            arrang_arr += ")";
            resultE.close();
            stmte.close();
            System.out.println("Anzahl Arrangement gefunden, die eine Auktion haben  " + anz_ar);
        } catch (Exception e) {
            System.out.println("getPeriodBasedAuctions Error Occured during receving Data from the DB");
            System.out.println(e.getMessage());
        }
        // Period Query		
        String SQL_Query = "SELECT DISTINCT arrangement.id, arrangement.cusebeda_objekt_id,"
                + "	   arrangement.ziwekategorie_id, arrangement.uebernachtungen,"
                + "	   arrangement.anreisetage"
                + "       FROM hofesoda.arrangement, hofesoda.arrangement_zeiten"
                + "			WHERE arrangement.status=1"
                + "			  AND arrangement_zeiten.arrangement_id = arrangement.id"
                + "			  AND arrangement.id IN" + arrang_arr;

        if (person != null) {
            SQL_Query += "			AND arrangement.personen = '" + person + "'";
        }

        if (accommodation != null) {
            SQL_Query += "			AND arrangement.uebernachtungen = '" + accommodation + "'";
        }

        // if earliest arrival set and latest departure not, set latest departure to a long date in the future
        if (earliest_arrival != null && latest_departure == null) {
            latest_departure = "2012-12-30";
            SQL_Query += "			AND arrangement_zeiten.gueltigvon <='" + earliest_arrival + "'";
            SQL_Query += "			AND arrangement_zeiten.gueltigbis >='" + latest_departure + "'";
        }
        // if earliest arrival is not set and latest departure set, set earliest arrival to now
        if (earliest_arrival == null && latest_departure != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date now = new Date();
            earliest_arrival = formatter.format(now).toString();
            SQL_Query += "		AND arrangement_zeiten.gueltigvon <='" + earliest_arrival + "'";
            SQL_Query += "		AND arrangement_zeiten.gueltigbis >='" + latest_departure + "'";
        }
        // if earliest arrival and latest departure are set.
        if (earliest_arrival != null && latest_departure != null) {
            SQL_Query += "		AND arrangement_zeiten.gueltigvon <='" + earliest_arrival + "'";
            SQL_Query += "		AND arrangement_zeiten.gueltigbis >='" + latest_departure + "'";
        }

        SQL_Query += "				GROUP BY ziwekategorie_id";

        // Query
        //System.out.println("SQL_Query for arrangement ======================  "+SQL_Query);
        Statement stmt;
        ResultSet result;
        try {
            stmt = vegaConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);
            // Now use the service to get a stub which implements the SDI.	
            CultChannel cultChannel = new CultChannel();
            // Map from ArrangementID to Arrangement's data
            HashMap<String, String[]> tmpArrMap = new HashMap<String, String[]>();
            while (result.next()) {
                // Set the SQL Connection 
                //System.out.println("ZimmerKategorie  : " + result.getString(3));
                String[] arrangData = {"0", "0", "0", "0"};
                arrangData[0] = result.getString(2);// Object ID
                arrangData[1] = result.getString(3);// zimmer kategorie	 			
                arrangData[2] = result.getString(4); //Accomodation	 			
                arrangData[3] = result.getString(5);// Anreise Tage

                tmpArrMap.put(result.getString(1), arrangData);
            }
            result.close(); // close the resultSet
            stmt.close();
            //regorConnection.close();

            // than select all auctions having an arrangement in another DB
            String SQL_QueryVA = " SELECT DISTINCT auktion.vorlage_id, vorlagen_arrangement.naechte "
                    + " FROM ebay.auktion, ebay.vorlagen_arrangement "
                    + " WHERE auktion.vorlage_id = vorlagen_arrangement.vorlage_id "
                    + " AND auktion.status = 1 "
                    + " AND auktion.startdatum > date_sub(now(), interval 31 day)";

            if (person != null) {
                SQL_QueryVA += "		AND	vorlagen_arrangement.personen = '" + person + "'";
            }

            if (accommodation != null) {
                SQL_QueryVA += "			AND vorlagen_arrangement.naechte = '" + accommodation + "'";
            }
            //System.out.println("Query Vorlagen-Arrangements: " + SQL_QueryVA);
            // Query 
            Statement stmt1 = regorConnection.createStatement();
            ResultSet resultVA = stmt1.executeQuery(SQL_QueryVA);
            System.out.println("Result " + resultVA);
            while (resultVA.next()) {
                // Set the SQL Connection 
                String[] arrangDataVA = {"0", "0", "0", "0"};
                arrangDataVA[0] = resultVA.getString(1);// Vorlage ID
                arrangDataVA[1] = "0";// zimmer kategorie	 			
                arrangDataVA[2] = resultVA.getString(2); //Accomodation	 			
                arrangDataVA[3] = "0";// Anreise Tage
                //System.out.println("TEST von Jenni: " + arrangDataVA[0]);
//                tmpArrMap.put(resultVA.getString(1), arrangDataVA);
                this.vorlage_ids.add(arrangDataVA[0]);
            }
            resultVA.close(); // close the resultSet
            stmt1.close();
            System.out.println("VorlageID.size : "+this.vorlage_ids.size());
            System.out.println("Anzahl Passenden Arrangement gefunden   " + tmpArrMap.size());
            if (tmpArrMap.size() > 0) {
                System.out.println("Start Cultchannel Anfrage...");
                Iterator<String> it = tmpArrMap.keySet().iterator();
                while (it.hasNext()) {
                    String akt_arr = it.next();
                    String[] arrangData = new String[4];
                    arrangData = tmpArrMap.get(akt_arr);
                    if (earliest_arrival != null && latest_departure != null && !arrangData[1].equals("0")) {
                        int distributor_id = 2;  //Cultbay.com

                        int ziwekat = Integer.parseInt(arrangData[1]); // zimmer kategorie	
                        System.out.println("Ziwekat : " + ziwekat);
                        int nights = 1; // at least one night
                        nights = Integer.parseInt(arrangData[2]);
                        System.out.println("Befroe callong CultChannel");
                        java.lang.Object[][] avail = cultChannel.askCultChannel(distributor_id, earliest_arrival, latest_departure, ziwekat, 0);
                        System.out.println("Size of CultChannel Res object  :" + avail.length);
                        int nrOfNightsOK = 0;
                        for (int r = 0; r < avail.length; r++) {
                            if (avail[r][1] != "0") {
                                nrOfNightsOK++;
                            } else {
                                nrOfNightsOK = 0;
                            }
                            if (nrOfNightsOK == nights) {
                                this.arrangement_ids.add(akt_arr);
                                if (!this.object_ids.contains(arrangData[0])) {
                                    this.object_ids.add(arrangData[0]);
                                }
                                break;
                            }
                        }
                    } else {
                        this.arrangement_ids.add(akt_arr);
                        if (!this.object_ids.contains(arrangData[0])) {
                            this.object_ids.add(arrangData[0]);
                        }
                    }
                }
            }
            regorConnection.close();
            System.out.println("Final Anzahl Arrangement " + this.arrangement_ids.size());
            //if (arrangement_ids.size() == 0) {
            if (arrangement_ids.isEmpty()) {
                this.stopSearch = true;
            }
        } catch (SQLException sqlfehler) {
            System.out.println("Error Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        } catch (Exception e) {
            System.out.println("Error Occured during Connection to CultChannelWS");
            System.out.println(e.getMessage());
        }
    }

    /**
     * return the object located in the search area
     *
     * @param country
     * @param region
     * @param place
     */
    public void getLocationBasedAuctions(String country, String region, String place) {
        dbConnection = new DBConnection();
        Connection vegaConnection = dbConnection.getVegaConnection();
        ArrayList<String> object_arr = new ArrayList<String>();
        // build the base SQL-Query
        String SQL_Query = " SELECT DISTINCT objekt.id AS id"
                + "	FROM cusebeda.objekt, cusebeda.laender"
                + "	WHERE objekt.laender_id = laender.id"
                + "	  	AND laender.bezeichnung LIKE '%" + country + "%'";
        System.out.println("in getLocationBasedAuctions\nCountry  " + country + "  Region  " + region + "Place   " + place);
        if (!region.equalsIgnoreCase("All Regions") && region != null && !region.isEmpty()) {
            String ISO_region = this.utf8_decode(region);
            SQL_Query = " SELECT DISTINCT objekt.id AS id"
                    + "	FROM cusebeda.objekt, cusebeda.laender, cusebeda.verwaltungseinheiten,"
                    + "		 cumulida.finder, cumulida.text"
                    + "	WHERE objekt.laender_id = laender.id"
                    + "	  	AND laender.bezeichnung LIKE '%" + country + "%'"
                    + "		AND objekt.verwaltungseinheiten_id = verwaltungseinheiten.id"
                    + "		AND verwaltungseinheiten.finder_id = finder.id"
                    + "		AND finder.text_id = text.id"
                    + "		AND text.cusebeda_sprache_id ='" + this.language_id + "' "
                    + "	  	AND text.text = '" + ISO_region + "' ";
        }

        if (!place.equalsIgnoreCase("All Places") && place != null && !place.isEmpty()) {
            String ISO_string = this.utf8_decode(place);
            SQL_Query += "	  	 	AND objekt.ort LIKE '%" + ISO_string + "%' ";
        }
        System.out.println("Query for getLocationBasedAuctions\n" + SQL_Query);
        // send the SQL-Query 
        Statement stmt;
        ResultSet result;
        try {
            stmt = vegaConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);
            while (result.next()) {
                object_arr.add(result.getString(1));
            }
            result.close(); // close the resultSet
            stmt.close();
            vegaConnection.close();
            System.out.println("object arrya size" + object_arr.size());
            // merge the array with the objekt_id array from the first call
            this.object_ids = this.merge(this.object_ids, object_arr);
            if (this.object_ids.size() == 0) {
                this.stopSearch = true;
            }
        } catch (SQLException sqlfehler) {
            System.out.println("Error Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        }
    }

    /**
     * return object having the defined facilities
     *
     * @param hotelBrand
     * @param stars
     * @param facilities
     */
    public void getFaciltyBasedAuctions(List hotelBrand, int starFrom, int starTo, List<CultbayRequestTypeImpl.FacilityTypeImpl.FacilityInfosTypeImpl.FacilityInfoTypeImpl> facilities) {

        dbConnection = new DBConnection();

        Connection vegaConnection = dbConnection.getVegaConnection();
        Statement stmt;
        ResultSet result;
        ArrayList<String> object_arr1 = new ArrayList<String>();
        if (hotelBrand != null) { // since more than one hotel brand
            String ISO_string = " AND ( ";
            for (int i = 0; i < hotelBrand.size(); i++) {
                if (i == hotelBrand.size() - 1) { // last element without the OR and close the (
                    ISO_string += "cusebeda.hotelmarken.bezeichnung LIKE '%" + this.utf8_decode(hotelBrand.get(i).toString()) + "%')";
                } else {
                    ISO_string += "cusebeda.hotelmarken.bezeichnung LIKE '%" + this.utf8_decode(hotelBrand.get(i).toString()) + "%' OR ";
                }
            }

            String SQL_Query = " SELECT DISTINCT hotelmarken_x_objekt.objekt_id"
                    + "	FROM cusebeda.hotelmarken_x_objekt, cusebeda.hotelmarken"
                    + "	WHERE hotelmarken_x_objekt.hotelmarken_id = hotelmarken.id	" + ISO_string;

            // System.out.println (" Query hotel brand \n"+SQL_Query);			
            // Query 

            try {
                stmt = vegaConnection.createStatement();
                result = stmt.executeQuery(SQL_Query);
                while (result.next()) {
                    object_arr1.add(result.getString(1));
                }
                result.close(); // close the resultSet
                stmt.close();
                // merge the array with the objekt_id array from the first call
                this.object_ids = this.merge(this.object_ids, object_arr1);
                System.out.println(" Objekt Array nach hotelmarken search " + this.object_ids.size());
                // if the result is empty, also break the search and return false.		
                if (this.object_ids.size() == 0) {
                    this.stopSearch = true;
                }
            } catch (SQLException sqlfehler) {
                System.out.println("Error Occured during receving Data from the DB");
                System.out.println(sqlfehler.getMessage());
            }
        }

        // stars Query
        ArrayList<String> object_arr2 = new ArrayList<String>();
        if (starFrom != 0 && !this.stopSearch) {
            String SQL_Query = "";
            if (starTo != 0) {
                SQL_Query = " SELECT DISTINCT hofesoda.daten.cusebeda_objekt_id"
                        + "	FROM hofesoda.daten"
                        + "	WHERE daten.wert >= '" + starFrom + "' "
                        + "		AND daten.wert <= '" + starTo + "' "
                        + "		AND daten.merkmal_id = '133'";
            } else {
                SQL_Query = " SELECT DISTINCT hofesoda.daten.cusebeda_objekt_id"
                        + "	FROM hofesoda.daten"
                        + "	WHERE daten.wert = '" + starFrom + "' "
                        + "		AND daten.merkmal_id = '133'";
            }
            // Query 
            try {
                stmt = vegaConnection.createStatement();
                result = stmt.executeQuery(SQL_Query);
                while (result.next()) {
                    object_arr2.add(result.getString(1));
                }
                System.out.println(" Objekt Array nach Stars search " + object_arr2.size());
                result.close(); // close the resultSet
                stmt.close();
                this.object_ids = this.merge(this.object_ids, object_arr2);
                if (this.object_ids.size() == 0) {
                    this.stopSearch = true;
                }
            } catch (SQLException sqlfehler) {
                System.out.println("Error Occured during receving Data from the DB");
                System.out.println(sqlfehler.getMessage());
            }
        }

        // facilities Query
        ArrayList<String> object_arr3 = new ArrayList<String>();
        if (facilities != null && !this.stopSearch) {

            String facilities_arr = "("; // this array is helpful for the SQL-query
            for (int j = 0; j < facilities.size(); j++) {
                if (j == facilities.size() - 1) { // last element without the comma
                    facilities_arr += "'" + facilities.get(j).getFacilityId() + "'";
                } else {
                    facilities_arr += "'" + facilities.get(j).getFacilityId() + "', ";
                }
            }
            facilities_arr += ")";

            // If we use merkmal_id IN(array), then it is not possible to choose more than one facility
            String SQL_Query = " SELECT hofesoda.daten.cusebeda_objekt_id, "
                    + "	SUM(IF(hofesoda.daten.merkmal_id IN " + facilities_arr + ", 1, 0)) as anzahlMerkmale"
                    + "	FROM hofesoda.daten"
                    + "		GROUP BY hofesoda.daten.cusebeda_objekt_id"
                    + "		HAVING  anzahlMerkmale =" + facilities.size();

            try {
                stmt = vegaConnection.createStatement();
                result = stmt.executeQuery(SQL_Query);
                while (result.next()) {
                    object_arr3.add(result.getString(1));
                }
                result.close(); // close the resultSet
                stmt.close();
                vegaConnection.close();
                this.object_ids = this.merge(this.object_ids, object_arr3);

                if (this.object_ids.size() == 0) {
                    this.stopSearch = true;
                }
            } catch (SQLException sqlfehler) {
                System.out.println("Error Occured during receving Data from the DB");
                System.out.println(sqlfehler.getMessage());
            }
        }
    }

    /**
     * return the object having the searched categories
     *
     * @param categories
     */
    public void getCategoryBasedAuctions(List<CultbayRequestTypeImpl.CategoryTypeImpl.CategoryInfoTypeImpl> categories) {
        // categories Query
        ArrayList<String> object_arr = new ArrayList<String>();
        dbConnection = new DBConnection();
        Connection vegaConnection = dbConnection.getVegaConnection();
        Statement stmt;
        ResultSet result;
        if (categories != null) {

            String categories_arr = "("; // this array is helpful for the SQL-query
            for (int j = 0; j < categories.size(); j++) {
                if (j == categories.size() - 1) { // last element without the comma
                    categories_arr += "'" + categories.get(j).getCategoryId() + "'";
                } else {
                    categories_arr += "'" + categories.get(j).getCategoryId() + "', ";
                }
            }
            categories_arr += ")";

            String SQL_Query = "SELECT arrangement.cusebeda_objekt_id,"
                    + "	SUM(IF(arrangement_x_rubrik.rubrik_id IN " + categories_arr + ", 1, 0)) as anzahlMerkmale"
                    + "		FROM hofesoda.arrangement, hofesoda.arrangement_x_rubrik"
                    + "			WHERE arrangement_x_rubrik.arrangement_id = arrangement.id"
                    + "		GROUP BY hofesoda.arrangement.cusebeda_objekt_id"
                    + "		HAVING  anzahlMerkmale =" + categories.size();

            // Query 
            try {
                stmt = vegaConnection.createStatement();
                result = stmt.executeQuery(SQL_Query);
                while (result.next()) {
                    object_arr.add(result.getString(1));
                }
                result.close(); // close the resultSet
                stmt.close();
                vegaConnection.close();
                this.object_ids = this.merge(this.object_ids, object_arr);

                if (this.object_ids.size() == 0) {
                    this.stopSearch = true;
                }
            } catch (SQLException sqlfehler) {
                System.out.println("Error Occured during receving Data from the DB");
                System.out.println(sqlfehler.getMessage());
            }
        }
    }

    public void getKeywordsBasedAuctions(List keyword) {
        ArrayList<String> object_arr = new ArrayList<String>();
        dbConnection = new DBConnection();
        Connection regorConnection = dbConnection.getRegorConnection();
        for (int i = 0; i < keyword.size(); i++) {
            String ISO_string = this.utf8_decode_keyword(keyword.get(i).toString());
            String SQL_Query = " SELECT cusebeda_objekt_id, auktion.id"
                    + "	FROM ebay.auktion"
                    + "	WHERE (auktion.text like '%" + ISO_string + "%'"
                    + "		OR auktion.ueberschrift like '%" + ISO_string + "%'"
                    + "		OR auktion.untertitel like '%" + ISO_string + "%'"
                    + "		OR auktion.ebayueberschrift like '%" + ISO_string + "%')"
                    + "		AND auktion.status = 1"
                    + "        AND auktion.startdatum > date_sub(now(), interval 31 day)";

            // Query 
            Statement stmt;
            ResultSet result;
            try {
                stmt = regorConnection.createStatement();
                result = stmt.executeQuery(SQL_Query);
                while (result.next()) {
                    object_arr.add(result.getString(1));
                }
                result.close(); // close the resultSet
                stmt.close();
                regorConnection.close();
                this.object_ids = this.merge(this.object_ids, object_arr);
                if (this.object_ids.size() == 0) {
                    this.stopSearch = true;
                }
            } catch (SQLException sqlfehler) {
                System.out.println("Error Occured during receving Data from the DB");
                System.out.println(sqlfehler.getMessage());
            }
        }
    }

    /**
     * return auction for a specified seller
     *
     * @param sellerID
     */
    public void getSellerIDBasedAuctions(String sellerID) {
        String ISO_string = this.utf8_decode(sellerID);
        ArrayList<String> object_arr = new ArrayList<String>();
        dbConnection = new DBConnection();
        Connection regorConnection = dbConnection.getRegorConnection();
        String SQL_Query = " SELECT cusebeda_objekt_id"
                + "	FROM ebay.ebaydaten"
                + "	WHERE ebaydaten.ebayname = '" + ISO_string + "'";

        // Query 
        Statement stmt;
        ResultSet result;
        try {
            stmt = regorConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);
            while (result.next()) {
                object_arr.add(result.getString(1));
            }
            result.close(); // close the resultSet
            stmt.close();
            regorConnection.close();
            this.object_ids = this.merge(this.object_ids, object_arr);
            if (this.object_ids.size() == 0) {
                this.stopSearch = true;
            }
        } catch (SQLException sqlfehler) {
            System.out.println("Error Occured during receving Data from the DB(getSellerIDBasedAuctions)");
            System.out.println(sqlfehler.getMessage());
        }
    }

    /**
     *
     * @param hotelName
     */
    public void getHotelNameBasedAuctions(String hotelName) {
        String ISO_string = this.utf8_decode(hotelName);
        ArrayList<String> object_arr = new ArrayList<String>();
        dbConnection = new DBConnection();
        Connection vegaConnection = dbConnection.getVegaConnection();
        String SQL_Query = " SELECT id"
                + "	FROM cusebeda.objekt"
                + "	WHERE objekt.bezeichnung = '" + ISO_string + "'"
                + "	   OR objekt.bezeichnungintern = '" + ISO_string + "'"
                + "	   OR objekt.bezeichnung2 = '" + ISO_string + "'";

        // Query 
        Statement stmt;
        ResultSet result;
        try {
            stmt = vegaConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);
            while (result.next()) {
                object_arr.add(result.getString(1));
            }
            result.close(); // close the resultSet
            stmt.close();
            vegaConnection.close();
            this.object_ids = this.merge(this.object_ids, object_arr);
            if (this.object_ids.size() == 0) {
                this.stopSearch = true;
            }
        } catch (SQLException sqlfehler) {
            System.out.println("Error Occured during receving Data from the DB(getHotelNameBasedAuctions)");
            System.out.println(sqlfehler.getMessage());
        }
    }

    /**
     * All Auctions
     */
    public void getAllAuctions() {
        dbConnection = new DBConnection();
        Connection regorConnection = dbConnection.getRegorConnection();
        String SQL_Query = "SELECT DISTINCT cusebeda_objekt_id"
                + "		FROM ebay.auktion "
                + "			WHERE auktion.status = 1 ";
//                + "              AND auktion.startdatum > date_sub(now(), interval 31 day)"
//                + "			   GROUP BY cusebeda_objekt_id";




        // Query 
        Statement stmt;
        ResultSet result;
        try {
            stmt = regorConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);
            while (result.next()) {
                //System.out.println(":::::::::::::::::::::::::::::::::All auktions objects::::::::" + result.getString("cusebeda_objekt_id"));
                this.object_ids.add(result.getString("cusebeda_objekt_id"));
            }
            result.close(); // close the resultSet
            stmt.close();
            regorConnection.close();
            if (this.object_ids.size() == 0) {
                this.stopSearch = true;
            }
        } catch (SQLException sqlfehler) {
            System.out.println("====================Error Occured during receving Data from the DB for all offers ==========================");
            System.out.println(sqlfehler.getMessage());
        }
    }

    /**
     * Merge the two inputs array to obtains a new array, which is the
     * intersection of the two input parameters
     *
     * @param arrayList1
     * @param arrayList2
     * @return
     */
    public ArrayList<String> merge(ArrayList<String> arrayList1, ArrayList<String> arrayList2) {
        ArrayList<String> result = new ArrayList<String>();
        if (arrayList1.size() == 0) {
            return arrayList2;
        } else if (arrayList2.size() == 0) {
            return arrayList2; // if the second List is empty, return the second because it means not match.
        } else {
            if (arrayList1.size() < arrayList2.size()) {
                for (int i = 0; i < arrayList1.size(); i++) {
                    if (arrayList2.contains(arrayList1.get(i))) {
                        result.add(arrayList1.get(i));
                    }
                }
            } else {
                for (int i = 0; i < arrayList2.size(); i++) {
                    if (arrayList1.contains(arrayList2.get(i))) {
                        result.add(arrayList2.get(i));
                    }
                }
            }
            return result;
        }
    }

    /**
     * decode utf8 String into ISO-String
     *
     * @param utf8_string
     * @return
     */
    public String utf8_decode(String utf8_string) {
        String ISO_string = utf8_string.replaceAll("'", "\\'");
        try {
            byte[] bytes = ISO_string.getBytes("UTF-8");
            String s = new String(bytes, "UTF-8");
            byte[] winCode = s.getBytes("ISO-8859-1");
            ISO_string = new String(winCode, "UTF-8");
        } catch (Exception e) {
            System.out.println(" UnsupportedEncodingException " + e.getMessage());
        }
        //System.out.println (" ISO String " +ISO_string);
        return ISO_string;
    }

    public String utf8_decode_keyword(String utf8_string) {
        String ISO_string = utf8_string.replaceAll("'", "\\\\'");
        try {
            byte[] bytes = ISO_string.getBytes("UTF-8");
            String s = new String(bytes, "UTF-8");
            byte[] winCode = s.getBytes("ISO-8859-1");
            ISO_string = new String(winCode, "UTF-8");
        } catch (Exception e) {
            System.out.println(" UnsupportedEncodingException " + e.getMessage());
        }
        //System.out.println (" ISO String " +ISO_string);
        return ISO_string;
    }
    
    public boolean getZuzzuIdChecking(){
        boolean zuzzuIdCheck=false;
       
        String zuzzuid=parseCulBayXML.getZuzzuItemId();
        String ebayid=parseCulBayXML.getAuctionID();
                 System.out.println("Zuzzu id "+zuzzuid);
            if(!zuzzuid.equals("")){
                
                 Connection zuzzuConnection = dbConnection.getZuzzuConnection();
        String SQL_Query = "select oxe.itemid from zuzzu.offer_x_ebayitemid oxe where oxe.ebayitemid="+ebayid+" and oxe.itemid='"+zuzzuid+"limit 1";

        // Query 
        Statement stmt;
        ResultSet result;
        try {
            int count=0;
            System.out.println("This is at zuzzuid checking");
            stmt = zuzzuConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);
            while (result.next()) {
                count++;
                System.out.println("This is zuzzu id "+result.getString("itemid")+"count"+count);
              zuzzuIdCheck=true;
            }
            if(count>0){
                zuzzuIdCheck=true;
            }else
                zuzzuIdCheck=false;
            
            result.close(); // close the resultSet
            stmt.close();
            zuzzuConnection.close();
           
        } catch (SQLException sqlfehler) {
            System.out.println("====================Error Occured during receving Data from the DB checking zuzzuid ==========================");
            System.out.println(sqlfehler.getMessage());
            zuzzuIdCheck=false;
        }
            }else{
                zuzzuIdCheck=true;
            }
            
       
        
        return zuzzuIdCheck;
    }
    
    
}
