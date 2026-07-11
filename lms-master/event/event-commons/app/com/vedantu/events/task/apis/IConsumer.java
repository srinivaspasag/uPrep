package com.vedantu.events.task.apis;

public interface IConsumer<E extends IConsumable> {

	void consume(E consumable);

}
