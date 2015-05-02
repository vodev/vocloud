<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<link href="styles/uwstuto.css" rel="stylesheet" type="text/css" />
		<link href="styles/basic.css" rel="stylesheet" type="text/css" />
		<script type="text/javascript" src="styles/basic.js"></script>
		
		<title>UWS Korel console</title>
	</head>
	<body>
		
		<div id="content">			
			
			
			<div id="request">
				<h3>UWS Korel Request</h3>
				
						<ul>
							<li id="actionLine">
								<b>Action: </b>
								<select id="action" onchange="changeAction()">
									<option value="">---</option>
									<option value="homePage">Show Home Page</option>
									<option value="jobList">List jobs</option>
									<option value="createJob">Create a job</option>
									<option value="jobSummary">Job Summary</option>
									<option value="getJobParam">Get Job Param</option>
									<option value="setJobParam1">Set Job Param</option>
									<option value="setJobParam2">Set Job Param (alt)</option>
									<option value="startJob">Start a job</option>
									<option value="abortJob">Abort a job</option>
									<option value="destroyJob1">Destroy a job</option>
									<option value="destroyJob2">Destroy a job (alt)</option>
								</select>
							</li>
							
							<li id="jobIdLine" style="visibility: hidden; display: none;">
								<b>Job ID: </b>
								<input id="jobId" type="text" value="" onchange="updateUri()"></input>
							</li>
							
							<li id="jobAttLine" style="visibility: hidden; display: none;">
								<b>Job Parameter: </b>
								<select id="jobAtt" onchange="updateUri(); updateParam();">
									<option value="">---</option>
									<option value="runId">runId</option>
									<option value="phase">phase</option>
									<option value="owner">owner</option>
									<option value="quote">quote</option>
									<option value="executionDuration">executionDuration</option>
									<option value="destruction">destruction</option>
									<option value="startTime">startTime</option>
									<option value="endTime">endTime</option>
									<option value="parameters">parameters</option>
									<option value="results">results</option>
								</select>
							</li>
							
							<li id="attValueLine" style="visibility: hidden; display: none;">
								<b>Parameter Value:</b>
								<input id="attValue" type="text" onchange="updateParam();"></input>
							</li>
						</ul>
					
						
						
						<ul>
							<li id="uriLine">
								<b>URI:</b>
								<input type="text" id="uri" name="URI" style="width: 50%;" onchange="resetAction()" value=""> <i>(pattern: <b>/</b>{jobList}<b>/</b>{jobId}<b>/</b>{jobAttribute(s)})</i>
							</li>
							
							<li id="methodLine">
								<b>Method:</b>
								<select id="method" onchange="changeMethod(); resetAction();">
									<option value="GET">GET</option>
									<option value="POST">POST</option>
									<option value="PUT">PUT</option>
									<option value="DELETE">DELETE</option>
								</select>
							</li>
							<li id="paramsLine" style="visibility: hidden; display: none;">
								<b>Parameters:</b>
								<input type="text" id="params" style="width: 50%;" onchange="resetAction()"></input>
							</li>
							<li id="formatLine">
								<b>Format:</b>
								<input type="radio" id="acceptXml" value="text/xml"  onclick="changeFormat('acceptXml')" />XML
								<input type="radio" id="acceptJson" value="application/json" checked="checked" onclick="changeFormat('acceptJson')" />JSON
							</li>
						</ul>
				
				<div id="buttonsPanel">
					<input type="button" onclick="execute()" value="Submit" style="font-weight: bold;"></input>
					<input type="button" onclick="reset()" value="Reset"></input>
				</div>
			</div>
			
			<div id="resultBlock">
				<h3>
					Response
				</h3>
				<ul>
					<li><b>Status: </b><span id="status"></span></li>
					<li><b>Format: </b><span id="format"></span></li>
					<li><b>Executed action: </b><span id="lastAction"></span></li>
					<li><b>Content:</b></li>
				</ul>
				<div id="results">
				
				</div>
			</div>
	    </div>
	</body>
</html>