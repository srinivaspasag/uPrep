package com.vedantu.billing.manager;

import java.util.ArrayList;
import java.util.List;

import com.vedantu.billing.dao.SaleDetailsDAO;
import com.vedantu.billing.models.SaleDetails;
import com.vedantu.billing.pojos.SaleDetailsDisplayInfo;
import com.vedantu.billing.pojos.SaleDetailsInfo;
import com.vedantu.billing.pojos.requests.saledetails.GetSaleDetailsListReq;
import com.vedantu.billing.pojos.responses.saledetails.GetSaleDetailsListRes;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.daos.OrgSectionDAO;
import com.vedantu.organization.models.OrgMember;
import com.vedantu.organization.pojos.OrgProgramBasicInfo;

public class SaleDetailsManager {

    public static final SaleDetailsManager INSTANCE = new SaleDetailsManager();

    public GetSaleDetailsListRes getSaleDetails(GetSaleDetailsListReq getSaleDetailsListReq) {
        List<SaleDetails> salesDetailsList = SaleDetailsDAO.INSTANCE
                .getAllSaleDetails(getSaleDetailsListReq.orgId);

        GetSaleDetailsListRes response = new GetSaleDetailsListRes();
        List<SaleDetailsDisplayInfo> saleDetailsDisplayInfos = new ArrayList<SaleDetailsDisplayInfo>();

        for (SaleDetails saleDetails : salesDetailsList) {
            SaleDetailsInfo salesDetailInfo = (SaleDetailsInfo) saleDetails.toBasicInfo();

            OrgMember student = OrgMemberDAO.INSTANCE.getById(saleDetails.orgMemberId);

            OrgMember salesPerson = OrgMemberDAO.INSTANCE.getMemberByUserId(
                    getSaleDetailsListReq.orgId, saleDetails.salesPersonId);

            OrgProgramBasicInfo programInfo = OrgSectionDAO.INSTANCE
                    .getProgramFromSectionId(saleDetails.sectionId);

            SaleDetailsDisplayInfo saleDetailsDisplayInfo = new SaleDetailsDisplayInfo();
            saleDetailsDisplayInfo.saleDetailsInfo = salesDetailInfo;
            saleDetailsDisplayInfo.salesPersonName = salesPerson == null ? "Not Available"
                    : salesPerson.firstName;
            saleDetailsDisplayInfo.studentName = student.firstName;
            saleDetailsDisplayInfo.saleHappenedOn = saleDetails.timeCreated;
            saleDetailsDisplayInfo.programName = programInfo.name;
            saleDetailsDisplayInfos.add(saleDetailsDisplayInfo);
        }
        response.saleDetailsDisplayInfos = saleDetailsDisplayInfos;
        return response;
    }

}
