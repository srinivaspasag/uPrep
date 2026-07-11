package com.lms.common.vedantu.event.api;

public interface IConsumer<E extends IConsumable> {

	void consume(E consumable);

}
