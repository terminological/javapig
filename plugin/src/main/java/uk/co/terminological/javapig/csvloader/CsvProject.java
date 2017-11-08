package uk.co.terminological.javapig.csvloader;

import java.util.List;
import java.util.stream.Collectors;

import uk.co.terminological.javapig.index.IndexableProject;
import uk.co.terminological.javapig.javamodel.JPackage;
import uk.co.terminological.javapig.javamodel.JProject;

public class CsvProject extends IndexableProject {

	public CsvProject(JProject in) {
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
