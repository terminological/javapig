package uk.co.terminological.javapig.csvloader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.log4j.BasicConfigurator;

import uk.co.terminological.javapig.csvloader.DelimitedParser.EOFException;
import uk.co.terminological.javapig.csvloader.DelimitedParser.MalformedCSVException;

public class Tester {

	public Tester() {
		// TODO Auto-generated constructor stub
	}
	
	

	public static void main(String[] args) throws IOException, MalformedCSVException {
		BasicConfigurator.configure();
		InputStream is = Tester.class.getResourceAsStream("/csv1.csv");
		Reader in = new InputStreamReader(is,"UTF-8");
		DelimitedParser util = new DelimitedParser(in, "\"", ",", "\n", "\\");
		try {
			while(true) {
				util.readLine().stream().forEach(System.out::println);
				System.out.println("NL");
			}
		} catch (EOFException e) {
			System.out.println("Done");
		}
		is.close();
	}

}
