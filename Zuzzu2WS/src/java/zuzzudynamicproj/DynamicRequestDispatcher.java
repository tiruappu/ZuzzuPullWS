/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zuzzudynamicproj;

import com.cultbay.zuzzu.CultBayRequestHandler;
import com.cultbay.zuzzu.utils.CulBayXMLParser;
import java.net.URLDecoder;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;

/**
 *
 * @author tirupathi
 */
@WebService(serviceName = "DynamicRequestDispatcher")
public class DynamicRequestDispatcher {

    /**
     * This is a sample web service operation
     */
    
     CulBayXMLParser parseCulBayXML = null;
     CultBayRequestHandler cbrh = null;
     
     @WebMethod(operationName = "response")
    public String response(String request) {
        String response = "";
        try {
            long s1 = System.currentTimeMillis();
            String req = URLDecoder.decode(request, "UTF-8");
            System.out.println("Request in DynamicRequestDispatcher" + request);
            if (req.contains("<UpdateCountriesRegionsPlacesRQ")) {
                response = new CountriesRegionsPlacesServ().response(req);
                return response;
            } else if (req.contains("<CurrentPriceBidCountRQ")) {
                response = new CurrentPriceBidService().response(req);
                long s2 = System.currentTimeMillis();
                System.out.println("\n\n\nResponse timeeeeeeeeeeeee" + (s2 - s1));
                return response;
            }
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Error>Invalid Request</Error>";
        } catch (Exception ex) {
            ex.printStackTrace();
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Error>Invalid Request</Error>";
        }
    }
     @WebMethod(operationName = "cultbayRequest")
    public String cultbayRequest(String request) {
        String resultString = null;
        try {
            //System.out.println("Request from before decode"+request);
            String req = URLDecoder.decode(request, "UTF-8");
            //System.out.println("Request after decode"+req);
            /*"--- Cultbay2ndWS Request Start Processing  ----";
             xml_input
             "--- Cultbay2ndWS Request END OF XML_INPUT";*/
            parseCulBayXML = new CulBayXMLParser();
            cbrh = new CultBayRequestHandler();
            String output = "";
            // first try to parse the XML-Request
            //System.out.println("in cultbayreq before decode"+request+"\n after decode"+req);
            boolean result = parseCulBayXML.parse(req);
            if (result == true) {
                //System.out.println("parser result: Successfull ("+result+")\n");
                int xmlFileType = parseCulBayXML.getXMLFileTyp();
                System.out.println("XmlFile Type    " + xmlFileType + "\nPeriodBased" + parseCulBayXML.getPeriodSet());
                output = cbrh.handleRequest(xmlFileType, parseCulBayXML);
                System.out.println("---  processing: Successfull  ---");
                if (xmlFileType == 1) {
                   // System.out.println("Output : "+output);                    
                }
                resultString = output.trim();
            } else {
                resultString = "<?xml version='1.0' encoding='UTF-8'?>\n"
                        + "<error>parser result: Unsuccessfull (" + result + ") Reason "
                        + parseCulBayXML.getErrorCode() + "</error>";
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("cultbayRequest: Error Occured during receving Data from the DB");
            System.out.println(e.getMessage());
        }
        return resultString;
    }
    /*public static void main(String[] args) {
     DynamicRequestDispatcher drd = new DynamicRequestDispatcher();
     //drd.cultbayRequest("<?xml version='1.0' encoding='UTF-8'?><Cultbay_Request><Location><Country>Germany</Country><Region></Region><Place></Place></Location><Period><EarliestArrival>2009-01-19</EarliestArrival><LatestDeparture>2009-01-21</LatestDeparture><Persons>2</Persons><Accommodations>2</Accommodations></Period></Cultbay_Request>");
     drd.cultbayRequest("<?xml version='1.0' encoding='UTF-8'?><Cultbay_Request><Location><Country>Germany</Country><Region></Region><Place></Place></Location></Cultbay_Request>");
     //System.out.println(drd.cultbayRequest("<?xml version='1.0' encoding='UTF-8'?><Cultbay_AuctionDetailsRQ><AuctionID>180300616316</AuctionID></Cultbay_AuctionDetailsRQ>"));
     }*/
    
    
    
}
