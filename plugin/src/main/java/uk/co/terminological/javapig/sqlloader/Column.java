package uk.co.terminological.javapig.sqlloader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.JDBCType;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Column {
	String name();
	JDBCType jdbcType();
	int length() default Integer.MAX_VALUE;
	boolean isAutoIncrement() default false;
	boolean isNullable() default true;
}
