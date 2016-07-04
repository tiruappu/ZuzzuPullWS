/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cultbay.zuzzu;

import com.cultuzz.util.DBConnection;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

/**
 *
 * @author kondalarao
 */
class CultChannel {

        Connection regor_con = null;
        Connection vega_con = null;
        DBConnection  javaMySQLConnectionEbay =null;
        public ArrayList<String> arrangement_ids = null;
        public ArrayList<String> object_ids = null;
        private ArrayList<Integer> forbiddenModules = new ArrayList();
        Statement st = null;
        private int m_iDistributor;
        private int m_bCA_Dist;
        private int m_iObjekt;

        public CultChannel() {
                System.out.println("Before creating MSCU object");
                javaMySQLConnectionEbay = new DBConnection();
                System.out.println("Afver create MSCU "+javaMySQLConnectionEbay);
                System.out.println("AFter createing connection ");
                this.vega_con = javaMySQLConnectionEbay.getVegaConnection();
                System.out.println("After getting Connection : "+this.vega_con);
        }
        
        public Object[][] askCultChannel(int dist_id, String earliest_arrival, String latest_arrival, int room_id, int bca_distId) {
                System.out.println("In AskCultChannel");
                Object[][] arrayOfObject = new Object[1][3];
                try {
                        System.out.println("Earliest Arrival : "+earliest_arrival);
                        System.out.println("Latest Departure : "+latest_arrival);
                        java.sql.Date localDate1 = java.sql.Date.valueOf(earliest_arrival);
                        java.sql.Date localDate2 = java.sql.Date.valueOf(latest_arrival);
                        System.out.println("Earleist DAtae : "+localDate1+" Latest Departure : "+localDate2);
                        if (dist_id == 0) {
                                throw new Exception("Fehlerhafte Anfrage: DistributorId <EMPTY>");
                        }
                        if ((bca_distId != 0) && (bca_distId != 1)) {
                                throw new Exception("Fehlerhafte Anfrage: iDist_type <EMPTY>");
                        }
                        if (room_id == 0) {
                                throw new Exception("Fehlerhafte Anfrage: iZiwekat <EMPTY>");
                        }
                        if (localDate2.before(localDate1)) {
                                throw new Exception("Fehlerhafte Anfrage: Abfragezeitraum fehlerhaft <end before start>");
                        }
                        System.out.println("Room ID : "+room_id);
                        arrayOfObject = askByTimespan(dist_id, localDate1, localDate2, room_id, bca_distId);
                } catch (Exception localCultChannelException) {
                        localCultChannelException.printStackTrace();
                } finally {
                        System.gc();
                }
                System.out.println("Before Returnig array object");
                return arrayOfObject;
        }

        private Object[][] askByTimespan(int paramInt1, java.sql.Date paramDate1, java.sql.Date paramDate2, int paramInt2, int paramInt3)
                throws Exception {
                System.out.println("In ASkBy Timespanc");
                TreeMap localTreeMap = new TreeMap();
                this.m_iDistributor = paramInt1;
                this.m_bCA_Dist = paramInt3;
                try {
                        localTreeMap = getAvailabilitiesByTimespan(paramDate1, paramDate2, Integer.valueOf(paramInt2));
                        Object[][] arrayOfObject1 = processAvailabilities(localTreeMap);
                        Object[][] arrayOfObject2 = arrayOfObject1;
                        return arrayOfObject2;
                } catch (Exception localException) {
                        throw new Exception("Fehler in CultChannel.askModule()");
                }
        }

        private TreeMap<Integer, Verfuegbarkeit> getAvailabilitiesByTimespan(java.sql.Date paramDate1, java.sql.Date paramDate2, Integer paramInteger) {
                System.out.println("In getAvailByTimspan");
                TreeMap localTreeMap = new TreeMap();
                String str = "SELECT * FROM auhoveda.verfuegbarkeit WHERE  hofesoda_ziwekat_id = " + paramInteger + " " + "AND    datum >= '" + paramDate1.toString() + "' " + "AND    datum <= '" + paramDate2.toString() + "' " + "ORDER BY datum ASC";
                try {
                        st = vega_con.createStatement();
                        ResultSet rs = st.executeQuery(str);
                        rs.beforeFirst();
                        while (rs.next()) {
                                System.out.println("Beofre seting Verfuegbarkeit object");
                                Verfuegbarkeit localVerfuegbarkeit = new Verfuegbarkeit();
                                System.out.println("vId : "+rs.getInt("id"));
                                localVerfuegbarkeit.setId(Integer.valueOf(rs.getInt("id")));
                                System.out.println("vObjectID : "+rs.getInt("cusebeda_objekt_id"));
                                localVerfuegbarkeit.setCusebedaObjektId(Integer.valueOf(rs.getInt("cusebeda_objekt_id")));
                                System.out.println("vhzid : "+rs.getInt("hofesoda_ziwekat_id"));
                                localVerfuegbarkeit.setHofesodaZiwekatId(rs.getInt("hofesoda_ziwekat_id"));
                                System.out.println("vdate : "+rs.getDate("datum"));
                                localVerfuegbarkeit.setDatum(rs.getDate("datum"));
                                System.out.println("vAnzahl : "+rs.getInt("anzahl"));
                                localVerfuegbarkeit.setAnzahl(rs.getInt("anzahl"));
                                System.out.println("vprice : "+rs.getInt("sonderpreis"));
                                localVerfuegbarkeit.setSonderpreis(Integer.valueOf(rs.getInt("sonderpreis")));
                                localVerfuegbarkeit.setHofesodaSaisonId(rs.getInt("hofesoda_saison_id"));
                                localVerfuegbarkeit.setManuell(rs.getInt("manuell"));
                                localVerfuegbarkeit.setHofesodaGarantietypId(rs.getInt("hofesoda_garantietyp_id"));
                                if ((localVerfuegbarkeit.getAnzahl() > 0) && (localVerfuegbarkeit.getHofesodaSaisonId() > 0)) {
                                        localVerfuegbarkeit.setChannelPrice(Integer.valueOf(rs.getInt("sonderpreis")));
                                } else {
                                        localVerfuegbarkeit.setChannelPrice(new Integer(0));
                                }
                                localTreeMap.put(localVerfuegbarkeit.getId(), localVerfuegbarkeit);
                        }
                        rs.close();
                } catch (SQLException localSQLException) {
                        localSQLException.printStackTrace();
                }
                return localTreeMap;
        }

