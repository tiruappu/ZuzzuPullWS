/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cultbay.zuzzu.module;

/**
 *
 * @author sravan
 */
import com.cultbay.zuzzu.Verfuegbarkeit;
import com.cultbay.zuzzu.db.dbConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

public class Module_yieldmanager extends CultChannelModule {

    public void askModule(int paramInt1, int paramInt2, TreeMap<Integer, Verfuegbarkeit> paramTreeMap, int paramInt3, dbConnection paramdbConnection)
            throws Exception {
        this.m_iDistributor = paramInt1;
        this.m_bCA_Dist = paramInt3;
        this.m_iObjekt = paramInt2;
        try {
            Set localSet = paramTreeMap.keySet();
            ArrayList localArrayList = new ArrayList();
            String str = "";
            Object localObject1 = localSet.iterator();
            while (((Iterator) localObject1).hasNext()) {
                Verfuegbarkeit localObject2 = (Verfuegbarkeit) paramTreeMap.get(((Iterator) localObject1).next());
                if (!localArrayList.contains(Integer.valueOf(((Verfuegbarkeit) localObject2).getHofesodaZiwekatId()))) {
                    localArrayList.add(Integer.valueOf(((Verfuegbarkeit) localObject2).getHofesodaZiwekatId()));
                    str = str + ((Verfuegbarkeit) localObject2).getHofesodaZiwekatId() + ",";
                }
            }
            str = str.substring(0, str.length() - 1);
            localObject1 = "SELECT ziwekategorie.id, ziwekategorie.anzahl FROM hofesoda.ziwekategorie WHERE id IN ( " + str + " )";
            Object localObject2 = paramdbConnection.leseDatenAus((String) localObject1);
            ((ResultSet) localObject2).beforeFirst();
            TreeMap localTreeMap = new TreeMap();
            while (((ResultSet) localObject2).next()) {
                localTreeMap.put(Integer.valueOf(((ResultSet) localObject2).getInt("id")), Integer.valueOf(((ResultSet) localObject2).getInt("anzahl")));
            }
            Iterator localIterator = localSet.iterator();
            while (localIterator.hasNext()) {
                Verfuegbarkeit localVerfuegbarkeit = (Verfuegbarkeit) paramTreeMap.get(localIterator.next());
                getYieldedPrice(localVerfuegbarkeit, localTreeMap, paramdbConnection);
            }
            ((ResultSet) localObject2).close();
        } catch (SQLException localSQLException) {
            localSQLException.printStackTrace();
        }
    }

    private void getYieldedPrice(Verfuegbarkeit paramVerfuegbarkeit, TreeMap<Integer, Integer> paramTreeMap, dbConnection paramdbConnection)
            throws Exception {
        try {
            Date localDate1 = new Date();
            Date localDate2 = paramVerfuegbarkeit.getDatum();
            long l = (localDate2.getTime() - localDate1.getTime()) / 86400000L;
            Object localObject = paramVerfuegbarkeit.getChannelPrice();
            double d = Integer.valueOf(paramVerfuegbarkeit.getAnzahl()).doubleValue() / Integer.valueOf(((Integer) paramTreeMap.get(Integer.valueOf(paramVerfuegbarkeit.getHofesodaZiwekatId()))).intValue()).doubleValue() * 100.0D;
            String str1 = "SELECT B.pos AS reihenfolge, B.yield_id, B.value AS price_factor FROM   distributoren.cc_rule_x_cc_yield AS B, distributoren.cc_rule_x_distributor_x_objekt AS A WHERE A.objekt_id = " + this.m_iObjekt + " " + "AND A.distributor_id = " + this.m_iDistributor + " " + "AND A.ext_dist_type = " + this.m_bCA_Dist + " " + "AND B.rule_id = A.rule_id " + "ORDER BY B.pos DESC ";
            ResultSet localResultSet1 = paramdbConnection.leseDatenAus(str1);
            localResultSet1.beforeFirst();
            int i = 0;
            while (localResultSet1.next()) {
                String str2 = "SELECT A.wert AS treshold FROM distributoren.yielder AS A WHERE A.yield_id = " + localResultSet1.getInt("yield_id") + " " + "AND " + "A.dba = " + l;
                ResultSet localResultSet2 = paramdbConnection.leseDatenAus(str2);
                if (paramdbConnection.liefereZeilenAnz(localResultSet2) > 0) {
                    localResultSet2.first();
                    if (100.0D - d < localResultSet2.getInt("treshold")) {
                        Integer localInteger = Integer.valueOf(paramVerfuegbarkeit.getChannelPrice().intValue() * localResultSet1.getInt("price_factor") / 100);
                        localObject = localInteger;
                        i = 1;
                    }
                }
            }
            localResultSet1.close();
            if (i != 0) {
                paramVerfuegbarkeit.setChannelPrice((Integer) localObject);
            } else {
                paramVerfuegbarkeit.setChannelPrice(new Integer(0));
            }
        } catch (SQLException localSQLException) {
            localSQLException.printStackTrace();
        }
    }
}