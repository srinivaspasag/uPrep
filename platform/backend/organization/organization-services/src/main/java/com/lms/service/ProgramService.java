package com.lms.service;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojo.request.*;

public interface ProgramService {

    VedantuResponse getPrograms(GetOrgProgramsReq getOrgProgramsReq);

    VedantuResponse getProgramInfo(GetProgramInfoReq getProgramInfoReq);

    VedantuResponse addProgram(AddOrgProgramReq addOrgProgramReq);

    VedantuResponse updateProgram(UpdateOrgProgramReq updateOrgProgramReq);

    VedantuResponse removeProgram(StateChangeOrgProgramReq stateChangeOrgProgramReq);

    VedantuResponse activateProgram(StateChangeOrgProgramReq stateChangeOrgProgramReq);

    VedantuResponse getProgramCenters(GetOrgProgramCentersReq getOrgProgramCentersReq);

    VedantuResponse addProgramCenters(AddOrgProgramCentersReq addOrgProgramCentersReq);

    VedantuResponse removeProgramCenters(AddOrgProgramCentersReq addOrgProgramCentersReq);

    VedantuResponse getProgramCourses(GetOrgProgramCoursesReq getOrgProgramCoursesReq);

    VedantuResponse addProgramCourses(OrgProgramCoursesReq addOrgProgramCoursesReq);

    VedantuResponse removeProgramCourses(OrgProgramCoursesReq removeOrgProgramCoursesReq);

    VedantuResponse getCoursePrograms(GetOrgCourseProgramsReq getOrgCourseProgramsReq);

}
