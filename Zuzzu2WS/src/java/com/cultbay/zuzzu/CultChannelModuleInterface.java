/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cultbay.zuzzu;

import com.cultbay.zuzzu.db.dbConnection;
import java.util.TreeMap;

public abstract interface CultChannelModuleInterface
{
  public abstract void askModule(int paramInt1, int paramInt2, TreeMap<Integer, Verfuegbarkeit> paramTreeMap, int paramInt3)
    throws Exception;
}
