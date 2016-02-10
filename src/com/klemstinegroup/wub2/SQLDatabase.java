package com.klemstinegroup.wub2;

import java.io.File;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class SQLDatabase {

	private static final String DB_NAME = "wub.sql";

	public static void delete(){
		File dbFile = new File(DB_NAME);
		dbFile.delete();
		
	}

	public static void create() throws SqlJetException {
		File dbFile = new File(DB_NAME);
		if (!dbFile.exists()){
		SqlJetDb db = SqlJetDb.open(dbFile, true);
		db.getOptions().setAutovacuum(true);
		db.beginTransaction(SqlJetTransactionMode.WRITE);
		try {
			db.getOptions().setUserVersion(1);
		} finally {
			db.commit();
		}
		db.close();
		}
	}

}
