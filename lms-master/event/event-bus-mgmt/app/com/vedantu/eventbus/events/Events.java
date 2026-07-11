package com.vedantu.eventbus.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.enums.Status;

public class Events implements IConsumable {

	private String uuid = UUID.randomUUID().toString();
	private List<Event> events;
	private SrcEntity srcEntity;
	private Map<String, Status> eventsStatus;

	public Events(SrcEntity srcEntity) {
		this.srcEntity = srcEntity;
		this.events = new ArrayList<Event>();
		this.eventsStatus = new HashMap<String, Status>();
	}

	public void add(Event event) {
		events.add(event);
	}

	public void addAll(Collection<Event> events) {
		this.events.addAll(events);
	}

	public SrcEntity getSrcEntity() {
		return srcEntity;
	}

	public List<Event> getEvents() {
		return events;
	}

	public Status getStatus(Event event) {
		return eventsStatus.get(event._getStringId());
	}

	public void setStatus(Event event, Status status) {
		eventsStatus.put(event._getStringId(), status);
	}

	@Override
	public String _getConsumableId() {
		return this.uuid;
	}

	@Override
	public void addProcessedBy(String processor) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<String> _getProcessedBy() {
		// TODO Auto-generated method stub
		return null;
	}

}
