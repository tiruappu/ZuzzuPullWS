package zuzzudynamicproj;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.cultuzz.util.DBConnection;
import com.cultuzz.zuzzu.ebay.AnyType;
import com.cultuzz.zuzzu.ebay.impl.AnyTypeImpl;
import com.cultuzz.zuzzu.ebay.impl.CurrentPriceBidCountRQImpl;
import com.cultuzz.zuzzu.ebay.impl.CurrentPriceBidCountRSImpl;
import com.cultuzz.zuzzu.ebay.impl.CurrentPriceBidCountRSTypeImpl;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.json.JSONObject;
import org.json.XML;

/**
 *
 * @author kondalarao
 */
public class CurrentPriceBidService {

    public String response(String request) {
        DBConnection util = new DBConnection();
        JAXBContext jc = util.getJAXBContext();
        CurrentPriceBidCountRSImpl cpbcrs = null;
        CurrentPriceBidCountRSTypeImpl.ItemStatusTypeImpl isti = null;
        boolean status = false;
        StringWriter sw = new StringWriter();
        Marshaller m = null;
        Unmarshaller un = null;
        cpbcrs = new CurrentPriceBidCountRSImpl();
        JSONObject jo = null;
        CurrentPriceBidCountRQImpl cbplreq = null;
        Statement stmt = null;
        ResultSet rs = null;
        Connection con = null;
        try {
            m = jc.createMarshaller();
            un = jc.createUnmarshaller();
            ByteArrayInputStream bis = new ByteArrayInputStream(request.getBytes("UTF-8"));
            cbplreq = (CurrentPriceBidCountRQImpl) un.unmarshal(bis);
            System.out.println("itemid" + cbplreq.getItemId());
            String[] priceRange = new String[2];
            if (cbplreq.isSetItemId() && cbplreq.getItemId().length() > 0) {
                String ItemID = cbplreq.getItemId();
                con = util.getRegorConnection();

                BigDecimal currentPrice = null;
                int bidCount = 0;
                stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                rs = stmt.executeQuery("select id,currentbid,anzahlgebote from ebay.auktion where ebayitemid = " + ItemID);
                if (rs.next()) {
                    System.out.println("in if");
                    status = true;
                    currentPrice = rs.getBigDecimal("currentbid");
                    bidCount = rs.getInt("anzahlgebote");
                    priceRange = this.getAuctionPriceRangeForCurrentBidPrice(rs.getString("id"));
                } else {
                    System.out.println("in else");
                    con = util.getMysqlJDBCConnection();
                    stmt = con.createStatement();
                    rs = stmt.executeQuery("select CurrentBid,Bids from  3WS_ebaydb.CultbayResponseType_HotelPac_4 where PackageCode = " + ItemID);
                    if (rs.next()) {
                        status = true;
                        currentPrice = rs.getBigDecimal(1);
                        bidCount = rs.getInt(2);
                        priceRange[0] = "-";
                        priceRange[1] = "";
                    }
                }

                isti = new CurrentPriceBidCountRSTypeImpl.ItemStatusTypeImpl();
                AnyType at = new AnyTypeImpl();
                if (status) {
                    isti.setSuccess(at);
                    isti.setBidCount(bidCount);
                    isti.setCurrentPrice(currentPrice);
                    isti.setPriceRange(priceRange[0] + " " + priceRange[1]);
                    cpbcrs.setItemStatus(isti);
                } else {
                    cpbcrs.setError("ItemId Not Found");
                }

                m.marshal(cpbcrs, sw);
                System.out.println("response xml  \n\n" + sw.toString());
            } else {
                System.out.println("print in else of itemid length");
                cpbcrs.setError("Invalid ItemID");
                m.marshal(cpbcrs, sw);
            }

        } catch (Exception e) {
            try {
                //e.printStackTrace();
                cpbcrs.setError("Error");
                m.marshal(cpbcrs, sw);
            } catch (JAXBException ex) {
                Logger.getLogger(CurrentPriceBidService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            jo = XML.toJSONObject(sw.toString());
            System.out.println("" + jo);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                con.close();
            } catch (Exception e) {
                System.out.println("In finally of CurrentPriceBidRequest" + e.getMessage());
            }
        }
        return jo.toString();
    }

    public String[] getAuctionPriceRangeForCurrentBidPrice(String auctionID) {
        //System.out.println("in getAuctionPriceRange \n\n");
        String[] priceRange = new String[2];
        priceRange[0] = "";
        priceRange[1] = "";
        String act_table = null;
        // Values for the dynamic CultbayData-Database
        Calendar cal = new GregorianCalendar();
        // Get the components of the time
        int hour24 = cal.get(Calendar.HOUR_OF_DAY); // 0..23
        // if it is an even hour
        if (hour24 % 2 == 1) {
            //System.out.println("in calculation odd hour of the day in getAuctionPriceRange\n\n");
            act_table = "1";
        } else {
            act_table = "2";
            //System.out.println("in calculation even hour of the day in getAuctionPriceRange\n\n");
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
        DBConnection dbConnection = new DBConnection();
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
//                    priceRange[0] = "http://www.cultbay.com/new_button_white1.gif";
//                    priceRange[1] = "";
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

    /*    public static void main(String[] args) {
     String req = "<?xml version='1.0' encoding='UTF-8'?><CurrentPriceBidCountRQ><ItemId>330803057994</ItemId></CurrentPriceBidCountRQ>";
     CurrentPriceBidService cpb = new CurrentPriceBidService();
     //cpb.response(req);
     System.out.println("response" + cpb.response(req));
     }*/
}
