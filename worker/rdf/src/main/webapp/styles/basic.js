/** Get the HTML element corresponding to the given ID. */
function $(id){
	return document.getElementById(id);
}

function changeAction(){
	var action = $('action').value;

	loadDetails(action);
	
	if (action == '')
		return;
	
	updateUri();
	
	if (action == 'homePage')
		load('GET', '');
	else if (action == 'jobList')
		load('GET', '');
	else if (action == 'createJob')
		load('POST', 'zip=http://localhost/rdf.zip&PHASE=RUN');
	else if (action == 'jobSummary')
		load('GET', '');
	else if (action == 'getJobParam')
		load('GET', '');
	else if (action == 'setJobParam1'){
		load('POST', '');
		updateParam();
	}else if (action == 'setJobParam2'){
		load('PUT', '');
		updateParam();
	}else if (action == 'startJob')
		load('POST', 'PHASE=RUN');
	else if (action == 'abortJob')
		load('POST', 'PHASE=ABORT');
	else if (action == 'destroyJob1')
		load('DELETE', '');
	else if (action == 'destroyJob2')
		load('POST', 'ACTION=DELETE');
}

function loadDetails(action){
	if (action == "jobSummary" || action == "destroyJob1" || action == "destroyJob2" || action == "startJob" || action == "abortJob"){
		$('jobIdLine').style.visibility = 'visible';
		$('jobIdLine').style.display = 'list-item';
		$('jobAttLine').style.visibility = 'hidden';
		$('jobAttLine').style.display = 'none';
		$('jobAtt').value = '';
	}else if (action == "getJobParam" || action == "setJobParam1" || action == "setJobParam2"){
		$('jobIdLine').style.visibility = 'visible';
		$('jobIdLine').style.display = 'list-item';
		$('jobAttLine').style.visibility = 'visible';
		$('jobAttLine').style.display = 'list-item';
	}else{
		$('jobIdLine').style.visibility = 'hidden';
		$('jobIdLine').style.display = 'none';
		$('jobId').value = '';
		$('jobAttLine').style.visibility = 'hidden';
		$('jobAttLine').style.display = 'none';
		$('jobAtt').value = '';
	}
	
	if (action == "setJobParam1" || action == "setJobParam2"){
		$('attValueLine').style.visibility = 'visible';
		$('attValueLine').style.display = 'list-item';
	}else{
		$('attValueLine').style.visibility = 'hidden';
		$('attValueLine').style.display = 'none';
	}
		
}

function load(method, params){
	$('method').value = method;
	changeMethod();
	
	$('params').value = params;
}

function resetAction(){
	if ($('action').value != ''){
		$('action').value = '';
		loadDetails('');
	}
}

function changeMethod(){
	if ($('method').value == "POST"){
		$('paramsLine').style.visibility = 'visible';
		$('paramsLine').style.display = 'list-item';
	}else{
		$('paramsLine').style.visibility = 'hidden';
		$('paramsLine').style.display = 'none';
	}
}

function changeFormat(id){
	if (id == 'acceptXml')
		$('acceptJson').checked = false;
	else
		$('acceptXml').checked = false;
}

function updateUri(){
	var uri = '/';
	var uriField = $('uri');
	var action = $('action').value;

	if (action == '')
		return;
	
	if (action != 'homePage'){
		uri = uri+'rdf';
	
		if (action != 'jobList' && action != 'createJob'){
			uri = uri+'/'+$('jobId').value;
			
			if (action != 'jobSummary' && action != 'destroyJob'){
				if (action == 'startJob' || action == 'abortJob')
					uri = uri+'/phase';
				else
					uri = uri+'/'+$('jobAtt').value;	
			}
		}
	}
		
	uriField.value = uri;
}

function updateParam(){
	var queryPart = $('jobAtt').value+"="+$('attValue').value;
	
	if ($('method').value == "POST"){
		updateUri();
		$('params').value = queryPart;
	}else if ($('method').value == "PUT"){
		updateUri();
		$('uri').value = $('uri').value+"?"+queryPart;
	}
}

function reset(){
	$('action').value = '';
	
	$('jobIdLine').style.visibility = 'hidden';
	$('jobIdLine').style.display = 'none';
	$('jobId').value = '';
	
	$('jobAttLine').style.visibility = 'hidden';
	$('jobAttLine').style.display = 'none';
	$('jobAtt').value = '';
	
	$('paramsLine').style.visibility = 'hidden';
	$('paramsLine').style.display = 'none';
	
	$('uri').value = '';
	$('method').value = 'GET';
	$('params').value = '';
	
	$('status').innerHTML = '';
	$('format').innerHTML = '';
	$('lastAction').innerHTML = '';
	$('results').innerHTML = '';
	
	$('resultBlock').style.visibility = 'hidden';
}

function execute(){
	// get the XML representation of the jobs list:
	var req = null;
	try {
		req = new ActiveXObject("Microsoft.XMLHTTP");    // Essayer Internet Explorer 
	}catch(e){   // Echec
		req = new XMLHttpRequest();  // Autres navigateurs
	}

	var uri = $('uri').value.replace(/\./g, '');
	req.open($('method').value, 'uws'+uri, true);
	req.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
	if ($('acceptXml').checked)
		req.setRequestHeader("Accept", $('acceptXml').value);
	else
		req.setRequestHeader("Accept", $('acceptJson').value);
	
	req.onreadystatechange=function(){
		 if (req.readyState==4){
			$('status').innerHTML = req.status+' <i>('+req.statusText+')</i>';
			$('format').innerHTML = req.getResponseHeader('Content-type');
			
			var myXml = req.responseText;
			if (req.status == 200){
				myXml = myXml.replace(/</g, "&lt;");
				myXml = myXml.replace(/>/g, "&gt;");
				$('results').innerHTML = '<pre>'+myXml+'</pre>';
				$('results').style.color = 'black';
			}else{
				myXml = myXml.substring(myXml.indexOf('<h1>')+4, myXml.indexOf('</h1>'));
				var indEndTitle = myXml.lastIndexOf(']')+1;
				var resultsDiv = $('results');
				resultsDiv.innerHTML = '<u>'+myXml.substring(0, indEndTitle)+'</u><br />'+myXml.substring(indEndTitle);
				resultsDiv.style.color = 'red';
			}
			getExecutedAction();
		 }else
			 $('status').innerHTML = "sending request...("+req.readyState+"/4)";		
	};
	 $('status').innerHTML = "sending request...(1/4)";
	 $('format').innerHTML = "";
	 $('results').innerHTML = "";
	 $('resultBlock').style.visibility = 'visible';
	req.send($('params').value);
}

function getExecutedAction(){
	// get the XML representation of the jobs list:
	var req = null;
	try {
		req = new ActiveXObject("Microsoft.XMLHTTP");    // Essayer Internet Explorer 
	}catch(e){   // Echec
		req = new XMLHttpRequest();  // Autres navigateurs
	}
	req.open('GET', 'uws?lastAction', true);
	req.onreadystatechange=function(){
		 if (req.readyState==4)
			 $('lastAction').innerHTML = req.responseText;
	}
	req.send(null);
}