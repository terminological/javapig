package uk.co.terminological.javapig.annotations;

public class RDFIRI {
	
	public static final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
	public static final String XSD = "http://www.w3.org/2001/XMLSchema#";
	public static final String OWL = "http://www.w3.org/2002/07/owl#";
	
	public static final String OWL_OBJECTPROPERTY = "http://www.w3.org/2002/07/owl#ObjectProperty";
	public static final String OWL_DATATYPEPROPERTY = "http://www.w3.org/2002/07/owl#DatatypeProperty";
	public static final String OWL_SAMEAS = "http://www.w3.org/2002/07/owl#sameAs";
	public static final String OWL_INVERSEOF = "http://www.w3.org/2002/07/owl#inverseOf";
	public static final String OWL_FUNCTIONAL = "http://www.w3.org/2002/07/owl#FunctionalProperty";
	public static final String OWL_TRANSITIVE = "http://www.w3.org/2002/07/owl#TransitiveProperty";
	public static final String OWL_CLASS = "http://www.w3.org/2002/07/owl#Class";
	
	public static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	public static final String RDF_PROPERTY = "http://www.w3.org/1999/02/22-rdf-syntax-ns#DataProperty";
	public static final String RDF_XMLLITERAL = "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral";
	public static final String RDF_NIL = "http://www.w3.org/1999/02/22-rdf-syntax-ns#nil";
	public static final String RDF_LIST = "http://www.w3.org/1999/02/22-rdf-syntax-ns#List";
	public static final String RDF_STATEMENT = "http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement";
	public static final String RDF_SUBJECT = "http://www.w3.org/1999/02/22-rdf-syntax-ns#subject";
	public static final String RDF_PREDICATE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate";
	public static final String RDF_OBJECT = "http://www.w3.org/1999/02/22-rdf-syntax-ns#object";
	public static final String RDF_FIRST = "http://www.w3.org/1999/02/22-rdf-syntax-ns#first";
	public static final String RDF_REST = "http://www.w3.org/1999/02/22-rdf-syntax-ns#rest";
	public static final String RDF_SEQ = "http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq";
	public static final String RDF_BAG = "http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag";
	public static final String RDF_ALT = "http://www.w3.org/1999/02/22-rdf-syntax-ns#Alt";
	public static final String RDF_VALUE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#value";
	
	public static String rdfIndex(int i) {
		return "http://www.w3.org/1999/02/22-rdf-syntax-ns#_"+i;
	}
	
	public static final String RDFS_DOMAIN = "http://www.w3.org/2000/01/rdf-schema#domain";
	public static final String RDFS_RANGE = "http://www.w3.org/2000/01/rdf-schema#range";
	public static final String RDFS_RESOURCE = "http://www.w3.org/2000/01/rdf-schema#Resource";
	public static final String RDFS_LITERAL = "http://www.w3.org/2000/01/rdf-schema#Literal";
	public static final String RDFS_DATATYPE = "http://www.w3.org/2000/01/rdf-schema#Datatype";
	public static final String RDFS_CLASS = "http://www.w3.org/2000/01/rdf-schema#Class";
	public static final String RDFS_SUBCLASSOF = "http://www.w3.org/2000/01/rdf-schema#subClassOf";
	public static final String RDFS_SUBPROPERTYOF = "http://www.w3.org/2000/01/rdf-schema#subPropertyOf";
	public static final String RDFS_MEMBER = "http://www.w3.org/2000/01/rdf-schema#member";
	public static final String RDFS_CONTAINER = "http://www.w3.org/2000/01/rdf-schema#Container";
	public static final String RDFS_CONTAINERMEMBERSHIPPROPERTY = "http://www.w3.org/2000/01/rdf-schema#ContainerMembershipProperty";
	public static final String RDFS_COMMENT = "http://www.w3.org/2000/01/rdf-schema#comment";
	public static final String RDFS_SEEALSO = "http://www.w3.org/2000/01/rdf-schema#seeAlso";
	public static final String RDFS_ISDEFINEDBY = "http://www.w3.org/2000/01/rdf-schema#isDefinedBy";
	public static final String RDFS_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
	
