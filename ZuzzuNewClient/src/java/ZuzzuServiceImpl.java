/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceRef;
import zuzzudynamicproj.DynamicRequestDispatcher_Service;

/**
 *
 * @author tirupathi
 */
public class ZuzzuServiceImpl extends HttpServlet {

    /* @WebServiceRef(wsdlLocation = "WEB-INF/wsdl/localhost_8080/Zuzzu2WS/DynamicRequestDispatcher.wsdl")
    private DynamicRequestDispatcher_Service service;*/

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/xml;charset=UTF-8");
        String req = request.getParameter("Cultbay_RQ");
        PrintWriter pw = response.getWriter();
        if(req.contains("CurrentPriceBidCountRQ")||req.contains("UpdateCountriesRegionsPlacesRQ")){
            System.out.println("Started");
           String responsexml =this.response(req);
            System.out.println(""+responsexml);
            pw.write(responsexml);
        }else{
            System.out.println("Started"+req);
            
            String responsexmls=this.cultbayRequest(req);
            
            System.out.println(""+responsexmls);
            pw.write(responsexmls);
            
        }
        
        
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private String cultbayRequest(java.lang.String arg0) {
        // Note that the injected javax.xml.ws.Service reference as well as port objects are not thread safe.
        // If the calling of port operations may lead to race condition some synchronization is required.
        DynamicRequestDispatcher_Service service=new DynamicRequestDispatcher_Service();
        zuzzudynamicproj.DynamicRequestDispatcher port = service.getDynamicRequestDispatcherPort();
        ((BindingProvider) port).getRequestContext().put("javax.xml.ws.client.receiveTimeout", "600000");
        return port.cultbayRequest(arg0);
    }

    private String response(java.lang.String arg0) {
        // Note that the injected javax.xml.ws.Service reference as well as port objects are not thread safe.
        // If the calling of port operations may lead to race condition some synchronization is required.
        DynamicRequestDispatcher_Service service1=new DynamicRequestDispatcher_Service();
        zuzzudynamicproj.DynamicRequestDispatcher port = service1.getDynamicRequestDispatcherPort();
        ((BindingProvider) port).getRequestContext().put("javax.xml.ws.client.receiveTimeout", "600000");
        return port.response(arg0);
    }
    
    

}
