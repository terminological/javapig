package uk.co.terminological.javapig.examples;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PluginTests {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
		File connectionProperties = new File(ClassLoader.getSystemResource("sqlite.prop").getFile());
		Properties prop =  new Properties();
		System.out.println("Properties file: "+connectionProperties.getAbsolutePath());
		prop.load(new FileInputStream(connectionProperties));

		System.out.println("Driver: "+prop.getProperty("driver"));
		System.out.println("Url: "+prop.getProperty("url"));
		
		Class.forName(prop.getProperty("driver"));
		Connection conn = DriverManager.getConnection(prop.getProperty("url"), prop);
	}

}
