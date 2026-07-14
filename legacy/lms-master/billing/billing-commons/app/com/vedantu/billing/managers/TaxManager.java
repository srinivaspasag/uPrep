package com.vedantu.billing.managers;

import java.util.ArrayList;
import java.util.List;

import com.vedantu.billing.dao.TaxDAO;
import com.vedantu.billing.enums.ItemCategory;
import com.vedantu.billing.models.ProvidedTax;
import com.vedantu.billing.models.Tax;
import com.vedantu.commons.pojos.Location;

public class TaxManager {

    private TaxManager() {

    }

    public static List<Tax> getTaxes(ItemCategory category, Location shippedToLocation) {

        // category wise taxes
        // location wise taxes
        // TODO: omplete this
        List<Tax> taxes = new ArrayList<Tax>();
        if (category == ItemCategory.PLAN) {
            taxes.add(new Tax("VAT", "Value Added Tax", 12.36f, "VAT"));
            taxes.add(new Tax("Service Tax", "Service Tax", 5.50f, "ServiceTax"));
        } else if (category == ItemCategory.SECTION) {
            // taxes.add(new Tax("VAT", "Value Added Tax", 12.36f, "VAT"));
            // taxes.add(new Tax("Service Tax", "Service Tax", 5.50f, "ServiceTax"));
        }
        return taxes;

    }

    public static void addTax(String name, String desc, float percentage, ItemCategory category,
            String location) {

        // category wise taxes
        TaxDAO.INSTANCE.save(new ProvidedTax(name, percentage, desc, location, category));

    }

}
