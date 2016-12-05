package uk.co.terminological.readv2;

import java.util.Optional;

import javax.persistence.Id;
import javax.persistence.ManyToOne;

import uk.co.terminological.javapig.annotations.Inverse;
import uk.co.terminological.javapig.csvloader.ByField;
import uk.co.terminological.javapig.csvloader.Csv;
import uk.co.terminological.javapig.csvloader.Type;

@Csv(Type.WIN_CSV)
public interface ReadV2Key {
	
	@ByField(0) public Optional<String> getTermKey();
	@ByField(1) public String getUniquifier();
	@ByField(2) public String getTerm30();
	@ByField(3) public Optional<String> getTerm60();
	@ByField(4) public Optional<String> getTerm198();
	@ByField(5) public String getTermCode();
	@ByField(6) public String getLanguageCode();
	@ManyToOne  
	@Inverse("uk.co.terminological.readv2.ReadV2Core#getKeys") 
	@ByField(7) public ReadV2Core getReadCode();
	@ByField(8) public String getStatusFlag();
	
	@Id 
	default String getUniqueId() {
		return getReadCode()+"-"+getTermCode();
	}
	
	default boolean isPreferredTerm() {
		return getTermCode().equals("00");
	}
	
	default boolean isSynonym() {
		return !isPreferredTerm() && !isUserDefined();
	}
	
	default boolean isUserDefined() {
		return getTermCode().startsWith("z");
	}
}