        private Object[][] processAvailabilities(TreeMap<Integer, Verfuegbarkeit> paramTreeMap)
                throws Exception {
                System.out.println("In process Availabilites ");
                try {
                        Object[][] arrayOfObject = new Object[paramTreeMap.size()][3];
                        int i = 0;
                        TreeMap localTreeMap1 = sortAvailabilitiesByObjekt(paramTreeMap);
                        Set localSet1 = localTreeMap1.keySet();
                        Iterator localIterator1 = localSet1.iterator();
                        while (localIterator1.hasNext()) {
                                Integer localInteger1 = (Integer) localIterator1.next();
                                this.m_iObjekt = localInteger1.intValue();
                                TreeMap localTreeMap2 = (TreeMap) localTreeMap1.get(localInteger1);
                                if (localTreeMap2.size() > 0) {
                                        askModules(localTreeMap2);
                                }
                                Set localSet2 = localTreeMap2.keySet();
                                Iterator localIterator2 = localSet2.iterator();
                                while (localIterator2.hasNext()) {
                                        Integer localInteger2 = (Integer) localIterator2.next();
                                        Verfuegbarkeit localVerfuegbarkeit = (Verfuegbarkeit) localTreeMap2.get(localInteger2);
                                        arrayOfObject[i][0] = Integer.valueOf(localVerfuegbarkeit.getId().intValue());
                                        arrayOfObject[i][1] = Integer.valueOf(localVerfuegbarkeit.getChannelPrice().intValue());
                                        arrayOfObject[i][2] = localVerfuegbarkeit.getDatum().toString();
                                        i++;
                                }
                        }
                        return arrayOfObject;
                } catch (Exception localException) {
                        System.out.println(localException.getMessage());
                }
                throw new Exception("ERROR IN processAvailabilities");
        }

        private TreeMap<Integer, TreeMap<Integer, Verfuegbarkeit>> sortAvailabilitiesByObjekt(TreeMap<Integer, Verfuegbarkeit> paramTreeMap)
                throws Exception {
                System.out.println("In sortAvailByObject");
                TreeMap localTreeMap1 = new TreeMap();
                Set localSet = paramTreeMap.keySet();
                Iterator localIterator = localSet.iterator();
                while (localIterator.hasNext()) {
                        Verfuegbarkeit localVerfuegbarkeit = (Verfuegbarkeit) paramTreeMap.get(localIterator.next());
                        TreeMap localTreeMap2;
                        if (localTreeMap1.containsKey(localVerfuegbarkeit.getCusebedaObjektId())) {
                                localTreeMap2 = (TreeMap) localTreeMap1.get(localVerfuegbarkeit.getCusebedaObjektId());
                                localTreeMap2.put(localVerfuegbarkeit.getId(), localVerfuegbarkeit);
                        } else {
                                localTreeMap2 = new TreeMap();
                                localTreeMap2.put(localVerfuegbarkeit.getId(), localVerfuegbarkeit);
                                localTreeMap1.put(localVerfuegbarkeit.getCusebedaObjektId(), localTreeMap2);
                        }
                }
                return localTreeMap1;
        }

