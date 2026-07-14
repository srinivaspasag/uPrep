// NODE.js SETUP
// sudo apt-get install nodejs
// sudo apt-get install npm
// sudo npm install github
// Run as  node issue_report.js 
// 

// ADDED 10/18/2013 currentMilestoneState added to fetch report for past milestones 

var GitHubApi = require("github");
var fs = require('fs');
var github = new GitHubApi({
    // required
    version: "3.0.0",
    // optional
    timeout: 5000
});

var MAX_PAGE_SIZE=30
var gOrg=null
var gTeam=null
var gRepo=null
var gUser=null

//// CONFIGURATION FOR SCRIPT
var currentMilestone = "v3.7.1"
var currentMilestoneState = "closed"
var myTeam = "Technical"
var myUsername=ADD_USER_NAME_HERE_IN_QUOTES
var myPassword=ADD_PASSWORD_HERE_IN_QUOTES;
var outputFileName="allIssues.txt";
var findLabels="status:closed"
// eg. "current_issue.txt"
// CONFIGURATION ENDS
if( fs.existsSync( outputFileName ) ){
 fs.unlinkSync(outputFileName);
}
github.authenticate({
    type: "basic",
    username: myUsername,
    password: myPassword } );



userFetchHandler=function(err,res ){
	if( res != null ){
process.stdout.write("Handling user");
		gUser=res;
		github.user.getOrgs({},orgHandler );
	}else{
process.stdout.write("user handling failed");
		console.log( JSON.stringify( err ) );
	}
}
orgHandler=function(err,res){
	if( res != null ){
process.stdout.write("Handling organization");
		for ( var i=0;i< res.length; i++ ){
		var org = res[i];
			if( org.login == "Vedantu" ){
			gOrg =org;
			 github.orgs.getTeams({
				id:org.id,
				org:org.login,
				user:gUser.login
				
			},handleTeams 
			);				
				}
		}
		
	}

}

handleTeams = function( err,res ){
	if( res != null ){
process.stdout.write("Handling teams ");
		for ( var i=0;i< res.length; i++ ){
			var team = res[i];
			if( team.name ==myTeam ){
			gTeam=team;
			 github.orgs.getTeamRepos({
				id:gTeam.id,
				user:gUser.login,
			 	org: gOrg.login	
			},handleTeamRepos );
			}
		}
	}	
}

handleTeamRepos = function( err , res ){
	if( res != null ){
process.stdout.write("Handling team repos");
		for ( var i=0;i< res.length; i++ ){
			var repoinput= res[i];
			if( repoinput.full_name=="Vedantu/vedantu" ){
			 gRepo=repoinput; 
			 github.issues.getAllMilestones({
				user:gRepo.owner.login,
				repo:gRepo.name,
			 	state:currentMilestoneState	
			},handleMileStones 
			);				
		}
		}
	}	

}
var gMilestone=null;
var gPage=1;
handleMileStones=function( err, res ){
	if( res != null ){
process.stdout.write("Handling milestones" + res.length);
			
		for ( var i=0;i< res.length; i++ ){
			var milestone= res[i];
                        console.log( "Checking for milestone" + milestone.title)
			if( milestone.title==currentMilestone ){
			 
			console.log( "Fetching result" );
		        gMilestone=milestone; 
			fetchIssues( );
		}
		}
	}	

}
function fetchIssues( ){
		     github.issues.repoIssues({
                                user:gRepo.owner.login,
                                repo:gRepo.name,
                                milestone:gMilestone.number,
                                state:currentMilestoneState,
                                labels:findLabels,
                                page:gPage,
                                per_page:MAX_PAGE_SIZE

                        },handleIssues
                        );

}

handleIssues=function( err, res ){
	if( res != null ){
		console.log( "Appending to file now"  );
	        var stream = fs.createWriteStream(outputFileName,{ flags: 'a'});
		for ( var i=0;i< res.length; i++ ){
			var issue=res[i];
			stream.write( "\n" );
                        var priority = null;
			var bugType=null
                        
		for( var bugIndex=0; bugIndex< issue.labels.length; bugIndex++){
				console.log( issue.labels[bugIndex].name);
			}
			stream.write("#" +issue.number+" :"+ issue.title  );
		}
		stream.end()
		if( res.length == MAX_PAGE_SIZE ){
			gPage++;
			fetchIssues();			
			
		}
	}else if( err !=null){
		console.log( "Failed to handle issues" + err )
	}	

}
github.user.get({}, userFetchHandler );
process.stdout.write("testing github");
