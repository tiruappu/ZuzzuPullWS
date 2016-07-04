/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cultbay.zuzzu.utils;


import com.cultuzz.util.DBConnection;
import com.cultuzz.zuzzu.ebay.impl.CultbayAuctionDetailsRSImpl;
import com.cultuzz.zuzzu.ebay.impl.CultbayAuctionDetailsRSTypeImpl;
import com.cultuzz.zuzzu.ebay.impl.CultbayGeoTagsResponseImpl;
import com.cultuzz.zuzzu.ebay.impl.CultbayGeoTagsResponseTypeImpl;
import com.cultuzz.zuzzu.ebay.impl.CultbayResponseImpl;
import com.cultuzz.zuzzu.ebay.impl.CultbayResponseTypeImpl;
import com.sun.xml.bind.marshaller.DataWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author kondalarao
 */
public class CultBayXMLBuilder {

    public int language = 2;
    public String XML_output = "";
    // interfaces to the connections
    public DBConnection dbConnection;
    // Map from ObjectID to Object's data
    public HashMap<String, String[]> objectMap = null;
    // Map from arrangement ID to arrangement's data
    public HashMap<String, String[]> arrangMap = null;
    // Map from vorlage ID to vorlage's data
    public HashMap<String, String[]> vorlageMap = null;
    // Map from ObjectID to Object's star
    public HashMap<String, String> objectStars = null;
    // Map from auction ID to auction's data
    public HashMap<String, String[]> auctionsMap = null;
    // Map from auction ID to price range
    public HashMap<String, String[]> priceRangeMap = null;
    // Map from site ID to Currency
    public HashMap<String, String> currencyMap = null;
    // Map from ObjectID to Geo-coordinates
    public HashMap<String, String[]> geoTagsMap = null;
    // Map from Objekt ID to Hotel-brand
    public HashMap<String, String> hotelBrandMap = null;
    private StringWriter sw = null;
    private Marshaller m = null;
    private Unmarshaller un = null;
    private String htmlTemplate = null;
    private String itemSpecifics = null;
    private String PictureURLS[] = null;
    private List pictureUrls = null;
    private String galaryURL = null;
    private DataWriter dw = null;
    private PrintWriter pw = null;

    public CultBayXMLBuilder() {
        DBConnection util = new DBConnection();
        JAXBContext jc = util.getJAXBContext();
        this.sw = new StringWriter();
        this.pw = new PrintWriter(this.sw);
        this.dw = new DataWriter(this.pw, "UTF-8", new JaxbCharacterEscapeHandler());
        try {
            this.m = jc.createMarshaller();
            this.un = jc.createUnmarshaller();
        } catch (JAXBException je) {
            System.out.println("\n\n\n\n\nErroo in  CultBayXMLBuilder " + je.getErrorCode() + "\n\n\n\n" + je.getMessage());
        }
    }

    public void setLanguageID(int i) {
        this.language = i;
    }

