package uk.co.terminological.javapig.sqlloader;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class SqlUtils {
	
	
	public static String defunctionSql(String sql) {
		sql = sql.trim();
		if (sql.endsWith(";")) sql = sql.substring(0,sql.length()-1);
		if (sql.toLowerCase().contains("where ")) {
			sql = sql + " and 1=0;";
		} else {
			sql = sql + " where 1=0;";
		}
		return sql;
	}
	
	public static Map<String,Class<?>> methodsFromResultSet(ResultSet rs) throws SQLException, ClassNotFoundException {
		
		Map<String,Class<?>> out = new LinkedHashMap<>();
		
		ResultSetMetaData rsm = rs.getMetaData();
		for (int i=1; i<=rsm.getColumnCount(); i++) {
			
			out.put(rsm.getColumnName(i), Class.forName(rsm.getColumnClassName(i)));
			
		}
		
		return out;
	}

}
