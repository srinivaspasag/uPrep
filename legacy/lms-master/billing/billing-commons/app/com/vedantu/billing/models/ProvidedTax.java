package com.vedantu.billing.models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.billing.enums.ItemCategory;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "taxes", noClassnameStored = true)
@Indexes({ @Index(value = "name, location,category", unique = true) })
public class ProvidedTax extends VedantuBaseMongoModel {

    public String       desc;
    public float        percentage;
    public String       name;
    public String       location;
    public ItemCategory category;

    public ProvidedTax() {

    }

    public ProvidedTax(String name, float percentage, String desc, String location,
            ItemCategory category) {

        super();
        this.desc = desc;
        this.percentage = percentage;
        this.name = name;
        this.location = location;
        this.category = category;
    }

}
