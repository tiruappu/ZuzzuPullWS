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
import java.sql.Date;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

public class Module_stay extends CultChannelModule {

    private int paresTimespan(Verfuegbarkeit paramVerfuegbarkeit, int paramInt1, int paramInt2, dbConnection paramdbConnection)
            throws Exception {
        try {
            String str = " SELECT A.id, A.start, A.end, A.pointer FROM distributoren.cc_objekt_x_distributor_x_stay AS A WHERE A.distributor_id = " + this.m_iDistributor + " AND   A.ext_dist_type \t= " + this.m_bCA_Dist + " AND   A.ziwekat_id     = " + paramVerfuegbarkeit.getHofesodaZiwekatId() + " AND   A.pointer\t\t\t\t= " + paramInt1 + " ORDER BY pointer DESC";
            ResultSet localResultSet = paramdbConnection.leseDatenAus(str);
            localResultSet.beforeFirst();
            while (localResultSet.next()) {
                Date localDate1 = localResultSet.getDate("start");
                Date localDate2 = localResultSet.getDate("end");
                Date localDate3 = (Date) paramVerfuegbarkeit.getDatum();
                if (((localDate3.after(localDate1)) || (localDate3.equals(localDate1))) && ((localDate3.before(localDate2)) || (localDate3.equals(localDate2)))) {
                    paramInt2 = localResultSet.getInt("id");
                    paramInt2 = paresTimespan(paramVerfuegbarkeit, paramInt2, paramInt2, paramdbConnection);
                }
            }
            localResultSet.close();
            return paramInt2;
        } catch (Exception localException) {
        }
        throw new Exception("Fehler in Module_stay\t.paresTimespan()");
    }

    public void askModule(int paramInt1, int paramInt2, TreeMap<Integer, Verfuegbarkeit> paramTreeMap, int paramInt3, dbConnection paramdbConnection)
            throws Exception {
        this.m_iDistributor = paramInt1;
        this.m_bCA_Dist = paramInt3;
        this.m_iObjekt = paramInt2;
        try {
            int i = 0;
            if (paramInt3 == 1) {
                return;
            }
            Set localSet = paramTreeMap.keySet();
            TreeMap localTreeMap = new TreeMap();
            Iterator localIterator = localSet.iterator();
            Verfuegbarkeit localVerfuegbarkeit;
            while (localIterator.hasNext()) {
                localVerfuegbarkeit = (Verfuegbarkeit) paramTreeMap.get(localIterator.next());
                ArrayList localArrayList;
                if (localTreeMap.containsKey(localVerfuegbarkeit.getDatum())) {
                    localArrayList = (ArrayList) localTreeMap.get(localVerfuegbarkeit.getDatum());
                    localArrayList.add(Integer.valueOf(localVerfuegbarkeit.getHofesodaZiwekatId()));
                } else {
                    localArrayList = new ArrayList();
                    localArrayList.add(Integer.valueOf(localVerfuegbarkeit.getHofesodaZiwekatId()));
                    localTreeMap.put((Date) localVerfuegbarkeit.getDatum(), localArrayList);
                }
            }
            localIterator = localSet.iterator();
            while (localIterator.hasNext()) {
                localVerfuegbarkeit = (Verfuegbarkeit) paramTreeMap.get(localIterator.next());
                int j = paresTimespan(localVerfuegbarkeit, 0, -1, paramdbConnection);
                String str = "SELECT min_stay, max_stay FROM distributoren.cc_objekt_x_distributor_x_stay WHERE id = " + j + " LIMIT 1";
                ResultSet localResultSet = paramdbConnection.leseDatenAus(str);
                if (paramdbConnection.liefereZeilenAnz(localResultSet) == 1) {
                    localResultSet.first();
                    i = localResultSet.getInt("min_stay");
                    GregorianCalendar localGregorianCalendar1 = new GregorianCalendar();
                    GregorianCalendar localGregorianCalendar2 = new GregorianCalendar();
                    int k = 0;
                    int m = 0;
                    k = i - 1;
                    int n = 0;
                    while (m != i) {
                        localGregorianCalendar1.setTime(localVerfuegbarkeit.getDatum());
                        localGregorianCalendar1.add(5, -m);
                        localGregorianCalendar2.setTime(localVerfuegbarkeit.getDatum());
                        localGregorianCalendar2.add(5, k);
                        if ((localTreeMap.containsKey(localGregorianCalendar1.getTime())) && (((ArrayList) localTreeMap.get(localGregorianCalendar1.getTime())).contains(Integer.valueOf(localVerfuegbarkeit.getHofesodaZiwekatId()))) && (localTreeMap.containsKey(localGregorianCalendar2.getTime())) && (((ArrayList) localTreeMap.get(localGregorianCalendar2.getTime())).contains(Integer.valueOf(localVerfuegbarkeit.getHofesodaZiwekatId())))) {
                            n = 1;
                        }
                        k--;
                        m++;
                    }
                    if (n == 0) {
                        localVerfuegbarkeit.setChannelPrice(new Integer(0));
                    }
                }
                localResultSet.close();
            }
        } catch (Exception localException) {
            System.out.println(localException.getClass().getName() + ": " + localException.getMessage());
            throw new Exception("Fehler in Module_stay.askModule()");
        }
    }
}