	public static final String XSD_STRING = "http://www.w3.org/2001/XMLSchema#string";
	public static final String XSD_BOOLEAN = "http://www.w3.org/2001/XMLSchema#boolean";
	public static final String XSD_DECIMAL = "http://www.w3.org/2001/XMLSchema#decimal";
	public static final String XSD_FLOAT = "http://www.w3.org/2001/XMLSchema#float";
	public static final String XSD_DOUBLE = "http://www.w3.org/2001/XMLSchema#double";
	public static final String XSD_DATETIME = "http://www.w3.org/2001/XMLSchema#dateTime";
	public static final String XSD_TIME = "http://www.w3.org/2001/XMLSchema#time";
	public static final String XSD_DATE = "http://www.w3.org/2001/XMLSchema#date";
	public static final String XSD_GYEARMONTH = "http://www.w3.org/2001/XMLSchema#gYearMonth";
	public static final String XSD_GYEAR = "http://www.w3.org/2001/XMLSchema#gYear";
	public static final String XSD_GMONTHDAY = "http://www.w3.org/2001/XMLSchema#gMonthDay";
	public static final String XSD_GDAY = "http://www.w3.org/2001/XMLSchema#gDay";
	public static final String XSD_GMONTH = "http://www.w3.org/2001/XMLSchema#gMonth";
	public static final String XSD_HEXBINARY = "http://www.w3.org/2001/XMLSchema#hexBinary";
	public static final String XSD_BASE64BINARY = "http://www.w3.org/2001/XMLSchema#base64Binary";
	public static final String XSD_ANYURI = "http://www.w3.org/2001/XMLSchema#anyURI";
	public static final String XSD_NORMALIZEDSTRING = "http://www.w3.org/2001/XMLSchema#normalizedString";
	public static final String XSD_TOKEN = "http://www.w3.org/2001/XMLSchema#token";
	public static final String XSD_LANGUAGE = "http://www.w3.org/2001/XMLSchema#language";
	public static final String XSD_NMTOKEN = "http://www.w3.org/2001/XMLSchema#NMTOKEN";
	public static final String XSD_NAME = "http://www.w3.org/2001/XMLSchema#Name";
	public static final String XSD_NCNAME = "http://www.w3.org/2001/XMLSchema#NCName";
	public static final String XSD_INTEGER = "http://www.w3.org/2001/XMLSchema#integer";
	public static final String XSD_NONPOSITIVEINTEGER = "http://www.w3.org/2001/XMLSchema#nonPositiveInteger";
	public static final String XSD_NEGATIVEINTEGER = "http://www.w3.org/2001/XMLSchema#negativeInteger";
	public static final String XSD_LONG = "http://www.w3.org/2001/XMLSchema#long";
	public static final String XSD_INT = "http://www.w3.org/2001/XMLSchema#int";
	public static final String XSD_SHORT = "http://www.w3.org/2001/XMLSchema#short";
	public static final String XSD_BYTE = "http://www.w3.org/2001/XMLSchema#byte";
	public static final String XSD_NONNEGATIVEINTEGER = "http://www.w3.org/2001/XMLSchema#nonNegativeInteger";
	public static final String XSD_UNSIGNEDLONG = "http://www.w3.org/2001/XMLSchema#unsignedLong";
	public static final String XSD_UNSIGNEDINT = "http://www.w3.org/2001/XMLSchema#unsignedInt";
	public static final String XSD_UNSIGNEDSHORT = "http://www.w3.org/2001/XMLSchema#unsignedShort";
	public static final String XSD_UNSIGNEDBYTE = "http://www.w3.org/2001/XMLSchema#unsignedByte";
	public static final String XSD_POSITIVEINTEGER = "http://www.w3.org/2001/XMLSchema#positiveInteger";
	
	public static final String JAVA_FQN = "http://www.bmj.com/model#hasJavaFQN";
}
