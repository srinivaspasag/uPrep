package com.vedantu.organization.models.licensing;

import java.util.Currency;
import java.util.List;
import java.util.Locale;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Transient;
import com.vedantu.commons.content.interfaces.ISellableEntity;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.CostRate;
import com.vedantu.commons.pojos.SellableItemDetails;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.organization.enums.PlanState;
import com.vedantu.organization.pojos.LicensingInfo;

/**
 * Example plans Monthly $1 / user/ month Annually
 * 
 * @author vikram
 * 
 */
@Entity(value = "plans", noClassnameStored = true)
public class LicensingPlan extends VedantuBaseMongoModel implements ISellableEntity {

    @Transient
    public static final String STATE = "state";
    public String              name;
    public String              desc;
    public boolean             peruser;
    public float               cost;
    public float               additionalCost;

    public long                users;
    public List<String>        features;
    // this will be needed
    public PlanState           state;
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
        licensingInfo.name = name;
        licensingInfo.cost = cost;
        licensingInfo.desc = desc;
        licensingInfo.peruser = peruser;
        licensingInfo.users = users;
        licensingInfo.features = features;
        licensingInfo.additionalCost = additionalCost;
        licensingInfo.planState = state;

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
