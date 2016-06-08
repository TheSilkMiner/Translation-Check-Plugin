package net.thesilkminer.gradle.plugin.translationchecker.database;

import net.thesilkminer.skl.interpreter.api.skd.structure.IStructure;
import net.thesilkminer.skl.interpreter.api.skd.structure.declarations.doctype.IDocTypeProvider;

import java.net.MalformedURLException;
import java.net.URL;

public class OpenModsProvider implements IDocTypeProvider {

	@Override
	public String name() {
		return "OpenMods";
	}

	@Override
	public boolean canUse() {
		return true;
	}

	@Override
	public URL docTypeUrl() {
		try {
			return new URL("http://openmods.info/thirdyparty/thesilkminer/translationchecker/database.skd");
		} catch (final MalformedURLException ex) {
			throw new RuntimeException("This should never happen", ex);
		}
	}

	@Override
	public boolean isStructureValidForProvider(final IStructure structure) {
		return true; //TODO
	}
}
