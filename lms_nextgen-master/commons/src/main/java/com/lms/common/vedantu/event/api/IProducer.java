package com.lms.common.vedantu.event.api;

public interface IProducer <E extends IConsumable> {
	ConsumableIterator<E> produce();
}
