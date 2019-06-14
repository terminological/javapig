package uk.co.terminological.javapig.sqlloader;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.persistence.Id;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import uk.co.terminological.datatypes.FluentList;
import uk.co.terminological.datatypes.FluentSet;
import uk.co.terminological.javapig.JModelWriter;
import uk.co.terminological.javapig.annotations.BuiltIn;
import uk.co.terminological.javapig.javamodel.JAnnotation;
import uk.co.terminological.javapig.javamodel.JAnnotationEntry;
import uk.co.terminological.javapig.javamodel.JAnnotationValue;
import uk.co.terminological.javapig.javamodel.JClassName;
import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JInterface;
import uk.co.terminological.javapig.javamodel.JMethodName;
import uk.co.terminological.javapig.javamodel.JPackage;
import uk.co.terminological.javapig.javamodel.JPackageMetadata;
import uk.co.terminological.javapig.javamodel.JProject;
import uk.co.terminological.javapig.scanner.ReflectionUtils;
import uk.co.terminological.javapig.sqlloader.SqlUtils.ColumnDetail;
import uk.co.terminological.javapig.sqlloader.SqlUtils.QueryDetail;
import uk.co.terminological.javapig.sqlloader.SqlUtils.TableDetail;
import uk.co.terminological.mappers.ProxyMapWrapper;

