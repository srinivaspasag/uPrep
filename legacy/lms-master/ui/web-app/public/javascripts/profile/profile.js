    var monthNames = [ "January", "February", "March", "April", "May", "June",
			"July", "August", "September", "October", "November", "December" ];
    $(function(){

        for (var k = 0; k < 12; k++){
            $('#start-month-picker').append($('<option />').val(monthNames[k]).html(monthNames[k]));
            $('#end-month-picker').append($('<optkon />').val(monthNames[k]).html(monthNames[k]));
        }
    });



    $('.correctBar').live('hover', function(){
    	$(this).find('.corrInfo').toggleClass('showInf');
    });
    $('.incorrectBar').live('hover', function(){
    	$(this).find('.incorrInfo').toggleClass('showInf');
    });
	$('.edit-edu-info').click(function(){
            $('#coll-name').hide();
            $('#edit-coll-name').show();
    });

	$('#edit-profile').live('click', function(){
        $.get("/Profile/editProfile",function(data) {
        	$('.profile-wrapper').empty().html(data);
        });
	});

    $('.add-section').live('click',function(){
		$('.add-new-section').slideDown();
        $('.add-section').hide();
		$('.save-section').show();
		$('.cancel-section').show();
	});

	function addNewSection(content,col,cor,start,end, klass){
		var addCont = $(content).find('.'+klass);
		$(addCont).append('<p><label for="school-coll">School/college</label><input id="school-coll" class="required coll" type="text" size="30" value="'+col+'"></p>');
		$(addCont).append('<p> <label for="subj-course">Subjects/Course</label><input id="subj-course" class="required name" type="text" size="30" value="'+cor+'"></p>');
		$(addCont).append('<label for="date-attend">Dates attended</label>');
		$(addCont).append('<div class="date-wrapper clearfix"><select name="startyearpicker" class="date-from"><option value='+start+'>'+start+'</option></select><span class="center-to">to</span><select name="endyearpicker" class="date-to"><option value='+end+'>'+end+'</option></select></div>');
		$(addCont).append('<button type="button" class="save-coll">Save</button><button type="button" class="cancel-coll">Cancel</button>');
            for (i = new Date().getFullYear(); i > 1980; i--)
                            {
                                $('.date-from').append($('<option />').val(i).html(i));
                                $('.date-to').append($('<option />').val(i).html(i));
                            }
                        return $(content);
		}

		$(".save-section").live('click',function(){
			var orgId = "123456";
			var orgName=$('.add-new-section').find("#school-coll").val();
			var subCourse = $('.add-new-section').find('#subj-course').val();
			var fromDate = $('.add-new-section').find('.date-from').val();
			var toDate = $('.add-new-section').find('.date-to').val();
			$('.add-new-section').quickValidate();
			if(fromDate>toDate){
				$('.add-new-section').find('.date-wrapper').append('<span class="error" style="display:block;">Please check the date</span>');
			}
			else{
				if($('.add-new-section').find('.error-icon').length == 0){
	                $.post("/Profile/saveEduInfo", {orgId: "123456", orgName: orgName,branch: subCourse,startDate: fromDate, endDate: toDate },function(data){
						var ran1 = Math.floor(Math.random()*1001);
						var ran = Math.floor(Math.random()*1001);
	                	var klass = "ns-"+ran;
						var secId = "institution"+ran1;
						var $newContent = '<div class="college-info" id="'+secId+'"></div>';
						var wrapper = $($newContent);
						$(wrapper).append('<a class="delete-college-info">Delete</a><a class="edit-college-info">Edit</a>').append('<h3>'+orgName+'</h3>').append('<h5 class="dep-branch">'+subCourse+'</h5>');
						$(wrapper).append('<h5><span id="from-'+ran+'" class="start-year">'+fromDate+'</span> - <span id="to-'+ran+'" class="end-year">'+toDate+'</span></h5>');
						$(wrapper).append('<div class="new-section '+klass+' clearfix"><div>');
						var lasId = $('.college-info').last().attr('id');
						var $content = addNewSection(wrapper, orgName, subCourse,fromDate,toDate,klass);
	                	if(lasId){
	                        $('#'+lasId).after(wrapper);
	                    }
	                    else{
							$('.edu-header').after($($content));
	                   	}
						$('.add-new-section').find('input').val('');
						if($('.add-new-section').has('.error')){
							$('.error').remove();
						}
						$('.add-new-section').hide();
						$('.add-section').show();
						$('.save-section').hide();
						$('.cancel-section').hide();
	                });
				}
			}
		});

		$(".cancel-section").live('click',function(){
			$('.add-new-section').slideUp();
			$('.add-section').show();
			$('.save-section').hide();
			$('.cancel-section').hide();
		});

		$('.edit-college-info').live('click',function(){
			var collId = $(this).parent().attr('id');
			$('#'+collId).children().slideUp();
            $('#'+collId).find('.new-section').css({'margin':0}).slideDown();

		});

		$('.delete-college-info').live('click',function(){
				var msg = "Are Your Sure?";
				var yClass = "deleteEdu";
          		var vId = $(this).parent().attr('id');
    			showError(msg,yClass);
    			$('.'+yClass).live('click', function(){
    				$('#'+vId).remove();
    				$.get("/Profile/deleteEduInfo", function(data){

    				});
    				cancelErrorPopup();
    			})
		});

		$('.cancel-coll').live('click',function(){
			var canClass = $(this).parent().parent().attr('id');
			$('#'+canClass).children().slideDown();
			$('#'+canClass).find('.new-section').slideUp();
		});

		$('.save-coll').live('click',function(){
            $that = $(this);
			var collNam = $(this).parent().find('#school-coll').val();
			var depNam = $(this).parent().find('#subj-course').val();
			var startDat = $(this).parent().find('.date-from').val();
			var endDat = $(this).parent().find('.date-to').val();
			var parClass = $(this).parent().parent().attr('id');
            var ind = $(this).parent().parent().attr('id').split("-")[1];
			$('#'+parClass).quickValidate();
			if(startDat>endDat){
                $('#'+parClass).find('.new-section').find('.date-wrapper').append('<span class="error" style="display:block;">Please check the date</span>');
			}
        	else{
            	if($(this).parent().find('.error-icon').length == 0){
                    $.post("/Profile/editEduInfo", {index: ind,orgId: "123456", orgName: collNam,branch: depNam,startDate: startDat, endDate: endDat },function(data){
                        var $par = $that.parent().parent();
                        $par.find('h3').html(collNam);
                        $par.find('.dep-branch').html(depNam);
                       	$par.find('.start-year').html(startDat);
                        $par.find('.end-year').html(endDat);
                        if($('#'+parClass).find('.new-section').has('.error')){
							$('.error').remove();
						}
                        $par.children().slideDown();
                        $that.parent().slideUp();
                    });
                }
            }
		});

		$('.edit-personal-info').live('click',function(){
		   $('.show-personal').hide();
		   $('.edit-personal').show();
		   $('.save-personal-info').show();
		   $('.cancel-personal-info').show();
		   $("#personal-info-holder").find(".nonner").removeClass("nonner");
		});

		$('.cancel-personal-info').live('click',function(){
			$('.show-personal').show();
			$('.edit-personal').hide();
			$(this).hide();
			$('.save-personal-info').hide();
			decideProfileShowOrHide("#personal-info-ambi",'#personal-ambi');
			decideProfileShowOrHide("#personal-info-contact",'#personal-no');
			decideProfileShowOrHide("#personal-info-hometown",'#per-hometown');
		});

		$('.save-personal-info').live('click',function(){
			var ambVal = $('#edit-ambition').val().trim();
			var fbVal = $('#edit-fb').val().trim();
			var twVal = $('#edit-tw').val().trim();
			var lnVal = $('#edit-ln').val().trim();
			var moblVal = $('#edit-contact').val().trim();
			moblVal = parseInt(moblVal)?moblVal:"";
			var hometown = $('#edit-hometown').val().trim();
			$('.personal-data-list').quickValidate();
			var params = {orgId: "123456",ambition: ambVal, homeTown: hometown, facebookLink: fbVal , twitterLink: twVal, linkedinLink: lnVal, mobileNo: moblVal };
			if($('.personal-data-list').find('.error-icon').length == 0){
				$.post("/Profile/savePersonalInfo",params,function(data){
                                   decideSocialLinks('#fb-link',fbVal);
                                   decideSocialLinks('#ln-link',lnVal);
                                   decideSocialLinks('#tw-link',twVal);
				   decideProfileShowOrHide("#personal-info-ambi",'#personal-ambi',ambVal);
				   decideProfileShowOrHide("#personal-info-contact",'#personal-no',moblVal);
				   hometown = decideProfileShowOrHide("#personal-info-hometown",'#per-hometown',hometown);
				   if(hometown != ""){ $(".location-from").removeClass("nonner").find("b").text(hometown);}
				   else{ $(".location-from").addClass("nonner").find("b").text("");}
				   $('.show-personal').show();
                                   $('.edit-personal').hide();
                                   $('.save-personal-info').hide();
                                   $('.cancel-personal-info').hide();
                		});
			}
		});

