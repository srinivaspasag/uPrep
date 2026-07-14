package com.lms.pojos.tests;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SimplifiedBoardNames {

    public String simplifiedName;
    public List<String> brdIds;

    public SimplifiedBoardNames() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean equals(Object obj) {
        if(simplifiedName == null){
            return false;
        }
        if(! (obj instanceof SimplifiedBoardNames)) {
            return false;
        }
        return simplifiedName.equals(((SimplifiedBoardNames)obj).simplifiedName);//super.equals(obj);
    }

}
