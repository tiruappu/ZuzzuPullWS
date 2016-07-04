/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cultbay.zuzzu.module;

/**
 *
 * @author sravan
 */
import com.cultbay.zuzzu.CultChannelModuleInterface;
import com.cultbay.zuzzu.Verfuegbarkeit;
import com.cultbay.zuzzu.db.dbConnection;
import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

public class Module_maxrooms extends CultChannelModule {

    public void askModule(int paramInt1, int paramInt2, TreeMap<Integer, Verfuegbarkeit> paramTreeMap, int paramInt3, dbConnection paramdbConnection)
            throws Exception {
        try {
            this.m_iDistributor = paramInt1;
            this.m_bCA_Dist = paramInt3;
            this.m_iObjekt = paramInt2;
            String str = "SELECT * FROM   distributoren.cc_objekt_x_distributor_x_maxrooms WHERE  objekt_id      = " + this.m_iObjekt + " " + "AND    distributor_id = " + this.m_iDistributor + " " + "AND    ext_dist_type  = " + this.m_bCA_Dist;
            ResultSet localResultSet = paramdbConnection.leseDatenAus(str);
            Integer localInteger = Integer.valueOf(0);
            if (paramdbConnection.liefereZeilenAnz(localResultSet) > 0) {
                localResultSet.first();
                localInteger = Integer.valueOf(localResultSet.getInt("maxrooms"));
            }
            localResultSet.close();
            Hashtable localHashtable = new Hashtable();
            Set localSet = paramTreeMap.keySet();
            Object localObject1 = localSet.iterator();
            while (((Iterator) localObject1).hasNext()) {
                Verfuegbarkeit localObject2 = (Verfuegbarkeit) paramTreeMap.get(((Iterator) localObject1).next());
                if (localHashtable.containsKey(Integer.valueOf(((Verfuegbarkeit) localObject2).getHofesodaZiwekatId()))) {
                    Integer localObject3 = (Integer) localHashtable.get(Integer.valueOf(((Verfuegbarkeit) localObject2).getHofesodaZiwekatId()));
                    if (((Integer) localObject3).intValue() <= ((Verfuegbarkeit) localObject2).getRequestedQuantity().intValue()) {
                        localHashtable.put(Integer.valueOf(((Verfuegbarkeit) localObject2).getHofesodaZiwekatId()), ((Verfuegbarkeit) localObject2).getRequestedQuantity());
                    }
                } else {
                    localHashtable.put(Integer.valueOf(((Verfuegbarkeit) localObject2).getHofesodaZiwekatId()), ((Verfuegbarkeit) localObject2).getRequestedQuantity());
                }
            }
            localObject1 = Integer.valueOf(0);
            Object localObject2 = localHashtable.keySet();
            Object localObject3 = ((Set) localObject2).iterator();
            while (((Iterator) localObject3).hasNext()) {
                localObject1 = Integer.valueOf(((Integer) localObject1).intValue() + ((Integer) localHashtable.get(((Iterator) localObject3).next())).intValue());
            }
            if (((Integer) localObject1).intValue() > localInteger.intValue()) {
                localObject3 = localSet.iterator();
                while (((Iterator) localObject3).hasNext()) {
                    Verfuegbarkeit localVerfuegbarkeit = (Verfuegbarkeit) paramTreeMap.get(((Iterator) localObject3).next());
                    localVerfuegbarkeit.setChannelPrice(new Integer(0));
                }
            }
        } catch (Exception localException) {
            System.out.println(localException.getClass().getName() + ": " + localException.getMessage());
            throw new Exception("Error in Module_maxrooms.askModule()");
        }
    }
}
