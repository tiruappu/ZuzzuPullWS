package zuzzudynamicproj;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author sreenivas
 */

import com.cultuzz.util.DBConnection;
import com.cultuzz.zuzzu.ebay.UpdateCountriesRegionsPlacesRSType.CountriesType.CountryType.RegionsType;
import com.cultuzz.zuzzu.ebay.UpdateCountriesRegionsPlacesRSType.CountriesType.CountryType.RegionsType.RegionType.PlacesType;
import com.cultuzz.zuzzu.ebay.impl.UpdateCountriesRegionsPlacesRSImpl;
import com.cultuzz.zuzzu.ebay.impl.UpdateCountriesRegionsPlacesRSTypeImpl.CountriesTypeImpl;
import com.cultuzz.zuzzu.ebay.impl.UpdateCountriesRegionsPlacesRSTypeImpl.CountriesTypeImpl.CountryTypeImpl;
import com.cultuzz.zuzzu.ebay.impl.UpdateCountriesRegionsPlacesRSTypeImpl.CountriesTypeImpl.CountryTypeImpl.RegionsTypeImpl;
import com.cultuzz.zuzzu.ebay.impl.UpdateCountriesRegionsPlacesRSTypeImpl.CountriesTypeImpl.CountryTypeImpl.RegionsTypeImpl.RegionTypeImpl;
import com.cultuzz.zuzzu.ebay.impl.UpdateCountriesRegionsPlacesRSTypeImpl.CountriesTypeImpl.CountryTypeImpl.RegionsTypeImpl.RegionTypeImpl.PlacesTypeImpl;
import java.io.StringWriter;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.*;


public class CountriesRegionsPlacesServ {

    /**
     * @param args the command line arguments
     */
    public String response(String request) {
        UpdateCountriesRegionsPlacesRSImpl ucrp = new UpdateCountriesRegionsPlacesRSImpl();
        CountriesTypeImpl cts = new CountriesTypeImpl();
        //System.out.println("Inside mainnnnnnnnnn");
        try {
            String country = "", region = "";
            Connection con = new DBConnection().getMysqlJDBCConnection();
            Statement st1 = con.createStatement();
           // String q = "select distinct country from CultbayGeoTagsResponseType_G_3";
            String q = "select distinct country,iso1_code from CultbayGeoTagsResponseType_G_3 g, countries c where lower(g.country)=lower(c.name)";
            System.out.println(q);
            ResultSet rs2, rs3 = null;
            Statement st2, st3 = null;
            ResultSet rs1 = st1.executeQuery(q);
            st2 = con.createStatement();
            st3 = con.createStatement();
            CountryTypeImpl ct = null;
            RegionTypeImpl rt = null;
            PlacesType pts = null;
            while (rs1.next()) {
                country = rs1.getString(1);
                //System.out.println("Countryyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy " + rs1.getString(1));
                ct = new CountryTypeImpl();
                RegionsType rts = new RegionsTypeImpl();
                ct.setName(rs1.getString(1));
                ct.setCode(rs1.getString(2));
                String query = "select distinct region from CultbayGeoTagsResponseType_G_3 where country='" + country + "'";
                //System.out.println("Queryyyyyyyyyyyyyyyyyyyyyyyyyy" + query);
                rs2 = st2.executeQuery(query);
                while (rs2.next()) {
                    rt = new RegionTypeImpl();
                    rt.setName(rs2.getString(1));
                    region = rs2.getString(1).replaceAll("'", "\\\\'");
                    //System.out.println("Region********************** " + rs2.getString(1));
                    String query1 = "select distinct place from CultbayGeoTagsResponseType_G_3 where country='" + country + "' and region='" + region + "'";
                    rs3 = st3.executeQuery(query1);
                    pts = new PlacesTypeImpl();
                    while (rs3.next()) {
                        pts.getPlace().add(rs3.getString(1));
                        //System.out.println("Result Place " + rs3.getString(1));
                    }
                    rt.setPlaces(pts);
                    rts.getRegion().add(rt);
                }

                ct.setRegions(rts);
                cts.getCountry().add(ct);
            }


            ucrp.setCountries(cts);
        } catch (Exception e) {
            e.printStackTrace();
        }


        //ByteArrayInputStream bis = new ByteArrayInputStream(request.getBytes("UTF-8"));
        JAXBContext jc = null;
        try {
            jc = JAXBContext.newInstance("com.cultuzz.zuzzu.ebay");
        } catch (JAXBException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            Unmarshaller un = jc.createUnmarshaller();
        } catch (Exception e) {
            System.out.println("Unmarshall exception");
        }
        try {
            Marshaller m = jc.createMarshaller();
            StringWriter sw = new StringWriter();
            m.marshal(ucrp, sw);
           return sw.toString();
            //System.out.println("Responseeeeeeeeeeeeeeeeeee" + sw.toString());
        } catch (JAXBException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
            return ex.getMessage();
        }





    }
}