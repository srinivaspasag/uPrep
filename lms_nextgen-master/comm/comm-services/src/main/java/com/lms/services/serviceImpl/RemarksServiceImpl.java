package com.lms.services.serviceImpl;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.components.RemarksComponent;
import com.lms.requests.remarks.AddRemarksReq;
import com.lms.requests.remarks.GetRemarksReq;
import com.lms.response.remarks.AddRemarksRes;
import com.lms.response.remarks.GetRemarksRes;
import com.lms.services.RemarksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RemarksServiceImpl implements RemarksService {
	@Autowired
	private RemarksComponent remarksComponent;

	@Override
	public VedantuResponse addRemark(AddRemarksReq addRemarksReq) {
		AddRemarksRes addRemarkRes;
		try {
			addRemarkRes = remarksComponent.addRemark(addRemarksReq);

		} catch (VedantuException e) {

			throw e;
		}
		return new VedantuResponse(addRemarkRes);
	}

	@Override
	public VedantuResponse getRemarksForUser(GetRemarksReq getRemarksReq) {
		GetRemarksRes getRemarksRes;
		try {
			getRemarksRes = remarksComponent.getRemarksForUser(getRemarksReq);

		} catch (VedantuException e) {

			throw e;
		}
		return new VedantuResponse(getRemarksRes);

	}

}
