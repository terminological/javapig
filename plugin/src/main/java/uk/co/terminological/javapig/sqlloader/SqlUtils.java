package uk.co.terminological.javapig.sqlloader;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import uk.co.terminological.javapig.StringCaster;

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

	public static Object defaultForType(JDBCType type) {
		Object result = null;
		switch (type) {
		case CHAR:
		case VARCHAR:
		case LONGVARCHAR:
		case NCHAR:
		case NVARCHAR:
		case LONGNVARCHAR:
			result = "";
			break;

		case NUMERIC:
		case DECIMAL:
			result = new BigDecimal(1);
			break;

		case BIT:
		case BOOLEAN:
			result = Boolean.TRUE;
			break;

		case TINYINT:
		case SMALLINT:
		case INTEGER:
			result = 1;
			break;

		case BIGINT:
			result = 1L;
			break;

		case REAL:
		case FLOAT:
			result = 1F;
			break;

		case DOUBLE:
			result = 1D;
			break;

		case BINARY:
		case VARBINARY:
		case LONGVARBINARY:
			result = new byte[] {};
			break;

		case DATE:
			result = java.sql.Date.from(Instant.now());
			break;

		case TIME:
		case TIME_WITH_TIMEZONE:
			result = java.sql.Time.from(Instant.now());
			break;

		case TIMESTAMP:
		case TIMESTAMP_WITH_TIMEZONE:
			result = java.sql.Timestamp.from(Instant.now());
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
			result = new Object();
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
		return getTableDetail(con,tablename,false);
	}
	
	public static TableDetail getTableDetail(Connection con, String tablename, boolean doCount) throws SQLException { 

		String sql = "select * from "+tablename+" where 1=0";
		Statement pstmt = con.createStatement();
		pstmt.setMaxRows(1);
		ResultSet rs = pstmt.executeQuery(sql);
		// PreparedStatement pstmt = con.prepareStatement(sql);
		ResultSetMetaData rsm = rs.getMetaData(); //pstmt.getMetaData();
		String schema = rsm.getSchemaName(1);
		String table = rsm.getTableName(1).equals("") ? tablename : rsm.getTableName(1);
		List<ColumnDetail> columns = columnsFromMetadata(rsm);

		if (pstmt != null) try { pstmt.close(); } catch(Exception e) {}

		long tmpCount = 0;
		if (doCount) {
			String sql2 = "select count(*) as n from "+tablename;
			Statement st = null;
			ResultSet rs2 = null;
			try {
				st = con.createStatement();
				rs2 = st.executeQuery(sql2);
				if (rs2.isBeforeFirst()) rs2.next();
				tmpCount = rs2.getLong("n");
			} catch (SQLException e1) {
				e1.printStackTrace();
			} finally {
				if (rs2 != null) try { rs2.close(); } catch(Exception e) {}  
				if (st != null) try { st.close(); } catch(Exception e) {}
			}

		}
		long count = tmpCount;
		return new TableDetail() {
			@Override public String getName() {return table;}
			@Override public List<ColumnDetail> getColumns() {return columns;}
			@Override public long getRowCount() {return count;}
			@Override public String getSchema() {return schema;}
		};



	}

	public static interface QueryDetail {
		public String getSql();
		public int getParameterCount();
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
		ParameterMetaData psm = pstmt.getParameterMetaData();
		int pcount = psm.getParameterCount();
		List<ParameterDetail> parameters = parametersFromMetadata(psm);
		for (int i=0; i<psm.getParameterCount(); i++) {
			pstmt.setObject(i+1, null);
		}

		/*for (int i = 0; i<parameters.size(); i++) {
			Class<?> pt = parameters.get(i).getJavaType();
			pstmt.setObject(i+1, defaultForType(parameters.get(i).getJDBCType()));
		}*/
		pstmt.setMaxRows(1);
		ResultSet rs = pstmt.executeQuery();
		ResultSetMetaData rsm = rs.getMetaData();
		List<ColumnDetail> columns = columnsFromMetadata(rsm);
		if (pstmt != null) try { pstmt.close(); } catch(Exception e) {}

		return new QueryDetail() {
			@Override public List<ColumnDetail> getColumns() {return columns;}
			@Override public String getSql() {return sql;}
			@Override public List<ParameterDetail> getParameters() {return parameters;}
			@Override public int getParameterCount() {return pcount;}
		};
	}


	// Tries to get parameter info but we cannot assume it is possible due to variable support
	private static List<ParameterDetail> parametersFromMetadata(ParameterMetaData psm) {
		List<ParameterDetail> columns = new ArrayList<>();

		try {
			for (int i=1; i<=psm.getParameterCount(); i++) {

				JDBCType jdbcType = JDBCType.valueOf(psm.getParameterType(i));
				if (jdbcType.equals(JDBCType.NULL)) throw new SQLException();

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

		} catch (SQLException e) {
			// TODO: MySQL /  SQLLite don't really support this.
			columns.add(new ParameterDetail() {
				@Override public Class<?> getJavaType() {return Object.class;}
				@Override public JDBCType getJDBCType() {return JDBCType.NULL;}
				@Override public int getLength() {return Integer.MAX_VALUE;}
				@Override public boolean isNullable() {return true;}
			});
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
	
	public static interface FunctionWithException<T, R, E extends Exception> {
		R apply(T t) throws E;
	}
	
	public static <X, E extends Exception> Stream<X> streamResultSet(ResultSet rs, FunctionWithException<ResultSet,X, E> mapper) {
		Iterable<X> iterable = () -> iterateResultSet(rs,mapper);
		return StreamSupport.stream(iterable.spliterator(), false);
	}
	
	public static <X, E extends Exception> Iterator<X> iterateResultSet(ResultSet rs, FunctionWithException<ResultSet,X,E> mapper) {
		return new Iterator<X>() {

			boolean ready = false;
			X out = null;
			@Override
			public boolean hasNext() {
				try {
					if (!ready) {
						ready = rs.next();
						out = mapper.apply(rs);
					}
				} catch (Exception e) {
					ready = false;
				} 
				return ready;
			}

			@Override
			public X next() {
				if (!hasNext()) throw new NoSuchElementException();
				return out;
			}
			
		};
	}
}
