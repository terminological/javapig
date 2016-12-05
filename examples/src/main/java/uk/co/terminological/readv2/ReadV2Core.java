package uk.co.terminological.readv2;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.Id;
import javax.persistence.OneToMany;

import uk.co.terminological.javapig.csvloader.Csv;
import uk.co.terminological.javapig.csvloader.Type;
import uk.co.terminological.javapig.index.Searchable;
import uk.co.terminological.javapig.index.Secondary;
import uk.co.terminological.javapig.csvloader.ByField;

@Csv(Type.WIN_CSV)
public interface ReadV2Core {
	
	@Id @ByField(0) public String getReadCode();
	@Searchable @ByField(1) public String getPrefTerm30();
	@Searchable @ByField(2) public Optional<String> getPrefTerm60();
	@Searchable @ByField(3) public Optional<String> getPrefTerm198();
	@Secondary @ByField(4) public String getICD9Code();
	@ByField(5) public Optional<String> getICD9CodeDef();
	@ByField(6) public String getICD9CMCode();
	@ByField(7) public Optional<String> getICD9CMCodeDef();
	@Secondary @ByField(8) public String getOPCS42Code();
	@ByField(9) public Optional<String> getOPCS42CodeDef();
	@ByField(10) public String getSpecialtyFlag();
	@ByField(10) public int getStatusFlag();
	@ByField(11) public String getLanguageCode();
	
	@OneToMany public List<ReadV2Key> getKeys();
	
	public default boolean isCurrent() {return getStatusFlag()==0;}
	public default boolean isDiscontinued() {return getStatusFlag()==1;}
	
	@Secondary public default Optional<String> getParentCode() {
		String out = getReadCode().replaceFirst("[^\\.]\\.", "..");
		if (out.equals(".....")) return Optional.empty();
		return Optional.of(out);
	}
	
	public default Optional<ReadV2Core> getParent() {
		return this.getParentCode().flatMap(pc -> Indexes.get().findReadV2CoreByReadCode(pc));
	}
	
	public default Set<ReadV2Core> getChildren() {
		return Indexes.get().findReadV2CoreByParentCode(this.getReadCode());
	}
	
}
