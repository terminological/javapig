package uk.co.terminological.javapig.csvloader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import uk.co.terminological.datatypes.Deferred;
import uk.co.terminological.parser.DelimitedParserBuilder;
import uk.co.terminological.parser.ParserException;

public class Tester {

	public Tester() {
		// TODO Auto-generated constructor stub
	}


	public static void main(String[] args) throws IOException, ParserException {
		BasicConfigurator.configure();
		InputStream is = Tester.class.getResourceAsStream("/csv1.csv");
		Reader in = new InputStreamReader(is,"UTF-8");
		Iterable<Deferred<List<String>, ParserException>> util = DelimitedParserBuilder.excelCsv(in);
		for (Deferred<List<String>, ParserException> item: util) {
				item.get().forEach(System.out::println);
				System.out.println("NL");
		}
		System.out.println("Done");
		is.close();
	}

}
