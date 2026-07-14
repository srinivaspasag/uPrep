package com.lms.pojo.responce.device.mgmt;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceStatusRes {
    public boolean recorded;

    public DeviceStatusRes(boolean recorded) {

        this.recorded = recorded;
    }
}
