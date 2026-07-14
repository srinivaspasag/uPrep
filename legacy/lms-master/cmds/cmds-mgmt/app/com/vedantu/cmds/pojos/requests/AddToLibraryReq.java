package com.vedantu.cmds.pojos.requests;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.OrganizationEntity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class AddToLibraryReq extends AbstractAuthCheckReq {
	@Required
	public List<SrcEntity> contents;
	@Required
	public List<OrganizationEntity> orgEntities;

	@Required
	public String orgId;

	public String validate() {
		if (CollectionUtils.isNotEmpty(orgEntities)) {
			for (OrganizationEntity orgEntity : orgEntities) {
				String value = orgEntity.validate();
				if (value != null) {
					return value;
				}
			}
		} else {
			return "orgentities missing";
		}
		if (CollectionUtils.isNotEmpty(contents)) {
			for (SrcEntity content : contents) {
				String value = content.validate();
				if (value != null) {
					return value;
				}
			}
		} else {
			return "contents missing";
		}
		return null;
	}

}
