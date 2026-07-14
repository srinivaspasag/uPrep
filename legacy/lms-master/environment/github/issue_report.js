// NODE.js SETUP
// sudo apt-get install nodejs
// sudo apt-get install npm
// sudo npm install github
// Run as  node issue_report.js 
// 

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
var myTeam = "Technical"
var myUsername="";
var myPassword="";
var outputFileName="371.txt";
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
process.stdout.write("Handling milestones");
			
		for ( var i=0;i< res.length; i++ ){
			var milestone= res[i];
			if( milestone.title==currentMilestone ){
			 
		        gMilestone=milestone; 
			fetchIssues( );
			console.log( "Fetched result" );
		}
		}
	}	

}
function fetchIssues( ){
		     github.issues.repoIssues({
                                user:gRepo.owner.login,
                                repo:gRepo.name,
                                milestone:gMilestone.number,
                                state:"closed",
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
			stream.write( "#"+issue.number+" "+ issue.title );
		}
		stream.end()
		if( res.length == MAX_PAGE_SIZE ){
			gPage++;
			fetchIssues();			
			
		}
	}	

}
github.user.get({}, userFetchHandler );
process.stdout.write("testing github");
