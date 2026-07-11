db.cmdsquestions.find({}).forEach(function(e){
					var re = new RegExp('\<img src=\\"(http|https):\/\/[\\w|\\d|\\:|\\.]+\/viewer\/view\/question\/img\/([\\d|\\w|\\-]+)\.qus[\\w|\.]+\\"\>');
					var optionList = [];
					print("Printing original");
					printjson(e);
					if( e ){
						if (e.questionBody){
							print("parsing questionbody");
							var questionBody = e.questionBody;
							var uuids = [];
							while(1){
								var match = re.exec(questionBody.newText);
								questionBody.newText = questionBody.newText.replace(re, "$2");
	
								 printjson(questionBody.newText);
								if (match){
	
									 printjson(match[2]);
									uuids.push(match[2]);
								}else{
									break;
								}
							}
							if (uuids.length != 0){
								if(!questionBody.uuidImages ){
									questionBody.uuidImages = [];
								}
								for( var k =0 ; k < uuids.length; k++ ){
									
									var uuid = uuids[k];
									if( questionBody.uuidImages.indexOf(uuid) == -1 ){
										questionBody.uuidImages.push(uuid);
									}
								}
							}
						}
						if( e.solutionInfo ){
							print("parsing solutionInfo newOptions");
							if (e.solutionInfo.optionBody ) {
								if (e.solutionInfo.optionBody.newOptions ) {
									var uuids = [];
									e.solutionInfo.optionBody.newOptions.forEach(function(d) {
												// printjson(d);
												while (1) {
													var match = re.exec(d);
													d = d.replace(re, "$1");
															// printjson(d);
													if (match) {
														// printjson(match[2]);
														uuids.push(match[2])
													} else {
														break;
													}
												}
		
												optionList.push(d);
		
											});
									// printjson(optionList);
									// printjson(uuids);
		
									e.solutionInfo.optionBody.newOptions = optionList;
									if (uuids.length != 0) {
										if( !e.solutionInfo.optionBody.uuidImages ){
											e.solutionInfo.optionBody.uuidImages = [];
										}
										for( var k =0 ; k < uuids.length; k++ ){
											
											var uuid = uuids[k];
											if( e.solutionInfo.optionBody.uuidImages.indexOf(uuid) == -1 ){
												e.solutionInfo.optionBody.uuidImages.push(uuid);
											}
										}
									}
								}
							}
						}

if (e.solutionInfo) {
							if (e.solutionInfo.solutions) {
								print("parsing solutionInfo solutions");
								for (i in e.solutionInfo.solutions) {
									
									var solution = e.solutionInfo.solutions[i];
									// printjson(solution);
									var uuids = [];
									
									while (1) {
	
										var match = re.exec(solution.newText);
										solution.newText = solution.newText
												.replace(re, "$1");
	
										// printjson(solution.newText);
										if (match) {
	
											// printjson(match[2]);
											uuids.push(match[2])
										} else {
											break;
										}
									}
									if (uuids.length != 0) {
										if( !solution.uuidImages ){
											solution.uuidImages = [];
										}
										for( var k =0 ; k < uuids.length; k++ ){
											
											var uuid = uuids[k];
											if( solution.uuidImages.indexOf(uuid) == -1 ){
												solution.uuidImages.push(uuid);
											}
										}
										
									}
									// printjson(solution);
								}
							}
						}
						if (e.hints) {
							if (e.hints.hints) {
								print("parsing hints");
								for (i in e.hints.hints) {
									var hint = e.hints.hints[i];
									// printjson(solution);
									var uuids = [];
								
									while (1) {
	
										var match = re.exec(hint.newText);
										hint.newText = hint.newText.replace(re,
												"$1");
	
										// printjson(solution.newText);
										if (match) {
	
											// printjson(match[2]);
											uuids.push(match[2])
										} else {
											break;
										}
									}
									
									if (uuids.length != 0) {
										if( !hint.uuidImages ){
											hint.uuidImages = [];
										}
										for( var k =0; k < uuids.length;k++ ){
											
											var uuid = uuids[k];
											if( hint.uuidImages.indexOf(uuid) == -1 ){
												hint.uuidImages.push(uuid);
											}
										}
									}
								
									// printjson(solution);
								}
							}
						}
					print("Printing updated");
					printjson(e);
					db.cmdsquestions.save( e );
					}	
				})
