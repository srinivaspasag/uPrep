package com.vedantu.events.task.apis;

public interface IProducer <E extends IConsumable> {
	ConsumableIterator<E> produce();
}