function decideSocialLinks(holderDiv,data){
		var imgSrc =  $(holderDiv).find("img").attr("src");
	if(data){
		$(holderDiv).attr("href",data);
		$(holderDiv).removeClass("disable")
			.find("img").attr("src",imgSrc.replace("Inactive","Active"));
	}else{
		$(holderDiv).addClass("disable")
			.find("img").attr("src",imgSrc.replace("Active","Inactive"));
	}
}
function decideProfileShowOrHide(holderDiv,dataDiv,data){
	if(data!=undefined){
		$(holderDiv).data('val',data);
	}else{
		data =  $(holderDiv).data('val');
		data = data?data:"";
	}
	if(data){
		$(holderDiv).removeClass("nonner");
		$(dataDiv).text(data);
	}else{
		$(holderDiv).addClass("nonner");
		$(dataDiv).text("");
	}
	return data;
}





    var instId = $('.college-info').first().attr('id');
    var noTest =false, noQues = false;
    if(instId){
        var collName = $('#'+instId).find('h3').html();
        if(collName){
            $('p.college-from').html(collName);
        }
    }




    var generateTAGraph = {
    //count: 1,
        init: function(data, type){
            var takenNo = data.result.taken,
            val = generateTAGraph.validate(takenNo, data, type);
            if(val){
                var avgSco = Math.round(data.result.percent*10)/10;
                var testInfo = data.result.testInfo;
                var d = {};
                var testNo = testInfo.length;
                if(testNo<8){var tLength = testNo}else tLength = 8;
                generateTAGraph.generateData(takenNo,avgSco, type, generateTAGraph.createItems(0, tLength, testInfo));
            }
        },
        validate: function(testTaken, data, type){
            if(testTaken>0){
                return true;//generateTAGraph.generateData(data, type);
            }
            else{
                $('.viewDummyWrapper').html('<a class="viewDummyAn" href="#dialog" name="modal">View Demo Analytics</a>');
                var $mesHtml = '<div class="wrapperForNoGraph"><p class="messageForQA">Looks like you have not <b>attempted</b> any tests</p><span style="color:#666;">Do so to view your analytics here</span><br><a class="attemptAdd goToExploreContent" href="/explorecontent/tests">Attempt Tests</a></div>';
                $('#allAnalyticsWrapper .tests-stats').html($($mesHtml));
            }
        },
        generateData: function(takenNo,avgSco,type, d){
                if(type == "real"){
                    $('#tests-taken').html(takenNo);
                    $('#avg-score').html(avgSco);
                    var $selector = $('.test-graph');
                    generateTAGraph.createGraph($selector,d);
                }
                else{
                    //console.log("generate data for dummy called");
                    $('#dummy-tests-taken').html(takenNo);
                    $('#dummy-avg-score').html(avgSco);
                    var $selector = $(".dummyTgraph");
                    generateTAGraph.createGraph($selector,d);
                }
        },
        createItems: function(start, end, testInfo){
            var xAxisItems=[],plotItems=[];
            for(var i=start; i<end;i++){                
                var testData=testInfo[i],marksVal=testData.scoredMarks,rankHTML="",marksHTML="";            
                xAxisItems.push('<a href="/test/'+testData.testId+'" data-test-id="'+testData.testId+'" class="openTestPage ptrTagHovered">'+testData.name+'</a>');
                var val=testData.percent||undefined;                        
                marksHTML="<div>Marks "+marksVal+"/"+testData.totalMarks+"</div>";
                if(testData.rank)rankHTML="<div>Rank "+testData.rank+"/"+testData.users+"</div>";
                plotItems.push({val:val,infoHTML:rankHTML+marksHTML,callback:"default"});
            }
            return {xAxisItems:xAxisItems,plotItems:plotItems};                     
        },
        createGraph: function(selector, dataJson){
            var  dataObj={
                xAxis:{title:"Tests",items:dataJson.xAxisItems,width:450,rotateTextBy:-90,textWidth:50},
                yAxis:{title:"Marks %",width:25,height:275},
                plotData:[{name:dataJson.name,items:dataJson.plotItems}]
            }; 
            selector.createAreaGraph(dataObj);           
        },
        nextGraph: function(data, count){
            //console.log("count sent next is" +count);
            var start = count*8,
            end = start + 8,
            length = data.result.testInfo.length,
            testInfo = data.result.testInfo,
            takenNo = data.result.taken,
            avgSco = Math.round(data.result.percent*10)/10, d={},
            type="real";
            if(end>length){
                end = length;
                $('#nextTA').removeClass('active').addClass("inactive");
                $('.tests-stats .inactive').bind("click", TAPagination.disableLink);
            }
            generateTAGraph.generateData(takenNo,avgSco, type, generateTAGraph.createItems(start, end, testInfo));
        },
        prevGraph: function(data, count){
            //console.log("count sent to prev is" +count);
            var start = count*8,
            end = start + 8,
            length = data.result.testInfo.length,
            testInfo = data.result.testInfo,
            takenNo = data.result.taken,
            avgSco = Math.round(data.result.percent*10)/10, d={},
            type="real";
            console.log("Before prev testinfo is" +testInfo.length)
            generateTAGraph.generateData(takenNo,avgSco, type, generateTAGraph.createItems(start, end, testInfo));

        }
    };


    var TAPagination = (function(){
                var count = 0;
                return{
                    disableLink: function(e){
                        e.preventDefault();
                        return false;
                    },
                    nextTa: function(){
                        $.get("/profile/getUserOverallTestAnalytics", function(data){
                            count++;
                            if(!$('#prevTA').hasClass("active")){
                                $('#prevTA').removeClass('inactive').addClass("active");
                                $('#prevTA.active').unbind('click', TAPagination.disableLink);
                            }
                            //console.log("count for next is" +count)
                            generateTAGraph.nextGraph(data, count);

                        });
                    },
                    prevTa: function(){
                        $.get("/profile/getUserOverallTestAnalytics", function(data){
                            count--;
                            //console.log("count for prev is " +count);
                            if(!$('#nextTA').hasClass("active")){
                                $('#nextTA').removeClass('inactive').addClass("active");
                            }
                            if(count == 0){
                                $('#prevTA').removeClass('active').addClass("inactive");
                                $('#prevTA.inactive').bind("click", TAPagination.disableLink);
                                $('#nextTA').removeClass('inactive').addClass('active');
                                $('#nextTA.active').unbind('click', TAPagination.disableLink)
                            }
                            generateTAGraph.prevGraph(data, count);
                        });
                    }
                }
    })();
    $('#nextTA.active').live('click', function(){
        TAPagination.nextTa();
    });

    $('#prevTA.active').live('click',function(){
        TAPagination.prevTa();
    });


    function generateQAGraph(data, type, noOfTopics){
            if(data.errorCode == ''){
                var res = data.result;
                if(type == "real"){

                    $('#q-added').html(res.added);
                    $('#q-solved').html(res.total);
                    $('#a-correct').html(res.correct);
                }
                if(type == "dummy"){
                    $('#dummy-q-added').html('48');
                    $('#dummy-q-solved').html('125');
                    $('#dummy-a-correct').html('84');
                }
                var len = res.questionDistribution.length;

                if(len>0){
                    var $topicList = '',
                    $finalhtml = '',
                    $subWrap ='',
                    $attHtml = '',
                    sub1, sub2,
                    attNo = 0;
                    $subjWrap='';
                    for(var i=0; i<len; i++){
                        if(res.questionDistribution[i].total != 0){
                            var subName = res.questionDistribution[i].name;
                            var topics = res.questionDistribution[i].children;

                            var topLen = topics.length;
			    noOfTopics = noOfTopics==-1?topLen:noOfTopics;
			    noOfTopics = noOfTopics?noOfTopics:3;
                            var attemLen = res.attemptedTopics.length;
                            if(attemLen>0){
                                var mostAttem = res.attemptedTopics[0].total;
                            }
                            if(topLen<noOfTopics){var topNo = topLen} else topNo = noOfTopics;
                            for(var j=0; j<topNo; j++){

                                var correct = topics[j].questionDetails.correct;
                                var attem = topics[j].questionDetails.attempts;
                                var topNam = topics[j].name;
                                var incor = attem - correct;
                                var attmpPerc = (attem/mostAttem)*100;
                                var corrPerc = (correct/attem)*100;
                                var incorPerc = (incor/attem)*100;

                            $topicList += '<div class="topicWrapper clearfix"><div class="nameWrapper clearfix"><span class="leftTopicNames" title="'+topNam+'">'+topNam+'</span></div><div class="valuesWrapper clearfix"><div class="attemptedPerc" style="width:'+attmpPerc+'%"><span class="correctBar" data-correct='+correct+' style="width:'+corrPerc+'%"></span><span class="incorrectBar" data-incorrect='+incor+' style="width:'+incorPerc+'%"></span><div class="qaInfoWrapper"><div class="qaInfo clearfix"><div><span class="corIcon"></span><span class="cVal">'+correct+'</span>Correct</div><div><span class="icorIcon"></span><span class="iVal">'+incor+'</span>Incorrect</div></div></div></div></div></div>';
                            }
			    if(topNo<topLen){
                            	$subjWrap +='<div class="subjectWrapper clearfix"><div class="subjectName"><h4>'+subName+'<a class="viewMoreQATopics doCStream" name="QA_SHOW_MORE_TOPICS" data-sub-index='+i+'>See All</a></h4></div>'+$topicList+'</div>';
			    }else{
                            	$subjWrap +='<div class="subjectWrapper clearfix"><div class="subjectName"><h4>'+subName+'</h4></div>'+$topicList+'</div>';
			    }
                        }

                        else{
                            if(attNo<=3){
                                $attHtml += '<div class="attQ clearfix"><h4>'+res.questionDistribution[i].name+'</h4><a>Attempt Questions</a></div>';
                                attNo++;
                            }
                        }
                        $topicList ='';
                    }
                    $('.colorIndicator').html("<span class=corIcon></span><span class=crt>Correct</span><span class=incorIcon></span><span class=incrt>Incorrect</span>");
                    $('.dummycolorIndicator').html("<span class=corIcon></span><span class=crt>Correct</span><span class=incorIcon></span><span class=incrt>Incorrect</span>");
                    $('#most-topic').html("Most attempted topic-<span>"+res.attemptedTopics[0].name+"</span>");
                    $('#dummy-most-topic').html("Most attempted topic-<span>"+res.attemptedTopics[0].name+"</span>");
                    //$finalhtml = '<div class="graphWrapper"><div class="graphContent"><div class="verticalLine"></div>'+$subjWrap+$attHtml+'</div></div>';

                    $finalhtml = '<div class="graphWrapper"><div class="graphContent">'+$subjWrap+$attHtml+'<div class="verticalLine"></div></div></div>';

                    return $finalhtml;


                }

                if(len==0){
                    $('.viewDummyWrapper').html('<a class="viewDummyAn" href="#dialog" name="modal">View Demo Analytics</a>');
                    var $mesHtml = '<div class="wrapperForNoGraph"><p class="messageForQA">Looks like you have not <b></br> attempted</b> any questions</p><span style="color:#666;">Do so to view your analytics here</span></br><a id="attQues" class="attemptAdd takeToHomePage" href="/">Attempt Questions</a></div>';
                    //$('#allAnalyticsWrapper .questions-stats').html($($mesHtml));
		    return $mesHtml;
                }

            }

            else if(data.errorCode != ''){
                //$('.ques-graph').contents().hide();
                $('.stats-error p').html('Oops! There has been some error. Try again');
            }
        }
    $('a[name=modal]').live('click', function(e){
        qdata = {"errorMessage":"","result":{"attempt":25,"total":27,"questionDistribution":[{"total":10,"percent":0,"correct":0,"_id":"502f92da0e4450ed17beff2f","marks":0,"name":"Mathematics","children":[{"questionDetails":{"correct":10,"marks":30,"attempts":20},"_id":"502f92da0e4450ed3ebeff2f","name":"Complex Numbers"},{"questionDetails":{"correct":10,"marks":20,"attempts":12},"_id":"502f92da0e4450ed3ebeff2f","name":"Integration"},{"questionDetails":{"correct":20,"marks":80,"attempts":30},"_id":"502f92da0e4450ed3ebeff2f","name":"Trigonometry"}]},{"total":26,"percent":0,"correct":13,"_id":"502f92da0e4450ed17beff2f","marks":44,"name":"Physics","children":[{"questionDetails":{"correct":5,"marks":20,"attempts":8},"_id":"502f92da0e4450ed3ebeff2f","name":"Thermodynamics"}, {"questionDetails":{"correct":8,"marks":24,"attempts":18},"_id":"502f92da0e4450ed3ebeff2f","name":"Force"}, {"questionDetails":{"correct":14,"marks":28,"attempts":28},"_id":"502f92da0e4450ed3ebeff2f","name":"Velocity And Angular Rotation"}]},{"total":6,"percent":0,"correct":1,"_id":"502f92da0e4450ed14beff2f","marks":-2,"name":"Chemistry","children":[{"questionDetails":{"correct":6,"marks":12,"attempts":10},"_id":"502f92da0e4450ed15beff2f","name":"Organic Compounds"},{"questionDetails":{"correct":4,"marks":20,"attempts":8},"_id":"502f92da0e4450ed15beff2f","name":"Covalent Bonds"}, {"questionDetails":{"correct":16,"marks":60,"attempts":25},"_id":"502f92da0e4450ed15beff2f","name":"Chemical Compounds"}]}],"added":10,"correct":4,"attemptedTopics":[{"total":30,"_id":"502f92da0e4450ed15beff2f","name":"Trigonometry "},{"total":1,"_id":"502f92da0e4450ed3ebeff2f","name":"Straight Line"}],"attemptedLevels":[]},"errorCode":""};

        tdata = {"errorMessage":"","result":{"testCreated":13,"percent":82.631578947368421,"testInfo":[{"users":10,"rank":4,"percent":77.77,"totalMarks":360,"_id":"503645230a8350ed54919551","scoredMarks":280,"name":"AIEEE Part Test-1"},{"users":200,"rank":11,"percent":73.33,"totalMarks":225,"_id":"503645230a8350ed54919551","scoredMarks":165,"name":"IIT Full Test-1"},{"users":350,"rank":2,"percent":87.5,"totalMarks":360,"_id":"50371f819f9c50edaa7f82c2","scoredMarks":315,"name":"Complex Numbers"},{"users":300,"rank":15,"percent":70,"totalMarks":300,"_id":"503dfebcfc1450edbf539039","scoredMarks":210,"name":"Board Pattern Test-1"},{"users":250,"rank":1,"percent":95.8333333,"totalMarks":360,"_id":"5046228286b950edab2fce24","scoredMarks":345,"name":"Physics Full Test-1"},{"users":250,"rank":2,"percent":88.8888,"totalMarks":360,"_id":"5046f34f86b950ede82fce24","scoredMarks":320,"name":"AIEEE Part Test-2"},{"users":200,"rank":6,"percent":80,"totalMarks":225,"_id":"5035ead20a8350ed6e909551","scoredMarks":180,"name":"IIT Full Test-2"}, {"users":400,"rank":28,"percent":90,"totalMarks":300,"_id":"5035ead20a8350ed6e909551","scoredMarks":270,"name":"Organic Chemistry"},{"users":10,"rank":2,"percent":90,"totalMarks":100,"_id":"5035ead20a8350ed6e909551","scoredMarks":90,"name":"AIEE"}],"scoredMarks":-2,"taken":9},"errorCode":""};



            generateTAGraph.init( tdata, "dummy"); //Generates Dummy Analytics graph

            var $finalhtml = generateQAGraph(qdata, "dummy");

           //$('#dialog').append($('.stats-details').html());
            $('.dummy-graph').html($($finalhtml));
            //Cancel the link behavior
            e.preventDefault();
            //Get the A tag
            var id = $(this).attr('href');
            //Get the window height and width
            var winH = $(window).height();
            var winW = $(window).width();

            //Set the popup window to center
            $(id).css('top',204);
            $(id).css('left', winW/2-$(id).width()/2);
            $(id).show();

            //if close button is clicked
            $('.window .dummyCloseIcon').click(function (e) {
                //Cancel the link behavior
                e.preventDefault();
                $('#mask, .window').hide();
            });

    });



//for profile pic uploading
function profilePicUploading(){
    $(".profile-picture img").attr("src","/public/images/loading.gif").addClass("uploadingProfilePic");
}
function profilePicUploaded(data){
    var pic=data.result.thumbnail;
    $(".profile-picture img").attr("src","").attr("src",pic).removeClass("uploadingProfilePic");
    $("#profilePage img").attr("src",pic);
    for(var k=0;k<profilePicChangeList.length;k++){
        profilePicChangeList[k].find(".myCurrentPic").attr("src",pic);
    }
    $("#CMBHead").find(".myCurrentPic").attr("src",pic);
    PROFILEPIC=pic;
}

//loading more people
$(".seeMoreFollowers").live('click',function(){
    var total=$(this).parent().find(".peopleWidgetCount").text();
    loadPeoplePopup("/widgets/moreFollowers","Followers",total,{targetUserId:$(this).data("targetUserId")});
});
$(".seeMoreFollowing").live('click',function(){
    var total=$(this).parent().find(".peopleWidgetCount").text();
    loadPeoplePopup("/widgets/moreFollowing","Following",total,{targetUserId:$(this).data("targetUserId")});
});

