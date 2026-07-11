package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.CostRate;
import com.lms.common.vedantu.commons.pojos.requests.SellableItemDetails;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.content.ISellableEntity;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.enums.PlanState;
import com.lms.pojo.LicensingInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Currency;
import java.util.List;
import java.util.Locale;

@Document(value = "plans")
@Setter
@Getter
public class LicensingPlan extends VedantuBaseMongoModel implements ISellableEntity {

    @Transient
    public static final String STATE = "state";
    public String              name;
    public String              desc;
    public boolean             peruser;
    public float               cost;
    public float               additionalCost;

    public long                users;
    public List<String> features;
    // this will be needed
    public PlanState state;
    public int                 rank;

    public LicensingPlan() {

        super();

    }

    public LicensingPlan(String name, String desc, boolean peruser, float cost,
                         float additionalCost, long users, List<String> features) {

        super();
        this.name = name;
        this.desc = desc;
        this.peruser = peruser;
        this.cost = cost;
        this.users = users;
        this.features = features;
        this.additionalCost = additionalCost;
        this.state = PlanState.DRAFT;
    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        LicensingInfo licensingInfo = new LicensingInfo(_getStringId(), recordState);
        licensingInfo.setName(name);
        licensingInfo.setCost(cost);
        licensingInfo.setDesc(desc);
        licensingInfo.setPeruser(peruser);
        licensingInfo.setUsers(users);
        licensingInfo.setFeatures(features);
        licensingInfo.setAdditionalCost(additionalCost);
        licensingInfo.setPlanState(state);

        return licensingInfo;
    }

    @Override
    public SrcEntity _getSeller() {

        return DEFAULT_SELLER;
    }

    @Override
    public CostRate _getCostRate() {

        return new CostRate((int) (cost * 100), "per user", Currency.getInstance(Locale.US)
                .getCurrencyCode());
    }


    @Override
    public String _getItemName() {

        return name;
    }

    @Override
    public SellableItemDetails _getSellableItemDetails() {

        return new SellableItemDetails(_getCostRate(), _getItemName(), _getSeller(), new SrcEntity(
                EntityType.PLAN, _getStringId()));
    }

}

