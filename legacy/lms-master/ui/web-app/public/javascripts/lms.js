jQuery.fn.exists = function(){
	return jQuery(this).length>0;
}

var lectureManager = {

	config : {
		//container : $('#lecturesList')
		CACHE : {}
	},

	init : function(){
		$(document).on('click','.editLect', lectureManager.editLecture);//to edit a lecture
		//$(document).on('mouseenter', '.lecturesWrapper', lectureManager.createEdit); //show edit
		$(document).on('mouseleave', '.lecturesWrapper', lectureManager.hideEdit); //hide edit
		$(document).on('click','.delSub', lectureManager.deleteST); // delete sub topics
		$(document).on('click', '.cancelEditLec', lectureManager.cancelEdit); 
		$(document).on('click', '.addSub', lectureManager.callAddSub);
		$(document).on('click', '.addST', lectureManager.callAddSub);
		$(document).on('click', '#closeST', lectureManager.closeST); //close subtopics box
		$(document).on('click', '.ST-link', lectureManager.addST); 
		//$(document).on('click', '.masterPlanBtn a', lectureManage.openCompareMaster);
		//$(document).on("click", 'ul.topicLists li .CUtopics', lectureManager.setActive); //active topics on left widget
		//$(document).on("click", 'ul.topicLists .CU-start .CUtopics', lectureManager.setActive); 
		$(document).on('click', '.viewST', lectureManager.callAddSub);
		$(document).on('click', '.editLD', lectureManager.editDate);
	},

	createEdit : function(){
		var $sel = $(this),
		klass = $sel.children().first().attr('class').split(' ')[0];
		if(!$sel.find('.editLect').exists() && klass != 'tmp-lectures'){
			$('<a/>')
				.attr('class', 'editLect')
				.text("Edit")
				.appendTo($sel);
			$sel.find('.lectures')
				.css('border','1px solid #33a3d7');
		}
	},

	// openCompareMaster : function(){

	// },

	hideEdit : function(){
		var $sel = $(this);
		$sel.find('.editLect').remove();
		$sel.find('.lectures')
			.css('border','1px solid transparent');
	},

	editLecture : function(){
		//console.log("edit called")
		if($('#lecturesList').find('.SUEditing').exists()){
			$('#lecturesList').find('.cancelEditLec').trigger('click');
		}
		var lid = $(this).prev().attr('id'),
			idSel = '#'+lid, $cross,
			$submitHtml = lectureManager.createSubmit,
			$addSub = $('<a/>')
						.attr('class', 'addSub')
						.html('add subtopics'),
			$spanSel = $('.STwrapper', idSel).find('span');
			$(idSel).addClass('SUEditing');
			$(idSel).find('.lecDate').addClass('editLD');
			if(!$(idSel).find('.addSub').length){ //add subtopic if not present
				$addSub.appendTo($(idSel))
			}
			if(!$(idSel).parent().next('.submitHtml').exists()){
				$(idSel).parent().after($submitHtml); // appends the submit after the lecture box
			}
			$spanSel.each(function(index){ //Loop span and add close after each
				$cross = $('<a/>').attr('class', 'delSub')
				//console.log("loop runs" +index);
				if(!$(this).next().is('a')){
					$(this).after($cross);
				}
			})
	},

	splitDate : function(date){
		var dateA = [];
		dateA.push(date.split('/')[0]);  //get day
		dateA.push(date.split('/')[1]); //get month
		dateA.push(date.split('/')[2]); //get year
		return dateA;
	},

	editDate : function(){
		var date = $(this).html(), 
		split = lectureManager.splitDate(date),
		$day = $('<input/>').attr({ id : 'lDay', type : "text", size: "2"}).val(split[0]),
		$month = $('<input/>').attr({ id : 'lMonth', type : "text", size: "2"}).val(split[1]),
		$year = $('<input/>').attr({ id : 'lYear', type : "text", size: "4"}).val(split[2]),
		$wrapper = $('<div/>').attr('class','leditWrapper')
						.append($day)
						.append($month)
						.append($year);
		$(this).siblings('.dTag').hide();
		$(this).after($wrapper);
		$(this).hide();
	},

	createSubmit : function(){
		var $submit = $('<a/>')
						.attr('id', 'submitLecture')
						.attr('class', 'greenButton')
						.text("Submit"),
			$cancel = $('<a/>')
						.attr('class', 'cancelEditLec')
						.text("Cancel"),
			$wrapper = $('<div/>')
						.attr('class', "submitHtml clearfix")
						.append($cancel)
						.append($submit);

		return $wrapper;
	},

	cancelEdit : function(){
		var selId = $(this).parent().prev().children().attr('id'),
		$sel = $('#'+selId);
		$sel.removeClass('SUEditing');
		$sel.find('.lecDate').removeClass('editLD').show();
		$sel.find('.dTag').show();
		$sel.find('.delSub').remove().end()
			.find('.addSub').remove().end()
			.find('.tmpST').remove();
		$sel.find('.leditWrapper').remove()	
		$(this).parent().remove();
		$('#lecturesList').find('.STholder').remove();
	},

	callAddSub : function(){
		// var topic = "optics",
		// $sel = $(this);
		// $.when(
		// 	lectureManager.getData(topic)
		// ).then(function(result){
		// 	lectureManager.createST(result, $sel);
		// })
		if(!$(this).parent().siblings('.STholder').exists()){ //create subtopic list only if it does not exist
			var jtopic = $('#classTopicWidget').find('.activeTopic').data('topic'),
			STList = (JSON.parse(jtopic)).subTopics,
			len = STList.length, i, $list = '',
			klass = $(this).attr('class'), lid, $ongoing = '';
			klass == "addST"?lid = $(this).parent().parent().attr('id') : lid = $(this).parent().attr('id');
			var $sel = $('#'+lid).parent();
			
			for(i = 0; i < len; i++){
                var status = JSON.stringify(STList[i].status),
                name = JSON.stringify(STList[i].name),
                name = name.replace(/\"/g,"");
				$list += '<li class=SU-'+status+'><a class = "ST-link" id="ST-'+i+'"><span>'+name+'</span></a>'+$ongoing+'</li>';
				$ongoing = '';
			}
			var $STul = $('<ul/>').append($list);
			$('<div/>')
				.append($STul)
				.attr('class', 'STholder')
				.appendTo($sel)
				.data('lid', lid);
			$('<a/>')
				.attr('id', 'closeST')
				.appendTo('.STholder')
		}//end of if
	},

	deleteST : function(){ 
		$(this).prev().remove();
		$(this).remove();
	},

	getData : function(topic){
		var cache = lectureManager.config.CACHE;
		return cache[topic] || $.ajax("/Institute/getSchedule",
			{dataType : 'json',
			success : function(data){
				cache[topic] = data;
				}
			});
	},

	addST : function(){
		var lid = $(this).parents().closest('.STholder').data('lid'),
		content = $(this).find('span').html(),
		$span = $('<span/>').html(content).attr('class','tmpST'),
		$del = $('<a/>').attr('class', 'delSub'),
		klass = $(this).parent().attr('class');
		//if($('#'+lid).find('.addST'))$('.addST').remove();
		if(klass == "SU-done")return false;
		else{
			$('#'+lid).find('.STwrapper').append($span).append($del);
		}
	},

	closeST : function(){
		$(this).parent().remove();
	},

	createST : function(data, selector){
		if(!selector.parent().siblings('.STholder').exists()){ //create subtopic list only if it does not exist
			var STList = data.batches[0].topics[1].subtopics,
			len = STList.length, i, $list = '',
			klass = selector.attr('class'), lid, $ongoing = '';
			klass == "addST"?lid = selector.parent().parent().attr('id') : lid = selector.parent().attr('id');
			var $sel = $('#'+lid).parent();
			//console.log("the length of ST is " +STList[0]);
			for(i = 0; i < len; i++){
				if(STList[i].state == "ongoing"){
					$ongoing = "<a class=SUOngoing>ongoing</a>";
				}
				$list += '<li class=SU-'+STList[i].state+'><a class = "ST-link" id="ST-'+i+'"><span>'+STList[i].name+'</span></a>'+$ongoing+'</li>';
				$ongoing = '';
			}
			var $STul = $('<ul/>').append($list);
			$('<div/>')
				.append($STul)
				.attr('class', 'STholder')
				.appendTo($sel)
				.data('lid', lid);
			$('<a/>')
				.attr('id', 'closeST')
				.appendTo('.STholder')
		}//end of if
	}
}//end of lecture manager

var lectureUpdate = {

	config : {
		//container : $('#lecturesList')
		CACHE : {}
	},

	init : function(){
		lectureUpdate.loadClassData(); // get batches, courses and topics
		//lectureUpdate.loadBatchDetails();
		//lectureUpdate.loadTopicInfo();
		//lectureUpdate.loadBatchandTopic();
		lectureUpdate.unbindClick();
		$(document).on('click', '#addLecture', lectureUpdate.addLecture); //Add Lecture
		$(document).on('change','#batches', lectureUpdate.loadBatchInfo);
		$(document).on('change', '#CUcourses', lectureUpdate.loadCourseTopics);
		$(document).on("click", 'ul.topicLists li .CUtopics', lectureUpdate.loadLectureInfo);
		$(document).on('click','.cancelLecture', lectureUpdate.cancelLecture);
		$(document).on("click", '#submitLecture', lectureUpdate.submitLecture);
		$(document).on('click', '#endTopic', lectureUpdate.endTopic);
		$(document).on('mouseenter', '.lecturesWrapper', lectureManager.createEdit);
		$('#lecturePicker').on('click', '.ui-datepicker-next', lectureUpdate.loadCalData);
		$('#lecturePicker').on('click', '.ui-datepicker-prev', lectureUpdate.loadCalData);

	},

//	loadBatchandTopic : function(){
//		$.get("/Institute/getCourseStatus", { programmeId: "502f6e1eda59ae44304ada29", courseId : "5046f52118fdae44b26a0866", userRole: "TEACHER", year:"2012"}, function(data){
//			console.log("data requested")
//		});
//	},

/*=================== To be added later ======================*/
	// loadTopicLectures : function(){
	// 	var course = $(this).parent().data('course'),
	// 	progId = $(this).parent().parent().data('pId');
	// 	$.when(
	// 	lectureUpdate.loadLectureData(course, progId) //must pass course/topic here
	// 	).then(function(result){
	// 		$('#lecturePicker').find('td[data-month] a').nextAll().remove();
	// 		lectureUpdate.showLectures(result, course);
	// 		//lectureUpdate.loadBatchDropdown(result);
	// 		//lectureUpdate.listTopics($('#batches').find(":selected").text(), result);
	// 	});
	// },

	// // loadLectureData : function(course, pId){
	// // 	var name = (JSON.parse(course)).name,
	// // 	cache = lectureUpdate.config.CACHE;
	// // 	return cache[name] || $.ajax("/Institute/getSchedule",
	// // 	{data: { userRole: "TEACHER", programmeId: pId},
	// // 	dataType:'json',
	// // 	success: function(data){
	// // 		cache[name] = data;
	// // 		}		
	// // 	});
	// // },
/*=============================End to be added ===================*/

	loadClassData : function(){
		$.get('/Institute/getBatchDetails', {userRole: "TEACHER", year: "2012"}, function(data){
			lectureUpdate.loadBatchDropdown(data);
		});
	},

	loadBatchDropdown : function(data){
        //console.log("the data receieved her is " +JSON.stringify(data))
		var dep = data.result.departments,
		depNos = dep.length,
		opts = '',
		sOpts = '';
		for(var i = 0; i < depNos; i++){
			var course = dep[i].course,
			courseLen = course.length,
			prog = dep[i].programme,
			progLen = prog.length;

			for(var j = 0; j < courseLen; j++){

				opts += '<option data-val='+JSON.stringify(course[j])+' value='+(j+1)+'>'+course[j].name+'</option>';	
			}
			for(var k = 0; k < progLen; k++ ){
				sOpts += '<option data-val='+JSON.stringify(prog[k])+' value='+(k+1)+'>'+prog[k].name+'</option>';
			}
			
		}
		$('#batches').html(sOpts)
		$('#CUcourses').html(opts).data('course', course);
		lectureUpdate.hideIds($('#CUcourses'));
		lectureUpdate.hideIds($('#batches'));
		var cVal = $('#CUcourses').find(':selected').data('val'),
		pVal = $('#batches').find(':selected').data('val'),
                cId = (JSON.parse(cVal)).id,
                pId = (JSON.parse(pVal)).id;
                //console.log('the cId and pId vals are ' +cId+ "and" +pId);
        lectureUpdate.loadCourseTopics(pId, cId);
		
	},

	hideIds : function($sel){
		var $selector = $sel.find('option');
		$selector.each(function(){
			var val = $(this).attr('data-val');
			$(this).removeAttr('data-val');
			console.log('the val is ' +val)
			$(this).data('val', val);
		})
	},

//	loadCourseDropdown : function(data, selCourse, state){
//		var courses = data.course,
//		courseLen = courses.length,
//		defaultCourse,
//		opts = '',
//		pId = data.programmeId;
//		if(!selCourse || state){
//			
//		for(var j = 0; j < courseLen; j++){
//			opts += '<option data-Id='+courses[j].id+' value='+(j+1)+'>'+courses[j].name+'</option>';
//		}
//		$('#CUcourses').html(opts);
//		lectureUpdate.hideIds($('#CUcourses'));
//		defaultCourse = $('#CUcourses').find(":selected").text();
//		}
//		if(selCourse)defaultCourse = $('#CUcourses').find(":selected").text();
//		for(var k = 0; k < courseLen; k++){
//			if(courses[k].name == defaultCourse){
//				lectureUpdate.listTopics(courses[k], pId);
//			}
//		}
//
//	},

	loadLectureInfo : function(){
		var topic = $(this).html(),
		//jtopic = JSON.parse($(this).parent().data('topic')),
		$sel = $(this).parent().parent(),
		jcourse = $sel.data('course'),
		pId = $sel.data('pId');
		lectureUpdate.setActiveTopic($(this)); 
		$.when(
    	lectureUpdate.loadLectureData(pId, topic)
    	).then(function(data){
    		$('#lecturePicker').find('td[data-month] a').nextAll().remove();
    		lectureUpdate.showLectures(data, topic);
    	});
	},

	loadLectureData : function(pId, topic){
		var cache = lectureUpdate.config.CACHE;
		return cache[topic] || $.ajax("/Institute/getSchedule",
		{data: {userRole: "TEACHER", programmeId: pId},
		dataType:'json',
		success: function(data){
			cache[topic] = data;
                        
			}		
		});
	},

	loadCourseTopics : function(pId, cId){
//		var pId = $('#batches').find(':selected').data('id'),
//		cId = $(this).find(':selected').data('id');
		$.when(
			lectureUpdate.getCourseTopics(pId, cId)
			).then(function(data){
				//console.log('the status data is ' +JSON.stringify(data));
                lectureUpdate.listTopics(data, pId)
		});
	},

	getCourseTopics : function(pId, cId){
		var cache = lectureUpdate.config.CACHE;
		return cache[cId] || $.ajax("/Institute/getCourseStatus",
		{data: {programmeId : pId, courseId : cId, userRole: "TEACHER", year:"2012"},
		dataType : 'json',
		success : function(data){
			cache[cId] = data;
		}
		});
	},

	loadBatchInfo : function(){ //on batch change pass the batch
		var batch = $('#batches').find(":selected").text(),
		course = '',
		state = true;
		$(this).attr('id') == "batches"?state:state=false;
		if($('#CUcourses').find('option').exists()){
			course = $('#CUcourses').find(":selected").text();
		}
		//console.log("COurse selected is " +course);
		$.when(
		lectureUpdate.loadBatchData(batch) 
		).then(function(data){
			$('#lecturePicker').find('td[data-month] a').nextAll().remove();
			var batches = data.result.programmes,
				batchLen = batches.length,
				reqBatch;
				for(var i = 0; i < batchLen; i++){
					if(batches[i].programme == batch){
						reqBatch = batches[i];
					}
				}
			lectureUpdate.loadCourseDropdown(reqBatch, course, state);
			//lectureUpdate.listTopics($('#batches').find(":selected").text(), reqBatch);
		});

	},

	loadBatchData : function(batch){ //pass topic to this function
		//console.log("batch sent to load here is " +batch)
		var cache = lectureUpdate.config.CACHE;
		return cache[batch] || $.ajax("/Institute/getBatchDetails",
			{data: {userRole: "TEACHER"}, // masterPlanId : "50642880655ab0e47d5071e5"
			dataType: 'json',
			success: function(data){
				cache[batch] = data;
			}});
	},

	listTopics : function(data, pId){
		var courseStatus = data.result.courseStatus,
                topics = courseStatus.topics,
		topLen = topics.length,
		topicList = [];
		for(var k = 0; k < topLen; k++){
		    var jTopic = JSON.stringify(topics[k]);
			topicList.push($('<li class="CU-state '+topics[k].status+'"><a class="CUtopics">'+topics[k].name+'</a></li>').data('topic', jTopic));
		}
		var $rawArray = lectureUpdate.generateList(topicList);
		$('ul.topicLists').html($rawArray).data('pId', pId).data('course',JSON.stringify(data));
        $('ul.topicLists li .CUtopics').first().trigger('click'); //must be replaced with ongoing topic
		//lectureUpdate.hideJSON();
	},

//	hideJSON : function(){
//		var $sel = $('#classTopicWidget').find('.topicLists li');
//		$sel.each(function(){
//			var jdata = $(this).attr('data-Topic');
//                        console.log("the jdata is " +jdata);
//                        console.log("the json jdata is " +JSON.stringify(jdata))
//			$(this).removeAttr('data-Topic').data('topic', jdata);
//		})
//	},

	loadOngoingDate : function(started){
			var today = new Date().getTime(),
			val = today - started;
            if(val>0){
                var fdate = lectureUpdate.getFormattedDate(val),
                day = fdate.split('/')[0];
                day == "1"?$('span.ongoingDays').html(+day+" Day"):$('span.ongoingDays').html(+day+" Days");
                if(day < 1 )$('span.ongoingDays').html('0 Day');
            }
            else{
               $('span.ongoingDays').html('Yet to start'); 
            }

		//}
	},

	setActiveTopic : function($sel){
		$sel.parent("li").addClass("activeTopic")
			.siblings().removeClass("activeTopic");
		$('#classTopicWidget').find('.viewST').remove();
		//$sel.after('<a class="viewST">view subtopics</a>');
	},

	getTimeStamp : function(date){
		var sDate = lectureManager.splitDate(date),
		startDay = new Date(),
		endDay = new Date();
		startDay.setFullYear(sDate[2],(sDate[1]-1),sDate[0]);
		endDay.setFullYear(sDate[2],(sDate[1]-1),sDate[0]);
		startDay.setHours(15, 00);
		endDay.setHours(16, 00);
		return {
			"starttime" : startDay.getTime(),
			"endtime" : endDay.getTime()
		}
		
	},

	submitLecture : function(){
		var $sel = $(this).parent().prev(),
		masterPId = $('#lecturesList').find('.lectures').first().data('masterId'),
		$subT = $sel.find('.STwrapper span.tmpST'),
		course = {}, ST = [],
		jsnTopic = $('.topicLists').find('.activeTopic').data('topic'),
		jsnCourse = $('.topicLists').data('course'),
		jCourse = JSON.parse(jsnCourse),
        courseObj = JSON.parse($('#CUcourses').find(':selected').data('val')),
		courseNam = courseObj.name, //jCourse.name,
		courseId = courseObj.id,
		//code = jCourse.code,
		jTopic = JSON.parse(jsnTopic),
		topicName = jTopic.topic,
		jSTLen = jTopic.subTopics.length,
		lDate = $sel.find('.lecDate').html(),
		timeStamp = lectureUpdate.getTimeStamp(lDate),
		starttime = timeStamp.starttime,
		endtime = timeStamp.endtime;  
        jTopic.subTopics = [];
		//console.log("Master jsntopic is " +JSON.stringify(jTopic))
		if($sel.find('.leditWrapper').exists()){ //if date is edited
			var day = $('#lDay').val(),
			month = $('#lMonth').val(),
			year = $('#lYear').val(); 
			isValid = validateAndGetMS(year, month, day);
			if(isValid != '0'){
				var date = day +"/"+month+"/"+year,
				timpeStamp = lectureUpdate.getTimeStamp(date);
				starttime = timeStamp.starttime,
				endtime = timeStamp.endtme;
				$sel.find('.editLD').html(date).show();
				$sel.find('.leditWrapper').remove();
				$sel.find('.dTag').show();
			}
			else{
				alert("Invalid Date!!")
			}
		}
		
		if($subT.exists()){
			$subT.each(function(){
                            var nam = $(this).html(),
                            nam = nam.replace(/\"/g,"");
                            console.log("the name is " +nam)
			jTopic.subTopics.push({"name" : nam});
			})
		}
		course = [{"topics" : [jTopic], "name" : courseNam, "courseId" : courseId }]; //might need to add code
		console.log("The course json sent is " +JSON.stringify(course));
		console.log("The startime and end tme are " +starttime+"   "+endtime);
		$.get('/Institute/addSchedule',{ courses: JSON.stringify(course), masterPlanId : masterPId, startTime : starttime, endTime : endtime, type : "LECTURE", userRole : "TEACHER"}, function(data){
			console.log("Date added")
		})

		var selId = $(this).parent().prev().children().attr('id'),
		$eSel = $('#'+selId);
		$eSel.removeClass('SUEditing');
		$eSel.find('.lecDate').removeClass('editLD');
		if($eSel.find('.leditWrapper').exists()){
			$eSel.find('.dTag').show();
			$eSel.find('.lecDate').show();
		}
		$eSel.find('.delSub').remove().end()
			.find('.addSub').remove().end()
		$eSel.find('.leditWrapper').remove()	
		$(this).parent().remove();
		if($('#lecturesList').find('.STholder').exists()){
			$('#lecturesList').find('.STholder').remove();
		}
	},

	cancelLecture : function(){
		var lectureId = $(this).parent().siblings().attr('id');
		lectureUpdate.cancelAddedLecture(lectureId);
	},

	unbindClick : function(){
		var $sel = $('#lecturePicker').find('td');
			$sel.each(function(){
		 		if($(this).data('handler')){
		 			$(this).unbind('click');
		 		}
 			})
	},

	generateList:function(htmlArray){
		var rawArray = jQuery.map(
			htmlArray,
			function(value, index){
				return(value.get());
			}
			);
		return rawArray;
	},

	loadSubtopics: function(){

	},

	getFormattedDate : function(val){
		var date = new Date(val), 
		year = date.getFullYear(),
		month = date.getMonth()+1,
		day = date.getDate(),
		fdate = day+"/"+month+"/"+year;
		return fdate;
	},

	getFormattedTime : function(time){
		var date = new Date(time),
		hours = date.getHours(),
		minutes = date.getMinutes(),
		ftime = hours+":"+minutes;
		return ftime;
	},

	orderDate : function(date){ //order to be used by Calendar
		var date = date.split('/'),
		day = date[0],
		month = date[1],
		year = date[2],
		orderedDate = month+"/"+day+"/"+year;
		return orderedDate;
	},

	showLectures: function(data, clickedTopic){
		var cenCalEntries = data.result.centerCalendarEntries,
		cenLen = cenCalEntries.length,
		masterId = data.result.masterPlanId;
		for(var j = 0; j < cenLen; j++){ //Looping through centers
			if(cenCalEntries[j].name == "COMMON"){ //Alter this or add more when new centers are added
				var calEntries = cenCalEntries[j].entries,
					calLen = calEntries.length,
					$ultimateHtml = '', count = 0;
					for(var k = 0; k < calLen; k++){ //looping through calendar entries of each center
						var lectures = calEntries[k].entry,
							lecLen = lectures.length,
							$finalHtml = '';
							if(lecLen > 0){
								for(var i = 0; i< lecLen; i++){ // looping through each lecture entries									
                                    if(clickedTopic == lectures[i].course.topics[0].name){
										count++;
	                                    var topics = lectures[i].course.topics[0],
										first = lectureUpdate.getFormattedDate(lectures[0].startTime),
										started = lectureUpdate.getFormattedDate(lectures[i].startTime),
										uuid = lectures[i].uuid,
										$headerContents='<span><b>Lecture <span class="lecNo">'+count+'</span></b></span><span class="lecDate">'+started+'</span><span class="dTag">Date-</span>',
										$headerWrapper = '<div class="lectureHeader">'+$headerContents+'</div>',
										subtLen = topics.subTopics.length,
										$STcontents = '',
										lectVersion = "L"+count,
										subtopics = topics.subTopics,
										setDate = lectureUpdate.orderDate(first);
										$('#lecturePicker').datepicker("setDate", setDate);
										if(count == 1){
											$('span.startedDate').html(first);
	                                        lectureUpdate.loadOngoingDate(lectures[0].startTime);
										}
										for(var j = 0; j < subtLen; j++){
											$STcontents += '<span>'+subtopics[j].name+'</span>';
										}

										var $STwrapper = '<div class="STwrapper"><div>Subtopics Covered</div>'+$STcontents+'</div>';
											$finalHtml += '<div class="lecturesWrapper clearfix"><div data-master='+masterId+' data-uid='+uuid+' id="lecture-'+count+'" class="lectures">'+$headerWrapper+$STwrapper+'</div></div>';
                                    	if(lectures[i].state == "COMPLETED"){
                                    		$('#addLecture').hide();
											$('#endTopic').hide();
											$('.classOngoing').html("Ended");
											var date = $('#lecturesList').children().last().find('span.lecDate').html();
											$('.topicLists').find('.activeTopic').addClass('strikeTopic');
											$('span.ongoingDays').html(date);
											$(document).off('mouseenter', '.lecturesWrapper', lectureManager.createEdit);

                                    	}
                                    	else{
											$('#addLecture').show();
											$('#endTopic').show();
											$('.classOngoing').html("Ongoing");
											if(count == 1){
												$('span.startedDate').html(first);
		                                        lectureUpdate.loadOngoingDate(lectures[0].startTime);
											}
											//$('span.ongoingDays').html('');
											$(document).on('mouseenter', '.lecturesWrapper', lectureManager.createEdit);
										}
                                    }
                                } 
								$ultimateHtml += $finalHtml;
								//$ultimateHtml += $rawArray;
							}
							else{
								lectureUpdate.loadZeroLevel();
							}
						$('#noOfLectures').html(+count+" Lectures");
						$('#lecturesList').html($ultimateHtml);
					}
			}// if the center is COMMON			
			//$('#lecturesList').html($ultimateHtml);
			lectureUpdate.loadCalData();
			
		}// end of center loop
		lectureUpdate.hideData();
	},

	hideData : function(){
		var $sel = $('#lecturesList').find('.lecturesWrapper');
		$sel.each(function(){
			var $lec = $(this).children().first(),
			mId = $lec.attr('data-master'),
			uId = $lec.attr('data-uid');
			$lec.removeAttr('data-master').removeAttr('data-uid');
			$lec.data('masterId',mId).data('uuId',uId);
		})
	},

	generateLectureHtml:function(contents, state){
		var lecLen = contents.lectures.length,
		lectures = contents.lectures,
		$finalHtml = '';
		//$endT = $('.lectureInfo a');
		if(lecLen > 0){
				$('#noOfLectures').html(+lecLen+" Lectures");
				for(var j=1; j<=lecLen; j++){
					var date=lectures[j-1].date,
					$headerContents='<span><b>Lecture <span class="lecNo">'+j+'</span></b></span><span class="lecDate">Date-'+date+'</span>',
					$headerWrapper = '<div class="lectureHeader">'+$headerContents+'</div>',
					subtLen = lectures[j-1].subtopics.length,
					$STcontents = '',//ST = subtopics
					lectVersion = "L"+j;
					subtopics = lectures[j-1].subtopics;
					if(j == 1){
						$('span.startedDate').html(lectures[j-1].date);
					}
					//lectureUpdate.loadCalendar(date,lectVersion);
					lectureUpdate.loadCalData();
					for(var k = 0; k<subtLen; k++){
						$STcontents += '<span>'+subtopics[k]+'</span>';
					}
					var $STwrapper = '<div class="STwrapper"><div>Subtopics Covered</div>'+$STcontents+'</div>';
					$finalHtml += '<div class="lecturesWrapper clearfix"><div id="lecture-'+j+'" class="lectures">'+$headerWrapper+$STwrapper+'</div></div>';
				}
				$('#lecturesList').html($finalHtml);
				$('.lectureInfo').children().show();
				$('.lectureInfo').find('h4').hide();
				if(state == "CU-done"){
					$('#addLecture').hide();
					$('#endTopic').hide();
					$('.classOngoing').html("Ended");
					var date = $('#lecturesList').children().last().find('span.lecDate').html().split('-')[1];
					$('span.ongoingDays').html(date);
					$(document).off('mouseenter', '.lecturesWrapper', lectureManager.createEdit);
				}
				else{
					$('#addLecture').show();
					$('#endTopic').show();
					$('.classOngoing').html("Ongoing");
					$('span.ongoingDays').html('');
					$(document).on('mouseenter', '.lecturesWrapper', lectureManager.createEdit);
				}
				lectureUpdate.loadOngoingDate();
		}
		else{
			lectureUpdate.loadZeroLevel();
		}
	},

	loadZeroLevel : function(){
		var $sel = $('.lectureInfo');
		//$('#endTopic').hide();
		$sel.children().hide().end()
			.append('<h4>You have not added any lectures yet! click add lecture</h4>');
		$('#lecturesList').html('');
		$('#noOfLectures').hide();
	},

	loadCalData : function(){
		var $sel = $('#lecturesList').find('.lecturesWrapper');
		$sel.each(function(index){
			var date = $(this).find('.lecDate').html(),
			lecNo = "L"+$(this).find('.lecNo').html();
			lectureUpdate.loadCalendar(date, lecNo);
			//console.log("The length entry here is " +index);
		});	
	},

	loadCalendar: function(date,no){
		var date = date.split('/'),
		day = date[0],
		month = date[1],
		year=date[2],
		$dayTag = $('#lecturePicker').find('td[data-month] a'),
		calMonth = $('#lecturePicker').find('td[data-month]').first().data('month');
		$dayTag.each(function(){
			if($(this).html() == day && month == (calMonth+1)){
				$(this).after('<span class='+no+'>'+no+'</span>');
			}
		});
	},

	addLecture: function(){
		if($('#lecturesList').find('.SUEditing').exists()){
			$('#lecturesList').find('.cancelEditLec').trigger('click');
		}
		var $sel = $('#lecturesList').children(),
		lecLen = $sel.length+1,
		newLec = "L"+lecLen;
		if(lecLen == 1){
			var date = "8/8/2012";
		}
		else{
			var lecDay = $sel.last().find('.lecDate').html(),
			date=[],
			split = lecDay.split('/');
			nextDay = split[0];
			nextDay++;
			date.push(nextDay);
			date.push(split[1]);
			date.push(split[2]);
			var date = date.join("/");
		}
		$('#noOfLectures').html(+lecLen+" Lectures");
		$headerContents='<span><b>Lecture <span class="lecNo">'+lecLen+'</span></b></span><span class="lecDate">'+date+'</span><span class="dTag">Date-</span>',
		$headerWrapper = '<div class="lectureHeader">'+$headerContents+'</div>',
		$STwrapper = '<div class="STwrapper"><div>Subtopics Covered</div><a class="addST">Add subtopics</a></div>',	
		$submitHtml = '<div class="submitHtml clearfix"><a class="cancelLecture">Cancel</a><a class="greenButton" id="submitLecture">Submit</a></div>';
		$finalHtml = '<div class="lecturesWrapper" style="position:relative"><div style="display: block;" id="lecture-'+lecLen+'" class="tmp-lectures clearfix">'+$headerWrapper+$STwrapper+'</div></div>'+$submitHtml;
		$sel.exists()?$sel.last().after($finalHtml):$('#lecturesList').append($finalHtml);
		$('#lecturePicker').find('td[data-month] a').nextAll().remove();
		lectureUpdate.loadCalData();
	},

	updateCalendar: function(){
		var $lectureDiv = $('#lecturesList').find('.lectures'),
		len = $lectureDiv.length,
		$calSel = $('#lecturePicker').find('td span');
		$calSel.each(function(index){
			index++;
			var klass = "L"+index;
			$(this).attr('class', klass);
			$(this).html(klass);
		})

	},

	endTopic: function(){
		var masterId = $('#lecturesList').find('.lectures').first().data('masterId'),
		$sel = $('#lecturesList').find('.lectures');
		$sel.each(function(){
			var uuid = $(this).data('uuId');
			$.post('/Institute/endTopic', {masterPlanId : masterId, calendarEntryUUID : uuid, userRole: "TEACHER"}, function(data){
				if(data.errorCode != "NO_PLAN_FOUND" && data.errorCode != "NO_CALENDAR_ENTRY_FOUND"){
					console.log("data " +JSON.stringify(data))
					//alert("Topic ended successfully")
				}
			});
		})
		
	},

	updateLectureNo: function(){
		var $lectureDiv = $('#lecturesList').find('.lectures'),
		len = $lectureDiv.length;
		$('#noOfLectures').html(+len+" Lectures");
		$lectureDiv.each(function(index){
			index++;
			var lectureId = "lecture-"+index;
			$(this).attr('id',lectureId);
			$(this).find('.lecNo').html(index);
		})
	},

	cancelAddedLecture: function(lectureId){
		var $lecDiv = $('#'+lectureId),
		index = lectureId.split('-')[1];
		$lecDiv.next().remove();
		$lecDiv.parent().remove();
		$lecDiv.remove();
		this.updateLectureNo();
		$('#lecturePicker').find('.L'+index).remove();
		this.updateCalendar();
	}
}




//Compare with master masterPlanId


var compareSchedule = {

	config : {
		CACHE : {}
	},

	init : function(){
		compareSchedule.loadClassData();
		compareSchedule.testFetchSchedule();
		$(document).on('change','#batches', compareSchedule.loadBatchInfo);
		$(document).on('change', '#CUcourses', compareSchedule.loadBatchInfo);
		$(document).on('click','.topicCompare', compareSchedule.showActiveContents);
		$(document).on('mouseenter','.topicCompare', compareSchedule.addHoverEffect);
		$(document).on('mouseleave', '.topicCompare', compareSchedule.removeHoverEffect);
		$(document).on('click', '.ui-datepicker-next', compareSchedule.showColorCode);
		$(document).on('click', '.ui-datepicker-prev', compareSchedule.showColorCode);
	},

	testFetchSchedule : function(){
		$.get('/Institute/fetchSchedule', {userRole: "TEACHER", programmeId: "502f6e1eda59ae44304ada29", courseId:"5046f52118fdae44b26a0866", centers: "COMMON"}, function(data){

		})
	},

	loadClassData : function(){
		$.get('/Institute/getBatchDetails', {userRole: "TEACHER", year: "2012"}, function(data){
			compareSchedule.loadBatchDropdown(data);
		});
	},

	addHoverEffect : function(){
		$(this).children().addClass('hoverEffect');
	},

	removeHoverEffect : function(){
		$(this).children().removeClass('hoverEffect');
	},

	loadBatchDropdown : function(data){
		var dep = data.result.departments,
		depNos = dep.length,
		opts = '',
		sOpts = '';
		for(var i = 0; i < depNos; i++){
			var course = dep[i].course,
			courseLen = course.length,
			prog = dep[i].programme,
			progLen = prog.length;
			
			for(var j = 0; j < courseLen; j++){

				opts += '<option data-Id='+course[j].id+' value='+(j+1)+'>'+course[j].name+'</option>';	
			}
			for(var k = 0; k < progLen; k++ ){
				sOpts += '<option data-Id='+prog[k].id+' value='+(k+1)+'>'+prog[k].name+'</option>';
			}
			
		}
		$('#batches').html(sOpts)
		$('#CUcourses').html(opts);
		lectureUpdate.hideIds($('#CUcourses'));
		lectureUpdate.hideIds($('#batches'));
		var cId = $('#CUcourses').find(':selected').data('id'),
		pId = $('#batches').find(':selected').data('id');
                
		compareSchedule.loadCourseData(cId, pId); 
		// var defaultBatch = $('#batches').find(":selected").text();
		// for(var j = 0; j < batchNos; j++){
		// 	if(batches[j].programme == defaultBatch){
		// 		compareSchedule.loadCourseDropdown(batches[j]);
		// 	}
		// }
		
	},

	// loadCourseDropdown : function(data, selCourse, state){
	// 	var courses = data.course,
	// 	courseLen = courses.length,
	// 	defaultCourse,
	// 	opts = '',
	// 	pId = data.programmeId,
	// 	cId;
	// 	if(!selCourse || state){
			
	// 	for(var j = 0; j < courseLen; j++){
	// 		opts += '<option data-Id='+courses[j].id+' value='+(j+1)+'>'+courses[j].name+'</option>';
	// 	}
	// 	$('#CUcourses').html(opts);
	// 	lectureUpdate.hideIds($('#CUcourses'));
	// 	defaultCourse = $('#CUcourses').find(":selected").text();
	// 	cId = $('#CUcourses').find(':selected').data('id');
	// 	}
	// 	if(selCourse)defaultCourse = $('#CUcourses').find(":selected").text();
	// 	// for(var k = 0; k < courseLen; k++){
	// 	// 	if(courses[k].name == defaultCourse){
	// 	// 		//lectureUpdate.listTopics(courses[k], pId);
	// 	// 	}
	// 	// }
	// 	var cId = $('#CUcourses').find(':selected').data('id');
	// 	compareSchedule.loadCourseData(cId, pId); //pass course json here

	// },

	loadCourseData : function(cId, pId){
		console.log("loadcoursedata called")
		$.when(
			compareSchedule.getCourseData(cId, pId) 
		).then(function(data){
			console.log("compare data called" +JSON.stringify(data))
			compareSchedule.showTopicComparision(data);
		})
	},

	getCourseData : function(cId, pId){
		var cache = compareSchedule.config.CACHE;
		return cache[cId] || $.ajax("/Institute/fetchSchedule",      //Institute/getTopicLectureStatus
			{data:{userRole: "TEACHER", programmeId: pId, courseId : cId, centers : "COMMON"},
			dataType: 'json',
			success: function(data){
				cache[cId] = data;
			}});
	},

	loadBatchInfo : function(){ //on batch change pass the batch
		var batch = $('#batches').find(":selected").text(),
		course = '',
		state = true;
		$(this).attr('id') == "batches"?state:state=false;
		if($('#CUcourses').find('option').exists()){
			course = $('#CUcourses').find(":selected").text();
		}
		//console.log("COurse selected is " +course);
		$.when(
		compareSchedule.loadBatchData(batch) 
		).then(function(data){
			$('#lecturePicker').find('td[data-month] a').nextAll().remove();
			var batches = data.result.programmes,
				batchLen = batches.length,
				reqBatch;
				for(var i = 0; i < batchLen; i++){
					if(batches[i].programme == batch){
						reqBatch = batches[i];
					}
				}
			compareSchedule.loadCourseDropdown(reqBatch, course, state);
			//lectureUpdate.listTopics($('#batches').find(":selected").text(), reqBatch);
		});

	},

	loadBatchData : function(batch){ //pass topic to this function
		//console.log("batch sent to load here is " +batch)
		var cache = compareSchedule.config.CACHE;
		return cache[batch] || $.ajax("/Institute/getBatchDetails",
			{data: {userRole: "TEACHER"}, // masterPlanId : "50642880655ab0e47d5071e5"
			dataType: 'json',
			success: function(data){
				cache[batch] = data;
			}});
	},

	showTopicComparision : function(data){
		var progress = data.result.progress,
		progLen = progress.length,
		$finalHtml = '',
        top = [];
		for(var i = 0; i < progLen; i++){ //looping through centers
			if(progress[i].center == "COMMON"){
				var calEntries = progress[i].centerCalendarEntries,
				entriesLen = calEntries.length;
				for(var j = 0; j < entriesLen; j++){ //looping through each calendar days(must change to topic)
					var topicEntries = calEntries[j].entries,
					tELen = topicEntries.length;                                        
					for(var k = 0; k < tELen; k++){                                           
						var topic = topicEntries[k].course.topics[0].name;
                            if(top.indexOf(topic) == -1){
                                //console.log("the topic name is "+topic)
                                top.push(topic);
                                var sLecNo = progress[i].completedTopicFrequency[topic], //completed number of lectures
                                pLecNo = progress[i].plannedTopicFrequency[topic], //planned number of lectures
                                $compare = compareSchedule.generateComparisionHtml(topicEntries[k], sLecNo, pLecNo);
                                //console.log('sLecNo is ' +sLecNo)
                                $finalHtml += '<div id="TC-'+(j+1)+'" class="topicCompare clearfix">'+$compare+'</div>'; 
                            }						
					}
				}// each cal day or each topic
			}//end if for COMMON center
		}//end of center loop
		$('.compareContents').append($finalHtml);
	},

	generateComparisionHtml : function(data, sLec, pLec){
		var topic = data.course.topics[0].name, //topic array should contain just one, so [0] used 
		$planHtml = compareSchedule.generatePlanHtml(data, pLec),
		$statusHtml = compareSchedule.generateStatusHtml(data, sLec),
		$topicHtml = '<span class="topicName">'+topic+'</span>',
		$topicPlan = '<div class="topicPlan">'+$topicHtml+$planHtml+'</div>',
		$topicStatus = '<div class="topicState">'+$topicHtml+$statusHtml+'</div>',
		$topicCompareHtml = $topicPlan + $topicStatus;
		return $topicCompareHtml;
	},

	generatePlanHtml : function(data, pLec){
		var $contentHtml = compareSchedule.generateContentHtml(data, pLec),
		$planHtml = '<div class="planDetails">'+$contentHtml+'</div>';
		return $planHtml;
	},

	generateStatusHtml : function(data, sLec){
		var statLen = data.status.length, 
		$contentHtml = compareSchedule.generateContentHtml(data.status[(statLen-1)], sLec),
		$statusHtml = '<div class="statusDetails">'+$contentHtml+'</div>';
		return $statusHtml;
	},

	generateContentHtml : function(data, lecNo){
		var startDate = data.startTime, //status array should contain just one
		endDate = data.endTime;
		lecNo == undefined?lecNo="0":lecNo;
		//lectureNo = data.lectures,
		var start = lectureUpdate.getFormattedDate(startDate),
		end = lectureUpdate.getFormattedDate(endDate),
		$lectNoHtml = '';
		startDate == endDate?$lectNoHtml:$lectNoHtml = '<span class="lectNum">Lectures '+lecNo+'</span>';
		var $dateHtml = compareSchedule.generateDateHtml(startDate, endDate),
		$finalHtml = $dateHtml+$lectNoHtml;
		return $finalHtml;
	},

	generateDateHtml : function(start, end){
		var startDate = lectureUpdate.getFormattedDate(start),
		endDate = lectureUpdate.getFormattedDate(end),
		$startDhtml = '', $endDhtml = '', $mes = '';
		if(start == end){
			$mes = '<span class="dMes">Not started</span>';
		}
		else{
			$startDhtml = '<span class="SDate">Start Date : <span class="planSD">'+startDate+'</span></span>',
			$endDhtml = '<span class="EDate">End Date : <span class="planED">'+endDate+'</span></span>';
		}
		$dateHtml = '<div class="dateDetails">'+$startDhtml+$endDhtml+$mes+'</div>';
		return $dateHtml;
	},

	showActiveContents : function(){
		$(this).children().addClass('activeStatus').end()
			.siblings().children().removeClass('activeStatus');
		compareSchedule.colorCodeCalendar($(this));
		var topic = $(this).find('.topicName').first().html(),
		$pSel = $(this).find('.topicPlan'), //plan selector
		$sSel = $(this).find('.topicState'), //status selector
		sED = '', sLec = '', sDay = '',
		pED = $pSel.find('.planED').html(),//plan end date
		$sED = $sSel.find('.planED'),
		$sLec = $sSel.find('.lectNum'),
		pDay = (lectureManager.splitDate(pED))[0],
		pLec = $pSel.find('.lectNum').html().split(" ")[1];
		if($sED.exists() && $sLec.exists()){ //if status end date
			sED = $sED.html(),
			sLec = $sLec.html().split(" ")[1],
			sDay = (lectureManager.splitDate(sED))[0];
		}
		var diff = pDay - sDay,
		lDiff = Math.abs(pLec-sLec);
		$('#initialMessage').html('');
		$('#topicLabel').html(topic);
		if(diff>0){
			if(sED != ''){
				$('#lecStatus').html("Completed Early!");
				$('#byDays').html(+diff+" Days");
				$('#byLec').html(+lDiff+" Lectures");
			}
			else{
				$('#lecStatus').html("Not Started");
				$('#byDays').html('');
				$('#byLec').html("");
			}
		}
		else if(diff == 0){
			$('#lecStatus').html("Completed on time!");
			$('#byDays').html(+diff+" Day");
			$('#byLec').html(+lDiff+" Lecture");
		}
		else{
			$('#lecStatus').html("Delay");
			$('#byDays').html(+diff+" Days");
			$('#byLec').html(+lDiff+" Lecture");
		}
               
 
	},

	showColorCode : function(){
		var selId = $('.compareContents').find('.activeStatus').parent().attr('id'),
		$sel = $('#'+selId);
		compareSchedule.colorCodeCalendar($sel);
		console.log("calling the colorCode")
	},

	colorCodeCalendar : function($sel){
		$('#firstCalendar').find('td[data-month] a').nextAll().remove();
		$('#secondCalendar').find('td[data-month] a').nextAll().remove();
		var planSD = $sel.find('.topicPlan').find('.planSD').html(),
		planED = $sel.find('.topicPlan').find('.planED').html(),
		stateSD = $sel.find('.topicState').find('.planSD').html(),
		stateED = $sel.find('.topicState').find('.planED').html(),
		pSD = lectureManager.splitDate(planSD),
		pED = lectureManager.splitDate(planED),
                setDat = lectureUpdate.orderDate(planSD),
                calMonth = $('#firstCalendar').find('td[data-month]').first().data('month');
                console.log("the month is " +pSD[1])
                console.log("the calmonth is " +calMonth)
                if(pSD[1] != (calMonth-1)){
                     $('#firstCalendar').datepicker('setDate', setDat);
                }
               
		if(stateSD && stateED){
			var sSD = lectureManager.splitDate(stateSD),
			sED = lectureManager.splitDate(stateED);
			compareSchedule.colorCodeStateCal(sSD, sED, "status");
		}
		compareSchedule.colorCodeStateCal(pSD, pED, "plan");
	},

	colorCodeStateCal : function(start, end, state){
		var sDay = start[0], sMonth = start[1],
		eDay = end[0], eMonth = end[1], stateClass='';
		state == "plan"?stateClass = "planColor":stateClass = 'statusColor';

		if(sMonth == eMonth){
			var $dayTag = $('#firstCalendar').find('td[data-month] a'),
			calMonth = $('#firstCalendar').find('td[data-month]').first().data('month'),
			len = eDay - sDay;
			if(calMonth != (eMonth-1)){
				calMonth = $('#secondCalendar').find('td[data-month]').first().data('month');
				$dayTag = $('#secondCalendar').find('td[data-month] a');
			}
			console.log("the diff date os " +len)
			$dayTag.each(function(){
				if($(this).html() == sDay && calMonth == (sMonth-1)){
					var $sel = $(this);
					for(var i = 0; i <= len; i++){
						$sel.after('<span class='+stateClass+'></span>');
						if($sel.parent().next().exists()){
							$sel = $sel.parent().next().find('a');
						}
						else{
							$sel = $sel.parent().parent().next().children().first().find('a');
						}
					}
				return false;	
				}
			});
		}
	},

	colorCodePlanCal : function(start, end){

	}
}