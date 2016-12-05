/**
 * 
 */
package uk.co.terminological.javapig;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * @author RCHALLEN
 *
 */
public class StringCaster {

	String string;

	public static StringCaster get(String s) {
		StringCaster out = new StringCaster();
		out.string = s;
		return out;
	}

	public String asString() {
		return string;
	}

	public int asInt() {
		return cast(Integer.TYPE);
	}

	public boolean asBoolean() {
		return cast(Boolean.TYPE);
	}

	public File asFile() {
		if (string==null || string.isEmpty()) return null;
		return new File(string);
	}

	public UUID asUUID() {
		if (string==null || string.isEmpty()) return null;
		return UUID.fromString(string);
	}

	public Date asDate() throws ParseException {
		if (string==null || string.isEmpty()) return null;
		return DateFormatString.tryAll(string);
	}

	public <Y extends Enum<?>> Y asEnum(Class<Y> enumClazz) {
		if (string==null || string.isEmpty()) return null;
		String prop = null;
		for (Field f: enumClazz.getFields()) {
			if (f.isEnumConstant()) {
				if (f.getName().equalsIgnoreCase(string.trim())) {
					prop = f.getName();
				}
			}
		}
		if (prop != null) {
			for (Y out:enumClazz.getEnumConstants()) {
				if (out.name().equals(prop)) return out;
			}
		}
		throw new ClassCastException("Unsupported value "+string+" for enumeration "+enumClazz.getCanonicalName());

	}


	public static <X extends Object> X cast(Class<X> fieldClazz, String string) {
		return StringCaster.get(string).cast(fieldClazz);
	}

	@SuppressWarnings("unchecked")
	public <X extends Object> X cast(Class<X> fieldClazz) {
		Object fieldObject = null;

		if (string == null) {
			return undefinedPrimitive(fieldClazz);
		}

		if (String.class.isAssignableFrom(fieldClazz)) {

			fieldObject = string;

		} else {

			if (fieldClazz.equals(Boolean.TYPE)
					|| Boolean.class.isAssignableFrom(fieldClazz)) {
				if (
						string.equalsIgnoreCase("true") ||
						string.equalsIgnoreCase("1") ||
						string.equalsIgnoreCase("yes") ||
						string.equalsIgnoreCase("y") ||
						string.equalsIgnoreCase("t") 
						) fieldObject = true;
				else fieldObject = false;
				//log.debug("Boolean text: "+string+" value: "+ fieldObject.toString());

			} else if (fieldClazz.equals(Short.TYPE)
					|| Short.class.isAssignableFrom(fieldClazz)) {
				fieldObject = Short.parseShort(string);

			} else if (fieldClazz.equals(Integer.TYPE)
					|| Integer.class.isAssignableFrom(fieldClazz)) {
				Matcher m = Pattern.compile("^([0-9]+)[^0-9]*.*$").matcher(string);
				if (m.matches()) fieldObject = Integer.parseInt(m.group(1));
				else throw new NumberFormatException(string+" is not an integer");

			} else if (fieldClazz.equals(Character.TYPE)
					|| Character.class.isAssignableFrom(fieldClazz)) {
				fieldObject = string.charAt(0);

			} else if (fieldClazz.equals(Byte.TYPE)
					|| Byte.class.isAssignableFrom(fieldClazz)) {
				fieldObject = string.charAt(0);

			} else if (fieldClazz.equals(Long.TYPE)
					|| Long.class.isAssignableFrom(fieldClazz)) {
				fieldObject = Long.parseLong(string);

			} else if (fieldClazz.equals(Double.TYPE)
					|| Double.class.isAssignableFrom(fieldClazz)) {
				fieldObject = Double.parseDouble(string);

			} else if (fieldClazz.equals(Float.TYPE)
					|| Float.class.isAssignableFrom(fieldClazz)) {
				fieldObject = Float.parseFloat(string);

			} else if (fieldClazz.equals(Void.TYPE) 
					|| Void.class.isAssignableFrom(fieldClazz)) {
				throw new RuntimeException("void type");

			} else if (UUID.class.isAssignableFrom(fieldClazz)) {
				return (X) asUUID();

			} else if (URI.class.isAssignableFrom(fieldClazz)) {
				return (X) asURI();

			} else if (File.class.isAssignableFrom(fieldClazz)) {
				return (X) asFile();

			} else if (Enum.class.isAssignableFrom(fieldClazz)) {
				return (X) asEnum((Class<Enum<?>>) fieldClazz);

			} else if (Date.class.isAssignableFrom(fieldClazz)) {
				try {
					return (X) asDate();
				} catch (ParseException e) {
					throw new ClassCastException("Unparseable date string: "+string);
				}

			} else if (XMLGregorianCalendar.class.isAssignableFrom(fieldClazz)) {
				try {
					return (X) DatatypeFactory.newInstance().newXMLGregorianCalendar(string);
				} catch (DatatypeConfigurationException | IllegalArgumentException e) {
					throw new ClassCastException("Unparseable Xml date string: "+string);
				}
			
			} else if (URL.class.isAssignableFrom(fieldClazz)) {
				try {
					return (X) new URL(string);
				} catch (MalformedURLException e) {
					throw new ClassCastException("Unparseable url string: "+string);
				}

			} else if (Class.class.isAssignableFrom(fieldClazz)) {
				try {
					return (X) fieldClazz.getClassLoader().loadClass(string);
				} catch (ClassNotFoundException e) {
					throw new ClassCastException("No class found for: "+string);
				}
			
				
			} else {
				throw new ClassCastException("Unsupported type: "+fieldClazz.getName()+" for "+string);
			}
		}
		return (X) fieldObject;

	}

