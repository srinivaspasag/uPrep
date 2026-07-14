package com.lms.repository.customclasses;

import com.lms.repository.OrgMemberCustom;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class OrgMembeRepoImpl implements OrgMemberCustom{

    private static String collectionName = "orgmembers";
    @Override
    public String getCollectionName() {
        return collectionName;
    }

    @Override
    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }
}
