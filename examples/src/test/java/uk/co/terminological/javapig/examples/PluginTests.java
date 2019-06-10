package uk.co.terminological.javapig.examples;



import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.co.terminological.javapig.scanner.ReflectionUtils;
import uk.co.terminological.javapig.sqlloader.SqlUtils;
import uk.co.terminological.javapig.sqlloader.SqlUtils.QueryDetail;
import uk.co.terminological.javapig.sqlloader.SqlUtils.TableDetail;


public class PluginTests {

	Connection sqlite() throws Exception {
		File connectionProperties = new File(ClassLoader.getSystemResource("sqlite.prop").getFile());
		Properties prop =  new Properties();
		System.out.println("Properties file: "+connectionProperties.getAbsolutePath());
		prop.load(new FileInputStream(connectionProperties));

		System.out.println("Driver: "+prop.getProperty("driver"));
		System.out.println("Url: "+prop.getProperty("url"));
		
		Class.forName(prop.getProperty("driver"));
		return DriverManager.getConnection(prop.getProperty("url"), prop); 
	}
	
	Connection mimic() throws Exception {
		File connectionProperties = new File(ClassLoader.getSystemResource("mysql.prop").getFile());
		Properties prop =  new Properties();
		System.out.println("Properties file: "+connectionProperties.getAbsolutePath());
		prop.load(new FileInputStream(connectionProperties));

		System.out.println("Driver: "+prop.getProperty("driver"));
		System.out.println("Url: "+prop.getProperty("url"));
		
		Class.forName(prop.getProperty("driver"));
		return DriverManager.getConnection(prop.getProperty("url"), prop); 
	}
	
	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {
		TableDetail td = SqlUtils.getTableDetail(sqlite(), "TestTable");
		System.out.println(td.getName());
		System.out.println(ReflectionUtils.gettersToString(td,3));
		System.out.println(ReflectionUtils.gettersToString(td.getColumns().get(0),3));
	}
	
	@Test
	public void test2() throws Exception {
		QueryDetail td = SqlUtils.getQueryDetail(sqlite(), "Select * from TestTable where col1=?");
		System.out.println(td.getSql());
		System.out.println(ReflectionUtils.gettersToString(td,3));
		System.out.println(ReflectionUtils.gettersToString(td.getColumns().get(0),3));
		System.out.println(ReflectionUtils.gettersToString(td.getParameters().get(0),3));
		//Data type of columns not correctly defined in splite when result set size is zero 
	}

	
	@Test
	public void test3() throws Exception {
		QueryDetail td = SqlUtils.getQueryDetail(mimic(), "Select * from ADMISSIONS where ADMISSION_TYPE=?");
		System.out.println(td.getSql());
		System.out.println(ReflectionUtils.gettersToString(td,3));
		System.out.println(ReflectionUtils.gettersToString(td.getColumns().get(0),3));
		System.out.println(ReflectionUtils.gettersToString(td.getParameters().get(0),3));
		
	}
	
	@Test
	public void test4() throws Exception {
		QueryDetail td = SqlUtils.getQueryDetail(mimic(), "Select * from ADMISSIONS where ROW_ID=-1");
		System.out.println(td.getSql());
		System.out.println(ReflectionUtils.gettersToString(td,3));
		System.out.println(ReflectionUtils.gettersToString(td.getColumns().get(0),3));
		
	}
	
}
