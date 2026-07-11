package com.vedantu.mongo;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import play.Application;
import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;

public class MorphiaManager {

	private static final ALogger		LOGGER		= Logger.of(MorphiaManager.class);

	public static final MorphiaManager	INSTANCE	= new MorphiaManager();

	private final Morphia				morphia;
	private final Datastore				ds;

	private MorphiaManager() {
		morphia = new Morphia();
		
		
		String userName = Play.application().configuration().getString("mongodb.username");
		if( StringUtils.isNotEmpty(userName)){
		    String password = Play.application().configuration().getString("mongodb.password");
		    ds = morphia.createDatastore(MongoManager.INSTANCE.getMongo(),
	                MongoManager.INSTANCE.getDBName(), userName, password.toCharArray());
		
		}else{
		    ds = morphia.createDatastore(MongoManager.INSTANCE.getMongo(),
	                MongoManager.INSTANCE.getDBName());
		}
	
	}

	public Morphia getMorphia() {
		return morphia;
	}

	public Datastore getDS() {
		return ds;
	}

	public void __initailize(Application app) {

		// TODO: Is there a better way to achieve this?
		final Application tApp = app;
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				mapEntities(tApp);
				ds.ensureIndexes();
			}
		});

		t.start();
	}

	private void mapEntities(Application app) {
		// REFER: http://softbork.com/blog/index.php?id=8
		// REFER: https://code.google.com/p/reflections/

		LOGGER.info("looking for annotations");

		Reflections reflections = new Reflections("com.vedantu");
		Set<Class<?>> entityClasses = reflections
				.getTypesAnnotatedWith(Entity.class);

		Set<Class<?>> embededClasses = reflections
				.getTypesAnnotatedWith(Embedded.class);
	
		Set<Class<?>> mappableClasses = new HashSet<Class<?>>();
		mappableClasses.addAll(embededClasses);
		mappableClasses.addAll(entityClasses);

		for (Class<?> mappableClass : mappableClasses) {
			if (StringUtils.startsWith(mappableClass.getName(), "com.vedantu.")) {
				LOGGER.info("included mappableClassname: "
						+ mappableClass.getName());
				morphia.map(mappableClass);
			} else {
				LOGGER.info("excluded mappableClassname: "
						+ mappableClass.getName());
			}
		}

		LOGGER.info("done");
	}
}
