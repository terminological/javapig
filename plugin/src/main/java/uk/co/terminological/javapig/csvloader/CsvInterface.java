package uk.co.terminological.javapig.csvloader;

import java.util.ArrayList;
import java.util.List;

import uk.co.terminological.javapig.index.IndexableInterface;
import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JInterface;
import uk.co.terminological.javapig.util.SourceCode;

public class CsvInterface extends IndexableInterface {

	public CsvInterface(JInterface copy) {
		super(copy);
	}
	
	public boolean isCsvBound() {return this.isAnnotationPresent(Csv.class);}
	
	private List<JGetMethod> fieldOrder = new ArrayList<>();
	
	public List<JGetMethod> getMethodsInOrder() {
		if (fieldOrder.isEmpty()) {
			this.getMethods().stream()
			.filter(m -> m.isAnnotationPresent(ByField.class))
			.forEach(
					m -> this.fieldOrder.add(m.getAnnotation(ByField.class).value(),m)
					);
		}
		
		return this.fieldOrder;
	}
	
	public String getParser(String readerVar) {
		
		Type type = this.getAnnotation(Csv.class).value(); 
		String sep = this.getAnnotation(Csv.class).seperator();
		String term = this.getAnnotation(Csv.class).enclosedBy();
		String enc = this.getAnnotation(Csv.class).lineTerminator();
		String esc = this.getAnnotation(Csv.class).escapedBy();
		boolean mandatoryEnclosed = this.getAnnotation(Csv.class).alwaysEnclosed();
		
		switch (type) {
		case CSV:
			return "DelimitedParserBuilder.excelCsv("+readerVar+")";
		case TSV:
			return "DelimitedParserBuilder.tsv("+readerVar+")";
		case PIPE_DELIM:
			return "DelimitedParserBuilder.pipe("+readerVar+")";
		case MYSQL:
			return "DelimitedParserBuilder.mysql("+readerVar+")";
		default:
			return "DelimitedParserBuilder.machine("+
				SourceCode.string(sep)+","+
				SourceCode.string(enc)+","+
				SourceCode.string(esc)+")"+
				".lineEnding("+SourceCode.string(term)+")"+
				(mandatoryEnclosed ? ".mandatoryEnclosure()" : "")+
				".build("+readerVar+")"
				;
		}
		
	}
	

}
