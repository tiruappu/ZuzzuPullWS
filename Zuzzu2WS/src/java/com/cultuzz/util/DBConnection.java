/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cultuzz.util;

import java.sql.Connection;
import java.sql.DriverManager;
import javax.xml.bind.JAXBContext;

/**
 *
 * @author kondalarao
 */
public class DBConnection {

     private JAXBContext jaxbc;
     
    private Connection vegeConnection;
    private Connection regorConnection;
    private Connection zuzzuConnection;

    public Connection getVegaConnection() {
        if (vegeConnection == null) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                //vegeConnection = DriverManager.getConnection("jdbc:mysql://10.50.1.84/cusebeda", "CultbayWSRW", "cUlT8$yW5rW");
                vegeConnection = DriverManager.getConnection("jdbc:mysql://91.203.200.116:8080/cusebeda","accounting","accounting23!");
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Vega connection exception");
            }
        }
        return vegeConnection;
    }

    public Connection getRegorConnection() {
        if (regorConnection == null) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                //regorConnection = DriverManager.getConnection("jdbc:mysql://10.50.1.95/ebay", "CultbayWSRW", "cUlT8$yW5rW");
                regorConnection = DriverManager.getConnection("jdbc:mysql://91.203.200.116:8080/ebay","accounting","accounting23!");
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Regor connection");
            }
        }
        return regorConnection;
    }
    
    public Connection getZuzzuConnection() {
        if(zuzzuConnection==null){
            
            try{
                Class.forName("com.mysql.jdbc.Driver");
                zuzzuConnection = DriverManager.getConnection("jdbc:mysql://91.203.200.116:8080/zuzzu","accounting","accounting23!");
            }catch(Exception ex){
                ex.printStackTrace();
                System.out.println("Zuzzu Connection");
            }
            
        }
        return zuzzuConnection;
        
    }
    
    public JAXBContext getJAXBContext() {
        if (jaxbc == null) {
            try {
                System.out.println("before jaxb context");
                jaxbc = JAXBContext.newInstance("com.cultuzz.zuzzu.ebay");
            } catch (Exception ex) {
                System.out.println("Exception at jaxb context object");
                ex.printStackTrace();
            }
        }
        return jaxbc;
    }
    
    public Connection getMysqlJDBCConnection() {

        Connection con = null;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            con = DriverManager.getConnection("jdbc:mysql://zosma.cultuzz.com/CultGeoInfo", "sreenivas", "");


        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
        }
        return con;
    }
}
