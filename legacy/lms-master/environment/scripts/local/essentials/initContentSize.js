// this is a mongo script which can be run as mongo DBNANE initContentSize.js
// this will insure that all contents are zeroed 

db.cmdsquestions.find({}).forEach(function(e){
					e.size=null;

					db.cmdsquestions.save(e);

					}
				)

db.cmdsvideos.find({}).forEach(function(e){
					e.size=null;

					db.cmdsvideos.save(e);

					}
				)
db.cmdsdocuments.find({}).forEach(function(e){
					e.size=null;

					db.cmdsdocuments.save(e);

					}
				)
db.cmdsmodules.find({}).forEach(function(e){
					e.size=null;

					db.cmdsmodules.save(e);

					}
				)
db.cmdstests.find({}).forEach(function(e){
					e.size=null;

					db.cmdstests.save(e);

					}
				)
db.cmdsfiles.find({}).forEach(function(e){
					e.size=null;

					db.cmdsfiles.save(e);

					}
				)
db.cmdsassignments.find({}).forEach(function(e){
					e.size=null;

					db.cmdsassignments.save(e);

					}
				)

db.questions.find({}).forEach(function(e){
					e.size=null;

					db.questions.save(e);

					}
				)

db.videos.find({}).forEach(function(e){
					e.size=null;

					db.videos.save(e);

					}
				)
db.documents.find({}).forEach(function(e){
					e.size=null;

					db.documents.save(e);

					}
				)
db.modules.find({}).forEach(function(e){
					e.size=null;

					db.modules.save(e);

					}
				)
db.tests.find({}).forEach(function(e){
					e.size=null;

					db.tests.save(e);

					}
				)
db.files.find({}).forEach(function(e){
					e.size=null;

					db.files.save(e);

					}
				)
db.assignments.find({}).forEach(function(e){
					e.size=null;

					db.assignments.save(e);

					}
				)
db.solutions.find({}).forEach(function(e){
					e.size=null;

					db.solutions.save(e);

					}
				)

