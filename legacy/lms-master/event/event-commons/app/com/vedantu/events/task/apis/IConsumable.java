package com.vedantu.events.task.apis;

import java.util.Set;

public interface IConsumable {

	String _getConsumableId();

	Set<String> _getProcessedBy();

	void addProcessedBy(String processor);

}
