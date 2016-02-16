package com.klemstinegroup.wub2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import com.klemstinegroup.wub.AudioObject;
import com.klemstinegroup.wub.Serializer;

public class WubConvert {
	
	static SessionIdentifierGenerator sig=new SessionIdentifierGenerator();

	public WubConvert() {
		File file = new File("Q:");
		try {
			Files.walk(Paths.get(file.getPath())).filter(Files::isRegularFile).forEach(WubConvert::process);

		} catch (UncheckedIOException e) {
			// e.printStackTrace();
		} catch (AccessDeniedException e) {
			// e.printStackTrace();
		} catch (IOException e)

		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args1) throws Exception {
		new WubConvert();

	}

	public static void process(Path path) {
//		if (path.toString().toLowerCase().endsWith(".dub")) {
//			path.toFile().renameTo(new File(path.toString().replaceAll("dub","wub")));
//		}
//		if (true)return;
		
		
		if (path.toString().toLowerCase().endsWith(".wub")) {
			System.out.println("Converting:" + path);
			AudioObject au = AudioObject.factory(path.toFile());
			// for (Object bb:au.analysis.getMap().keySet()){
			// System.out.println(bb);
			//
			// }
			System.out.println(au.analysis.getMap().get("meta"));
			JSONObject json = (JSONObject) au.analysis.getMap().get("meta");
			System.out.println(json.get("artist"));
			try {
				SqlJetDb db = SqlJetDb.open(new File(SQLDatabase.DB_NAME), true);
				ISqlJetTable table = db.getTable("tracks");
//				db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
//				ISqlJetCursor result = table.lookup("filename", json.get("filename"));
//				db.commit();
//				if (result.eof()) {
					Map<String, Object> map = new HashMap<>();
					map.put("filename", json.get("filename"));
					map.put("artist", json.get("artist"));
					map.put("album", json.get("album"));
					map.put("title", json.get("title"));
					map.put("genre", json.get("genre"));
					map.put("bitrate", json.get("bitrate"));
					map.put("sample_rate", json.get("sample_rate"));
					map.put("seconds", json.get("seconds"));
//					map.put("data", au.data);
//					try {
//						map.put("analysis", Serializer.toByteArray(au.analysis));
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					String index=sig.nextSessionId();
					map.put("index",index);
					
					try {
						writeBytes("Q:\\"+index+".au",au.data);
						writeBytes("Q:\\"+index+".an",Serializer.toByteArray(au.analysis));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					db.beginTransaction(SqlJetTransactionMode.WRITE);
					System.out.print("inserting record\t");
					table.insertByFieldNames(map);
					db.commit();
					System.out.println("...inserted record");
				// }
				// else System.out.println("skipping "+json.get("filename"));
				db.close();
				path.toFile().renameTo(new File(path.toString().replaceAll("wub","dub")));
			} catch (SqlJetException e) {
				e.printStackTrace();
			}
			try {
				restart();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public static void writeBytes(String file, byte[] bytes) throws IOException{
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(bytes);
		fos.close();
	}
	
	public static void restart() throws IOException {
		StringBuilder cmd = new StringBuilder();
		cmd.append(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java ");
		for (String jvmArg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
			cmd.append(jvmArg + " ");
		}
		cmd.append("-jar ").append(ManagementFactory.getRuntimeMXBean().getClassPath()).append(" ");
		cmd.append(WubConvert.class.getName()).append(" ");
		// for (String arg : args) {
		// cmd.append(arg).append(" ");
		// }
		System.out.println(cmd);
		Runtime.getRuntime().exec(cmd.toString());
		System.exit(0);
	}
	
	
}
