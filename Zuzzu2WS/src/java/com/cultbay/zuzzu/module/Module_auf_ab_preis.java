/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cultbay.zuzzu.module;

/**
 *
 * @author kondalarao
 */
import com.cultbay.zuzzu.Verfuegbarkeit;
import com.cultbay.zuzzu.db.dbConnection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

public class Module_auf_ab_preis extends CultChannelModule {

        public void askModule(int paramInt1, int paramInt2, TreeMap<Integer, Verfuegbarkeit> paramTreeMap, int paramInt3, dbConnection paramdbConnection)
                throws Exception {
                this.m_iDistributor = paramInt1;
                this.m_bCA_Dist = paramInt3;
                this.m_iObjekt = paramInt2;
                try {
                        Set localSet = paramTreeMap.keySet();
                        Iterator localIterator = localSet.iterator();
                        while (localIterator.hasNext()) {
                                Verfuegbarkeit localVerfuegbarkeit = (Verfuegbarkeit) paramTreeMap.get(localIterator.next());
                                int i = paresTimespan(localVerfuegbarkeit, 0, -1, paramdbConnection);
                                String str = "SELECT price, type, up_down, round FROM distributoren.cc_objekt_x_distributor_x_aufpreis WHERE id = " + i + " LIMIT 1";
                                ResultSet localResultSet = paramdbConnection.leseDatenAus(str);
                                if (paramdbConnection.liefereZeilenAnz(localResultSet) > 0) {
                                        localResultSet.first();
                                        int j = localVerfuegbarkeit.getChannelPrice().intValue();
                                        if (j != 0) {
                                                if (localResultSet.getInt("type") == 0) {
                                                        if (localResultSet.getInt("up_down") == 0) {
                                                                j += localResultSet.getInt("price");
                                                        } else {
                                                                j -= localResultSet.getInt("price");
                                                        }
                                                } else if (localResultSet.getInt("up_down") == 0) {
                                                        j += j * (localResultSet.getInt("price") / 100) / 100;
                                                } else {
                                                        j -= j * (localResultSet.getInt("price") / 100) / 100;
                                                }
                                                if (localResultSet.getInt("round") > 0) {
                                                        Double localDouble = Double.valueOf(Math.pow(10.0D, localResultSet.getInt("round")));
                                                        j = (int) Math.round(Math.round(j / localDouble.doubleValue()) * localDouble.doubleValue());
                                                }
                                        } else {
                                                j = 0;
                                        }
                                        localVerfuegbarkeit.setChannelPrice(Integer.valueOf(j));
                                }
                                localResultSet.close();
                        }
                } catch (SQLException localSQLException) {
                        System.out.println(localSQLException.getClass().getName() + ": " + localSQLException.getMessage());
                        throw new Exception("Fehler in Module_auf_ab_preis.askModule2()");
                }
        }

        private int paresTimespan(Verfuegbarkeit paramVerfuegbarkeit, int paramInt1, int paramInt2, dbConnection paramdbConnection)
                throws Exception {
                try {
                        String str = " SELECT A.id, A.start, A.end, A.pointer FROM distributoren.cc_objekt_x_distributor_x_aufpreis AS A WHERE A.distributor_id = " + this.m_iDistributor + " AND   A.ext_dist_type \t= " + this.m_bCA_Dist + " AND   A.ziwekat_id     = " + paramVerfuegbarkeit.getHofesodaZiwekatId() + " AND   A.pointer\t\t\t\t= " + paramInt1 + " ORDER BY pointer DESC";
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
                throw new Exception("Fehler in Module_auf_ab_preis.paresTimespan()");
        }
}
