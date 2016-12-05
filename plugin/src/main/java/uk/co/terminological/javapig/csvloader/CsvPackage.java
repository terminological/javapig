package uk.co.terminological.javapig.csvloader;

import java.util.List;
import java.util.stream.Collectors;

import uk.co.terminological.javapig.index.IndexablePackage;
import uk.co.terminological.javapig.javamodel.JPackage;

public class CsvPackage extends IndexablePackage {

	public CsvPackage(JPackage in) {
		super(in);
	}

	public List<CsvInterface> getCsvClasses() {
		return this.getClasses().stream()
				.filter(c -> c instanceof CsvInterface)
				.map(c -> ((CsvInterface) c))
				.filter(c -> c.isCsvBound())
				.collect(Collectors.toList());
	}
}
