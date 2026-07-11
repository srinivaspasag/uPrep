package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.enums.ShipmentStatus;
import com.lms.pojo.request.AbstractOrgScopeReq;
import com.lms.pojos.ContactDetails;
import com.lms.pojos.SellerInfo;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
public class GenerateAccessCodeReq extends AbstractOrgScopeReq {
    private static final Logger logger = LoggerFactory.getLogger(GenerateAccessCodeReq.class);

    @NotBlank
    public ContactDetails buyerContactDetails;
    @NotBlank
    public SellerInfo sellerInfo;
    @NotBlank
    public List<SrcEntity> entities; // //TODO
    public ShipmentStatus shipmentStatus;

    public String validate() {
        logger.debug(".....Inside buyerContactDetails validate function.........");

        if (buyerContactDetails == null) {
            return "buyerContactDetails is null";
        }

        // else if (buyerContactDetails.email == null) {
        // 	return "email is null";
        // }

        if (sellerInfo == null) {
            return "sellerInfo is null";
        } else if (sellerInfo.pointOfSale == null) {
            return "pointOfSale is null";
        } else if (sellerInfo.sellerReferenceNo == null) {
            return "sellerReferenceNo is null";
        }

        return null;
    }
}
