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
import com.cultbay.zuzzu.model.db.ModuleDayblockModel;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

public class Module_dayblock extends CultChannelModule implements CultChannelModuleInterface{

        public void askModule(int paramInt1, int paramInt2, TreeMap<Integer, Verfuegbarkeit> paramTreeMap, int paramInt3, dbConnection paramdbConnection)
                throws Exception {
                try {
                        this.m_iDistributor = paramInt1;
                        this.m_bCA_Dist = paramInt3;
                        this.m_iObjekt = paramInt2;
                        String str = "SELECT * FROM distributoren.cc_objekt_x_distributor_x_dayBlock WHERE cusebeda_objekt_id = " + this.m_iObjekt + " " + "AND " + "distributoren_distributor_id = " + this.m_iDistributor + " " + "AND " + "ext_dist_type = " + this.m_bCA_Dist;
                        ResultSet localResultSet = paramdbConnection.leseDatenAus(str);
                        localResultSet.beforeFirst();
                        ArrayList localArrayList = new ArrayList();
                        while (localResultSet.next()) {
                                ModuleDayblockModel localObject = new ModuleDayblockModel();
                                ((ModuleDayblockModel) localObject).setCusebeda_objekt_id(Integer.valueOf(localResultSet.getInt("cusebeda_objekt_id")));
                                ((ModuleDayblockModel) localObject).setDistributor_id(Integer.valueOf(localResultSet.getInt("distributoren_distributor_id")));
                                ((ModuleDayblockModel) localObject).setExt_dist_type(Integer.valueOf(localResultSet.getInt("ext_dist_type")));
                                ((ModuleDayblockModel) localObject).setBlocking_from(localResultSet.getDate("blocking_from"));
                                ((ModuleDayblockModel) localObject).setBlocking_till(localResultSet.getDate("blocking_till"));
                                ((ModuleDayblockModel) localObject).setHofesoda_ziwekat_id(Integer.valueOf(localResultSet.getInt("hofesoda_ziwekat_id")));
                                localArrayList.add(localObject);
                        }
                        localResultSet.close();
                        Object localObject = paramTreeMap.keySet();
                        Iterator localIterator1 = ((Set) localObject).iterator();
                        while (localIterator1.hasNext()) {
                                Verfuegbarkeit localVerfuegbarkeit = (Verfuegbarkeit) paramTreeMap.get(localIterator1.next());
                                Iterator localIterator2 = localArrayList.iterator();
                                while (localIterator2.hasNext()) {
                                        ModuleDayblockModel localModuleDayblockModel = (ModuleDayblockModel) localIterator2.next();
                                        if ((localVerfuegbarkeit.getHofesodaZiwekatId() == localModuleDayblockModel.getHofesoda_ziwekat_id().intValue()) && ((localVerfuegbarkeit.getDatum().after(localModuleDayblockModel.getBlocking_from())) || (localVerfuegbarkeit.getDatum().equals(localModuleDayblockModel.getBlocking_from()))) && ((localVerfuegbarkeit.getDatum().before(localModuleDayblockModel.getBlocking_till())) || (localVerfuegbarkeit.getDatum().equals(localModuleDayblockModel.getBlocking_till())))) {
                                                localVerfuegbarkeit.setChannelPrice(new Integer(0));
                                        }
                                }
                        }
                } catch (Exception localException) {
                        System.out.println(localException.getClass().getName() + ": " + localException.getMessage());
                        throw new Exception("Fehler in Module_dayblock.askModule()");
                }
        }
}