@Mojo( name = "javaFromSql", defaultPhase = LifecyclePhase.GENERATE_SOURCES )
public class JavaFromSqlMojo extends AbstractMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		JProject proj = new JProject();
		ArrayList<JavaFromQuery> executions = new ArrayList<>();
		ArrayList<JavaFromTable> tablesExec = new ArrayList<>();


		Properties prop = null;
		Connection conn = null;

		try {

			prop =  new Properties();
			System.out.println("Properties file: "+this.connectionProperties.getAbsolutePath());
			prop.load(new FileInputStream(this.connectionProperties));

			System.out.println("Driver: "+prop.getProperty("driver"));
			System.out.println("Url: "+prop.getProperty("url"));

			Class.forName(prop.getProperty("driver"));
			conn = DriverManager.getConnection(prop.getProperty("url"), prop);

		} catch (Exception e) {
			throw new MojoExecutionException("exception setting up database connection: "+e.getLocalizedMessage(), e);
		}

		if (baseDirectory != null && filenameFilter != null && defaultTargetPackage != null) {
			FileFilter filter = new WildcardFileFilter(this.filenameFilter);
			File[] inputs = this.baseDirectory.listFiles(filter);

			for (File input: inputs) {
				try {
					JavaFromQuery execution = new JavaFromQuery();
					String sql;
					BOMInputStream bomIn = new BOMInputStream(Files.newInputStream(input.toPath()),
							ByteOrderMark.UTF_8,
							ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_16BE,
							ByteOrderMark.UTF_32LE, ByteOrderMark.UTF_32BE
							);
					if (bomIn.hasBOM() == false) {
						sql = IOUtils.toString(bomIn);
					} else {
						sql = IOUtils.toString(bomIn, bomIn.getBOMCharsetName());
					}
					execution.setSql(sql.trim());
					execution.setTargetFQN(ProxyMapWrapper.className(defaultTargetPackage,input.getName()));
					executions.add(execution);
				} catch (IOException e) {
					e.printStackTrace();
					throw new MojoFailureException(e.getLocalizedMessage());
				}
			}
		}

		if (javaFromDatabases != null) {
			Map<String, List<String>> tableNamesLookup;
			try {
				tableNamesLookup = SqlUtils.getDatabaseTables(conn);
			} catch (Exception e) {
				e.printStackTrace();
				throw new MojoFailureException(e.getLocalizedMessage());
			}
			for(JavaFromDatabase database: javaFromDatabases) {
				List<String> tableNames = tableNamesLookup.getOrDefault(database.getName(), Collections.emptyList());
				for (String tableName: tableNames) {
					JavaFromTable table = new JavaFromTable();
					table.setName(tableName);
					table.setTargetFQN(ProxyMapWrapper.className(database.getTargetPackage(),tableName));
					tablesExec.add(table);
				}
			}
		}

		if (javaFromTables != null) {
			tablesExec.addAll(Arrays.asList(javaFromTables));
		}

		if (javaFromQueries != null) {
			executions.addAll(Arrays.asList(javaFromQueries));
		}

		// SQL execution methods.

		for (JavaFromQuery f: executions) {

			System.out.println("JavaFromSQL Execution: "+ f.getTargetFQN() + ": from '"+ f.getSql()+"'");

			QueryDetail methodSignatures;
			try {
				methodSignatures = SqlUtils.getQueryDetail(conn, f.getSql());
			} catch (SQLException e) {
				e.printStackTrace();
				throw new MojoExecutionException(e.getLocalizedMessage());
			}


			String packageFQN = JClassName.from(f.getTargetFQN()).getPackageName();
			if (!proj.packageIsDefined(packageFQN)) {
				proj.addPackage(new JPackage(proj, "",
						FluentList.empty(), 
						packageFQN, 
						Optional.of(new JPackageMetadata(
								FluentList.create(BuiltIn.CORE, BuiltIn.FLUENT, BuiltIn.FLUENT_IMPL, BuiltIn.FACTORY),
								FluentList.create(
										"uk.co.terminological.javapig.sqlloader.SqlPlugin$Model",
										"uk.co.terminological.javapig.sqlloader.SqlPlugin$Interface"
										)
								))));
			}
			List<Class<?>> parameterTypes = methodSignatures.getParameters().stream().map(p -> p.getJavaType()).collect(Collectors.toList());
			proj.addInterface(new JInterface(
					proj,"",
					FluentList.create( 
							new JAnnotation(
									Optional.of(Query.class.getCanonicalName()),
									"Query",
									FluentList.create(
											new JAnnotationEntry(
													JMethodName.from(Query.class.getCanonicalName()+"#sql"),
													JAnnotationValue.of(f.getSql())),
											new JAnnotationEntry(
													JMethodName.from(Query.class.getCanonicalName()+"#parameterTypes"),
													JAnnotationValue.of(parameterTypes))

											))),
					JClassName.from(f.getTargetFQN()),
					FluentSet.empty()));
			addColumnMethods(proj,methodSignatures.getColumns(),f.getTargetFQN());
			proj.addMethod(new JGetMethod(
					proj,"",
					FluentList.create(new JAnnotation(Optional.of(Id.class.getCanonicalName()),"Id",FluentList.empty())),
					JClassName.from(f.getTargetFQN()),
					JMethodName.from(f.getTargetFQN()+"#getRowNumber"),
					"java.lang.Integer",
					JClassName.from("java.lang.Integer"),
					null,false
					));
		}



		/***********************************************************************/





		for (JavaFromTable t: tablesExec) {


			System.out.println("JavaFromSQL Table: "+ t.getTargetFQN() + ": from "+ t.getName());

			TableDetail tableDetail;
			try {
				tableDetail = SqlUtils.getTableDetail(conn, t.getName());
			} catch (SQLException e) {
				e.printStackTrace();
				throw new MojoExecutionException(e.getLocalizedMessage());
			}

			String packageFQN = JClassName.from(t.getTargetFQN()).getPackageName();
			if (!proj.packageIsDefined(packageFQN)) {
				proj.addPackage(new JPackage(proj, "",
						FluentList.empty(), 
						packageFQN, 
						Optional.of(new JPackageMetadata(
								FluentList.create(BuiltIn.CORE, BuiltIn.FLUENT, BuiltIn.FLUENT_IMPL, BuiltIn.FACTORY),
								FluentList.create(
										"uk.co.terminological.javapig.sqlloader.SqlPlugin$Model",
										"uk.co.terminological.javapig.sqlloader.SqlPlugin$Interface"
										)
								))));
			}

			proj.addInterface(new JInterface(
					proj,"",
					FluentList.create( 
							new JAnnotation(
									Optional.of(Table.class.getCanonicalName()),
									"Table",
									FluentList.create(
											new JAnnotationEntry(
													JMethodName.from(Table.class.getCanonicalName()+"#name"),
													JAnnotationValue.of(tableDetail.getName())),
											new JAnnotationEntry(
													JMethodName.from(Table.class.getCanonicalName()+"#schema"),
													JAnnotationValue.of(tableDetail.getSchema()))
											))),
					JClassName.from(t.getTargetFQN()),
					FluentSet.empty()));
			addColumnMethods(proj,tableDetail.getColumns(),t.getTargetFQN());

		}

		/***********************************************************************/

		if (conn != null) try { conn.close(); } catch(Exception e) {} 

		// Pass whole model to JModelWriter
		JModelWriter writer = new JModelWriter();
		writer.setTargetDirectory(targetDirectory);
		writer.setModel(proj);
		writer.writeSafe(); //TODO: THIS DOESN'T WORK but is ignored


	}

	void addColumnMethods(JProject proj, List<ColumnDetail> methodSignatures, String targetFQN) {
		for (ColumnDetail methodSignature : methodSignatures) {
			proj.addMethod(new JGetMethod(
					proj,"",
					FluentList.create( 
							new JAnnotation(
									Optional.of(Column.class.getCanonicalName()),
									"Column",
									FluentList.create(
											new JAnnotationEntry(
													JMethodName.from(Column.class.getCanonicalName()+"#name"),
													JAnnotationValue.of(methodSignature.getColumnLabel())
													),
											new JAnnotationEntry(
													JMethodName.from(Column.class.getCanonicalName()+"#jdbcType"),
													JAnnotationValue.of(methodSignature.getJDBCType())
													),
											new JAnnotationEntry(
													JMethodName.from(Column.class.getCanonicalName()+"#length"),
													JAnnotationValue.of(methodSignature.getLength())
													),
											new JAnnotationEntry(
													JMethodName.from(Column.class.getCanonicalName()+"#isNullable"),
													JAnnotationValue.of(methodSignature.isNullable())
													),
											new JAnnotationEntry(
													JMethodName.from(Column.class.getCanonicalName()+"#isAutoIncrement"),
													JAnnotationValue.of(methodSignature.isAutoIncrement())
													)
											))),
					JClassName.from(targetFQN),
					JMethodName.from(targetFQN+"#"+ProxyMapWrapper.methodName(methodSignature.getColumnLabel())),
					//TODO: Optionals if isNullable?
					methodSignature.getJavaType().getCanonicalName(),
					JClassName.from(ReflectionUtils.box(methodSignature.getJavaType()).getCanonicalName()),
					null,
					false
					));
		}
	}



	@Parameter(required=false)
	File baseDirectory;

	@Parameter(required=false)
	String filenameFilter = "*.sql";

	@Parameter(required=false)
	String defaultTargetPackage;

	@Parameter(required=true)
	File connectionProperties;

	@Parameter(required=false)
	JavaFromQuery[] javaFromQueries;

	@Parameter(required=false)
	JavaFromTable[] javaFromTables;

	@Parameter(required=false)
	JavaFromDatabase[] javaFromDatabases;

	@Parameter(required=true)
	File targetDirectory;

	public File getBaseDirectory() {
		return baseDirectory;
	}

	public void setBaseDirectory(File baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	public File getConnectionProperties() {
		return connectionProperties;
	}

	public void setConnectionProperties(File connectionProperties) {
		this.connectionProperties = connectionProperties;
	}

	public String getDefaultTargetPackage() {
		return defaultTargetPackage;
	}

	public void setDefaultTargetPackage(String defaultTargetPackage) {
		this.defaultTargetPackage = defaultTargetPackage;
	}


	public JavaFromQuery[] getJavaFromSqlExecutions() {
		return javaFromQueries;
	}

	public void setJavaFromSqlExecutions(JavaFromQuery[] JavaFromSqlExecutions) {
		this.javaFromQueries = JavaFromSqlExecutions;
	}

	public File getTargetDirectory() {
		return targetDirectory;
	}

	public void setTargetDirectory(File targetDirectory) {
		this.targetDirectory = targetDirectory;
	}

	public String getFilenameFilter() {
		return filenameFilter;
	}

	public void setFilenameFilter(String filenameFilter) {
		this.filenameFilter = filenameFilter;
	}

	public JavaFromQuery[] getJavaFromQueries() {
		return javaFromQueries;
	}

	public void setJavaFromQueries(JavaFromQuery[] javaFromQueries) {
		this.javaFromQueries = javaFromQueries;
	}

	public JavaFromTable[] getJavaFromTables() {
		return javaFromTables;
	}

	public void setJavaFromTables(JavaFromTable[] javaFromTables) {
		this.javaFromTables = javaFromTables;
	}

	public JavaFromDatabase[] getJavaFromDatabases() {
		return javaFromDatabases;
	}

	public void setJavaFromDatabases(JavaFromDatabase[] javaFromDatabases) {
		this.javaFromDatabases = javaFromDatabases;
	}




}
