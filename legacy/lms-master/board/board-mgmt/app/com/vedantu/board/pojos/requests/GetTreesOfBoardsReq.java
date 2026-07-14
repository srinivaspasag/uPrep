package com.vedantu.board.pojos.requests;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.board.daos.BoardDAO;
import com.vedantu.board.enums.BoardContextType;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetTreesOfBoardsReq extends AbstractAuthCheckReq {

	@Required
	public BoardContextType context;
	public String ownerId = BoardDAO.OWNER_SYSTEM;
	@Required
	public List<String> treeRootIds;
	public int depth = 3;
	
	public String validate() {
		String superValidate = super.validate();
		if (StringUtils.isNotEmpty(superValidate)) {
			return superValidate;
		}
		if (null == context) {
			return "context missing";
		}
		if (CollectionUtils.isEmpty(treeRootIds)) {
			return "treeRootIds missing";
		}
		return null;
	}

}
