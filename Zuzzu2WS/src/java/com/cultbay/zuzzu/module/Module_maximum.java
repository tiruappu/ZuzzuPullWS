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
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

public class Module_maximum extends CultChannelModule implements CultChannelModuleInterface {
  public void askModule(int paramInt1, int paramInt2, TreeMap<Integer, Verfuegbarkeit> paramTreeMap, int paramInt3, dbConnection paramdbConnection)
    throws Exception
  {
    this.m_iDistributor = paramInt1;
    this.m_bCA_Dist = paramInt3;
    this.m_iObjekt = paramInt2;
    try
    {
      Set localSet = paramTreeMap.keySet();
      Iterator localIterator = localSet.iterator();
      while (localIterator.hasNext())
      {
        Verfuegbarkeit localVerfuegbarkeit = (Verfuegbarkeit)paramTreeMap.get(localIterator.next());
        String str1 = "SELECT maximum, periode FROM   distributoren.cc_objekt_x_distributor_x_maximum WHERE  cusebeda_objekt_id = " + this.m_iObjekt + " " + "AND    distributoren_distributor_id = " + this.m_iDistributor + " " + "AND    ext_dist_type = " + this.m_bCA_Dist + " " + "AND    hofesoda_ziwekat_id = " + localVerfuegbarkeit.getHofesodaZiwekatId() + " " + "LIMIT 1";
        ResultSet localResultSet1 = paramdbConnection.leseDatenAus(str1);
        if (paramdbConnection.liefereZeilenAnz(localResultSet1) == 1)
        {
          localResultSet1.first();
          int i = localResultSet1.getInt("periode");
          int j = localResultSet1.getInt("maximum");
          String str2 = "";
          switch (i)
          {
          case 0:
            str2 = "%j";
            break;
          case 1:
            str2 = "%u";
            break;
          case 2:
            str2 = "%m";
          }
          String str3 = "";
          if (this.m_bCA_Dist == 0)
            str3 = "SELECT SUM(auhoveda.buchungsdaten.anzahl) AS Anzahl FROM auhoveda.buchungsdaten, auhoveda.buchung, distributoren.distributor WHERE DATE_FORMAT(auhoveda.buchungsdaten.datum,'" + str2 + "') = DATE_FORMAT('" + localVerfuegbarkeit.getDatum() + "','" + str2 + "') " + "AND   DATE_FORMAT(auhoveda.buchungsdaten.datum,'%Y')         = DATE_FORMAT('" + localVerfuegbarkeit.getDatum() + "','%Y') " + "AND   auhoveda.buchungsdaten.hofesoda_ziwekat_id \t\t\t\t\t  = " + localVerfuegbarkeit.getHofesodaZiwekatId() + " " + "AND   auhoveda.buchungsdaten.buchung_id \t\t\t\t\t\t\t\t\t\t\t= auhoveda.buchung.id " + "AND   auhoveda.buchung.kontext_id \t\t\t\t\t\t\t\t\t\t\t\t\t\t= distributoren.distributor.auhoveda_kontext_id " + "AND   distributoren.distributor.id \t\t\t\t\t\t\t\t\t\t\t\t  = " + this.m_iDistributor + " " + "AND   auhoveda.buchung.cusebeda_objekt_id \t\t\t\t  \t\t\t\t\t= " + this.m_iObjekt;
          else
            str3 = "SELECT SUM(auhoveda.buchungsdaten.anzahl) AS Anzahl FROM auhoveda.buchungsdaten, auhoveda.buchung WHERE DATE_FORMAT(auhoveda.buchungsdaten.datum,'" + str2 + "') = DATE_FORMAT('" + localVerfuegbarkeit.getDatum() + "','" + str2 + "') " + "AND   DATE_FORMAT(auhoveda.buchungsdaten.datum,'%Y') \t\t\t\t = DATE_FORMAT('" + localVerfuegbarkeit.getDatum() + "','%Y') " + "AND   auhoveda.buchungsdaten.hofesoda_ziwekat_id \t\t\t\t\t\t = " + localVerfuegbarkeit.getHofesodaZiwekatId() + " " + "AND   auhoveda.buchungsdaten.buchung_id \t\t\t\t\t\t\t\t\t\t = auhoveda.buchung.id " + "AND   auhoveda.buchung.ca_distributor_id \t\t\t\t\t\t\t\t\t\t = " + this.m_iDistributor + " " + "AND   auhoveda.buchung.cusebeda_objekt_id \t\t\t\t\t\t\t\t\t = " + this.m_iObjekt;
          ResultSet localResultSet2 = paramdbConnection.leseDatenAus(str3);
          localResultSet2.first();
          if (localResultSet2.getInt("Anzahl") + localVerfuegbarkeit.getRequestedQuantity().intValue() > j)
            localVerfuegbarkeit.setChannelPrice(new Integer(0));
          localResultSet2.close();
        }
        localResultSet1.close();
      }
    }
    catch (Exception localException)
    {
      System.out.println(localException.getClass().getName() + ": " + localException.getMessage());
      throw new Exception("Fehler in Module_maximum.askModule()");
    }
  }
}
