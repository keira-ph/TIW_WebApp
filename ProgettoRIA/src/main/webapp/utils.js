/**
 * AJAX call management
 */

function makeCall(method, url, formElement, cback, reset = true) {
  let req = new XMLHttpRequest(); // visible by closure
  req.onreadystatechange = function() {
    cback(req);
  }; // closure
  req.open(method, url);
  if (formElement === null) {
    req.send();
  } else {
    req.send(new FormData(formElement));
  }
  if (formElement !== null && reset === true) {
    formElement.reset();
  }
}

function makeCallJson(method, url, element, cback) {
	
    var req = new XMLHttpRequest();
	
    req.onreadystatechange = function () {
        cback(req)
    };
	console.log(element);
    req.open(method,url,true);
    req.setRequestHeader('Content-Type','application/json');
    
	req.send(element);
    
    
    
}