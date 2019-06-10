@Model(
		plugins = {
				"uk.co.terminological.javapig.sqlloader.SqlPlugin$Model",
				"uk.co.terminological.javapig.sqlloader.SqlPlugin$Interface"
		}, 
		builtins = { 
				//BuiltIn.IMPL,
				//BuiltIn.MIRROR,
				//BuiltIn.VISITOR,
				BuiltIn.FLUENT,
				BuiltIn.FLUENT_IMPL,
				//BuiltIn.FACTORY,
				BuiltIn.DOTUML,
				BuiltIn.DEBUG
		})
package uk.co.terminological.mimic6;
import uk.co.terminological.javapig.annotations.Model;
import uk.co.terminological.javapig.annotations.BuiltIn;















