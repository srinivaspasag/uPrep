var vHistory = new (function() {
    var urlPathStrips, urlParams, urlMapper = {};

    var home = "HOME", resources = 'RESOURCES', addcontent = 'ADDCONTENT', user = "USER",
            test = 'TEST', testseries = 'TESTSERIES', question = 'QUESTION',
            document = "DOCUMENT", video = "VIDEO", search = "SEARCH", assignment = "ASSIGNMENT",
            challenge = "CHALLENGE", packageVar = "PACKAGE", cdp = "CDP", file = "FILE",
            academicstructure = "ACADEMICSTRUCTURE", addmember = "ADDMEMBER",
            people = "PEOPLE", coursemanagement = "COURSEMANAGEMENT", devices = "DEVICES",
            program = "PROGRAM", questionset = "QUESTIONSET", channels = "CHANNELS", member = "MEMBER", exports = "EXPORTS",
	    sdcardGroup = "SDCARDGROUP",sdcardGroups = "SDCARDGROUPS"
            student = "STUDENT", offlineuser = "OFFLINEUSER", profile = "PROFILE", folder = "FOLDER", instituteinfo = "INSTITUTEINFO",
            addmember = "ADDMEMBER", editmember = "EDITMEMBER", orgplan = "ORGPLANS", invoices = "INVOICES", managesignup = "MANAGESIGNUP"
            customizesignup = "CUSTOMIZESIGNUP", reviews="REVIEWS",generatetest = "GENERATETEST",module = "MODULE",sellerdashboard="SELLERDASHBOARD",referralusers="REFERRALUSERS",sendtogcmpopup="SENDTOGCMPOPUP",checkDuplicates = "CHECKDUPLICATES";


    var siglePathList = [home, resources, addcontent, search, academicstructure,
        coursemanagement, people, channels, profile, instituteinfo, addmember, editmember,checkDuplicates];
    var doublePathList = [question, test, testseries, user, document, video, assignment, file,
        challenge, packageVar, cdp, questionset, member, student, offlineuser, folder, sellerdashboard,reviews];

    var pageNotFound = function() {
        $.get("/uicomwidgets/pageNotFound", function(data) {
            cSecHolder.html(data);
            var target = $("#pageNotFound");
            var h = $(document).height();
            if ($(document).height() < $(window).height()) {
                h = $(window).height();
            }
            target.height(h - target.offset().top);
        });
    };
    this.init = function(urlStrips, paramUrl) {
        urlPathStrips = urlStrips;
        urlParams = paramUrl;
        var category = urlPathStrips[2].toUpperCase();
        if (category == "") {
            urlMapper["HOME"]();
        }
        else if (urlMapper[category]) {
            if ((siglePathList.indexOf(category) > -1 && urlPathStrips.length !== 3)
                    || (doublePathList.indexOf(category) > -1 && urlPathStrips.length !== 4)) {
                pageNotFound();
            } else {
                urlMapper[category]();
            }

        } else
            pageNotFound();
    };


    //home page
    urlMapper[home] = function() {
        opencmdsResources();
    };
    urlMapper[resources] = function() {
        opencmdsResources();
    };

    urlMapper[referralusers] = function(){
        referralUsers();
    }

    urlMapper[sendtogcmpopup] = function(){
        cmdsNotification();
    }

    //programs
    urlMapper[program] = function() {
        if (urlPathStrips.length === 4) {
            goToProgramPage(urlPathStrips[3], "content");
        } else if (urlPathStrips.length === 5) {
            goToProgramPage(urlPathStrips[3], urlPathStrips[4]);
        } else {
            pageNotFound();
        }
    };


    //resources
    urlMapper[addcontent] = function() {
        opencmdsAddContent();
    };
    urlMapper[folder] = function() {
        goToFolderPage(urlPathStrips[3]);
    };
    urlMapper[packageVar] = function() {
        goToPkgPage(urlPathStrips[3]);
    };
    urlMapper[cdp] = function() {
        goToCDPPage(urlPathStrips[3]);
    };
    urlMapper[questionset] = function() {
        goToQuestionSetPage(urlPathStrips[3]);
    };
    urlMapper[question] = function() {
        goToQuesPage(urlPathStrips[3]);
    };
    urlMapper[challenge] = function() {
        goToChalPage(urlPathStrips[3]);
    };
    urlMapper[test] = function() {
        goToTestPage(urlPathStrips[3]);
    };
    urlMapper[reviews] = function() {
        console.log(urlPathStrips);
        goToReviewsPage(urlPathStrips[3]);
    };
    urlMapper[assignment] = function() {
        goToAssignmentPage(urlPathStrips[3]);
    }
    urlMapper[file] = function() {
        goToFilePage(urlPathStrips[3]);
    }
    urlMapper[testseries] = function() {
        goToTestSeriesPage(urlPathStrips[3]);
    };
    urlMapper[document] = function() {
        goToDocPage(urlPathStrips[3]);
    };
    urlMapper[video] = function() {
        goToVideoPage(urlPathStrips[3]);
    };
    urlMapper[module] = function() {
        goToModulePage(urlPathStrips[3]);
    };

    urlMapper[generatetest] = function() {
        generateTestPage(urlPathStrips[3]);
    };



    //institute settings
    urlMapper[academicstructure] = function() {
        opencmdsAcadStr();
    }
    urlMapper[instituteinfo] = function() {
        opencmdsInstituteInfo();
    }
    urlMapper[coursemanagement] = function() {
        opencmdsCourseMagnt();
    };
    urlMapper[sellerdashboard] = function() {
        opencmdsSellerDashboard(urlPathStrips[3]);
    };    

    //Devices 
    urlMapper[devices] = function() {
        opencmdsDevices();
    }

    //Exports 
    urlMapper[exports] = function() {
        opencmdsExports();
    }
    urlMapper[sdcardGroup] = function() {
        opencmdsSDCardGroup(urlPathStrips[3]);
    }
    urlMapper[sdcardGroups] = function() {
        opencmdsSDCardGroupsPage(urlPathStrips[3]);
    }

    urlMapper[checkDuplicates] = function(){
        openCheckDuplicates();
    }

    //PLAN 
    urlMapper[orgplan] = function() {
        openOrgPlans();
    }

    //INVOICE 
    urlMapper[invoices] = function() {
        openOrgInvoices();
    }

    //SIGNUP EXT 
    urlMapper[managesignup] = function() {
        openExtSignupPage();
    }
    urlMapper[customizesignup] = function() {
        openCustomizeSignupPage();
    }

    //people
    urlMapper[people] = function() {
        opencmdsPeople();
    }
    urlMapper[addmember] = function() {
        opencmdsAddMember();
    }
    urlMapper[user] = function() {
        goToUserPage(urlPathStrips[3]);
    }
    urlMapper[member] = function() {
        goToMemberPage(urlPathStrips[3]);
    }
    urlMapper[student] = function() {
        goToStudentPage(urlPathStrips[3]);
    }
    urlMapper[offlineuser] = function() {
        goToOfflineUserPage(urlPathStrips[3]);
    }
    urlMapper[addmember] = function() {
        addEditMemberPage(urlParams, "ADD");
    }
    urlMapper[editmember] = function() {
        addEditMemberPage(urlParams, "EDIT");
    }




    //institute offerings
    urlMapper[channels] = function() {
        openChannels();
    };

    //user settings
    urlMapper[profile] = function() {
        viewPublicProfile();
    };
})(jQuery);


