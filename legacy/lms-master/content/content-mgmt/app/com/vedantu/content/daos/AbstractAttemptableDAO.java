package com.vedantu.content.daos;

import org.bson.types.ObjectId;

import com.google.code.morphia.query.UpdateOperations;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.socials.apis.IAttemptable;
import com.vedantu.user.daos.AbstractUserActionDAO;

public abstract class AbstractAttemptableDAO<T extends VedantuBaseMongoModel, K>
		extends AbstractUserActionDAO<T, K> implements IAttemptable {

	public AbstractAttemptableDAO(Class<T> entityClass) {
		super(entityClass);
	}

	@Override
	public VedantuBaseMongoModel incAttemptsCount(String id, int inc) {
		UpdateOperations<T> update = getDS()
				.createUpdateOperations(entityClazz).inc(
						IAttemptable.FIELD_NAME, 1);

		T model = getDS().findAndModify(
				getQuery().filter(FIELD_ID, new ObjectId(id)), update);
		return model;
	}

}