	public URI asURI() {
		if (string==null || string.isEmpty()) return null;
		return URI.create(string);
	}

	@SuppressWarnings("unchecked")
	public static <Y extends Object> Y undefinedPrimitive(Class<Y> fieldClazz) {
		Object fieldObject;
		if (fieldClazz.equals(Boolean.TYPE)) {
			fieldObject = false;
		} else if (fieldClazz.equals(Short.TYPE)) {
			fieldObject = 0;
		} else if (fieldClazz.equals(Integer.TYPE)) {
			fieldObject = 0;
		} else if (fieldClazz.equals(Character.TYPE)) {
			fieldObject = 0;
		} else if (fieldClazz.equals(Byte.TYPE)) {
			fieldObject = 0;
		} else if (fieldClazz.equals(Long.TYPE)) {
			fieldObject = 0L;
		} else if (fieldClazz.equals(Double.TYPE)) {
			fieldObject = 0D;
		} else if (fieldClazz.equals(Float.TYPE)) {
			fieldObject = 0.0;
		} else {
			fieldObject = null;
		}
		return (Y) fieldObject;
	}
	
	
	public static enum DateFormatString {
		
		ISO_DATETIME_FORMAT	("yyyy-MM-dd'T'HH:mm:ss"),
		ISO_DATETIME_TIME_ZONE_FORMAT ("yyyy-MM-dd'T'HH:mm:ssZZ"),
		ISO_DATE_FORMAT ("yyyy-MM-dd"),
		ISO_DATE_TIME_ZONE_FORMAT ("yyyy-MM-ddZZ"),
		ISO_TIME_FORMAT ("'T'HH:mm:ss"),
		ISO_TIME_TIME_ZONE_FORMAT ("'T'HH:mm:ssZZ"),
		ISO_TIME_NO_T_FORMAT ("HH:mm:ss"),
		ISO_TIME_NO_T_TIME_ZONE_FORMAT ("HH:mm:ssZZ"),
		SMTP_DATETIME_FORMAT ("EEE, dd MMM yyyy HH:mm:ss Z"),
		RSS2 ("EEE, dd MMM yyyy HH:mm:ss z"),
		APACHE_LOG ("dd/MMM/yyyy:HH:mm:ss Z"),
		YYYYMM ("yyyyMM"),
		YYYYMMDD ("yyyyMMdd"),
		UK_DEFAULT ("dd/MM/yyyy"),
		DATESTAMP ("yyyyMMdd"),
		TIMESTAMP ("HHmmss"),
		HUMAN_MON_DAY_YEAR ("MMM dd, yyyy")
		;
		
		String str;
		DateFormatString(String str) {
			this.str = str;
		}
		
		public String str() {return str;} 

		public DateFormat format() {return new SimpleDateFormat(str());}
		
		public Date parse(String s) throws ParseException { return format().parse(s);}
		public String format(Date d) { return format().format(d);}
		public String now() {return format().format(new Date());}
		
		public static Date tryAll(String s) throws ParseException {
			
			for (DateFormatString e: DateFormatString.values()) {
				try {
					return e.parse(s);
				} catch (ParseException exp) {
					
				} 
			}
			
			throw new ParseException("Does not match any formats",1);
		}
		
		public XMLGregorianCalendar parseForXml(String s) throws ParseException {
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(parse(s));
			try {
				return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			} catch (DatatypeConfigurationException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
