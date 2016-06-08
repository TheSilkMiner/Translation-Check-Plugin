package net.thesilkminer.gradle.plugin.translationchecker.bridge;

import net.thesilkminer.gradle.plugin.translationchecker.database.OpenModsProvider;
import net.thesilkminer.gradle.plugin.translationchecker.descriptor.TaskDescriptor;
import net.thesilkminer.skl.interpreter.api.skd.SkdApi;
import net.thesilkminer.skl.interpreter.api.skd.holder.IDatabaseHolder;
import net.thesilkminer.skl.interpreter.api.skd.parser.ISkdParser;
import net.thesilkminer.skl.interpreter.api.skd.structure.IDatabase;
import net.thesilkminer.skl.interpreter.api.skd.structure.declarations.doctype.IDocTypeProvider;

import java.lang.reflect.Method;

@SuppressWarnings("WeakerAccess")
public class PluginBridge$Java {

	private static boolean init = false;

	public static TaskDescriptor obtainDescriptor(final String filePath) {
		if (!init) {
			try {
				final IDocTypeProvider provider = new OpenModsProvider();
				final Class<?> docTypes = Class.forName("net.thesilkminer.skl.interpreter.api."
						+ "skd.structure.declarations.doctype.DocTypes");
				final Method get = docTypes.getDeclaredMethod("get");
				final Object instance = get.invoke(null);
				final Method addProvider = docTypes.getDeclaredMethod("addProvider", IDocTypeProvider.class);
				final boolean result = (boolean) addProvider.invoke(instance, provider);
				if (!result) {
					throw new ReflectiveOperationException("Something went wrong while registering the provider");
				}
			} catch (final ReflectiveOperationException ex) {
				throw new RuntimeException("An unknown error has occurred", ex);
			}

			init = true;
		}

		final IDatabaseHolder holder = SkdApi.get().databaseHolder(new java.io.File(filePath));
		final ISkdParser parser = SkdApi.get().parser(holder);
		parser.init(false);
		final IDatabase database = parser.read();
		final TaskDescriptor descriptor = new TaskDescriptor();
		populateDescriptor(database, descriptor);
		return descriptor;
	}

	public static void populateDescriptor(final IDatabase database, final TaskDescriptor descriptor) {
		PluginBridge$Groovy.populateDescriptor(database, descriptor);
	}
}
