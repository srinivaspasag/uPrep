var helpCenter = new function(){
	var parDivId = "#helpCenter";
	var parDiv;
	var limitTop = 700;
	this.init = function(){
		if(!topicsData || !questionsData){
			return false;
		}
		setTimeout(function(){window.onpopstate = onPopState;},1000);
		parDiv = $(parDivId);
		parDiv
			.on("click",".helpTopicTitle",openTopic)
			.on("click",".helpQuestionTitle",openQuestion);
		$(".helpTopBar").on("click",".goToHelpHome",function(e){
			goToHome();
			e.preventDefault();
			return false;
		});
		return true;
	}
	this.home = new function(){
		var parDivId = "#helpCenterHome";
		var parDiv;
		this.init = function(){
			var go = helpCenter.init();
			if(go){
				parDiv = $(parDivId);
				drawTopics();
				drawQuestions();
			}
			return go;
		};	
		var drawTopics = function(){
			drawAllTopics(parDiv,".helpHomeTopicsHolder",".helpTopicTitle");
		};
		var drawQuestions = function(){
			drawAllQuestions(parDiv,".helpHomeQuesHolder",".helpQuestionTitle");
		};
	};
	var imagePreview = function(e){
		var popup = showVPopup();
		var html = "<div class='boldy big16 blueTextColor' style='padding:12px 0;'>Image Preview</div>"+$(this).html();
                popup.html(html);
                if(e) e.preventDefault();
                return false;
        };
	var drawAllTopics = function(parDiv,holder,itemClass){
		var topics = topicsData.list;
		holder = parDiv.find(holder);
		if(topics && topics.length>0){
			uiCloneHelper.set(holder,itemClass,0);
			$(topics).each(function(index,topic){
				var div = uiCloneHelper.create(index,{"data-index":index});
				$(div).text(topic.title);
			});	
		}			
	}
	var drawAllQuestions = function(parDiv,holder,itemClass){
		var questions = questionsData.list;
		holder = parDiv.find(holder);
		if(questions && questions.length>0){
			uiCloneHelper.set(holder,itemClass,0);
			$(questions).each(function(index,ques){
				var div = uiCloneHelper.create(index,{"data-index":index});
				var title = "- "+ques.title;
				$(div).text(title);
			});	
		}			
	};
	this.topics = new function(){
		var parDivId = "#helpCenterTopics";
		var parDiv;
		this.init = function(){
			parDiv = $(parDivId);
			helpCenter.init();
			parDiv.on("click",".helpTopicItem",goToTopic)
				.on("click",".image,.pImage",imagePreview);
			drawLeft();
			drawBody();
			drawQuestions();
			limitTop = window.innerHeight/2;
			initScroll();
		};
		var drawLeft = function(){
			drawAllTopics(parDiv,".helpTopicsHolder",".helpTopicItem");
		};
		var drawQuestions = function(){
			var questions = questionsData.list;
			var holder = parDiv.find(".helpTopicsQuestionsHolder");
			if(questions && questions.length>0){
				uiCloneHelper.set(holder,".helpEachQues",0);
				$(questions).each(function(index,question){
					var div = uiCloneHelper.create(index,{"data-index":index});
					div = $(div);
					div.attr("id","HelpQues-"+index);
					if(question.title){
						var title = (index+1)+") "+question.title;
						div.find(".title").text(title);
					}
					if(question.image){
						var img = div.find(".image").removeClass("nonner").find(".helpEachQuesImg");
						setTimeout(function(){
							img.attr("src",question.image);
						},1000);
					}else{
						div.find(".image").addClass("nonner");
					}
					if(question.desc){
						div.find(".helpEachQuesContent").html(question.desc).removeClass("nonner");
					}else{
						div.find(".helpEachQuesContent").addClass("nonner");
					}
					if(question.answer){
						div.find(".helpEachQuesAnswer").html(question.answer).removeClass("nonner");
					}else{
						div.find(".helpEachQuesAnswer").addClass("nonner");
					}
				});
			}
		};
		var drawBody = function(){
			var topics = topicsData.list;
			var holder = parDiv.find(".helpTopicsContentHolder");
			if(topics && topics.length>0){
				uiCloneHelper.set(holder,".helpEachTopic",0);
				$(topics).each(function(index,topic){
					var div = uiCloneHelper.create(index,{"data-index":index});
					div = $(div);
					div.attr("id","HelpTp-"+index);
					if(topic.title){
						var title = index?index+". "+topic.title:topic.title;
						div.find(".title").text(title);
					}
					if(topic.image){
						var img = div.find(".image").removeClass("nonner").find(".helpEachTopicImg").attr("src",topic.image);
					}else{
						div.find(".image").addClass("nonner");
					}
					if(topic.video){
						var img = div.find(".video").removeClass("nonner").html(topic.video);
					}else{
						div.find(".video").addClass("nonner").html("");
					}
					if(topic.body){
						div.find(".helpEachTopicContent").html(topic.body).removeClass("nonner");
					}else{
						div.find(".helpEachTopicContent").addClass("nonner");
					}
					if(topic.para && topic.para.length>0){
						var paraDiv = div.find(".helpTopicParaContainer");
						$(topic.para).each(function(pIndex,para){
							var pDiv = paraDiv.find(".helpEachTopicPara").get(pIndex);
							if(pDiv){ pDiv = $(pDiv);}
							else{ 
								pDiv = $(paraDiv.find(".helpEachTopicPara").get(0)).clone(true);
								paraDiv.append(pDiv);
								pDiv = $(pDiv);
							}
							if(para.title){
								pDiv.find(".pTitle").html(para.title).removeClass("nonner");
							}else{
								pDiv.find(".pTitle").addClass("nonner");
							}
							if(para.body){
								pDiv.find(".helpTopicParaContent").html(para.body).removeClass("nonner");
							}else{
								pDiv.find(".helpTopicParaContent").addClass("nonner");
							}
							if(para.image){
								pDiv.find(".pImage").removeClass("nonner")
									.find(".helpEachTopicImg").attr("src",para.image);
							}else{
								pDiv.find(".pImage").addClass("nonner");
							}
							if(para.video){
								pDiv.find(".pVideo").removeClass("nonner").html(para.video);
							}else{
								pDiv.find(".pVideo").addClass("nonner").html("");
							}
						});
					}
				});	
			}			
		};
	};
	var bigLoader = function(el){
		$(el).html("<div class='bigLoader'></div>");
	};
	var goToTopic = function(){
		var $this = $(this);
		var tIndex = $this.data("index");
		scrollTo("#HelpTp-"+tIndex);		
	};
	var scrollTo = function(div){
		var ttop = $(div).position()["top"];
		ttop -= 0;
		ttop = ttop?ttop:0;
		//window.scrollTo(0,ttop);
		$('html, body').animate({
    			scrollTop: ttop
		}, 200);
	};
	var openTopic = function(){
		var $this = $(this);
		var tIndex = $this.data("index");
		openPage(function(){
			scrollTo("#HelpTp-"+tIndex);		
		});
	};
	var openPage = function(cbFn){
		var holder = $("#helpCenter");
		bigLoader(holder);
		var params = {"index":$(this).data("index")};
		$.get("/UIComHelpCenter/topics",function(data){
			holder.html(data);
			pushHistory("/helptopics");
			if(cbFn){
				try{ cbFn();}catch(err){ console.error(err);}
			}
		});
	}
	var goToHome = function(){
		var holder = $("#helpCenter");
		bigLoader(holder);
		$.get("/UIComHelpCenter/homePage",function(data){
			holder.html(data);
			pushHistory("/help");
		});
	};
	var onPopState = function(e){
		//console.log(e);
		var loc = history.location || document.location;
		var pageId = loc.pathname.split("/")[1];
		pageId = pageId?pageId.toUpperCase():"HELP";
		switch(pageId){
			case "HELP":goToHome();
				break;
			case "HELPTOPICS":openTopic();
				break;
			default : goToHome();
				break;
		};
	};
	var pushHistory = function(path){
		var returnLocation = history.location || document.location;
    		var currPath = returnLocation.pathname;
    		if(path!=currPath){
        		history.pushState(null , null, path );
    		}
	};
	var openQuestion = function(){
		var $this = $(this);
		var tIndex = $this.data("index");
		openPage(function(){
			scrollTo("#HelpQues-"+tIndex);		
		});
	};
	var scrollTimeout;
	var initScroll = function(){
		/*var left = $("#helpCenterTopics .helpTopicsTable").position()["left"]+50;
		$(".scrollToTop").css({"left":left+"px"});*/
		$(window).on("scroll",function(e){
			if(scrollTimeout){ clearTimeout(scrollTimeout);}
			scrollTimeout = setTimeout(function(){
				var curScrollTop = $(window).scrollTop();
				//console.log(limitTop+" < "+curScrollTop);
				if(limitTop < curScrollTop){
					$(".scrollToTop").fadeTo(250,1);
				}else{
					$(".scrollToTop").fadeTo(250,0);
				}
			},500);
		});
		$(".scrollToTop").on("click",function(){
			scrollTo("#HelpTp-0");		
		});
	}
};
