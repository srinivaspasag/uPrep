package com.lms.user.vedantu.user.enums.points;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import com.lms.user.vedantu.user.pojo.points.BadgesCriteria;
public enum Titles {

	NEWBIE("Newbie", 0),
	RAZOR("Razor",
			new HashSet<BadgesCriteria>(
					Arrays.asList(new BadgesCriteria(PointCategory.UPLOAD, RequiredType.COMPULSORY, 0, 5),
							new BadgesCriteria(PointCategory.FOLLOW, RequiredType.COMPULSORY, 2, 10))),
			1500),
	FUNDOO("Fundoo",
			new HashSet<BadgesCriteria>(
					Arrays.asList(new BadgesCriteria(PointCategory.UPLOAD, RequiredType.COMPULSORY, 0, 5),
							new BadgesCriteria(PointCategory.VIEW, RequiredType.COMPULSORY, 5, 10),
							new BadgesCriteria(PointCategory.FOLLOW, RequiredType.COMPULSORY, 2, 10))),
			5000),
	STUD("Stud",
			new HashSet<BadgesCriteria>(
					Arrays.asList(new BadgesCriteria(PointCategory.UPLOAD, RequiredType.COMPULSORY, 0, 5),
							new BadgesCriteria(PointCategory.VIEW, RequiredType.COMPULSORY, 5, 10),
							new BadgesCriteria(PointCategory.FOLLOW, RequiredType.COMPULSORY, 2, 10))),
			20000),
	EXPERT("Expert",
			new HashSet<BadgesCriteria>(
					Arrays.asList(new BadgesCriteria(PointCategory.UPLOAD, RequiredType.COMPULSORY, 0, 5),
							new BadgesCriteria(PointCategory.VIEW, RequiredType.COMPULSORY, 5, 10),
							new BadgesCriteria(PointCategory.FOLLOW, RequiredType.COMPULSORY, 2, 10))),
			50000),
	GURU("Guru",
			new HashSet<BadgesCriteria>(
					Arrays.asList(new BadgesCriteria(PointCategory.UPLOAD, RequiredType.COMPULSORY, 0, 5),
							new BadgesCriteria(PointCategory.VIEW, RequiredType.COMPULSORY, 5, 10),
							new BadgesCriteria(PointCategory.FOLLOW, RequiredType.COMPULSORY, 2, 10))),
			150000),
	GOD("God",
			new HashSet<BadgesCriteria>(
					Arrays.asList(new BadgesCriteria(PointCategory.UPLOAD, RequiredType.COMPULSORY, 0, 5),
							new BadgesCriteria(PointCategory.VIEW, RequiredType.COMPULSORY, 5, 10),
							new BadgesCriteria(PointCategory.FOLLOW, RequiredType.COMPULSORY, 2, 10))),
			500000);

	private String displayName;
	private Set<BadgesCriteria> criterias;
	private int minPoints;

	private Titles(String displayName) {
		this(displayName, 0);
	}

	private Titles(String displayName, int totalPoints) {
		this(displayName, new HashSet<BadgesCriteria>(), 0);
	}

	private Titles(String displayName, Set<BadgesCriteria> criterias, int minPoints) {
		this.displayName = displayName;
		this.criterias = criterias;
		this.minPoints = minPoints;
	}

	public String getDisplayName() {
		return displayName;
	}

	public Set<BadgesCriteria> getCriterias() {
		return criterias;
	}

	public int getMinPoints() {
		return minPoints;
	}

}
