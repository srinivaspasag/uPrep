package com.vedantu.cmds.pojos;

import com.vedantu.commons.pojos.CostRate;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;

public class ConsumableItemInfo {
	public SrcEntity entity;
	public long verifiedTime;
	public boolean verified;
	public CostRate costRate;
	public ModelBasicInfo info;
}
