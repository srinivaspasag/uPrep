package com.lms.pojos.tests;

import com.lms.enums.TestResultVisibility;
import com.lms.pojos.TestMetadata;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TestMiniInfo {
    public String name;
    public String code;
    public String id;
    public int qusCount;
    public long duration;
    public int totalMarks;
    public long attempts;
    public List<TestMetadata> metadata;
    public List<TestMiniInfo> children; // paper1/paper2
    public TestResultVisibility resultVisibility;

    public TestMiniInfo() {
    }

    public TestMiniInfo(String name, String code, String id, int qusCount,
                        long duration, int totalMarks) {
        this.name = name;
        this.code = code;
        this.id = id;
        this.qusCount = qusCount;
        this.duration = duration;
        this.totalMarks = totalMarks;
    }

    public void addChild(TestMiniInfo info) {
        if (this.children == null) {
            this.children = new ArrayList<TestMiniInfo>();
        }
        this.children.add(info);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{name:");
        builder.append(name);
        builder.append(", code:");
        builder.append(code);
        builder.append(", id:");
        builder.append(id);
        builder.append(", qusCount:");
        builder.append(qusCount);
        builder.append(", duration:");
        builder.append(duration);
        builder.append(", totalMarks:");
        builder.append(totalMarks);
        builder.append(", metadata:");
        builder.append(metadata);
        builder.append(", children:");
        builder.append(children);
        builder.append("}");
        return builder.toString();
    }

}
