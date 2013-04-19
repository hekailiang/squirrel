package org.squirrelframework.foundation.util;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DuplicateChecker {
	
	private static final Logger logger = LoggerFactory.getLogger(DuplicateChecker.class);
	
	private DuplicateChecker() {}

	public static void checkDuplicate(Class<?> cls) {
		checkDuplicate(cls.getName().replace('.', '/') + ".class");
	}

	public static void checkDuplicate(String path) {
		try {
			// Search file within classpath
			Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(path);
			Set<String> files = new HashSet<String>();
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				if (url != null) {
					String file = url.getFile();
					if (file != null && file.length() > 0) {
						files.add(file);
					}
				}
			}
			// If there are multiple files, it means duplicate jar placed in the classpath
			if (files.size() > 1) {
				logger.error("Duplicate class " + path + " in " + files.size() + " jar " + files);
			}
		} catch (Throwable e) { 
			logger.error(e.getMessage(), e);
		}
	}
}