package com.lms.user.vedantu.user.pojo.points;

import com.lms.user.vedantu.user.enums.points.PointCategory;
import com.lms.user.vedantu.user.enums.points.RequiredType;

public class BadgesCriteria {
	public PointCategory pointCategory;
	public RequiredType type;
	public int minEventAsOwner;
	public int minEventAsActor;

	public BadgesCriteria(PointCategory pointCategory) {
		this(pointCategory, RequiredType.OPTIONAL);
	}

	public BadgesCriteria(PointCategory pointCategory, RequiredType type) {
		this(pointCategory, type, 0, 0);
	}

	public BadgesCriteria(PointCategory pointCategory, RequiredType type, int minEventAsOwner, int minEventAsActor) {
		this.pointCategory = pointCategory;
		this.type = type;
		this.minEventAsOwner = minEventAsOwner;
		this.minEventAsActor = minEventAsActor;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BadgesCriteria [pointCategory=").append(pointCategory).append(", type=").append(type)
				.append(", minEventAsOwner=").append(minEventAsOwner).append(", minEventAsActor=")
				.append(minEventAsActor).append("]");
		return builder.toString();
	}

}
