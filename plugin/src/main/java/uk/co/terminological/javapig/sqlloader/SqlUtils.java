package uk.co.terminological.javapig.sqlloader;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.JDBCType;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlUtils {

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

	public static Map<String,List<String>> getDatabaseTables(Connection con) throws Exception { 

		DatabaseMetaData md = con.getMetaData();
		ResultSet rs = md.getTables(null, null, "%", null);
		HashMap<String,List<String>> databaseTables = new HashMap<String,List<String>>();
		while (rs.next()) {
			String table = rs.getString(3);
			String db = rs.getString(1);
			databaseTables.computeIfAbsent(db, (k) -> new ArrayList<>());
			databaseTables.get(db).add(table);
		}
		return databaseTables;

	}

	public static interface TableDetail {
		public String getSchema();
		public String getName();
		public List<ColumnDetail> getColumns();
		public long getRowCount();
	}

	public static interface ColumnDetail {
		public String getColumnLabel();
		public Class<?> getJavaType();
		public JDBCType getJDBCType();
		public int getLength();
		public boolean isNullable();
		public boolean isAutoIncrement();
	}

	public static TableDetail getTableDetail(Connection con, String tablename) throws SQLException { 

		String sql = "select * from "+tablename;
		PreparedStatement pstmt = con.prepareStatement(sql);
		ResultSetMetaData rsm = pstmt.getMetaData();
		String schema = rsm.getSchemaName(1);
		String table = rsm.getTableName(1);
		List<ColumnDetail> columns = columnsFromMetadata(rsm);

		if (pstmt != null) try { pstmt.close(); } catch(Exception e) {}

		String sql2 = "select count(*) as n from "+tablename;
		Statement st = null;
		ResultSet rs2 = null;
		try {
			st = con.createStatement();
			rs2 = st.executeQuery(sql2);
			long count = rs2.getLong('n');
			return new TableDetail() {
				@Override public String getName() {return table;}
				@Override public List<ColumnDetail> getColumns() {return columns;}
				@Override public long getRowCount() {return count;}
				@Override public String getSchema() {return schema;}
			};
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			if (rs2 != null) try { rs2.close(); } catch(Exception e) {}  
			if (st != null) try { st.close(); } catch(Exception e) {}
		}
		return null;

	}

	public static interface QueryDetail {
		public String getSql();
		public List<ParameterDetail> getParameters();
		public List<ColumnDetail> getColumns();
	}

	public static interface ParameterDetail {
		public Class<?> getJavaType();
		public JDBCType getJDBCType();
		public int getLength();
		public boolean isNullable();
	}
	
	public static QueryDetail getQueryDetail(Connection con, String sql) throws SQLException {
		PreparedStatement pstmt = con.prepareStatement(sql);
		ResultSetMetaData rsm = pstmt.getMetaData();
		ParameterMetaData psm = pstmt.getParameterMetaData();
		List<ColumnDetail> columns = columnsFromMetadata(rsm);
		List<ParameterDetail> parameters = parametersFromMetadata(psm);
		if (pstmt != null) try { pstmt.close(); } catch(Exception e) {}
		
		return new QueryDetail() {
			@Override public List<ColumnDetail> getColumns() {return columns;}
			@Override public String getSql() {return sql;}
			@Override public List<ParameterDetail> getParameters() {return parameters;}
		};
	}
	
	private static List<ParameterDetail> parametersFromMetadata(ParameterMetaData psm) throws SQLException {
		List<ParameterDetail> columns = new ArrayList<>();
		
		for (int i=1; i<=psm.getParameterCount(); i++) {

				JDBCType jdbcType = JDBCType.valueOf(psm.getParameterType(i));
				Class<?> javaClass;
				try {
					javaClass = Class.forName(psm.getParameterClassName(i)).equals(Object.class) ?
							toClass(jdbcType) :
								Class.forName(psm.getParameterClassName(i));

							int length = psm.getPrecision(i);
							boolean nullable = psm.isNullable(i) != ResultSetMetaData.columnNoNulls;
							columns.add(new ParameterDetail() {
								@Override public Class<?> getJavaType() {return javaClass;}
								@Override public JDBCType getJDBCType() {return jdbcType;}
								@Override public int getLength() {return length;}
								@Override public boolean isNullable() {return nullable;}
							});
				} catch (ClassNotFoundException e) {
					throw new RuntimeException("Setup error: SQL datatypes are not found on the class path: "+psm.getParameterClassName(i),e);
				}
			}
			return columns;
	}

	public static List<ColumnDetail> columnsFromMetadata(ResultSetMetaData rsm) throws SQLException {
		List<ColumnDetail> columns = new ArrayList<>();
		
		for (int i=1; i<=rsm.getColumnCount(); i++) {

				JDBCType jdbcType = JDBCType.valueOf(rsm.getColumnType(i));
				Class<?> javaClass;
				try {
					javaClass = Class.forName(rsm.getColumnClassName(i)).equals(Object.class) ?
							toClass(jdbcType) :
								Class.forName(rsm.getColumnClassName(i));

							String colLabel = rsm.getColumnLabel(i);
							int length = rsm.getPrecision(i);
							boolean nullable = rsm.isNullable(i) != ResultSetMetaData.columnNoNulls;
							boolean autoincrement = rsm.isAutoIncrement(i);
							columns.add(new ColumnDetail() {
								@Override public String getColumnLabel() {return colLabel;}
								@Override public Class<?> getJavaType() {return javaClass;}
								@Override public JDBCType getJDBCType() {return jdbcType;}
								@Override public int getLength() {return length;}
								@Override public boolean isNullable() {return nullable;}
								@Override public boolean isAutoIncrement() {return autoincrement;}
							});
				} catch (ClassNotFoundException e) {
					throw new RuntimeException("Setup error: SQL datatypes are not found on the class path: "+rsm.getColumnClassName(i),e);
				}
			}
			return columns;
		 
	}
}
