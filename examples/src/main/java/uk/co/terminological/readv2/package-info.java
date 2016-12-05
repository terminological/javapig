@Model(
		plugins = {
				"uk.co.terminological.javapig.index.IndexablePlugin$Model",
				"uk.co.terminological.javapig.index.IndexablePlugin$Interface",
				"uk.co.terminological.javapig.csvloader.CsvPlugin$Factory",
				"uk.co.terminological.javapig.csvloader.CsvPlugin$POJO"
		}, 
		builtins = { 
				BuiltIn.IMPL,
				//BuiltIn.MIRROR,
				//BuiltIn.VISITOR,
				//BuiltIn.FLUENT,
				//BuiltIn.FLUENT_IMPL,
				//BuiltIn.FACTORY,
				//BuiltIn.DOTUML,
				BuiltIn.DEBUG
		})
package uk.co.terminological.readv2;
import uk.co.terminological.javapig.annotations.Model;
import uk.co.terminological.javapig.annotations.BuiltIn;















