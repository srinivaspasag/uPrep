var topicsData = {
    list:[
	{
		title : "Guide Home",
		body : "Administrator’s Guide: \n\
			As Institute Administrator, you have full control over your entire institute. \n\
			You have the ability to view/control all courses, programs, and users, \n\
			as well as to customize the Learnpedia system to meet your institution's needs.",
		image : "",
		para : [
			{
				body : "",
				image : ""
			}
		]
	},
	{
		title : "Creating and editing users",
		body : "Use the People Section in CMDS to manage user accounts at your institute. You may view, edit, and manually create user accounts. \n\
			Select the People Tab on top header of your CMDS page to begin. \n\
			People are segregated into Students and Members - Members can be Teachers, Managers and Editors.",
		image : "/public/images/help/people-small.jpg",
		video : '',
		para : [
			{
				title:"<b style='font-size:15px;'>How to add Teachers:</b>",
				video : "<iframe width='640' height='360' src='//www.youtube.com/embed/TA0oJMpNSdU?feature=player_detailpage&showinfo=0&rel=0' frameborder='0' allowfullscreen></iframe>"
			},
			{
				title:"<b>How to add students:</b>",
				body : "Go to Students Section by clicking on People Tab on your CMDS top header. Now, click on Add Students and choose the mode of addition - Bulk Addition or Manual Addition \n\
				<ul> \n\
				<li> \n\
				  <b>Manual Addition -</b> It is a two step process. First fill the profile information of the student. First Name, \n\
				  Institute ID (Enrollment ID), Date of Birth, Gender are compulsory fields. \n\
				  In the second step assign the Program > Center > Section(s) to the student. \n\
				</li> \n\
				<li>  \n\
				  <b>Bulk Addition -</b> First refer to the format of upload (excel) file. \n\
				  Note that students of only one Program (may be studying at multiple centers or sections under that program) can be added through one file.\n\
				  Choose the Program you want to upload file for - upload the file. In case there is some missing (compulsory) information in the file,\n\
				  an error message stating the same will pop up.\n\
				</li> \n\
				</ul>",
				image : "",
				video : ""
			},
			{
				title:"<b>How to edit student information:</b>",
				body : "Select the Program > Center > Section under the Student tab to see the list of corresponding students. You can also Search for any specific student whose information you want to edit. Click on Edit (Pencil symbol) button alongside student name. You can edit the following information:  \n\
				<ul> \n\
				<li> \n\
				  <b>Profile Information:</b> \n\
				  <ul> \n\
				    <li>First Name, Last Name</li> \n\
				    <li>Institute ID / Member ID / Enrollment ID</li> \n\
				    <li>Date of Birth</li> \n\
				    <li>Gender</li> \n\
				    <li>Contact Number</li> \n\
				    <li>Parent’s Information - Father/Mother/Guardian’s name and contact number, Parent Email ID \n\
				      (which is used as Login ID for the Parent to access his child’s progress on Learnpedia)</li> \n\
				    <li>Add / Change Profile Picture</li> \n\
				  </ul> \n\
				</li> \n\
				<li>  \n\
				  <b>Institute Information:</b> \n\
				</li> \n\
				<li>  \n\
				  Add / Edit Institute Information - Click on Add/Edit Institute Info which is towards the bottom end of the Edit Student Profile popup. \n\
				  First remove the added Program > Center > Section mapping for the Student. Add the new mapping as desired. \n\
				  Note that first you will have to select Program, then Center and then Section(s), to which student needs to be mapped with. \n\
				  If multiple Programs are to be mapped, the process will have to be repeated.  \n\
				</li> \n\
				<li><b>Remove email id associated with student’s profile -</b></li> \n\
				<li><b>Reset login id of student from email id to institute id (enrollment id) - </b></li> \n\
				<li><b>Generate new password</b> for institute id (enrollment id) as login id for student -</li> \n\
				</ul>",
				image : ""
			},
			{
				title:"<b>How to resolve Login ID related problems faced by Students: </b>",
				body : "As administrator of the Institute, you will have the right to view Login IDs of each student and reset \n\
					or change their password as per the requirement raised by student. \n\
				<ul> \n\
				 <li> \n\
				 When a student can’t login using his/her Login id, following can be the reasons (make sure you verify \n\
				 the student using his Identity Card or Profile Pic before going ahead with any of the following):  \n\
				    <ul> \n\
				      <li><b>Wrong Institute ID</b> \n\
					<ul> \n\
					  <li>Solution: confirm student’s Institute ID, check if he is using the correct one.</li> \n\
					</ul> \n\
				      </li> \n\
				      <li><b>Student changed his password and forgot it</b> \n\
					<ul> \n\
					  <li>Solution: reset student’s password, create a new one and provide the same to the student. The method of changing password is mentioned in the point above. </li> \n\
					</ul> \n\
				      </li> \n\
				      <li><b>Student made registered email id his Login Id, and forgot his password</b> \n\
					<ul> \n\
					  <li>Solution: ask student to use forgot password link on the login page of Learnpedia</li> \n\
					</ul> \n\
				      </li> \n\
				      <li><b>Someone else has logged in the name of the student (using his First Login credentials) and \n\
					has changed the password or has registered an email id as the Login ID</b> \n\
					<ul> \n\
					  <li>Solution: Check if the email id (if any) registered with student’s profile is his or not. If not, remove the email id. Reset the login id to Institute id (if email id has been made login id). Generate new password for the student. Ask student to login into Learnpedia using his Institute id and the new generated password. Do not forget to remind him that he should change his password as soon as he logs into the system. Also, ask him to register his email id once his logs in.</li> \n\
					</ul> \n\
				      </li> \n\
				    </ul> \n\
				 </li> \n\
				</ul>",
				image : ""
			},
			{
				title:"<b>How to add profile pics of students:</b>",
				body : "As Administrator of the Institute, you can upload profile pics of the students for teachers to easily identify them \n\
				  and analyze their performance on Learnpedia. You can upload profile pics in the following ways: \n\
				<ul> \n\
				 <li> \n\
				  Manual Upload - Select the Program > Center > Section under the Student tab to see the list of corresponding students. You can also Search for any specific student whose information you want to edit. Click on Edit (Pencil symbol) button alongside student name. You can Add or Change Profile Pic now.\n\
				 </li> \n\
				 <li> \n\
				  Bulk Upload - Add Bulk Actions Tab on the Home Screen of CMDS and select Import Profile Pics. Choose the Program>Center>Section for which you want to upload Profile Pics.\n\
				 </li> \n\
				</ul>",
				image : ""
			}
		]
	},
	{
		title : "Organization structure management",
		body : "<b>Edit Academic Structure:</b>Learnpedia provides a highly flexible and scalable structure for Institutes to map their Academic Offerings with. Institute can map it’s Academic Offerings under Program > Courses > Center > Section structure. Programs represent the Academic Offerings of the Institute to students. Each Programs can have multiple Courses aligned with it. Institute can run multiple programs at multiple centers. And, at any center, under each Program, Institute can have students studying under multiple sections.",
		image : "/public/images/help/acad-structure_small.jpg",
		para : [
			{
				body : "You, as an Administrator of your Institute, can add/edit Program, Center, Section, Courses structure in the following way: \n\
					Choose Institute Setting tab from top header on CMDS. Choose Edit Academic Structure tab inside the Institute Setting page. \n\
					You can now add/edit Departments, Programs, Centers and Sections as per your Institute Structure: \n\
					<ul> \n\
					  <li> \n\
					  Department - Departments are the heads under which any Institute distinguishes it’s academic staff (mostly teachers) into. Departments are visible only on CMDS and are not visible to students. You, as administrator or the Institute, can add new Department by clicking on Add New Department under Edit Academic Structure tab. You can edit the Name and Code of any Department by clicking on the Pencil (Edit) Sign that appears when you hover on the Department name. \n\
					  <div>Note that, you can not delete any Department once created.</div> \n\
					  </li> \n\
					  <!--<li> \n\
					    Programs - \n\
					  </li> \n\
					  <li> \n\
					    Centers - \n\
					  </li> \n\
					  <li> \n\
					    Sections - \n\
					  </li>--> \n\
					</ul> \n\
					<div>Assign Courses: Courses can be assigned to the respective Programs (under Departments). Note that only those courses can be assigned which have been added to the Institute under Courses Section (refer to Courses Tab at top header in CMDS). To assign courses to a Program, choose Institute Setting tab from top header on CMDS. Choose Assign Courses tab inside the Institute Setting page. Choose Department (under Departments tab), then choose the Program (under Runs Program tab) you want to add courses to, now click on Map Courses tab (under Runs Courses tab). The pop-up gives you the list of all the Courses running in your Institute and the courses mapped with the selected Program are checked (tick) - you can choose to uncheck (deselect) or check (select) courses as per your requirement. \n\
					</div> \n\
					<div>Note that if you uncheck (deselect) any course from a Program, all the course content shared with students (under the Program) will no longer be available for them to access.</div> \n\
					",
				image : ""
			}
		]
	},
	{
		title : "Content creation and distribution",
		body : "",
		image : "",
		para : [
			{
				body : "<b>Resources : </b> Resources is where all your Institute content uploaded or created by your teachers reside. Imagine Resources as a shared content folder between your teachers. Teachers can add/create following formats of content under Resources (by clicking on Add Resources tab under Resources head): \n\
					<ul> \n\
					  <li> \n\
					    <b>Add a Question Set:</b> \n\
					    <div class='embedVideo'><iframe width='640' height='360' src='//www.youtube.com/embed/UahvRRhRW84?feature=player_detailpage&showinfo=0&rel=0' \n\
					    frameborder='0' allowfullscreen></iframe> </div>\n\
					    You can either Add a Question Set under a Folder or you can directly add inside the Resources. Choose Add Resources tab in Resources head and then choose Add a Question Set. Choose Target gives you the option of adding the Question Set to a Folder or if you do nott want to add it in any Folder, just choose Resources as Target. Now click on Choose File. Select the relevant file you want to upload (Only Docx files are allowed). The file is uploaded with the name you give as Title in the file. You can change the name of the Question Set if you want. Once the file is uploaded, all questions are displayed. Click Submit after reviewing the file. You can now see the Question Set you uploaded in the Target Folder. \n\
					  </li> \n\
					  <li> \n\
					    <b>Add a Question:</b> \n\
					       <div class='embedVideo'><iframe width='640' height='360' src='//www.youtube.com/embed/6nl__efPbmU?feature=player_detailpage&showinfo=0&rel=0' \n\ frameborder='0' allowfullscreen></iframe></div> \n\
					  </li> \n\
					  <li> \n\
					    <b>Add a Folder:</b> You can add Folders to organize the content under Folder structure. Choose Add a Folder under Add Resources tab in Resources head. Choose Title of the folder in the pop up that appears. You can now add resources (all formats) under this folder or choose to add another folder under the folder.\n\
					  </li> \n\
					  <!--<li> \n\
					    <b>Add an Assignment:</b> \n\
					  </li>--> \n\
					  <li> \n\
					    <b>Add a Test:</b> \n\
					    <div class='embedVideo'><iframe width='640' height='360' src='//www.youtube.com/embed/UmutQ0-h_ec?feature=player_detailpage&showinfo=0&rel=0' \n\ frameborder='0' allowfullscreen></iframe></div> \n\
					  </li> \n\
					  <!--<li> \n\
					    <b>Add a Video: </b> \n\
					  </li> \n\
					  <li> \n\
					    <b>Add a Document:</b> \n\
					  </li>--> \n\
					</ul> \n\
					",
				image : "/public/images/help/resources-small.jpg"
			},
			{
				body : "<b>Programs : </b>  \n\
				<div class='embedVideo'><iframe width='640' height='360' src='//www.youtube.com/embed/uZK6ldi8ESw?feature=player_detailpage&showinfo=0&rel=0' \n\ frameborder='0' allowfullscreen></iframe></div> \n\
				Program Pages gives you snapshot of the Programs your Institute is running (under multiple centers and sections). Click on the Program Tab on the top header of CMDS, choose a particular program from the drop down to open the respective Program Page. You can choose to further filter down to a particular Center or Section from the Select a Center, Select a Section drop downs under Program Page. Default view shows all the Content added to the Program > Center > Section that has been opened. You can also view Members and Students associated with the Program > Center > Section from the respective options on the left side tabs. Following are the options available:\n\
					<ul> \n\
					  <li> \n\
					    <b>Content: </b>Shows all the content added to the Program > Center > Section. In case, no particular Center or Section is chosen, all the content under the Program across all Centers and Sections (under the Program) is listed. The content may or may not be accessible to students of respective Program > Center > Section on learn.learnpedia.in. \n\
					  </li> \n\
					  <li> \n\
					    To make a content accessible to students, \n\
					  </li> \n\
					  <!--<li> \n\
					    <b>Members:</b> \n\
					  </li> \n\
					  <li> \n\
					    <b>Student:</b> \n\
					  </li> \n\
					  <li> \n\
					    <b>Upload Result Sheet: </b> \n\
					  </li>--> \n\
					</ul> \n\
					",
				image : "/public/images/help/programs-content-small.jpg"
			}
		]
	}
	]
};
