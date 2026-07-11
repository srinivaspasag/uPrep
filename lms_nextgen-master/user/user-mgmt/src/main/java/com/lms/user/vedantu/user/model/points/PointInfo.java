package com.lms.user.vedantu.user.model.points;

import com.lms.user.vedantu.user.enums.points.PointCategory;

public class PointInfo {

	private PointCategory pointCategory;
	private long pointAsActor;
	private long pointAsOwner;
	private int totalEventAsActor;
	private int totalEventAsOwner;

	public PointInfo() {
		super();
	}

	public PointInfo(PointCategory pointCategory) {
		this.pointCategory = pointCategory;
	}

	public long cumulateActions() {
		return pointAsActor + pointAsOwner;
	}

	public PointCategory getPointCategory() {
		return pointCategory;
	}

	public void setPointCategory(PointCategory pointCategory) {
		this.pointCategory = pointCategory;
	}

	public long getPointAsActor() {
		return pointAsActor;
	}

	public void setPointAsActor(long pointAsActor) {
		this.pointAsActor = pointAsActor;
	}

	public long getPointAsOwner() {
		return pointAsOwner;
	}

	public int getTotalEventAsOwner() {
		return totalEventAsOwner;
	}

	public void setPointAsOwner(long pointAsOwner) {
		this.pointAsOwner = pointAsOwner;
	}

	public void setTotalEventAsOwner(int totalEventAsOwner) {
		this.totalEventAsOwner = totalEventAsOwner;
	}

	public int getTotalEventAsActor() {
		return totalEventAsActor;
	}

	public void setTotalEventAsActor(int totalEventAsActor) {
		this.totalEventAsActor = totalEventAsActor;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((pointCategory == null) ? 0 : pointCategory.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PointInfo other = (PointInfo) obj;
		if (pointCategory != other.pointCategory)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PointInfo [pointCategory:").append(pointCategory)
				.append(", pointAsActor:").append(pointAsActor)
				.append(", pointAsOwner:").append(pointAsOwner)
				.append(", totalEventAsActor:").append(totalEventAsActor)
				.append(", totalEventAsOwner:").append(totalEventAsOwner)
				.append("]");
		return builder.toString();
	}

}
