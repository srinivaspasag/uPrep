package com.lms.models;

import com.lms.common.utils.ImageDisplayURLUtil;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.content.IIndexable;
import com.lms.common.vedantu.entity.storage.FileCategory;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.interfaces.ICMDSModel;
import com.lms.pojos.requests.CMDSFileInfo;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.StringUtils;

import java.util.Map;

@Document(value = "cmdsfiles")
public class CMDSFile extends Files implements IIndexable, ICMDSModel {

	@Indexed
	public String globalFileId;

	Map<String, String> presetMap;

	public CMDSFile() {

		super();
		published = false;
		scope = Scope.ORG;
		recordState = VedantuRecordState.TEMPORARY;
		contentType = EntityType.CMDSFILE;
	}

	@Override
	public String toString() {

		String currentObjectString = super.toString();

		StringBuilder builder = new StringBuilder();
		if (!StringUtils.isEmpty(currentObjectString)) {
			builder.append(currentObjectString);
		}

		builder.append(" g   info.url = url;lobalFileId : ").append(globalFileId);
		return builder.toString();
	}

	@Override
	public ModelBasicInfo toBasicInfo() {

		String orgId = (contentSrc != null) ? contentSrc.id : "";

		CMDSFileInfo info = new CMDSFileInfo(_getStringId(), name, EntityType.CMDSFILE, orgId, timeCreated, lastUpdated,
				userId, 0, published, completed, converted, globalFileId, recordState, linkType,
				this.getExportableSize());

		if (!StringUtils.isEmpty(thumbnail)) {
			info.thumbnail = ImageDisplayURLUtil.getEntityImageURL(EntityType.CMDSFILE, thumbnail);

		}
		// info.url = url;

		info.url = ImageDisplayURLUtil.getEntityFileURL(EntityType.CMDSFILE, this.uuid, extension,
				FileCategory.ORIGINAL);

		return info;
	}

	@Override
	public String getGlobalId() {

		return globalFileId;
	}

	@Override
	public long getExportableSize() {

		if (size != null) {
			return size.getThumbnail() + size.getEncrypted();
		}
		return 0;
	}

}
