package com.vedantu.cmds.pojos;

import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.commons.pojos.responses.ModelExtendedInfo;

public class NotificationInfo extends ModelExtendedInfo implements
		IListResponseObj {
		public String regId;
		public String userId;

		public NotificationInfo() {

		}

		public NotificationInfo(String regId, String userId)
		{
			this.regId=regId;
			this.userId=userId;
		}
}



