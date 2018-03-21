package uk.co.terminological.javapig.sqlloader;

import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class SqlUtils {
	
	
	/*public static String defunctionSql(String sql) {
		sql = sql.trim();
		if (sql.endsWith(";")) sql = sql.substring(0,sql.length()-1);
		return "SELECT xxx.* FROM ("+sql+") xxx WHERE 1=0";
	}*/
	
	public static Map<String,Class<?>> methodsFromResultSet(ResultSet rs) throws SQLException, ClassNotFoundException {
		
		Map<String,Class<?>> out = new LinkedHashMap<>();
		
		ResultSetMetaData rsm = rs.getMetaData();
		for (int i=1; i<=rsm.getColumnCount(); i++) {
			
			System.out.println(rsm.getColumnClassName(i)+": "+rsm.getColumnTypeName(i));
			Class<?> tmp = Class.forName(rsm.getColumnClassName(i));
			if (tmp.equals(Object.class)) {
				JDBCType type = JDBCType.valueOf(rsm.getColumnType(i));
				tmp = toClass(type);
			}
			out.put(rsm.getColumnName(i), tmp);
			
		}
		
		return out;
	}

	
	public static Class<?> toClass(JDBCType type) {
	        Class<?> result = Object.class;
	        switch (type) {
	            case CHAR:
	            case VARCHAR:
	            case LONGVARCHAR:
	            case NCHAR:
	            case NVARCHAR:
	            case LONGNVARCHAR:
	                result = String.class;
	                break;

	            case NUMERIC:
	            case DECIMAL:
	                result = java.math.BigDecimal.class;
	                break;

	            case BIT:
	            case BOOLEAN:
	                result = Boolean.class;
	                break;

	            case TINYINT:
	                result = Byte.class;
	                break;

	            case SMALLINT:
	                result = Short.class;
	                break;

	            case INTEGER:
	                result = Integer.class;
	                break;

	            case BIGINT:
	                result = Long.class;
	                break;

	            case REAL:
	            case FLOAT:
	                result = Float.class;
	                break;

	            case DOUBLE:
	                result = Double.class;
	                break;

	            case BINARY:
	            case VARBINARY:
	            case LONGVARBINARY:
	                result = Byte[].class;
	                break;

	            case DATE:
	                result = java.sql.Date.class;
	                break;

	            case TIME:
	            case TIME_WITH_TIMEZONE:
	                result = java.sql.Time.class;
	                break;

	            case TIMESTAMP:
	            case TIMESTAMP_WITH_TIMEZONE:
	                result = java.sql.Timestamp.class;
	                break;
			
	            case ARRAY:
	            case BLOB:
				case CLOB:
				case DATALINK:
				case DISTINCT:
				case JAVA_OBJECT:
				case NCLOB:
				case NULL:
				case OTHER:
				case REF:
				case REF_CURSOR:
				case ROWID:
				case SQLXML:
				case STRUCT:
				default:
					result = Object.class;
	        }

	        return result;
	    }
	
}