vcmdsUrls = new (function($) {
    this.PROGRAM = function(programId) {
        return   "/organization/" + cmdsOrgId + "/program/" + programId;
    },
            this.PEOPLE = function(urlParams) {
        var path = "/organization/" + cmdsOrgId + "/people";
        if (urlParams && !$.isEmptyObject(urlParams)) {
            path += "?" + $.param(urlParams);
        }
        return path;
    },
            this.MEMBER = function(targetUserId) {
        return   "/organization/" + cmdsOrgId + "/member/" + targetUserId;
    },
            this.STUDENT = function(targetUserId) {
        return   "/organization/" + cmdsOrgId + "/student/" + targetUserId;
    },
            this.OFFLINE_USER = function(targetUserId) {
        return   "/organization/" + cmdsOrgId + "/offlineuser/" + targetUserId;
    },
            this.ADDMEMBER = function(targetProfile) {
        return   "/organization/" + cmdsOrgId + "/addmember?profile=" + targetProfile;
    },
            this.EDITMEMBER = function(targetProfile, targetUserId) {
        return   "/organization/" + cmdsOrgId + "/editmember?profile=" + targetProfile + "&userid=" + targetUserId;
    },
            this.RESOURCES = function() {
        var path = "/organization/" + cmdsOrgId + "/resources";
//        if (finalUrlParams && !$.isEmptyObject(finalUrlParams)) {
//            path += "?" + $.param(finalUrlParams);
//        }
        return path;
    },
            this.TEST = function(testId) {
        return   "/organization/" + cmdsOrgId + "/test/" + testId;
    },

            this.GENERATETEST = function(testId){
        return   "/organization/" + cmdsOrgId + "/generatetest/" + testId;
    },
            this.ASSIGNMENT = function(assignmentId) {
        return   "/organization/" + cmdsOrgId + "/assignment/" + assignmentId;
    },
            this.FOLDER = function(folderId) {
        return "/organization/" + cmdsOrgId + "/folder/" + folderId;
    },
            this.MODULE = function(moduleId) {
        return "/organization/" + cmdsOrgId + "/module/" + moduleId;
    },            
            this.ADDCONTENT = function() {
        return   "/organization/" + cmdsOrgId + "/addcontent";
    }

})(jQuery);


