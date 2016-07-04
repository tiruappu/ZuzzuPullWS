/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cultbay.zuzzu.db;

/**
 *
 * @author kondalarao
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

public class dbConnection {

        private Connection verbindung;
        private boolean live;
        private Properties keys = new Properties();

        public dbConnection(boolean paramBoolean) {
                if (paramBoolean) {
                        ladeJDBC_ODBC_Bruecke();
                } else {
                        ladeJDBC_MYSQL_Treiber();
                }
        }

        public dbConnection() {
                dbProperties();
                ladeJDBC_MYSQL_Treiber();
                this.live = true;
                try {
                        ladeVerbindung("jdbc:mysql://" + this.keys.getProperty("server_main") + "/cusebeda", this.keys.getProperty("user_main"), this.keys.getProperty("password_main"));
                } catch (Exception localException) {
                        System.out.println(localException.getMessage());
                }
        }

        public dbConnection(int paramInt) {
                dbProperties();
                this.live = false;
                ladeJDBC_MYSQL_Treiber();
                try {
                        ladeVerbindung("jdbc:mysql://kolibri.cultuzz.de/cusebeda", "root", "d872cs");
                } catch (Exception localException) {
                        System.out.println(localException.getMessage());
                }
        }

        public void ladeJDBC_ODBC_Bruecke() {
                try {
                        Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
                } catch (ClassNotFoundException localClassNotFoundException) {
                }
        }

        public void ladeJDBC_MYSQL_Treiber() {
                try {
                        Class.forName("org.gjt.mm.mysql.Driver");
                } catch (ClassNotFoundException localClassNotFoundException) {
                        System.out.print("MYSQL-Treiber konnte nicht geladen werden!");
                }
        }

        public void ladeVerbindung(String paramString1, String paramString2, String paramString3) {
                try {
                        this.verbindung = DriverManager.getConnection(paramString1, paramString2, paramString3);
                } catch (SQLException localSQLException) {
                        System.out.println(localSQLException.getMessage());
                }
        }

        public int sqlInsert(String paramString) {
                int i;
                try {
                        Statement localStatement = this.verbindung.createStatement();
                        localStatement.executeUpdate(paramString);
                        ResultSet localResultSet = localStatement.getGeneratedKeys();
                        localResultSet.first();
                        i = localResultSet.getInt(1);
                } catch (SQLException localSQLException) {
                        i = -1;
                }
                if (i != -1) {
                        syncDB(paramString);
                }
                return i;
        }

        public String sqlAnweisung(String paramString) {
                String str = "";
                try {
                        Statement localStatement = this.verbindung.createStatement();
                        localStatement.executeUpdate(paramString);
                } catch (SQLException localSQLException) {
                        str = localSQLException.getMessage();
                }
                if (str.equals("")) {
                        syncDB(paramString);
                }
                return str;
        }

        public ResultSet leseDatenAus(String paramString) {
                try {
                        Statement localStatement = this.verbindung.createStatement();
                        return localStatement.executeQuery(paramString);
                } catch (SQLException localSQLException) {
                        System.out.println(localSQLException.getMessage());
                        System.out.println(paramString);
                }
                return null;
        }

        public ArrayList liefereSpaltenBezeichner(ResultSet paramResultSet) {
                ArrayList localArrayList = new ArrayList();
                try {
                        ResultSetMetaData localResultSetMetaData = paramResultSet.getMetaData();
                        for (int i = 0; i < liefereSpaltenAnz(paramResultSet); i++) {
                                localArrayList.add(localResultSetMetaData.getColumnName(i + 1).toString());
                        }
                        return localArrayList;
                } catch (SQLException localSQLException) {
                        System.out.println(localSQLException.getMessage());
                        return null;
                } catch (NullPointerException localNullPointerException) {
                }
                return localArrayList;
        }

        /**
         * @deprecated
         */
        public ArrayList liefereDatenSaetzeAsArray(ResultSet paramResultSet) {
                try {
                        int i = liefereSpaltenAnz(paramResultSet);
                        ArrayList localArrayList = new ArrayList();
                        while (paramResultSet.next()) {
                                for (int j = 0; j < i; j++) {
                                        localArrayList.add(paramResultSet.getString(j + 1));
                                }
                        }
                        paramResultSet.close();
                        return localArrayList;
                } catch (SQLException localSQLException) {
                        System.out.println(localSQLException.getMessage());
                }
                return null;
        }

        public int liefereSpaltenAnz(ResultSet paramResultSet) {
                try {
                        ResultSetMetaData localResultSetMetaData = paramResultSet.getMetaData();
                        return localResultSetMetaData.getColumnCount();
                } catch (SQLException localSQLException) {
                        System.out.println(localSQLException.getMessage());
                }
                return -1;
        }

        public int liefereZeilenAnz(ResultSet paramResultSet) {
                try {
                        int i = 0;
                        for (i = 0; paramResultSet.next(); i++);
                        return i;
                } catch (SQLException localSQLException) {
                        System.out.println(localSQLException.getMessage());
                }
                return -1;
        }

        public void close() {
                try {
                        this.verbindung.close();
                } catch (SQLException localSQLException) {
                        System.out.println(localSQLException.getMessage());
                }
                System.gc();
        }

        public void syncDB(String paramString) {
                int i = Integer.parseInt(this.keys.getProperty("server_anzahl"));
                if ((this.live == true) && (i > 0)) {
                        ladeJDBC_MYSQL_Treiber();
                        Statement localStatement = null;
                        String[] arrayOfString = new String[i];
                        Connection[] arrayOfConnection = new Connection[i];
                        for (int j = 0; j < i; j++) {
                                arrayOfString[j] = (this.keys.getProperty(new StringBuilder().append("name_slave_").append(j).toString()) + "/aus Java");
                                try {
                                        arrayOfConnection[j] = DriverManager.getConnection("jdbc:mysql://" + this.keys.getProperty(new StringBuilder().append("server_slave_").append(j).toString()) + "/ebay3", this.keys.getProperty("user_slave_" + j), this.keys.getProperty("password_slave_" + j));
                                } catch (SQLException localSQLException1) {
                                        arrayOfConnection[j] = null;
                                }
                        }
                        String str = getQueryType(paramString);
                        if ((!str.equals("UPDATE")) || (!str.equals("TRUNCATE")) || (!str.equals("FALSE"))) {
                                for (int k = 0; k < arrayOfConnection.length; k++) {
                                        if (arrayOfConnection[k] != null) {
                                                try {
                                                        localStatement = arrayOfConnection[k].createStatement();
                                                        localStatement.executeUpdate(paramString);
                                                } catch (SQLException localSQLException2) {
                                                }
                                        }
                                }
                        }
                }
        }

        public String getQueryType(String paramString) {
                paramString = paramString.trim();
                paramString = paramString.toLowerCase();
                if (paramString.substring(0, 6).equals("insert")) {
                        return "INSERT";
                }
                if (paramString.substring(0, 6).equals("delete")) {
                        return "DELETE";
                }
                if (paramString.substring(0, 7).equals("replace")) {
                        return "REPLACE";
                }
                if (paramString.substring(0, 6).equals("select")) {
                        return "SELECT";
                }
                if (paramString.substring(0, 6).equals("update")) {
                        return "UPDATE";
                }
                if (paramString.substring(0, 8).equals("truncate")) {
                        return "TRUNCATE";
                }
                return "FALSE";
        }

        public void dbProperties() {
                try {
                        boolean bool = new File("/www/java_config/db.properties").exists();
                        if (bool) {
                                this.keys.load(new FileInputStream("/www/java_config/db.properties"));
                        } else {
                                this.keys.load(new FileInputStream("C:\\xampp\\xampp\\tomcat\\webapps\\axis\\WEB-INF\\lib\\db.properties"));
                        }
                } catch (IOException localIOException) {
                        System.out.println(localIOException);
                }
        }
}
