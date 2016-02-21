package com.klemstinegroup.wub2.utilities;

import org.hsqldb.Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Paul on 2/21/2016.
 */
public class HsqlDatabase {
    public void startSever(){
        Server hsqlServer = new Server();
        hsqlServer.setLogWriter(null);
        hsqlServer.setSilent(true);
        hsqlServer.setDatabaseName(0, "iva");
        hsqlServer.setDatabasePath(0, "file:ivadb");
        hsqlServer.start();

        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver" );
        } catch (Exception e) {
            System.err.println("ERROR: failed to load HSQLDB JDBC driver.");
            e.printStackTrace();
            return;
        }


        try {
            Connection c = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/iva", "SA", "");

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