    /**
     * function to retrieve Auction Data from the Data Base going from
     * Object_IDs or arrangement_IDs
     *
     */
    public String buildXML(ArrayList<String> arrangement_ids,
            ArrayList<String> object_ids, ArrayList<String> vorlage_ids, String person, String accomodation) {
        
        System.out.println("List of arrangement_ids count===>"+arrangement_ids.size()+"\n Object_ids==>"+object_ids.size()+"\n vorlage_ids==>"+vorlage_ids.size());
        System.out.println("Person==>"+person+"\n accomodation==>"+accomodation);
        
        // Initialize the Maps
        this.objectMap = new HashMap<String, String[]>();
        this.arrangMap = new HashMap<String, String[]>();
        this.vorlageMap = new HashMap<String, String[]>();
        this.auctionsMap = new HashMap<String, String[]>();
        this.priceRangeMap = new HashMap<String, String[]>();
        this.currencyMap = new HashMap<String, String>();
        this.geoTagsMap = new HashMap<String, String[]>();
        // this.currentBidMap = new HashMap<String, String>();

        // Set the SQLs Connection
        dbConnection = new DBConnection();
        Connection vegaConnection = dbConnection.getVegaConnection();
        Connection regorConnection = dbConnection.getRegorConnection();
        

        // set the currencies
        this.setCurrencies();

        // build the response xml
        CultbayResponseImpl cultbayResponse = new CultbayResponseImpl();
        CultbayResponseImpl.HotelPackagesTypeImpl hotelPackages = new CultbayResponseImpl.HotelPackagesTypeImpl();
        CultbayResponseImpl.HotelPackagesTypeImpl.HotelPackageTypeImpl hotelPackage = null;
        System.out.println("Arrangement size " + arrangement_ids.size());
        System.out.println("Test von Jenni - Personen: " + person);
        System.out.println("Test von Jenni - Accomondation: " + accomodation);

        // 1. Build the response XML from the arrangement ID
        if (arrangement_ids.size() > 0) {
            // first get the objects and arrangements data once (time saving)
            this.getObjectDataMap(object_ids);
            this.getArrangDataMap(arrangement_ids);

            // this array is helpful for the SQL-query for getting the Data once
            String arrang_arr = "(";
            for (int j = 0; j < arrangement_ids.size(); j++) {
                if (j == arrangement_ids.size() - 1) { // last element without
                    // the comma
                    arrang_arr += "'" + arrangement_ids.get(j) + "'";
                } else {
                    arrang_arr += "'" + arrangement_ids.get(j) + "', ";
                }
            }
            arrang_arr += ")";

            String SQL_Query = " SELECT DISTINCT auktion.id, auktion.ebayitemid, auktion.ebaysiteid,"
                    + "		auktion.cusebeda_objekt_id AS objectID, auktion.startdatum, "
                    + "		auktion.dauer, DATE_ADD(auktion.startdatum, INTERVAL auktion.dauer DAY) as endDate, "
                    + "		auktion.startpreis,auktion.retailprice, auktion.currentbid, auktion.ebaysofortundneupreis, auktion.untertitel, "
                    + "		auktion.ebayueberschrift, auktion.ebayueberschrifthighlight, auktion.anzahlgebote,"
                    + "		auktion.ebaygaleriebild, auktion.ebaysofortkauf,"
                    + "		ebaydaten.galeriebild_id, vorlage.arrangement_id,ebaydaten.ebayname,auktion.AuctionMasterTypeID "
                    + "		FROM ebay.auktion LEFT JOIN ebay.ebaydaten ON auktion.cusebeda_objekt_id = ebaydaten.cusebeda_objekt_id, ebay.vorlage"
                    + "			WHERE auktion.vorlage_id = vorlage.id"
                    + "				AND auktion.cusebeda_objekt_id = 122 "
                    + "				AND auktion.cusebeda_objekt_id = vorlage.cusebeda_objekt_id"
                    + "				AND vorlage.arrangement_id IN"
                    + arrang_arr
                    + "				AND auktion.status = 1 ";

            System.out.println("Query for all arrangement auctions arr============================" + SQL_Query);
            try {
                Statement stmt = regorConnection.createStatement();
                ResultSet result = stmt.executeQuery(SQL_Query);
                // go over all data and build the response XML
                int nrOfAuct = 0;
                while (result.next()) {
                    if (this.objectMap.containsKey(result.getString("objectID"))) {
                        nrOfAuct++;
                        String[] objectData = new String[5];
                        String[] arrangData = new String[4];
                        String[] priceRange = new String[2];
                        objectData = this.objectMap.get(result.getString("objectID"));
                        arrangData = this.arrangMap.get(result.getString("arrangement_id"));
                        priceRange = this.getAuctionPriceRange(result.getString("id"));
                        int startPreis = result.getInt("startpreis");
                        int currentBid = result.getInt("currentbid");
                        int retailPrice = result.getInt("retailprice");
                        int ebaysofortundneupreisInt = 0;
                        int auctionMasterTypeID = result.getInt("AuctionMasterTypeID");
                        try {
                            String ebaysofortundneupreis = result.getString("ebaysofortundneupreis");
                            if (ebaysofortundneupreis != null && ebaysofortundneupreis.length() > 0) {
                                ebaysofortundneupreisInt = result.getInt("ebaysofortundneupreis");
                            }
                        } catch (SQLException e) {
                            System.out.println("SQLException for ebaysofortundneupreis");
                            System.out.flush();
                            e.printStackTrace();
                        }

                        if (startPreis == 0) {
                            startPreis = ebaysofortundneupreisInt;
                        }
                        if (currentBid < 1) {
                            currentBid = startPreis;
                        }
                        String image = this.getImageURLByXml(result.getString("id"));
                        int soldnumbers = 0;
                        String SQL_Soldnumbers = "SELECT sum(quantity_purchased) as soldnumbers FROM ebay.transaction where ebayitemid = " + result.getString("ebayitemid");
                        try {
                            Statement stmt_soldnumbers = regorConnection.createStatement();
                            ResultSet result_soldnumbers = stmt_soldnumbers.executeQuery(SQL_Soldnumbers);
                            if (result_soldnumbers.next()) {
                                soldnumbers = result_soldnumbers.getInt("soldnumbers");
                            } else {
                                soldnumbers = 0;
                            }
                            result_soldnumbers.close();
                        } catch (Exception e) {
                            System.out.println("soldnumbers Exception : " + e.getMessage());
                        }

                        hotelPackage = new CultbayResponseTypeImpl.HotelPackagesTypeImpl.HotelPackageTypeImpl();
                        //System.out.println("Item ID for arng arr:::============================" + result.getString("ebayitemid"));
                        hotelPackage.setPackageCode(result.getString("ebayitemid"));
                        hotelPackage.setPackagename(result.getString("ebayueberschrift"));
                        hotelPackage.setPackageIdentifier(this.getAuctionMarket(result.getString("ebaysiteid")));
                        hotelPackage.setPackageDesc(result.getString("untertitel"));
                        hotelPackage.setHotelName(objectData[0]);
                        hotelPackage.setAccomodation(new BigInteger(arrangData[0]));
                        hotelPackage.setNoOfPerson(new BigInteger(arrangData[1]));
                        hotelPackage.setTypeOfRoom(arrangData[2]);
                        hotelPackage.setCatering(arrangData[3]);
                        if (currentBid > 0) {
                            hotelPackage.setCurrentBid(new BigDecimal(currentBid));
                        }
                        hotelPackage.setCountry(objectData[1]);
                        hotelPackage.setPlace(objectData[2]);
                        hotelPackage.setRegion(objectData[3]);
                        // setBookItNow only if the arrangement is set
                        if (result.getInt("arrangement_id") > 0) {
                            hotelPackage.setBookItNow(new BigInteger(objectData[4]));
                        } else {
                            hotelPackage.setBookItNow(new BigInteger("0"));
                        }
                        hotelPackage.setUrl(this.getAuctionURL(result.getString("ebaysiteid")) + result.getString("ebayitemid"));

                        if (auctionMasterTypeID == 1) {
                            hotelPackage.setStartBidPrice(new BigDecimal(startPreis));
                        } else if (startPreis > 0) {
                            hotelPackage.setBuyNowPrice(new BigDecimal(startPreis));
                        }

                        hotelPackage.setRetailPrice(new BigDecimal(retailPrice));
                        if (result.getString("ebaysofortkauf") != null
                                && result.getString("ebaysofortkauf").length() != 0 && auctionMasterTypeID == 1 && currentBid == 0) {
                            hotelPackage.setBuyNowPrice(new BigDecimal(result.getString("ebaysofortkauf")));
                        } else {
                            hotelPackage.setBuyNowPrice(new BigDecimal(0));
                        }

                        hotelPackage.setPriceRange(priceRange[0] + " " + priceRange[1]);

                        hotelPackage.setCurrency(this.currencyMap.get(result.getString("ebaysiteid")));
                        hotelPackage.setImage(image);
                        hotelPackage.setStartDate(result.getString("startdatum"));
                        hotelPackage.setEndDate(result.getString("endDate"));
                        hotelPackage.setDurationDays(new BigInteger(result.getString("dauer")));
                        hotelPackage.setObjectID(result.getString("objectID"));
                        if (result.getString("anzahlgebote") != null) {
                            hotelPackage.setBids(new BigInteger(result.getString("anzahlgebote")));
                        } else {
                            hotelPackage.setBids(new BigInteger("0"));
                        }
                        if (result.getString("ebayname") != null && result.getString("ebayname") != "null") {
                            hotelPackage.setSellerId(result.getString("ebayname"));
                        }
                        hotelPackage.setSoldnumbers(soldnumbers);
                        hotelPackage.setProductId(BigInteger.valueOf(result.getInt("arrangement_id")));
                        hotelPackage.setZuzzuItemId(this.getZuzzuItemid(result.getString("ebayitemid")));
                    }
                    hotelPackages.getHotelPackage().add(hotelPackage);
                }

                System.out.println("Nbr Of auctions found(1): " + nrOfAuct);
                result.close();
                stmt.close();
            } catch (Exception sqlfehler) {
                System.out.println("buildXML: Error Occured during receving Data from the DB");
                sqlfehler.printStackTrace();
            }
        }

        // 2. Build the response XML from the Object ID
        if (arrangement_ids.size() == 0 && object_ids.size() > 0) {
            String obj_arr = "("; // this array is helpful for the SQL-query
            for (int j = 0; j < object_ids.size(); j++) {
                if (j == object_ids.size() - 1) {
                    obj_arr += "'" + object_ids.get(j) + "'";
                } else {
                    obj_arr += "'" + object_ids.get(j) + "', ";
                }
            }

            obj_arr += ")";


            String SQL_Query = "SELECT 	DISTINCT auktion.id, auktion.ebayitemid, auktion.ebaysiteid,"
                    + "		auktion.cusebeda_objekt_id AS objectID, auktion.startdatum, "
                    + "		auktion.dauer, DATE_ADD(auktion.startdatum, INTERVAL auktion.dauer DAY) as endDate, "
                    + "		auktion.startpreis, auktion.retailprice, auktion.currentbid, auktion.untertitel, "
                    + "		auktion.ebayueberschrift, auktion.ebayueberschrifthighlight, auktion.anzahlgebote,"
                    + "		auktion.ebaygaleriebild, auktion.ebaysofortkauf, vorlage.arrangement_id,"
                    + "		ebaydaten.galeriebild_id, ebaydaten.ebayname, auktion.ebaysofortundneupreis, auktion.vorlage_id, auktion.AuctionMasterTypeID "
                    + "		FROM ebay.auktion LEFT JOIN ebay.ebaydaten ON auktion.cusebeda_objekt_id = ebaydaten.cusebeda_objekt_id , ebay.vorlage"
                    + "			WHERE 	"
                    + "                                                                                 auktion.cusebeda_objekt_id = 122 AND "
                    + "				auktion.cusebeda_objekt_id IN "
                    + obj_arr
                    + "				AND auktion.status = 1 "
                    + "				AND vorlage.id = auktion.vorlage_id";
            System.out.println("Query for all object auctions objectarray============================" + SQL_Query);
            // System.out.println("SQL_Query :\n"+SQL_Query+"\n");
            // System.out.flush();

            // Query
            try {
                Statement stmt = regorConnection.createStatement();
                ResultSet result = stmt.executeQuery(SQL_Query);
                // define temporally array to save Data into, this step is done
                // because we want to get all Data in one time from
                // our Data base. that is time saving
                // save all arrangement ID, because we want to retrieve all
                // arrangement Data once from the Data base
                ArrayList<String> arragListtmp = new ArrayList<String>();
                ArrayList<String> auctiontmp = new ArrayList<String>();
                ArrayList<String> vorlListtmp = new ArrayList<String>();
                // first save the auction data in array
                while (result.next()) {
                    if (!(result.getString("arrangement_id").equals("0"))
                            && result.getString("arrangement_id") != null) {
                        arragListtmp.add(result.getString("arrangement_id"));
                    } else {
                        vorlListtmp.add(result.getString("vorlage_id"));
                    }
                    auctiontmp.add(result.getString("id"));
                    // auction Data
                    int startPreisInt = result.getInt("startpreis");
                    String startPreis = result.getString("startpreis");
                    int currentBidInt = result.getInt("currentbid");
                    String currentBidS = result.getString("currentbid");
                    int auctionMasterTypeID = result.getInt("AuctionMasterTypeID");
                    String ebaysofortkauf = "0";
                    try {
                        String ebaysofortundneupreis = result.getString("ebaysofortundneupreis");
                        ebaysofortkauf = result.getString("ebaysofortkauf");
                        if (startPreisInt == 0 && ebaysofortundneupreis.length() > 0) {
                            startPreis = ebaysofortundneupreis;
                        }
                    } catch (Exception e) {
                        System.out.println("SQLException for ebaysofortundneupreis");
                        System.out.flush();
                        e.printStackTrace();
                    }

                    if (currentBidInt < 1) {
                        currentBidS = startPreis;
                    }

                    int soldnumbers = 0;
                    String SQL_Soldnumbers = "SELECT sum(quantity_purchased) as soldnumbers FROM ebay.transaction where ebayitemid = " + result.getString("ebayitemid");
                    try {
                        Statement stmt_soldnumbers = regorConnection.createStatement();
                        ResultSet result_soldnumbers = stmt_soldnumbers.executeQuery(SQL_Soldnumbers);
                        if (result_soldnumbers.next()) {
                            soldnumbers = result_soldnumbers.getInt("soldnumbers");
                        } else {
                            soldnumbers = 0;
                        }
                        //System.out.println("sold numbers=========="+soldnumbers);
                        result_soldnumbers.close();
                    } catch (Exception e) {
                        System.out.println("soldnumbers Exception : " + e.getMessage());
                    }

                    //String[] auctionData = new String[18];
                    String[] auctionData = new String[19];
                    //     System.out.println("Item ID for obj arr:::============================" + result.getString("ebayitemid"));
                    auctionData[0] = result.getString("ebayitemid");
                    auctionData[1] = result.getString("ebayueberschrift");
                    auctionData[2] = "0";
                    if (auctionMasterTypeID == 1) {
                        auctionData[2] = startPreis;
                    }

                    auctionData[3] = "0";
                    if (auctionMasterTypeID == 4) {
                        auctionData[3] = startPreis;
                        //System.out.println("AD3 else if : " + auctionData[3]);
                    } else if (ebaysofortkauf != null && ebaysofortkauf.length() != 0 && currentBidInt == 0) {
                        auctionData[3] = ebaysofortkauf;
                    } else {
                        auctionData[3] = "0";
                    }
                    auctionData[4] = this.getImageURLByXml(result.getString("id"));

                    auctionData[5] = result.getString("startdatum");
                    auctionData[6] = result.getString("dauer");
                    auctionData[7] = result.getString("arrangement_id");
                    auctionData[8] = result.getString("objectID");
                    auctionData[9] = this.currencyMap.get(result.getString("ebaysiteid"));
                    if (result.getString("ebayname") != null && result.getString("ebayname") != "null") {
                        auctionData[10] = result.getString("ebayname");
                    } else {
                        auctionData[10] = "";
                    }

                    auctionData[11] = currentBidS;
                    auctionData[12] = result.getString("endDate");
                    auctionData[13] = result.getString("untertitel");
                    if (auctionData[13] == null) {
                        auctionData[13] = "";
                    }
                    auctionData[14] = result.getString("ebaysiteid");
                    if (result.getString("anzahlgebote") != null) {
                        auctionData[15] = result.getString("anzahlgebote");
                    } else {
                        auctionData[15] = "0";
                    }
                    int retailPrice = result.getInt("retailprice");
                    auctionData[16] = retailPrice + "";
                    auctionData[17] = result.getString("vorlage_id");
                    auctionData[18] = soldnumbers + "";
                    // save the auction data
                    this.auctionsMap.put(result.getString("id"), auctionData);
                    //hotelPackages.getHotelPackage().add(hotelPackage);
                }
                // close the result Set
                result.close();

                if (this.auctionsMap.size() > 0) {
                    System.out.println("Nbr Of auctions found(2): " + this.auctionsMap.size());
                    // first get the objects data and arrangement data (reducing
                    // the number of call to one call)
                    this.getObjectDataMap(object_ids);
                    if (arragListtmp.size() > 0) {
                        this.getArrangDataMap(arragListtmp);
                    }
                    this.getVorlageDataMap(vorlListtmp);
                    // System.out.println("after getArrangDataMap");
                    // System.out.flush();
                    this.getAuctionPriceRangeMap(auctiontmp);
                    // System.out.println("after getAuctionPriceRangeMap");
                    // System.out.flush();
                    // this.getCurrentBidMap(auctiontmp);
                    Iterator<String> it = this.auctionsMap.keySet().iterator();
                    while (it.hasNext()) {
                        String akt_auktion = it.next();

                        String[] auctionData = this.auctionsMap.get(akt_auktion);

                        String[] objectData = {"0", "0", "0", "0", "0"};
                        if (this.objectMap.containsKey(auctionData[8])) {
                            objectData = this.objectMap.get(auctionData[8]);
                        }
                        String currentBid = auctionData[11];
                        String[] priceRange = new String[2];
                        priceRange[0] = "";
                        priceRange[1] = "";
                        if (this.priceRangeMap.containsKey(akt_auktion)) {
                            priceRange = this.priceRangeMap.get(akt_auktion);
                        } else {
                            //priceRange[0] = "http://www.cultbay.com/new_button_white1.gif";
                        }
                        // Initialize the arrangement Data because not all
                        // auction are connect to arrangement
                        String[] arrangData = {"0", "0", "0", "0"};
                        if (this.arrangMap.containsKey(auctionData[7])) {
                            arrangData = this.arrangMap.get(auctionData[7]);
                        }
                        if (this.vorlageMap.containsKey(auctionData[17])) {
                            arrangData = this.vorlageMap.get(auctionData[17]);
                        }
                        // build the XML Response
                        hotelPackage = new CultbayResponseTypeImpl.HotelPackagesTypeImpl.HotelPackageTypeImpl();
                        hotelPackage.setPackageCode(auctionData[0]);
                        hotelPackage.setPackagename(auctionData[1]);
                        hotelPackage.setPackageIdentifier(this.getAuctionMarket(auctionData[14]));
                        hotelPackage.setPackageDesc(auctionData[13]);
                        hotelPackage.setHotelName(objectData[0]);
                        hotelPackage.setAccomodation(new BigInteger(arrangData[0]));

                        hotelPackage.setNoOfPerson(new BigInteger(arrangData[1]));
                        hotelPackage.setTypeOfRoom(arrangData[2]);
                        hotelPackage.setCatering(arrangData[3]);
                        if (Double.valueOf(currentBid) > 0.0) {
                            hotelPackage.setCurrentBid(new BigDecimal(currentBid));
                        }
                        //hotelPackage.setRetailPrice(new BigDecimal(auctionData[16]));
                        hotelPackage.setCountry(objectData[1]);
                        hotelPackage.setPlace(objectData[2]);
                        hotelPackage.setRegion(objectData[3]);

                        int arrg_id = new Integer(auctionData[7]).intValue();
                        if (arrg_id > 0) {
                            hotelPackage.setBookItNow(new BigInteger(
                                    objectData[4]));
                        } else {
                            hotelPackage.setBookItNow(new BigInteger("0"));
                        }
                        hotelPackage.setUrl(this.getAuctionURL(auctionData[14])
                                + auctionData[0]);
                        hotelPackage.setStartBidPrice(new BigDecimal(
                                auctionData[2]));
                        hotelPackage.setBuyNowPrice(new BigDecimal(
                                auctionData[3]));
                        hotelPackage.setPriceRange(priceRange[0] + " "
                                + priceRange[1]);
                        hotelPackage.setCurrency(auctionData[9]);
                        hotelPackage.setImage(auctionData[4]);
                        hotelPackage.setStartDate(auctionData[5]);
                        hotelPackage.setEndDate(auctionData[12]);
                        hotelPackage.setDurationDays(new BigInteger(auctionData[6]));

                        hotelPackage.setBids(new BigInteger(auctionData[15]));
                        hotelPackage.setSellerId(auctionData[10]);
                        hotelPackage.setObjectID(auctionData[8]);
                        hotelPackage.setSoldnumbers(Integer.parseInt(auctionData[18]));
                        hotelPackage.setZuzzuItemId(this.getZuzzuItemid(auctionData[0]));
                        hotelPackage.setProductId(BigInteger.valueOf(this.getProductId(auctionData[0])));
                        
                        
                        hotelPackages.getHotelPackage().add(hotelPackage);
                    }
                    // System.out.println("after while");
                    // System.out.flush();
                }
            } catch (Exception sqlfehler) {
                System.out.println("buildXML: Error Occured during receving Data from the DB");
                sqlfehler.printStackTrace();
            }
        }

        if (vorlage_ids.size() > 0) {
            System.out.println("For Template related accomodations");
            String vorlage_arr = "(";
            for (int j = 0; j < vorlage_ids.size(); j++) {
                if (j == vorlage_ids.size() - 1) {
                    vorlage_arr += "'" + vorlage_ids.get(j) + "'";
                } else {
                    vorlage_arr += "'" + vorlage_ids.get(j) + "', ";
                }
            }
            vorlage_arr += ")";


            String SQL_Query = "SELECT 	DISTINCT auktion.id, auktion.ebayitemid, auktion.ebaysiteid,"
                    + "		auktion.cusebeda_objekt_id AS objectID, auktion.startdatum, "
                    + "		auktion.dauer, DATE_ADD(auktion.startdatum, INTERVAL auktion.dauer DAY) as endDate, "
                    + "		auktion.startpreis, auktion.retailprice, auktion.currentbid, auktion.untertitel, "
                    + "		auktion.ebayueberschrift, auktion.ebayueberschrifthighlight, auktion.anzahlgebote,"
                    + "		auktion.ebaygaleriebild, auktion.ebaysofortkauf, auktion.vorlage_id,"
                    + "		ebaydaten.galeriebild_id, ebaydaten.ebayname, auktion.ebaysofortundneupreis, auktion.AuctionMasterTypeID "
                    + "		FROM ebay.auktion LEFT JOIN ebay.ebaydaten ON auktion.cusebeda_objekt_id = ebaydaten.cusebeda_objekt_id, ebay.vorlage"
                    + "			WHERE 	"
                    + "                                                                                     auktion.cusebeda_objekt_id = 122 AND "
                    + "				 auktion.vorlage_id IN "
                    + vorlage_arr
                    + "				AND auktion.status = 1 "
                    + "				AND vorlage.id = auktion.vorlage_id";
            System.out.println("Query for all vorlage auctions objectarray============================" + SQL_Query);
            try {
                Statement stmt = regorConnection.createStatement();
                ResultSet result = stmt.executeQuery(SQL_Query);
                // define temporally array to save Data into, this step is done
                // because we want to get all Data in one time from
                // our Data base. that is time saving
                // save all arrangement ID, because we want to retrieve all
                // arrangement Data once from the Data base
                ArrayList<String> vorlListtmp = new ArrayList<String>();
                ArrayList<String> auctiontmp = new ArrayList<String>();
                // first save the auction data in array
                while (result.next()) {
                    vorlListtmp.add(result.getString("vorlage_id"));
                    auctiontmp.add(result.getString("id"));
                    // auction Data
                    int startPreisInt = result.getInt("startpreis");
                    String startPreis = result.getString("startpreis");
                    int currentBidInt = result.getInt("currentbid");
                    String currentBidS = result.getString("currentbid");
                    int auctionMasterTypeID = result.getInt("AuctionMasterTypeID");
                    String ebaysofortkauf = "0";
                    try {
                        String ebaysofortundneupreis = result.getString("ebaysofortundneupreis");
                        ebaysofortkauf = result.getString("ebaysofortkauf");
                        if (startPreisInt == 0 && ebaysofortundneupreis.length() > 0) {
                            startPreis = ebaysofortundneupreis;
                        }
                    } catch (Exception e) {
                        System.out.println("SQLException for ebaysofortundneupreis");
                        System.out.flush();
                        e.printStackTrace();
                    }

                    if (currentBidInt < 1) {
                        currentBidS = startPreis;
                    }
                    int soldnumbers = 0;
                    String SQL_Soldnumbers = "SELECT sum(quantity_purchased) as soldnumbers FROM ebay.transaction where ebayitemid = " + result.getString("ebayitemid");
                    try {
                        Statement stmt_soldnumbers = regorConnection.createStatement();
                        ResultSet result_soldnumbers = stmt_soldnumbers.executeQuery(SQL_Soldnumbers);
                        if (result_soldnumbers.next()) {
                            soldnumbers = result_soldnumbers.getInt("soldnumbers");
                        } else {
                            soldnumbers = 0;
                        }
                        result_soldnumbers.close();
                    } catch (Exception e) {
                        System.out.println("soldnumbers Exception : " + e.getMessage());
                    }

                    String[] auctionData = new String[18];
                    //   System.out.println("Item ID for obj arr:::============================" + result.getString("ebayitemid"));
                    auctionData[0] = result.getString("ebayitemid");
                    auctionData[1] = result.getString("ebayueberschrift");
                    auctionData[2] = "0";
                    if (auctionMasterTypeID == 1) {
                        auctionData[2] = startPreis;
                    }
                    auctionData[3] = "0";
                    if (auctionMasterTypeID == 4) {
                        auctionData[3] = startPreis;
                    } else if (ebaysofortkauf != null && ebaysofortkauf.length() != 0 && currentBidInt == 0) {
                        auctionData[3] = ebaysofortkauf;
                    } else {
                        auctionData[3] = "0";
                    }
                    auctionData[4] = this.getImageURLByXml(result.getString("id"));

                    auctionData[5] = result.getString("startdatum");
                    auctionData[6] = result.getString("dauer");
                    auctionData[7] = result.getString("vorlage_id");
                    auctionData[8] = result.getString("objectID");
                    auctionData[9] = this.currencyMap.get(result.getString("ebaysiteid"));
                    if (result.getString("ebayname") != null && result.getString("ebayname") != "null") {
                        auctionData[10] = result.getString("ebayname");
                    } else {
                        auctionData[10] = "";
                    }
                    auctionData[11] = currentBidS;
                    auctionData[12] = result.getString("endDate");
                    auctionData[13] = result.getString("untertitel");
                    if (auctionData[13] == null) {
                        auctionData[13] = "";
                    }
                    auctionData[14] = result.getString("ebaysiteid");
                    if (result.getString("anzahlgebote") != null) {
                        auctionData[15] = result.getString("anzahlgebote");
                    } else {
                        auctionData[15] = "0";
                    }
                    int retailPrice = result.getInt("retailprice");
                    auctionData[16] = retailPrice + "";
                    auctionData[17] = soldnumbers + "";
                    // save the auction data
                    this.auctionsMap.put(result.getString("id"), auctionData);
                    //hotelPackages.getHotelPackage().add(hotelPackage);
                }
                // close the result Set
                result.close();

                if (this.auctionsMap.size() > 0) {
                    System.out.println("Nbr Of auctions found(3): "
                            + this.auctionsMap.size());
                    // first get the objects data and arrangement data (reducing
                    // the number of call to one call)
                    this.getObjectDataMap(object_ids);
//                    this.getArrangDataMap(arragListtmp);
                    this.getVorlageDataMap(vorlListtmp);
                    // System.out.println("after getArrangDataMap");
                    // System.out.flush();
                    this.getAuctionPriceRangeMap(auctiontmp);
                    // System.out.println("after getAuctionPriceRangeMap");
                    // System.out.flush();
                    // this.getCurrentBidMap(auctiontmp);
                    Iterator<String> it = this.auctionsMap.keySet().iterator();
                    while (it.hasNext()) {
                        String akt_auktion = it.next();

                        String[] auctionData = this.auctionsMap.get(akt_auktion);

                        // System.out.println("akt_auktion :
                        // "+akt_auktion+"\n");
                        // System.out.println("auctionData :
                        // "+auctionData+"\n");
                        // System.out.flush();

                        String[] objectData = {"0", "0", "0", "0", "0"};
                        if (this.objectMap.containsKey(auctionData[8])) {
                            objectData = this.objectMap.get(auctionData[8]);
                        }
                        String currentBid = auctionData[11];
                        String[] priceRange = new String[2];
                        priceRange[0] = "";
                        priceRange[1] = "";
                        if (this.priceRangeMap.containsKey(akt_auktion)) {
                            priceRange = this.priceRangeMap.get(akt_auktion);
                        } else {
                            //   priceRange[0] = "http://www.cultbay.com/new_button_white1.gif";
                        }
                        // Initialize the arrangement Data because not all
                        // auction are connect to arrangement
                        String[] arrangData = {"0", "0", "0", "0"};
                        if (this.vorlageMap.containsKey(auctionData[7])) {
                            arrangData = this.vorlageMap.get(auctionData[7]);
                        }
                        // build the XML Response
                        hotelPackage = new CultbayResponseTypeImpl.HotelPackagesTypeImpl.HotelPackageTypeImpl();
                        hotelPackage.setPackageCode(auctionData[0]);
                        hotelPackage.setPackagename(auctionData[1]);
                        hotelPackage.setPackageIdentifier(this.getAuctionMarket(auctionData[14]));
                        hotelPackage.setPackageDesc(auctionData[13]);
                        hotelPackage.setHotelName(objectData[0]);
                        hotelPackage.setAccomodation(new BigInteger(
                                arrangData[0]));
                        hotelPackage.setNoOfPerson(new BigInteger(arrangData[1]));
                        hotelPackage.setTypeOfRoom(arrangData[2]);
                        hotelPackage.setCatering(arrangData[3]);
                        if (Double.valueOf(currentBid) > 0.0) {
                            hotelPackage.setCurrentBid(new BigDecimal(
                                    currentBid));
                        }
                        //hotelPackage.setRetailPrice(new BigDecimal(auctionData[16]));
                        hotelPackage.setCountry(objectData[1]);
                        hotelPackage.setPlace(objectData[2]);
                        hotelPackage.setRegion(objectData[3]);
                        // setBookItNow only if the arrangement is set
                        // if(result.getInt("arrangement_id") > 0){
                        int arrg_id = new Integer(auctionData[7]);
                        if (arrg_id > 0) {
                            hotelPackage.setBookItNow(new BigInteger(
                                    objectData[4]));
                        } else {
                            hotelPackage.setBookItNow(new BigInteger("0"));
                        }
                        hotelPackage.setUrl(this.getAuctionURL(auctionData[14])
                                + auctionData[0]);
                        hotelPackage.setStartBidPrice(new BigDecimal(
                                auctionData[2]));
                        hotelPackage.setBuyNowPrice(new BigDecimal(
                                auctionData[3]));
                        hotelPackage.setPriceRange(priceRange[0] + " "
                                + priceRange[1]);
                        hotelPackage.setCurrency(auctionData[9]);
                        hotelPackage.setImage(auctionData[4]);
                        hotelPackage.setStartDate(auctionData[5]);
                        hotelPackage.setEndDate(auctionData[12]);
                        hotelPackage.setDurationDays(new BigInteger(
                                auctionData[6]));
                        hotelPackage.setBids(new BigInteger(auctionData[15]));
                        hotelPackage.setSellerId(auctionData[10]);
                        hotelPackage.setObjectID(auctionData[8]);
                        hotelPackage.setSoldnumbers(Integer.parseInt(auctionData[17]));
                        hotelPackage.setZuzzuItemId(this.getZuzzuItemid(auctionData[0]));
                        hotelPackage.setProductId(BigInteger.valueOf(this.getProductId(auctionData[0])));
                        hotelPackages.getHotelPackage().add(hotelPackage);
                    }
                    // System.out.println("after while");
                    // System.out.flush();
                }
            } catch (Exception sqlfehler) {
                System.out.println("buildXML: Error Occured during receving Data from the DB vorlage");
                System.out.println(sqlfehler.getMessage());
            }

        }

        cultbayResponse.setHotelPackages(hotelPackages);
        try {
            regorConnection.close();
            vegaConnection.close();
            m.marshal(cultbayResponse, sw);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        //System.out.println("XMLResponse : " + sw.toString());
        return sw.toString();
    }

    /**
     * return the details of a auction specified by the auction ID
     */
    public String buildXMLAuctionDetails(String AuctionID) {
        System.out.println("in buildXMLAuctionDetails");

        dbConnection = new DBConnection();
        Connection vegaConnection = dbConnection.getVegaConnection();
        Connection regorConnection = dbConnection.getRegorConnection();

        CultbayAuctionDetailsRSImpl cultbayDetailsResponse = new CultbayAuctionDetailsRSImpl();

        String SQL_Query = null;
        // Since ended auction are deleted after some months in the
        // ebay.auktion-DB-Table, w
        // we need to retrieve ended auction's information from another DB-Table
        System.out.println("checkIfRunningAuction");
        if (this.checkIfRunningAuction(AuctionID)) {
            SQL_Query = "SELECT auktion.id, auktion.cusebeda_objekt_id AS objectID, auktion.ebayueberschrift, auktion.anzahlgebote,"
                    + "		auktion.currentbid, auktion.startpreis, auktion.ebaysiteid, DATE_ADD(auktion.startdatum, INTERVAL auktion.dauer DAY) as endDate,"
                    + "		auktion.ebaysofortkauf,if(auktion.retailprice is NULL,0,auktion.retailprice) as retailprice, auktion.untertitel, auktion.endpreis, auktion.ebaysofortkauf, auktion.status, auktion.ebaysiteid, ebaydaten.galeriebild_id,ebaydaten.ebayname,auktion.AuctionMasterTypeID, auktion.quantity  "
                    + "		FROM ebay.auktion LEFT JOIN ebay.ebaydaten ON auktion.cusebeda_objekt_id = ebaydaten.cusebeda_objekt_id "
                    + "				WHERE auktion.ebayitemid ='" + AuctionID + "'";
        } else {
            System.out.println("in past auktions query");
            SQL_Query = "SELECT 	auktion_sich.id, auktion_sich.cusebeda_objekt_id AS objectID, auktion_sich.ebayueberschrift, auktion_sich.anzahlgebote,"
                    + "		auktion_sich.endpreis as currentbid, auktion_sich.startpreis, auktion_sich.ebaysiteid, DATE_ADD(auktion_sich.startdatum, INTERVAL auktion_sich.dauer DAY) as endDate, "
                    + "		auktion_sich.ebaysofortkauf,if(auktion_sich.retailprice is NULL,0,auktion_sich.retailprice) as retailprice, auktion_sich.untertitel, auktion_sich.endpreis, auktion_sich.ebaysofortkauf, auktion_sich.status, auktion_sich.ebaysiteid, ebaydaten.galeriebild_id,ebaydaten.ebayname,auktion_sich.AuctionMasterTypeID, auktion_sich.quantity "
                    + "		FROM ebay.auktion_sich LEFT JOIN ebay.ebaydaten ON auktion_sich.cusebeda_objekt_id = ebaydaten.cusebeda_objekt_id"
                    + "			WHERE auktion_sich.ebayitemid ='" + AuctionID + "'";
        }
        // Query
        try {
            Statement stmt = regorConnection.createStatement();
            ResultSet result = stmt.executeQuery(SQL_Query);
            while (result.next()) {
                String object_id = result.getString("objectID");
                String auction_id = result.getString("id");
                String[] geoCoord = new String[2];
                geoCoord = this.getObjectGeoTags(object_id);
                CultbayAuctionDetailsRSImpl.GeoTagTypeImpl geoTag = new CultbayAuctionDetailsRSTypeImpl.GeoTagTypeImpl();
                CultbayAuctionDetailsRSTypeImpl.GeoTagTypeImpl.GeoPositionTypeImpl geoPosition = new CultbayAuctionDetailsRSTypeImpl.GeoTagTypeImpl.GeoPositionTypeImpl();
                if (geoCoord[0] != null && geoCoord[1] != null) {
                    geoPosition.setLongitude(new BigDecimal(geoCoord[0]));
                    geoPosition.setLatitude(new BigDecimal(geoCoord[1]));
                }
                geoTag.setGeoPosition(geoPosition);
                cultbayDetailsResponse.setGeoTag(geoTag);

                String[] objectData = this.getObjectData(object_id);
                String image = this.getImageURLByXml(result.getString("id"));
                //String htmlTemplate1 = this.getHtmlTemplate(auction_id);
                this.htmlTemplate = "";
                this.pictureUrls = new ArrayList();
                this.itemSpecifics = "";
                this.getHtmlRequest(auction_id);

                String adress = objectData[1] + "+" + objectData[3] + "+" + objectData[2] + "+" + objectData[4];

                String[] priceRange = new String[2];
                priceRange = this.getAuctionPriceRange(auction_id);
                String buyNowPrice = "0";
                if (result.getString("ebaysofortkauf") != null && result.getString("ebaysofortkauf").length() != 0 && result.getInt("currentbid") == 0) {
                    buyNowPrice = result.getString("ebaysofortkauf");
                }

                String currentBidS = "0";
                int status = result.getInt("status");

                if (status == 1) { // running auction
                    int currentBidInt = result.getInt("currentbid");
                    currentBidS = result.getString("currentbid");
                    if (currentBidInt < 1) {
                        currentBidS = result.getString("startpreis");
                    }
                } else if (status > 1 && result.getInt("quantity") == 1) { // ended auction
                    int endpreis = result.getInt("endpreis");
                    if (endpreis > 0) { // sold auction
                        currentBidS = result.getString("endpreis");
                    } else { // not sold auction
                        currentBidS = buyNowPrice;
                    }
                }
                if (result.getInt("AuctionMasterTypeID") == 4) {
                    buyNowPrice = result.getString("startpreis");
                    currentBidS = "0";
                }
                String retailPrice = null;

                int retailPriceInt = result.getInt("retailprice");
                retailPrice = result.getString("retailprice");
                System.out.println("Retail priceeeeeeeeeeeeeeeee" + retailPriceInt);
                if (retailPriceInt < 1) {
                    retailPrice = result.getString("retailprice");
                    retailPrice = "0";
                }
                if (retailPrice.equals("")) {
                    retailPrice = "0";
                }

                String SQL_Soldnumbers = "SELECT sum(quantity_purchased) as soldnumbers FROM ebay.transaction where ebayitemid = " + AuctionID;
                int soldnumbers = 0;
                try {
                    Statement stmt_soldnumbers = regorConnection.createStatement();
                    ResultSet result_soldnumbers = stmt_soldnumbers.executeQuery(SQL_Soldnumbers);
                    if (result_soldnumbers.next()) {
                        soldnumbers = result_soldnumbers.getInt("soldnumbers");
                    } else {
                        soldnumbers = 0;
                    }
                    result_soldnumbers.close();
                } catch (Exception e) {
                    System.out.println("soldnumbers Exception : " + e.getMessage());
                }
                cultbayDetailsResponse.setHotelName(objectData[0]);
                cultbayDetailsResponse.setHotelPicture(image);
                cultbayDetailsResponse.setAddress(adress);
                
                //added for new requirements 
                cultbayDetailsResponse.setHtmltemplate(this.htmlTemplate);
                CultbayAuctionDetailsRSTypeImpl.PictureURLsTypeImpl purls = new CultbayAuctionDetailsRSTypeImpl.PictureURLsTypeImpl();
                if (this.pictureUrls != null) {
                    purls.getPictureURL().addAll(this.pictureUrls);
                }
                cultbayDetailsResponse.setPictureURLs(purls);
                cultbayDetailsResponse.setItemSpecifics(this.itemSpecifics);
                cultbayDetailsResponse.setPackagename(result.getString("ebayueberschrift"));
                cultbayDetailsResponse.setPackageDesc(result.getString("untertitel"));
                cultbayDetailsResponse.setUrl(this.getAuctionURL(result.getString("ebaysiteid")) + AuctionID);
                if (Double.valueOf(currentBidS) > 0.0) {
                    cultbayDetailsResponse.setCurrentBid(new BigDecimal(
                            currentBidS));
                }
                if (Double.valueOf(retailPrice) > 0.0) {
                    cultbayDetailsResponse.setRetailPrice(new BigDecimal(retailPrice));
                }
                cultbayDetailsResponse.setBuyNowPrice(new BigDecimal(
                        buyNowPrice));
                cultbayDetailsResponse.setPriceRange(priceRange[0] + " "
                        + priceRange[1]);
                cultbayDetailsResponse.setCurrency(this.getSiteCurrency(result.getString("ebaysiteid")));
                cultbayDetailsResponse.setEndDate(result.getString("endDate"));
                if (result.getString("anzahlgebote") != null) {
                    cultbayDetailsResponse.setBids(new BigInteger(result.getString("anzahlgebote")));
                } else {
                    cultbayDetailsResponse.setBids(new BigInteger("0"));
                }
                if (result.getString("ebayname") != null && result.getString("ebayname") != "null") {
                    cultbayDetailsResponse.setSellerId(result.getString("ebayname"));
                } else {
                    cultbayDetailsResponse.setSellerId("");
                }

                cultbayDetailsResponse.setStatus(status);
                cultbayDetailsResponse.setSoldnumbers(soldnumbers);
                /*
                 * Get Categories at template Level
                 */

                CultbayAuctionDetailsRSImpl.EquipmentTypeImpl equipment = new CultbayAuctionDetailsRSTypeImpl.EquipmentTypeImpl();

                String templateCategoriesQuery = "SELECT r.rubrik_id ";
                templateCategoriesQuery += " FROM ebay.auktion a,ebay.vorlage v,ebay.vorlage_arrangement_rubrik r ";
                templateCategoriesQuery += " WHERE a.ebayitemid =  " + AuctionID;
                templateCategoriesQuery += " AND v.id = a.vorlage_id ";
                templateCategoriesQuery += " AND r.vorlage_id = v.id ";
                templateCategoriesQuery += " GROUP BY r.rubrik_id";

                try {
                    stmt = regorConnection.createStatement();
                    result = stmt.executeQuery(templateCategoriesQuery);
                    while (result.next()) {
                        int rubrik_id = result.getInt("rubrik_id");
                        equipment.getCategories().add(rubrik_id);
                    }
                } catch (Exception e) {
                    System.out.println("Rubrik Exceptin : " + e.getMessage());
                }
                if (equipment.getCategories().isEmpty()) {
                    /*
                     * Get Categories at Package level
                     */
                    int arrangement_id = 0;
                    String arrangementIdQuery = "SELECT v.arrangement_id ";
                    arrangementIdQuery += " FROM ebay.auktion a , ebay.vorlage v ";
                    arrangementIdQuery += " WHERE a.ebayitemid=" + AuctionID;
                    arrangementIdQuery += " AND v.id = a.vorlage_id ";
                    arrangementIdQuery += " AND v.arrangement_id != 0 limit 1";
                    stmt = regorConnection.createStatement();
                    result = stmt.executeQuery(arrangementIdQuery);
                    while (result.next()) {
                        arrangement_id = result.getInt("arrangement_id");
                    }
                    cultbayDetailsResponse.setProductId(BigInteger.valueOf(arrangement_id));
                    String packageCategoriesQuery = "SELECT arrangement_x_rubrik.rubrik_id from hofesoda.arrangement_x_rubrik where arrangement_x_rubrik.arrangement_id = " + arrangement_id;
                    Statement vegaStmt = vegaConnection.createStatement();
                    result = vegaStmt.executeQuery(packageCategoriesQuery);
                    while (result.next()) {
                        int rubrik_id = result.getInt("rubrik_id");
                        equipment.getCategories().add(rubrik_id);
                    }
                }else{
                //This else block only for setting arrangement_id as productid
                cultbayDetailsResponse.setProductId(BigInteger.valueOf(this.getProductId(AuctionID)));
                
                }
                cultbayDetailsResponse.setEquipment(equipment);
            }
            
            cultbayDetailsResponse.setZuzzuItemId(this.getZuzzuItemid(AuctionID));

            result.close();
        } catch (Exception sqlfehler) {
            sqlfehler.printStackTrace();
            System.out.println("buildXMLAuctionDetails: Error Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        }
        try {
            regorConnection.close();
            vegaConnection.close();
            m.marshal(cultbayDetailsResponse, dw);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return sw.toString();
    }

    /**
     * return the coordinates of hotels
     */
    public String buildXMLGeoTags(HashMap<String, String> object_ids) {
        // Initialize the Maps
        this.objectStars = new HashMap<String, String>();
        this.geoTagsMap = new HashMap<String, String[]>();
        this.hotelBrandMap = new HashMap<String, String>();

        // Set the SQL Connection
        dbConnection = new DBConnection();
        Connection vegaConnection = dbConnection.getVegaConnection();
        Connection regorConnection = dbConnection.getRegorConnection();

        CultbayGeoTagsResponseImpl cultbayGeoTagsResponse = new CultbayGeoTagsResponseImpl();

        CultbayGeoTagsResponseTypeImpl.GeoTagsTypeImpl geoTags = new CultbayGeoTagsResponseTypeImpl.GeoTagsTypeImpl();
        // this array is helpful for the SQL-query
        Object[] objectIDS = object_ids.keySet().toArray();
        String obj_arr = "(";
        for (int j = 0; j < objectIDS.length; j++) {
            if (j == objectIDS.length - 1) { // last element without the
                // comma
                obj_arr += "'" + (String) objectIDS[j] + "'";
            } else {
                obj_arr += "'" + (String) objectIDS[j] + "', ";
            }
        }

        obj_arr += ")";

        this.getObjectStarsMap(objectIDS);
        this.getObjectGeoTagsMap(objectIDS);
        this.getHotelBrands(objectIDS);

        String SQL_Query1 = "SELECT objekt.id, objekt.bezeichnung as hotelname, objekt.dateien_bilder_hauptbild_id, objekt.dateien_bilder_logo_id,"
                + "	   objekt.strasse, objekt.ort, laender.bezeichnung as land, text.text as region,"
                + "	   objekt.plz, objekt.fon, objekt.fax, objekt.url, objekt.email"
                + "			FROM cusebeda.objekt, cusebeda.laender, cusebeda.verwaltungseinheiten, cumulida.finder, cumulida.text"
                + "			    WHERE objekt.id  IN "
                + obj_arr
                + "					AND  objekt.laender_id = laender.id"
                + "					AND  laender.sprache_id ='"
                + this.language
                + "' "
                + "			  		AND  objekt.verwaltungseinheiten_id = verwaltungseinheiten.id"
                + "		      		AND  verwaltungseinheiten.finder_id = finder.id"
                + "			 		AND  finder.text_id = text.id"
                + "			        AND  text.cusebeda_sprache_id ='"
                + this.language
                + "'";
        System.out.println("GeoCodes query===================================================================" + SQL_Query1);
        // Query
        try {
            Statement stmt = vegaConnection.createStatement();
            ResultSet result1 = stmt.executeQuery(SQL_Query1);
            while (result1.next()) {
                String object_id = result1.getString("id");
                String hotelBrand = "";
                String[] geoCoord = new String[2];
                geoCoord[0] = "0.0";
                geoCoord[1] = "0.0";

                if (this.geoTagsMap.containsKey(object_id)) {
                    geoCoord = this.geoTagsMap.get(object_id);
                }

                if (this.hotelBrandMap.containsKey(object_id)) {
                    hotelBrand = this.hotelBrandMap.get(object_id);
                }

                String image = getImageURL(result1.getString("dateien_bilder_hauptbild_id"), result1.getString("id"));
                if (image.matches("")) {
                    image = getImageURL(result1.getString("dateien_bilder_logo_id"), result1.getString("id"));
                }
                CultbayGeoTagsResponseTypeImpl.GeoTagsTypeImpl.GeoTagTypeImpl geoTag = new CultbayGeoTagsResponseTypeImpl.GeoTagsTypeImpl.GeoTagTypeImpl();

                CultbayGeoTagsResponseTypeImpl.GeoTagsTypeImpl.GeoTagTypeImpl.GeoPositionTypeImpl geoPosition = new CultbayGeoTagsResponseTypeImpl.GeoTagsTypeImpl.GeoTagTypeImpl.GeoPositionTypeImpl();
                geoPosition.setLongitude(new BigDecimal(geoCoord[0]));
                geoPosition.setLatitude(new BigDecimal(geoCoord[1]));
                geoTag.setGeoPosition(geoPosition);

                CultbayGeoTagsResponseTypeImpl.GeoTagsTypeImpl.GeoTagTypeImpl.HotelInfoTypeImpl hotelInfo = new CultbayGeoTagsResponseTypeImpl.GeoTagsTypeImpl.GeoTagTypeImpl.HotelInfoTypeImpl();

                hotelInfo.setObjectId(new BigInteger(result1.getString("id")));
                hotelInfo.setHotelName(result1.getString("hotelname"));
                hotelInfo.setHotelImageurl(image);
                hotelInfo.setCountry(result1.getString("land"));
                hotelInfo.setRegion(result1.getString("region"));
                hotelInfo.setPlace(result1.getString("ort"));
                hotelInfo.setAddress(result1.getString("strasse") + "+" + result1.getString("plz"));

                hotelInfo.setHotelBrand(hotelBrand);
                if (this.objectStars.containsKey(object_id)) {
                    hotelInfo.setStars(new BigInteger(this.objectStars.get(object_id)));
                } else {
                    hotelInfo.setStars(new BigInteger("0"));
                }
                hotelInfo.setTelephone(result1.getString("fon"));
                hotelInfo.setFax(result1.getString("fax"));
                hotelInfo.setMailId(result1.getString("email"));
                hotelInfo.setWebsite(result1.getString("url"));
                hotelInfo.setNumberOfAuctions(new BigInteger(object_ids.get(object_id)));
                geoTag.setHotelInfo(hotelInfo);
                /*
                 * Facilties is back
                 */
                CultbayGeoTagsResponseTypeImpl.GeoTagsTypeImpl.GeoTagTypeImpl.EquipmentTypeImpl equipment = new CultbayGeoTagsResponseTypeImpl.GeoTagsTypeImpl.GeoTagTypeImpl.EquipmentTypeImpl();

                String SQL_Query2 = "SELECT text.text as category, rubrik.id as rubrik_id"
                        + "		FROM hofesoda.arrangement, hofesoda.rubrik, hofesoda.arrangement_x_rubrik,"
                        + "		 	 cumulida.finder, cumulida.text"
                        + "			WHERE arrangement.cusebeda_objekt_id  ='"
                        + object_id
                        + "'"
                        + "				  AND arrangement.id = arrangement_x_rubrik.arrangement_id"
                        + "				  AND arrangement_x_rubrik.rubrik_id = rubrik.id"
                        + "			      AND text.cusebeda_sprache_id ='"
                        + this.language
                        + "' "
                        + "				  AND rubrik.finder_id = finder.id"
                        + "				  AND finder.text_id = text.id"
                        + "		    GROUP by rubrik_id";


                try {
                    Statement stmt1 = vegaConnection.createStatement();
                    ResultSet result2 = stmt1.executeQuery(SQL_Query2);
                    while (result2.next()) {
                        equipment.getCategory().add(result2.getString("category"));
                    }

                    result2.close();
                } catch (SQLException sqlfehler) {
                    System.out.println("addCategory Error Occured during receving Data from the DB");
                    System.out.println(sqlfehler.getMessage());
                }
                String SQL_Query3 = "SELECT text.text as facility, merkmal.id as merkmal_id"
                        + "		FROM hofesoda.merkmal, hofesoda.daten,"
                        + "		 	 cumulida.finder, cumulida.text"
                        + "			WHERE daten.merkmal_id = merkmal.id"
                        + "				  AND daten.cusebeda_objekt_id  ='"
                        + object_id
                        + "'"
                        + "				  AND merkmal.cultbay_kriterium = 1"
                        + "				  AND merkmal.finder_id = finder.id"
                        + "			      AND text.cusebeda_sprache_id ='"
                        + this.language
                        + "' "
                        + "				  AND finder.text_id = text.id"
                        + "			GROUP by merkmal_id";

                try {
                    Statement stmt3 = vegaConnection.createStatement();
                    ResultSet result3 = stmt3.executeQuery(SQL_Query3);
                    while (result3.next()) {
                        equipment.getCategory().add(result3.getString("facility"));
                    }
                    result3.close();
                } catch (SQLException sqlfehler) {
                    System.out.println("addFacility Error Occured during receving Data from the DB");
                    System.out.println(sqlfehler.getMessage());
                }
                geoTag.setEquipment(equipment);

                /*
                 * Facilities is above
                 */
                geoTags.getGeoTag().add(geoTag);
            }

            result1.close();
        } catch (SQLException sqlfehler) {
            System.out.println("buildXMLGeoTags: Error Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        }

        cultbayGeoTagsResponse.setGeoTags(geoTags);
        try {
            vegaConnection.close();
            regorConnection.close();
            m.marshal(cultbayGeoTagsResponse, sw);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return sw.toString();
    }

    public String getImageURLByXml(String auction_id) {

        String SQL_Query = "SELECT request"
                + "       FROM ebay3.apiCallRequest" + "			WHERE uuid like '"
                + auction_id + "%'";
        String img = "";
        // Query
        dbConnection = new DBConnection();
        Connection vegaConnection = dbConnection.getVegaConnection();
        Statement stmt;
        ResultSet result;
        try {
            stmt = vegaConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);

            while (result.next()) {
                String apiCallRequest = result.getString("request");
                if (apiCallRequest.contains("<GalleryURL>")) {
                    int indexbeg = apiCallRequest.indexOf("<GalleryURL>") + 12;
                    int indexend = apiCallRequest.lastIndexOf("</GalleryURL>");
                    img = apiCallRequest.substring(indexbeg, indexend);
                }
            }
            result.close();
            stmt.close();
            vegaConnection.close();
        } catch (SQLException sqlfehler) {
            System.out.println("getImageURLBYXml : Error Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        }
        return img;
    }

    public String getImageURL(String ebayBildID, String object_id) {
        boolean imageFound = false;
        // Set the SQL Connection
        String imageUrl = "http://albatros.cultuzz.de/service/_img/bv/"
                + object_id + "/";
        String SQL_Query = "SELECT datei FROM dateien.bilder WHERE id ='"
                + ebayBildID + "'";
        // Query
        dbConnection = new DBConnection();
        Connection vegaConnection = dbConnection.getVegaConnection();
        Statement stmt;
        ResultSet result;
        try {
            stmt = vegaConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);
            while (result.next()) {
                imageUrl += result.getString("datei");
                imageFound = true;
            }
            result.close();
            stmt.close();
            vegaConnection.close();
        } catch (SQLException sqlfehler) {
            System.out.println("getImageURL: Error Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        }
        // return
        if (imageFound) {
            return imageUrl;
        } else {
            return "";
        }
    }

    /**
     * set the stars of each object containing in the Object array
     *
     * @param object_ids
     */
    public void getObjectStarsMap(Object[] object_ids) {
        String object_arr = "(";
        for (int j = 0; j < object_ids.length; j++) {
            if (j == object_ids.length - 1) {
                object_arr += "'" + (String) object_ids[j] + "'";
            } else {
                object_arr += "'" + (String) object_ids[j] + "', ";
            }
        }

        object_arr += ")";

        dbConnection = new DBConnection();
        Connection vegaConnection = dbConnection.getVegaConnection();

        String SQL_Query = " SELECT DISTINCT hofesoda.daten.cusebeda_objekt_id, daten.wert as stars "
                + "	FROM hofesoda.daten"
                + "	WHERE hofesoda.daten.cusebeda_objekt_id IN "
                + object_arr
                + "		AND daten.merkmal_id = '133'";
        // Query
        Statement stmt;
        ResultSet result;
        try {
            stmt = vegaConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);
            while (result.next()) {
                String object_id = result.getString("cusebeda_objekt_id");
                String star = result.getString("stars");
                this.objectStars.put(object_id, star);
            }
            result.close();
            stmt.close();
            vegaConnection.close();
        } catch (SQLException sqlfehler) {
            System.out.println("getObjectStarsMap: Error Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        }
    }

    /**
     * get data of all object containing in the List in one time
     *
     * @param object_ids
     */
    public void getObjectDataMap(ArrayList<String> object_ids) {
        String object_arr = "(";
        for (int j = 0; j < object_ids.size(); j++) {
            if (j == object_ids.size() - 1) {
                object_arr += "'" + object_ids.get(j) + "'";
            } else {
                object_arr += "'" + object_ids.get(j) + "', ";
            }
        }

        object_arr += ")";

        // Period Query

// Modified query as per the Mysql5 upgrade


        String SQL_Query = " SELECT cusebeda.objekt.id, objekt.bezeichnung, objekt.ort as city,laender.bezeichnung as land, text.text as region, IF(objekt_x_kalender.kalender = 1, 1, 0) as kalendar FROM cusebeda.objekt LEFT JOIN cusebeda.objekt_x_kalender  ON objekt_x_kalender.cusebeda_objekt_id = objekt.id LEFT JOIN cusebeda.laender ON objekt.laender_id = laender.id LEFT JOIN   cusebeda.verwaltungseinheiten ON objekt.verwaltungseinheiten_id =  verwaltungseinheiten.id LEFT JOIN cumulida.finder ON verwaltungseinheiten.finder_id = finder.id LEFT JOIN cumulida.text     ON   finder.text_id = text.id WHERE objekt.id IN " + object_arr + "  AND   laender.sprache_id ='" + this.language + "'" + " AND text.cusebeda_sprache_id ='" + this.language + "'";

        dbConnection = new DBConnection();
        Connection vegaConnection = dbConnection.getVegaConnection();
        Statement stmt;
        ResultSet result;
        try {
            stmt = vegaConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);
            while (result.next()) {
                String object_id = result.getString("id");
                String[] objectData = new String[5];
                objectData[0] = result.getString("bezeichnung");
                objectData[1] = result.getString("land");
                objectData[2] = result.getString("city");
                objectData[3] = result.getString("region");
                objectData[4] = result.getString("kalendar");
                this.objectMap.put(object_id, objectData);
            }
            result.close();
            stmt.close();
            vegaConnection.close();
        } catch (SQLException sqlfehler) {
            System.out.println("getObjectDataMap: Error Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        }
    }

    /**
     * Get Hotels Brand of all Objects having running auctions
     *
     * @param object_ids
     */
    public void getHotelBrands(Object[] object_ids) {
        String object_arr = "(";
        for (int j = 0; j < object_ids.length; j++) {
            if (j == object_ids.length - 1) {
                object_arr += "'" + (String) object_ids[j] + "'";
            } else {
                object_arr += "'" + (String) object_ids[j] + "', ";
            }
        }

        object_arr += ")";

        // Period Query
        String SQL_Query = "SELECT cusebeda.hotelmarken_x_objekt.objekt_id, hotelmarken.bezeichnung as brand"
                + "       FROM cusebeda.hotelmarken, cusebeda.hotelmarken_x_objekt "
                + "			WHERE hotelmarken_x_objekt.objekt_id IN"
                + object_arr
                + ""
                + "				AND hotelmarken.id = hotelmarken_x_objekt.hotelmarken_id";

        // System.out.println (SQL_Query);
        // Query
        dbConnection = new DBConnection();
        Connection vegaConnection = dbConnection.getVegaConnection();
        Statement stmt;
        ResultSet result;
        try {
            stmt = vegaConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);
            while (result.next()) {
                String object_id = result.getString("objekt_id");
                String brand = result.getString("brand");
                this.hotelBrandMap.put(object_id, brand);
            }
            result.close();
            stmt.close();
            vegaConnection.close();
        } catch (SQLException sqlfehler) {
            System.out.println("getObjectDataMap: Error Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        }
    }

    /**
     * get data of all object containing in the List in one time
     *
     * @param object_ids
     */
    public void getObjectGeoTagsMap(Object[] object_ids) {
        String object_arr = "(";
        for (int j = 0; j < object_ids.length; j++) {
            if (j == object_ids.length - 1) {
                object_arr += "'" + (String) object_ids[j] + "'";
            } else {
                object_arr += "'" + (String) object_ids[j] + "', ";
            }
        }

        object_arr += ")";

        // Period Query
        String SQL_Query = " SELECT objekt_x_geoData.objekt_id,"
                + "	objekt_x_geoData.longitude, objekt_x_geoData.latitude"
                + "			FROM cusebeda.objekt_x_geoData"
                + "			    WHERE objekt_x_geoData.objekt_id  IN " + object_arr;

        // Query
        dbConnection = new DBConnection();
        Connection vegaConnection = dbConnection.getVegaConnection();
        Statement stmt;
        ResultSet result;
        try {
            stmt = vegaConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);
            while (result.next()) {
                String object_id = result.getString("objekt_id");
                String[] objectData = new String[2];
                objectData[0] = "";
                objectData[1] = "";
                objectData[0] = result.getString("longitude");
                objectData[1] = result.getString("latitude");
                this.geoTagsMap.put(object_id, objectData);
            }
            result.close();
            stmt.close();
            vegaConnection.close();
        } catch (SQLException sqlfehler) {
            System.out.println("getObjectGeoTagsMap: Error Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        }
    }

    /**
     * get data of all object containing in the List in one time
     *
     * @param object_ids
     */
    public String[] getObjectGeoTags(Object object_id) {
        String[] objectData = new String[2];
        objectData[0] = "0.0";
        objectData[1] = "0.0";
        // Period Query
        String SQL_Query = " SELECT objekt_x_geoData.objekt_id,"
                + "	objekt_x_geoData.longitude, objekt_x_geoData.latitude"
                + "			FROM cusebeda.objekt_x_geoData"
                + "			    WHERE objekt_x_geoData.objekt_id  ='" + object_id
                + "'";

        // Query
        dbConnection = new DBConnection();
        Connection vegaConnection = dbConnection.getVegaConnection();
        Statement stmt;
        ResultSet result;
        try {
            stmt = vegaConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);
            while (result.next()) {
                objectData[0] = result.getString("longitude");
                objectData[1] = result.getString("latitude");
            }
            result.close();
            stmt.close();
            vegaConnection.close();
        } catch (SQLException sqlfehler) {
            System.out.println("getObjectGeoTagsMap: Error Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        }
        return objectData;
    }

    public void getArrangDataMap(ArrayList<String> arrang_ids) {
        String arrang_arr = "(";

        for (int j = 0; j < arrang_ids.size(); j++) {
            if (j == arrang_ids.size() - 1) {
                arrang_arr += "'" + arrang_ids.get(j) + "'";
            } else {
                arrang_arr += "'" + arrang_ids.get(j) + "', ";
            }

        }

        arrang_arr += ")";
        // System.out.println (" getArrangDataMap Language ID "+this.language);
        String SQL_Query = "SELECT arrangement.id, arrangement.uebernachtungen, arrangement.personen,"
                + "		text.text as room"
                + "       FROM hofesoda.arrangement, hofesoda.ziwekategorie, hofesoda.zimmerart,"
                + "			cumulida.finder, cumulida.text"
                + "			WHERE arrangement.status=1"
                + "                AND arrangement.ziwekategorie_id = ziwekategorie.id	"
                + "			  	AND arrangement.id IN "
                + arrang_arr
                + "				AND ziwekategorie.zimmerart_id = zimmerart.id"
                + "				AND zimmerart.finder_id = finder.id"
                + "				AND finder.text_id = text.id"
                + "				AND text.cusebeda_sprache_id ='" + this.language + "'";

        String SQL_Query2 = "SELECT arrangement.id, text.text as verpflegung"
                + "       FROM hofesoda.arrangement, hofesoda.verpflegung,"
                + "			cumulida.finder, cumulida.text"
                + "			WHERE arrangement.status=1"
                + "			  	AND arrangement.id IN " + arrang_arr
                + "				AND arrangement.verpflegung_id = verpflegung.id"
                + "				AND verpflegung.finder_id = finder.id"
                + "				AND finder.text_id = text.id"
                + "			    AND text.cusebeda_sprache_id ='" + this.language
                + "'";

        // System.out.println("in getAuctionPriceRangeMap :\n");
        // System.out.println("SQL_Query :\n"+SQL_Query+"\n");
        // System.out.println("SQL_Query2 :\n"+SQL_Query2+"\n");
        // System.out.flush();

        // Query

        dbConnection = new DBConnection();
        Connection vegaConnection = dbConnection.getVegaConnection();
        Statement stmt;
        ResultSet result;
        try {
            stmt = vegaConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);
            while (result.next()) {
                String[] arrangData = new String[4];
                arrangData[0] = "0";
                arrangData[1] = "0";
                arrangData[2] = "0";
                arrangData[3] = "0";
                arrangData[0] = result.getString("uebernachtungen");
                arrangData[1] = result.getString("personen");
                arrangData[2] = result.getString("room");
                arrangMap.put(result.getString("id"), arrangData);
            }
            result.close();
            stmt.close();
        } catch (SQLException sqlfehler) {
            System.out.println("getArrangDataMap: Error Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        }

        // Query
        ResultSet result2;

        try {
            stmt = vegaConnection.createStatement();
            result2 = stmt.executeQuery(SQL_Query2);
            while (result2.next()) {
                // first retrieve the old value from the Map
                if (arrangMap.containsKey(result2.getString("id"))) {
                    String[] arrangData2 = {"0", "0", "0", "0"};
                    arrangData2 = arrangMap.get(result2.getString("id"));
                    arrangData2[3] = result2.getString("verpflegung");
                    arrangMap.put(result2.getString("id"), arrangData2);
                }
            }
            result2.close();
            stmt.close();
            vegaConnection.close();
        } catch (SQLException sqlfehler) {
            System.out.println("getArrangDataMap: Error Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        }
        System.out.println("Arrangement Data found anzahl " + arrangMap.size());
    }

    public void getVorlageDataMap(ArrayList<String> vorlage_ids) {

        String vorlage_arr = "(";
        for (int j = 0; j < vorlage_ids.size(); j++) {
            if (j == vorlage_ids.size() - 1) {
                vorlage_arr += "'" + vorlage_ids.get(j) + "'";
            } else {
                vorlage_arr += "'" + vorlage_ids.get(j) + "', ";
            }
        }

        vorlage_arr += ")";
        // System.out.println (" getArrangDataMap Language ID "+this.language);
        String SQL_Query = "SELECT vorlagen_arrangement.vorlage_id , vorlagen_arrangement.naechte ,"
                + " vorlagen_arrangement.personen ,vorlagen_arrangement.text as room"
                + " from ebay.vorlagen_arrangement where vorlagen_arrangement.vorlage_id in " + vorlage_arr;
        System.out.println("vorlage queryyyyyyy" + SQL_Query);
        dbConnection = new DBConnection();
        Connection regorConnection = dbConnection.getRegorConnection();
        Statement stmt;
        ResultSet result;
        try {
            stmt = regorConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);
            while (result.next()) {
                String[] arrangData = new String[4];
                arrangData[0] = "0";
                arrangData[1] = "0";
                arrangData[2] = "0";
                arrangData[3] = "0";
                arrangData[0] = result.getString("naechte");
                arrangData[1] = result.getString("personen");
                arrangData[2] = result.getString("room");
                vorlageMap.put(result.getString("vorlage_id"), arrangData);
            }
            result.close();
            stmt.close();
        } catch (SQLException sqlfehler) {
            System.out.println("getVorlageData: Error Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        }

        System.out.println("Vorlage Data found anzahl " + vorlageMap.size());
    }

    public String[] getArrangData(String arr_id) {
        String[] arrangData = new String[4];
        arrangData[0] = "0";
        arrangData[1] = "0";
        arrangData[2] = "0";
        arrangData[3] = "0";
        // Period Query
        String SQL_Query = "SELECT arrangement.uebernachtungen, arrangement.personen,"
                + "		text.text as room"
                + "       FROM hofesoda.arrangement, hofesoda.ziwekategorie, hofesoda.zimmerart,"
                + "			cumulida.finder, cumulida.text"
                + "			WHERE arrangement.status=1"
                + "                AND arrangement.ziwekategorie_id = ziwekategorie.id	"
                + "			  	AND arrangement.id='"
                + arr_id
                + "'"
                + "				AND ziwekategorie.zimmerart_id = zimmerart.id"
                + "				AND zimmerart.finder_id = finder.id"
                + "				AND finder.text_id = text.id"
                + "				AND text.cusebeda_sprache_id ='" + this.language + "'";

        // Period Query
        String SQL_Query2 = "SELECT text.text as verpflegung"
                + "       FROM hofesoda.arrangement, hofesoda.verpflegung,"
                + "			cumulida.finder, cumulida.text"
                + "			WHERE arrangement.status=1"
                + "			  	AND arrangement.id='" + arr_id + "'"
                + "				AND arrangement.verpflegung_id = verpflegung.id"
                + "				AND verpflegung.finder_id = finder.id"
                + "				AND finder.text_id = text.id"
                + "			    AND text.cusebeda_sprache_id ='" + this.language
                + "'";

        // Query
        dbConnection = new DBConnection();
        Connection vegaConnection = dbConnection.getVegaConnection();
        Statement stmt;
        ResultSet result;

        try {
            stmt = vegaConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);
            while (result.next()) {
                arrangData[0] = result.getString("uebernachtungen");
                arrangData[1] = result.getString("personen");
                arrangData[2] = result.getString("room");
            }
            result.close();
            stmt.close();
        } catch (SQLException sqlfehler) {
            System.out.println("getArrangData: Error Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        }

        // Query
        ResultSet result2;
        try {
            stmt = vegaConnection.createStatement();
            result2 = stmt.executeQuery(SQL_Query2);
            while (result2.next()) {
                arrangData[3] = result2.getString("verpflegung");
            }
            result2.close();
            stmt.close();
            vegaConnection.close();
        } catch (SQLException sqlfehler) {
            System.out.println("getArrangData: Error Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        }

        return arrangData;
    }

    /**
     * use to give auction's details
     *
     * @param auction_id
     * @return
     */
    public String getHtmlTemplate(String auction_id) {
        String template = "";
        String SQL_Query = "SELECT request"
                + "       FROM ebay3.apiCallRequest" + "			WHERE uuid like '"
                + auction_id + "%'";

        // Query
        dbConnection = new DBConnection();
        Connection vegaConnection = dbConnection.getVegaConnection();
        Statement stmt;
        ResultSet result;

        try {
            stmt = vegaConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);
            while (result.next()) {
                template = this.parseHtmlTemplate(result.getString("request"));
            }
            result.close();
            stmt.close();
            vegaConnection.close();
        } catch (SQLException sqlfehler) {
            System.out.println("getHtmlTemplate: Error Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        }
        return template;
    }

    /**
     * use to give auction's details
     *
     * @param auction_id
     * @return
     */
    public String getHtmlRequest(String auction_id) {
        String template = "";
        String SQL_Query = "SELECT request"
                + "       FROM ebay3.apiCallRequest" + "			WHERE uuid like '"
                + auction_id + "%'";

        // Query
        dbConnection = new DBConnection();
        Connection vegaConnection = dbConnection.getVegaConnection();
        Statement stmt;
        ResultSet result;
        try {
            stmt = vegaConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);
            while (result.next()) {
                String request = result.getString("request");
                this.htmlTemplate = this.parseHtmlTemplate(request);
                this.itemSpecifics = this.parseItemSpecifics(request);
                //this.PictureURLS = this.parseAuctionPicture(request);

                this.pictureUrls = this.parseAuctionPicture(request);
                System.out.println("Picture URLs=======>" + this.pictureUrls);
            }
            result.close();
            stmt.close();
            vegaConnection.close();
        } catch (SQLException sqlfehler) {
            System.out.println("getHtmlTemplate: Error Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        }
        return template;
    }

    /**
     * use to give auction's details
     *
     * @param auction_id
     * @return
     */
    public String getHtmlTemplateOld(String auction_id) {
        // Set the SQL Connection
        String template = "";
        String SQL_Query = "SELECT request" + "       FROM ebay3.apiCallSoap"
                + "			WHERE auctionID ='" + auction_id + "'";

        // Query
        dbConnection = new DBConnection();
        Connection vegaConnection = dbConnection.getVegaConnection();
        Statement stmt;
        ResultSet result;
        try {
            stmt = vegaConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);
            while (result.next()) {
                template = this.parseHtmlTemplate(result.getString("request"));
            }
            result.close();
            stmt.close();
            vegaConnection.close();
        } catch (SQLException sqlfehler) {
            System.out.println("getHtmlTemplate: Error Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        }
        return template;
    }

    /**
     * remove the important part from the Description
     *
     * @param apiCallRequest
     * @return
     */
    public String parseHtmlTemplateOld(String apiCallRequest) {
        String apiCallRequestISO = this.utf8_decode(apiCallRequest);
        int indexbeg = apiCallRequestISO.indexOf("<Description><![CDATA[") + 22;
        int indexend = apiCallRequestISO.indexOf("]]></Description>");
        String desc = apiCallRequestISO.substring(indexbeg, indexend);

        String output = desc.replace("´", "'");
        output = output.replace("##hochkomma##", "'");
        return output;
    }

    /**
     *
     * @param utf8_string
     * @return
     */
    public String utf8_decode(String utf8_string) {
        String ISO_string = utf8_string;
        try {
            byte[] bytes = ISO_string.getBytes("UTF-8");
            String s = new String(bytes, "UTF-8");
            byte[] winCode = s.getBytes("ISO-8859-1");
            ISO_string = new String(winCode, "UTF-8");
        } catch (Exception e) {
            System.out.println(" UnsupportedEncodingException "
                    + e.getMessage());
        }
        return ISO_string;
    }

    /**
     * remove the important part from the Description
     *
     * @param apiCallRequest
     * @return
     */
    public String parseHtmlTemplate(String apiCallRequest) {
        int indexbeg = apiCallRequest.indexOf("<Description>") + 13;
        int indexend = apiCallRequest.indexOf("</Description>");
        String desc = apiCallRequest.substring(indexbeg, indexend);

        String output = desc.replace("´", "'");
        output = desc.replace("�", "'");

        return output;
    }

    /**
     * remove the important part from the Description
     *
     * @param apiCallRequest
     * @return
     */
    public List parseAuctionPicture(String apiCallRequest) {
        int indexbeg = apiCallRequest.indexOf("<PictureURL>") + 12;
        int indexend = apiCallRequest.lastIndexOf("</PictureURL>");
        List pictureUrl = null;
        System.out.println("Begin index======" + indexbeg);
        if (indexbeg != 11) {
            String desc = apiCallRequest.substring(indexbeg, indexend);
            String output = desc.replace("´", "'");

            pictureUrl = new ArrayList();
            String urls[] = output.split("<PictureURL>");
            for (int i = 0; i < urls.length; i++) {
                System.out.println("" + urls[i].replace("</PictureURL>", ""));
                urls[i] = urls[i].replace("</PictureURL>", "");
                pictureUrl.add(urls[i].replace("</PictureURL>", ""));
            }
        }
        //return urls;
        return pictureUrl;
    }

    public String parseItemSpecifics(String apiCallRequest) {
        String desc = "";
        if (apiCallRequest.contains("<ItemSpecifics>")) {
            int indexbeg = apiCallRequest.indexOf("<ItemSpecifics>") + 15;
            int indexend = apiCallRequest.lastIndexOf("</ItemSpecifics>");
            desc = apiCallRequest.substring(indexbeg, indexend);
        }

        String output = desc.replace("´", "'");

        return output;
    }

    /**
     * @param object_id
     * @return
     */
    public String[] getObjectData(String object_id) {
        String[] objectData = new String[5];

        String SQL_Query = "SELECT objekt.bezeichnung, objekt.ort as city, objekt.strasse as street, "
                + "	  laender.bezeichnung as land, text.text as region"
                + "       FROM cusebeda.objekt, cusebeda.laender, cusebeda.verwaltungseinheiten,"
                + "		 	cumulida.finder, cumulida.text"
                + "			WHERE objekt.id='"
                + object_id
                + "'"
                + "			  AND objekt.laender_id = laender.id"
                + "			  AND laender.sprache_id ='"
                + this.language
                + "'"
                + "			  AND objekt.verwaltungseinheiten_id = verwaltungseinheiten.id"
                + "		      AND verwaltungseinheiten.finder_id = finder.id"
                + "			  AND finder.text_id = text.id"
                + "			  AND text.cusebeda_sprache_id ='" + this.language + "'";
        // Query
        dbConnection = new DBConnection();
        Connection vegaConnection = dbConnection.getVegaConnection();
        Statement stmt;
        ResultSet result;
        try {
            stmt = vegaConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);
            while (result.next()) {
                objectData[0] = result.getString("bezeichnung");
                objectData[1] = result.getString("land");
                objectData[2] = result.getString("city");
                objectData[3] = result.getString("region");
                objectData[4] = result.getString("street");
            }
            result.close();
            stmt.close();
            vegaConnection.close();
        } catch (SQLException sqlfehler) {
            System.out.println("Error Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        }

        return objectData;
    }

    /**
     *
     * @param object_id
     * @return
     */
    public void getAuctionPriceRangeMap(ArrayList<String> auctionIDs) {
        String act_table = "";

        Calendar cal = new GregorianCalendar();
        // Get the components of the time
        int hour24 = cal.get(Calendar.HOUR_OF_DAY); // 0..23

        // if it is an even hour
        if (hour24 % 2 == 1) {
            act_table = "1";
        } else {
            act_table = "2";
        }

        String auktion_arr = "(";
        for (int j = 0; j < auctionIDs.size(); j++) {
            if (j == auctionIDs.size() - 1) {
                auktion_arr += "'" + auctionIDs.get(j) + "'";
            } else {
                auktion_arr += "'" + auctionIDs.get(j) + "', ";
            }
        }

        auktion_arr += ")";

        // Period Query
        String SQL_Query = " SELECT cultbaydata.auktionen_" + act_table
                + ".auktion_id," + "		cultbaydata.vorlagen_" + act_table
                + ".sigma," + "		cultbaydata.vorlagen_" + act_table
                + ".ebaymittel" + "     		FROM cultbaydata.auktionen_"
                + act_table + ",cultbaydata.vorlagen_" + act_table
                + "				WHERE cultbaydata.auktionen_" + act_table
                + ".auktion_id IN" + auktion_arr
                + "	                 AND cultbaydata.vorlagen_" + act_table
                + ".vorlage_id = cultbaydata.auktionen_" + act_table
                + ".vorlage_id  ";
        // Query
        //System.out.println("Query for Auction Price Range Map:\n" + SQL_Query);
        dbConnection = new DBConnection();
        Connection vegaConnection = dbConnection.getVegaConnection();
        Statement stmt;
        ResultSet result;
        try {
            stmt = vegaConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);
            while (result.next()) {
                String[] priceRange = new String[2];
                priceRange[0] = "";
                priceRange[1] = "";
                Double rangeMin;
                Double rangeMax;

                Double sigma = result.getDouble("sigma");
                Double ebaymittel = result.getDouble("ebaymittel");
                // System.out.println(" priceRange: Auction
                // "+result.getString("auktion_id")+" sigma "+sigma+" ebaymittel
                // "+ebaymittel);
                if (ebaymittel == 0) {
                    // System.out.println("in ebaymittel/avg is zero\n\n");
//                    priceRange[0] = "http://www.cultbay.com/new_button_white1.gif";
                    priceRange[0] = "";
                    priceRange[1] = "";
                } else {
                    if (sigma == 0) {
                        //System.out.println("in sigma/sum is zero and mittel is not zero\n\n");
                        rangeMax = Math.ceil(ebaymittel + sigma);
                        priceRange[0] = "";
                        priceRange[1] = rangeMax.toString();
                    } else {
                        //System.out.println("in sigma/sum is not zero and mittel is not zero\n\n");
                        rangeMin = Math.floor(ebaymittel - sigma);
                        rangeMax = Math.ceil(ebaymittel + sigma);
                        priceRange[0] = rangeMin.toString();
                        priceRange[1] = rangeMax.toString();
                    }
                }
                this.priceRangeMap.put(result.getString("auktion_id"),
                        priceRange);
            }
            result.close();
            stmt.close();
            vegaConnection.close();
        } catch (SQLException sqlfehler) {
            System.out.println("getAuctionPriceRangeMap: Error Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        }
    }

    /**
     *
     * @param object_id
     * @return
     */
    public String[] getAuctionPriceRange(String auctionID) {
        String[] priceRange = new String[2];
        priceRange[0] = "";
        priceRange[1] = "";
        String act_table = null;

        Calendar cal = new GregorianCalendar();
        // Get the components of the time
        int hour24 = cal.get(Calendar.HOUR_OF_DAY); // 0..23
        // if it is an even hour
        if (hour24 % 2 == 1) {
            act_table = "1";
        } else {
            act_table = "2";
        }

        // Period Query
        String SQL_Query = " SELECT cultbaydata.auktionen_" + act_table
                + ".auktion_id," + "		cultbaydata.vorlagen_" + act_table
                + ".sigma," + "		cultbaydata.vorlagen_" + act_table
                + ".ebaymittel" + "     		FROM cultbaydata.auktionen_"
                + act_table + ",cultbaydata.vorlagen_" + act_table
                + "				WHERE cultbaydata.auktionen_" + act_table
                + ".auktion_id ='" + auctionID + "'"
                + "	                 AND cultbaydata.vorlagen_" + act_table
                + ".vorlage_id = cultbaydata.auktionen_" + act_table
                + ".vorlage_id  ";
        // Query
        //System.out.println("Price Range Query:" + SQL_Query);
        dbConnection = new DBConnection();
        Connection vegaConnection = dbConnection.getVegaConnection();
        Statement stmt;
        ResultSet result;
        try {
            stmt = vegaConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);
            while (result.next()) {
                Double rangeMin;
                Double rangeMax;
                Double sigma = result.getDouble("sigma");
                Double ebaymittel = result.getDouble("ebaymittel");
                // System.out.println(" priceRange: Auction "+auctionID+" sigma
                // "+sigma+" ebaymittel "+ebaymittel);
                if (ebaymittel == 0) {
                    //System.out.println("in ebaymittel/avg is zero\n\n");
                    //priceRange[0] = "http://www.cultbay.com/new_button_white1.gif";
                    priceRange[0] = "";
                    priceRange[1] = "";
                } else {
                    if (sigma == 0) {
                        //System.out.println("in sigma/sum is zero and mittel is not zero\n\n");
                        rangeMax = Math.ceil(ebaymittel + sigma);
                        priceRange[0] = "";
                        priceRange[1] = rangeMax.toString();
                    } else {
                        //System.out.println("in sigma/sum is not zero and mittel is not zero\n\n");
                        rangeMin = Math.floor(ebaymittel - sigma);
                        rangeMax = Math.ceil(ebaymittel + sigma);
                        priceRange[0] = rangeMin.toString();
                        priceRange[1] = rangeMax.toString();
                    }
                }
            }
            result.close();
            stmt.close();
            vegaConnection.close();
        } catch (SQLException sqlfehler) {
            System.out.println("getAuctionPriceRange: Error Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        }
        return priceRange;
    }

    public void setCurrencies() {
        // Period Query
        String SQL_Query = "SELECT ebaystammdaten.siteid.id, ebaystammdaten.currency.kurz"
                + " FROM ebaystammdaten.currency, ebaystammdaten.siteid"
                + "		WHERE "
                + "			ebaystammdaten.currency.id = ebaystammdaten.siteid.currency_id";
        // Query
        dbConnection = new DBConnection();
        Connection vegaConnection = dbConnection.getVegaConnection();
        Statement stmt;
        ResultSet result;
        try {
            stmt = vegaConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);
            while (result.next()) {
                this.currencyMap.put(result.getString(1), result.getString(2));
            }
            result.close();
            stmt.close();
            vegaConnection.close();
        } catch (SQLException sqlfehler) {
            System.out.println("setCurrencies: Error Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        }
    }

    public String getSiteCurrency(String siteID) {
        String siteCurrency = "";
        // Period Query
        String SQL_Query = "SELECT ebaystammdaten.siteid.id, ebaystammdaten.currency.kurz"
                + " FROM ebaystammdaten.currency, ebaystammdaten.siteid"
                + "		WHERE "
                + "			ebaystammdaten.currency.id = ebaystammdaten.siteid.currency_id"
                + "			AND siteid.id ='" + siteID + "'";
        // Query
        dbConnection = new DBConnection();
        Connection vegaConnection = dbConnection.getVegaConnection();
        Statement stmt;
        ResultSet result;
        try {
            stmt = vegaConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);
            while (result.next()) {
                siteCurrency = result.getString(2);
            }
            result.close();
            stmt.close();
            vegaConnection.close();
        } catch (SQLException sqlfehler) {
            System.out.println("setCurrencies: Error Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        }
        return siteCurrency;
    }

    public boolean checkIfRunningAuction(String AuctionID) {
        boolean isRunning = false;
        String SQL_Query = " SELECT auktion.id" + "	FROM ebay.auktion"
                + "		WHERE auktion.ebayitemid = '" + AuctionID + "'";

        // Query
        dbConnection = new DBConnection();
        Connection regorConnection = dbConnection.getRegorConnection();
        Statement stmt;
        ResultSet result;
        try {
            stmt = regorConnection.createStatement();
            result = stmt.executeQuery(SQL_Query);
            while (result.next()) {
                isRunning = true;
            }
            result.close();
            stmt.close();
            regorConnection.close();
        } catch (SQLException sqlfehler) {
            System.out.println("Error Occured during receving Data from the DB(checkIfRunningAuction)");
            System.out.println(sqlfehler.getMessage());
        }
        System.out.println("checkIfRunningAuction: " + AuctionID
                + " In ebay.auktion " + isRunning);
        return isRunning;
    }

    public String getAuctionURL(String siteID) {
        String URL = null;

        if (siteID.equals("0")) {
            URL = "http://cgi.ebay.com/ws/eBayISAPI.dll?ViewItem&item=";
        } else if (siteID.equals("3")) {
            URL = "http://cgi.ebay.co.uk/ws/eBayISAPI.dll?ViewItem&item=";
        } else if (siteID.equals("77")) {
            URL = "http://cgi.ebay.de/ws/eBayISAPI.dll?ViewItem&item=";
        } else if (siteID.equals("15")) {
            URL = "http://cgi.ebay.com.au/ws/eBayISAPI.dll?ViewItem&item=";
        } else if (siteID.equals("71")) {
            URL = "http://cgi.ebay.fr/ws/eBayISAPI.dll?ViewItem&item=";
        } else if (siteID.equals("101")) {
            URL = "http://cgi.ebay.it/ws/eBayISAPI.dll?ViewItem&item=";
        } else if (siteID.equals("16")) {
            URL = "http://cgi.ebay.at/ws/eBayISAPI.dll?ViewItem&item=";
        } else if (siteID.equals("193")) {
            URL = "http://cgi.ebay.ch/ws/eBayISAPI.dll?ViewItem&item=";
        } else if (siteID.equals("146")) {
            URL = "http://cgi.ebay.nl/ws/eBayISAPI.dll?ViewItem&item=";
        } else {
            URL = "http://cgi.ebay.de/ws/eBayISAPI.dll?ViewItem&item=";
        }

        return URL;
    }

    public String getAuctionMarket(String siteID) {
        String URL = null;
        if (siteID.equals("0")) {
            URL = "2WS_ebay.com";
        } else if (siteID.equals("3")) {
            URL = "2WS_ebay.co.uk";
        } else if (siteID.equals("77")) {
            URL = "2WS_ebay.de";
        } else if (siteID.equals("15")) {
            URL = "2WS_ebay.com.au";
        } else if (siteID.equals("71")) {
            URL = "2WS_ebay.fr";
        } else if (siteID.equals("101")) {
            URL = "2WS_ebay.it";
        } else if (siteID.equals("16")) {
            URL = "2WS_ebay.at";
        } else if (siteID.equals("193")) {
            URL = "2WS_ebay.ch";
        } else if (siteID.equals("146")) {
            URL = "2WS_ebay.nl";
        } else {
            URL = "2WS_ebay.de";
        }

        return URL;
    }
    
    public String getZuzzuItemid(String ebayitemid){
        String zuzzuid="";
        String zuzzuIdQuery="select oxe.itemid from zuzzu.offer_x_ebayitemid oxe where oxe.ebayitemid="+ebayitemid+" limit 1";
        
        dbConnection = new DBConnection();
        Connection zuzzuConnection = dbConnection.getZuzzuConnection();
        Statement stmt;
        ResultSet result;
        try {
            stmt = zuzzuConnection.createStatement();
            result = stmt.executeQuery(zuzzuIdQuery);
            while (result.next()) {
                
              zuzzuid=result.getString("itemid");
            }
            result.close();
            stmt.close();
            zuzzuConnection.close();
        } catch (SQLException sqlfehler) {
            System.out.println("Error Occured during receving Data from the DB");
            System.out.println(sqlfehler.getMessage());
        }
        
        return zuzzuid;
    }
    
    
    public int getProductId(String ebayitemid){
    int productId=0;
    
    String productIdQuery="select v.arrangement_id from ebay.auktion ak,ebay.vorlage v where ak.vorlage_id=v.id and v.arrangement_id !=0 ak.ebayitemid="+ebayitemid+" limit 1";
    dbConnection = new DBConnection();
    Connection zuzzuConnection = dbConnection.getVegaConnection();
    Statement stmt;
    ResultSet result;
    try {
    stmt = zuzzuConnection.createStatement();
    result = stmt.executeQuery(productIdQuery);
    while (result.next()) {
    
    productId=result.getInt("arrangement_id");
    }
    result.close();
    stmt.close();
    zuzzuConnection.close();
    } catch (SQLException sqlfehler) {
    System.out.println("Error Occured during receving Data from the DB");
    System.out.println(sqlfehler.getMessage());
    }
    
    
    return productId;
   
    }
}


