package uk.co.terminological.javapig.sqlloader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

import javax.persistence.Id;

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
import uk.co.terminological.mappers.ProxyMapWrapper;

@Mojo( name = "javaFromSql", defaultPhase = LifecyclePhase.GENERATE_SOURCES )
public class JavaFromSqlMojo extends AbstractMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		
		JProject proj = new JProject();
		ArrayList<JavaFromSqlExecution> executions = new ArrayList<>();
		// Declare the JDBC objects.  
		Connection con;
		Statement stmt;
		ResultSet rs;
		ResultSetMetaData rsm;
		
		try {
			Properties p = new Properties();
			p.load(new FileReader(this.connectionProperties));
			
			
			
			String connectionUrl = p.getProperty("url"); 
			// "jdbc:sqlserver://10.174.129.118:1433;databaseName=RobsDatabase;user=YYYY;password=XXXXX";  

			con = null;  
			stmt = null;
			rs = null;
			rsm = null;
			
			Class.forName(p.getProperty("driver"));  
			con = DriverManager.getConnection(connectionUrl);
		} catch (ClassNotFoundException | IOException | SQLException e) {
			throw new MojoFailureException("Could not set up database connection",e);
		}
		
		
		for (JavaFromSqlExecution f: executions) {
			
				System.out.println("JavaFromSql Execution: ["+ f.getSql() + "]: as "+ f.getTargetFQN());
				String sql = "SELECT xxx.* FROM ("+f.getSql()+") xxx WHERE 1=0";
				
				Map<String, Class<?>> methodSignatures;
				try {
					stmt = con.createStatement();
					stmt.executeQuery(sql);
					rs = stmt.getResultSet();
					rsm = rs.getMetaData();
					
					List<String> headers = new ArrayList<>();
					methodSignatures = new LinkedHashMap<>();
					
					for (int i=0; i<rsm.getColumnCount(); i++) {
						
						headers.add(rsm.getColumnLabel(i));
						
						methodSignatures.put(
								rsm.getColumnLabel(i), 
								Class.forName(rsm.getColumnClassName(i)));
						
					}
					
					rs.close();
				} catch (ClassNotFoundException | SQLException e) {
					throw new MojoExecutionException("Could not get query result: "+f.getSql(),e);
				}
				
				proj.addPackage(new JPackage(proj, "",
						FluentList.empty(), 
						JClassName.from(f.getTargetFQN()).getPackageName(), 
						Optional.of(new JPackageMetadata(
								FluentList.create(BuiltIn.CORE),
								FluentList.empty()
								))));
				proj.addInterface(new JInterface(
						proj,"",
						FluentList.create( 
								new JAnnotation(
										Optional.of(Sql.class.getCanonicalName()),
										"Sql",
										FluentList.create(
												new JAnnotationEntry(
														JMethodName.from(Sql.class.getCanonicalName()+"#value"),
														JAnnotationValue.of(f.getSql()))
												))),
						JClassName.from(f.getTargetFQN()),
						FluentSet.empty()));
				int j=0;
				for (Entry<String,Class<?>> methodSignature : methodSignatures.entrySet()) {
					proj.addMethod(new JGetMethod(
							proj,"",
							FluentList.create( 
									new JAnnotation(
											Optional.of(ByLabel.class.getCanonicalName()),
											"ByField",
											FluentList.create(
													new JAnnotationEntry(
															JMethodName.from(ByLabel.class.getCanonicalName()+"#value"),
															JAnnotationValue.of(j))
													))),
							JClassName.from(f.getTargetFQN()),
							JMethodName.from(f.getTargetFQN()+"#"+ProxyMapWrapper.methodName(methodSignature.getKey())),
							methodSignature.getValue().getCanonicalName(),
							JClassName.from(ReflectionUtils.box(methodSignature.getValue()).getCanonicalName()),
							null,
							false
							));
					j++;
				}
				proj.addMethod(new JGetMethod(
						proj,"",
						FluentList.create(new JAnnotation(Optional.of(Id.class.getCanonicalName()),"Id",FluentList.empty())),
						JClassName.from(f.getTargetFQN()),
						JMethodName.from(f.getTargetFQN()+"#getId"),
						"java.lang.String",
						JClassName.from("java.lang.String"),
						null,false
						));
				// Pass whole model to JModelWriter
				JModelWriter writer = new JModelWriter();
				writer.setTargetDirectory(targetDirectory);
				writer.setModel(proj);
				writer.writeSafe(); //TODO: THIS DOESN@T WORK
			} 
		}
	

	@Parameter(required=true)
	JavaFromSqlExecution[] JavaFromSqlExecutions;

	public JavaFromSqlExecution[] getJavaFromSqlExecutions() {
		return JavaFromSqlExecutions;
	}

	public void setJavaFromSqlExecutions(JavaFromSqlExecution[] JavaFromSqlExecutions) {
		this.JavaFromSqlExecutions = JavaFromSqlExecutions;
	}

	@Parameter(required=true)
	File targetDirectory;

	public File getTargetDirectory() {
		return targetDirectory;
	}

	public void setTargetDirectory(File targetDirectory) {
		this.targetDirectory = targetDirectory;
	}

	@Parameter(required=true)
	File connectionProperties;

	public File getConnectionProperties() {
		return connectionProperties;
	}

	public void setConnectionProperties(File file) {
		this.connectionProperties = file;
	}

}
