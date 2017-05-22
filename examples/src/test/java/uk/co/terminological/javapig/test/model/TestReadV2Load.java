package uk.co.terminological.javapig.test.model;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Optional;

import uk.co.terminological.readv2.CsvFactory;
import uk.co.terminological.readv2.Indexes;
import uk.co.terminological.readv2.ReadV2Core;
import uk.co.terminological.readv2.ReadV2Key;

public class TestReadV2Load {

	public TestReadV2Load() {
		
	}
	
	static Path core =  FileSystems.getDefault().getPath("/home/terminological/Data/TRUD/nhs_readv2_21.0.0_20160401000001/V2/Unified/Corev2.all");
	static Path key =  FileSystems.getDefault().getPath("/home/terminological/Data/TRUD/nhs_readv2_21.0.0_20160401000001/V2/Unified/Keyv2.all");
	
	public static void main(String[] args) throws IOException {
		System.out.println("CORES");
		for (ReadV2Core coreEntry : CsvFactory.getReadV2Core(core)) {
			if (coreEntry.getReadCode().equalsIgnoreCase("H33..")) System.out.println(coreEntry.print());
		}
		System.out.println("KEYS");
		for (ReadV2Key keyEntry : CsvFactory.getReadV2Key(key)) {
			// System.out.println(keyEntry.print());
		}
		
		Optional<ReadV2Core> opt = Indexes.get().findReadV2CoreByReadCode("H33..");
		System.out.println(opt.get().print());
		for (ReadV2Core children: opt.get().getChildren()) {
			System.out.println("C\t"+children.print());
		}
		for (ReadV2Key keys: opt.get().getKeys()) {
			System.out.println("K\t"+keys.print());
		}
	}

}
