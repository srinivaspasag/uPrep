package com.lms.models;


import com.lms.common.vedantu.enums.Scope;
import com.lms.enums.EnumBasket;
import com.lms.enums.TestMode;
import com.lms.pojos.TestMetadata;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document(value = "usersmodulestatus")
public class UserModuleStatus extends AbstractUserModuleStatus {
    public String iD;
    public String userId;


    public UserModuleStatus() {

        super();
    }

    public UserModuleStatus(String userId, String name, String desc, int qusCount, long duration,
                            int totalMarks, List<TestMetadata> metadata, EnumBasket.TestType type, TestMode mode, String code,
                            Scope scope) {

        //ssuper(userId, name, desc, qusCount, duration, totalMarks, metadata, type, mode, code, scope);
    }
}
