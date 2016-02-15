package com.klemstinegroup.wub2;

import java.io.File;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class SQLDatabase {

	static final String DB_NAME = "Q:\\wub.sql";
	
   
	static String trackTable = "CREATE TABLE tracks (key INTEGER PRIMARY KEY AUTOINCREMENT, filename TEXT,artist TEXT, album TEXT, title TEXT, genre TEXT, bitrate INTEGER,sample_rate INTEGER,seconds INTEGER,data BLOB, analysis BLOB)";
	
	//    String createFirstNameIndexQuery = "CREATE INDEX " + FULL_NAME_INDEX + " ON " + TABLE_NAME + "(" +  FIRST_NAME_FIELD + "," + SECOND_NAME_FIELD + ")";
//    String createDateIndexQuery = "CREATE INDEX " + DOB_INDEX + " ON " + TABLE_NAME + "(" +  DOB_FIELD + ")";
	

	public static void delete() {
		File dbFile = new File(DB_NAME);
		dbFile.delete();

	}

	public static void create() throws SqlJetException {
		File dbFile = new File(DB_NAME);
		if (!dbFile.exists()) {
			SqlJetDb db = SqlJetDb.open(dbFile, true);
			db.getOptions().setAutovacuum(true);
			db.beginTransaction(SqlJetTransactionMode.WRITE);
			try {
				db.getOptions().setUserVersion(1);
			} finally {
				db.commit();
			}
			
			db.createTable(trackTable);
			//db.createIndex(fileIndex);
			db.close();
		}
		
	}
	
	public static void main(String[] args){
		delete();
		try {
			create();
		} catch (SqlJetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
