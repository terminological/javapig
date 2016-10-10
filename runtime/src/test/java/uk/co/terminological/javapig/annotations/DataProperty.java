package uk.co.terminological.javapig.annotations;

public @interface DataProperty {
	String value() default RDFIRI.XSD_STRING;
	//String lang() default "en";
}
