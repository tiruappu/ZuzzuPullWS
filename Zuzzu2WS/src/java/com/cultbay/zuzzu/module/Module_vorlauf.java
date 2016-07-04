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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

public class Module_vorlauf extends CultChannelModule {

    public void askModule(int paramInt1, int paramInt2, TreeMap<Integer, Verfuegbarkeit> paramTreeMap, int paramInt3, dbConnection paramdbConnection)
            throws Exception {
        try {
            this.m_iDistributor = paramInt1;
            this.m_bCA_Dist = paramInt3;
            this.m_iObjekt = paramInt2;
            String str = "SELECT A.vorlaufzeit FROM distributoren.cc_objekt_x_distributor_x_vorlauf AS A WHERE A.objekt_id = " + this.m_iObjekt + " " + "AND\tA.distributor_id = " + this.m_iDistributor + " " + "AND\tA.ext_dist_type = " + this.m_bCA_Dist + " " + "LIMIT 1";
            ResultSet localResultSet = paramdbConnection.leseDatenAus(str);
            int i = 0;
            if (paramdbConnection.liefereZeilenAnz(localResultSet) == 1) {
                localResultSet.first();
                i = localResultSet.getInt(i);
            }
            localResultSet.close();
            Set localSet = paramTreeMap.keySet();
            Iterator localIterator = localSet.iterator();
            while (localIterator.hasNext()) {
                Verfuegbarkeit localVerfuegbarkeit = (Verfuegbarkeit) paramTreeMap.get(localIterator.next());
                GregorianCalendar localGregorianCalendar = new GregorianCalendar();
                localGregorianCalendar.setTime(new Date());
                localGregorianCalendar.add(5, i);
                if (localVerfuegbarkeit.getDatum().before(localGregorianCalendar.getTime())) {
                    localVerfuegbarkeit.setChannelPrice(new Integer(0));
                }
            }
        } catch (Exception localException) {
            throw new Exception("Fehler in Module_vorlauf.askModule2()");
        }
    }
}