        private void askModules(TreeMap<Integer, Verfuegbarkeit> paramTreeMap)
                throws Exception {
                Vector localVector1 = getActiveGlobalModules();
                Vector localVector2 = getActiveModules();
                String str1 = "";
                if (this.m_bCA_Dist == 1) {
                        str1 = "AND (oneway = 1 || oneway = 2) ";
                } else {
                        str1 = "AND (oneway = 0 || oneway = 1) ";
                }
                try {
                        Integer localInteger;
                        for (int i = 0; i < localVector2.size(); i++) {
                                localInteger = (Integer) localVector2.get(i);
                                int j = localVector1.indexOf(localInteger);
                                if (j != -1) {
                                        localVector1.remove(j);
                                }
                        }
                        String str2;
                        ResultSet localResultSet;
                        String str3;
                        String str4;
                        Class localClass;
                        Constructor[] arrayOfConstructor;
                        Object localObject;
                        int i;
                        for (i = 0; i < localVector1.size(); i++) {
                                localInteger = (Integer) localVector1.get(i);
                                if (!this.forbiddenModules.contains(localInteger)) {
                                        str2 = "SELECT name FROM distributoren.cc_modules WHERE cc_modules.id = " + localInteger + " " + str1 + "AND java_ws = 1 LIMIT 1";
                                        st = vega_con.createStatement();
                                        ResultSet rs = st.executeQuery(str2);
                                        int j = 0;
                                        for (j = 0; rs.next(); j++);
                                        if (j == 1) {
                                                rs.first();
                                                str3 = rs.getString("name");
                                                str4 = "com.cultbay.zuzzu.module.Module_" + str3;
                                                localClass = Class.forName(str4);
                                                arrayOfConstructor = localClass.getConstructors();
                                                localObject = arrayOfConstructor[0].newInstance(new Object[0]);
                                                ((CultChannelModuleInterface) localObject).askModule(0, this.m_iObjekt, paramTreeMap, this.m_bCA_Dist);
                                        }
                                        rs.close();
                                }
                        }
                        for (i = 0; i < localVector2.size(); i++) {
                                localInteger = (Integer) localVector2.get(i);
                                if (!this.forbiddenModules.contains(localInteger)) {
                                        str2 = "SELECT name FROM distributoren.cc_modules WHERE cc_modules.id = " + localInteger + " " + str1 + "AND java_ws = 1 LIMIT 1";
                                        st = vega_con.createStatement();
                                        ResultSet rs = st.executeQuery(str2);
                                        int j = 0;
                                        for (j = 0; rs.next(); j++);
                                        if (j == 1) {
                                                rs.first();
                                                str3 = rs.getString("name");
                                                str4 = "com.cultbay.zuzzu.module.Module_" + str3;
                                                localClass = Class.forName(str4);
                                                arrayOfConstructor = localClass.getConstructors();
                                                localObject = arrayOfConstructor[0].newInstance(new Object[0]);
                                                ((CultChannelModuleInterface) localObject).askModule(this.m_iDistributor, this.m_iObjekt, paramTreeMap, this.m_bCA_Dist);
                                        }
                                        rs.close();
                                }
                        }
                } catch (Exception localException) {
                        System.out.println(localException.getMessage());
                        throw new Exception("Fehler in CultChannel.askModule()");
                }
        }

        private Vector<Integer> getActiveModules()
                throws Exception {
                Vector localVector = new Vector();
                try {
                        String str1 = "SELECT active_modules FROM distributoren.cc_objekt_x_distributor_x_module WHERE distributoren_distributor_id = " + this.m_iDistributor + " " + "AND   ext_dist_type = " + this.m_bCA_Dist + " " + "AND   cusebeda_objekt_id = " + this.m_iObjekt;
                        st = vega_con.createStatement();
                        ResultSet rs = st.executeQuery(str1);
                        int j = 0;
                        for (j = 0; rs.next(); j++);
                        if (j > 0) {
                                rs.first();
                                String str2 = rs.getString("active_modules");
                                if (str2 != "") {
                                        String[] arrayOfString = new String[0];
                                        arrayOfString = str2.split("-");
                                        if (arrayOfString[0].length() > 0) {
                                                for (int i = 0; i < arrayOfString.length; i++) {
                                                        localVector.add(Integer.valueOf(arrayOfString[i]));
                                                }
                                        }
                                }
                        }
                        rs.close();
                        return localVector;
                } catch (Exception localException) {
                }
                throw new Exception("Error in CultChannel.getActiveModules()");
        }

        private Vector<Integer> getActiveGlobalModules()
                throws Exception {
                Vector localVector = new Vector();
                try {
                        String str1 = "SELECT active_modules FROM distributoren.cc_objekt_x_distributor_x_moduleglobal WHERE cusebeda_objekt_id = " + this.m_iObjekt;
                        st = vega_con.createStatement();
                        ResultSet rs = st.executeQuery(str1);
                        int j = 0;
                        for (j = 0; rs.next(); j++);
                        if (j > 0) {
                                rs.first();
                                String str2 = rs.getString("active_modules");
                                if (str2 != "") {
                                        String[] arrayOfString = new String[0];
                                        arrayOfString = str2.split("-");
                                        if (arrayOfString[0].length() != 0) {
                                                for (int i = 0; i < arrayOfString.length; i++) {
                                                        localVector.add(Integer.valueOf(arrayOfString[i]));
                                                }
                                        }
                                }
                        }
                        rs.close();
                        return localVector;
                } catch (Exception localException) {
                }
                throw new Exception("Fehler in CultChannel.getActiveGlobalModules()");
        }
}
