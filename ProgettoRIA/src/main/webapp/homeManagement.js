(function() {

    // Componenti della pagina
    let pageOrchestrator = new PageOrchestrator();
    let personalMessage;
    let categoriesList, confirmButton;
    let creationForm;
    let startUl, startElement;
    let addRenameEventListener;
    //let modifiedList;


    window.addEventListener("load", () => {

        // controllo se l'utente è loggato
        if(sessionStorage.getItem("userinfo") == null) {
            window.location.href = "index.html";
        } else {

            // inizializza i componenti
            pageOrchestrator.start();
            pageOrchestrator.refresh();
        }
    }, false);


    function PageOrchestrator() {

        let alertContainer = document.getElementById("id_alert");

        this.start = function() {

            // inizializzazione del saluto personalizzato
            personalMessage = new PersonalMessage(
                sessionStorage.getItem("userinfo"),
                document.getElementById("id_userinfo")
            );
            personalMessage.show();


            // inizializzazione della lista delle categorie
            categoriesList = new CategoriesList(
                alertContainer,
                document.getElementById("id_listcontainer")
            );
            // inizializzazione lista movimenti
            resetModifiedList();


            // inizializzazione del pulsante di conferma
            confirmButton = new ConfirmButton(
                alertContainer,
                document.getElementById("id_confirmbutton")
            );
            confirmButton.reset();
            confirmButton.registerEvents(this);


            // inizializzazione del form
            creationForm = new CreationForm(
                alertContainer,
                document.getElementById("id_categoryform")
            );

            creationForm.registerEvents(this);


            /*addRenameEventListener= new AddRenameEventListener(
                document.createElement("input"),
                categoriesList.id
            );*/


            // gestione logout
            document.getElementById("id_logout").addEventListener(
                "click", () => {
                    window.sessionStorage.removeItem("userinfo");
                }
            )
        }; //fine di function();



        // refresh
        this.refresh = function() {
            alertContainer.textContent = "";
            categoriesList.show();
            creationForm.reset();
            creationForm.show();
            confirmButton.reset();
            resetModifiedList();
        };

    } //fine della funzione PageOrchestrator

    function resetModifiedList() {
        let modifiedList = new Array();
        sessionStorage.setItem("modList",JSON.stringify(modifiedList));
    }



    function PersonalMessage(_userinfo, messagecontainer) {
        this.userinfo = _userinfo;
        this.show = function () {
            messagecontainer.textContent = this.userinfo;
        }
    }


    function CategoriesList(_alert, listcontainer) {
        this.alert = _alert;
        this.listcontainer = listcontainer;

        this.show = function() {

            // per la chiusura
            let self = this;

            makeCall("GET", "GetCategoryListData", null, function(req) {

                if(req.readyState == 4) {
                    let message = req.responseText;

                    if(req.status == 200) {

                        let categoriesToShow = JSON.parse(message);

                        if(categoriesToShow.length == 0) {
                            self.alert.textContent = "There are no categories present!";
                            return;
                        }
                        self.update(categoriesToShow);

                    } else if(req.status == 403) {
                        window.location.href = req.getResponseHeader("Location");
                        window.sessionStorage.removeItem("userinfo");

                    } else {
                        self.alert.textContent = message;
                    }
                }
            });
        };


        this.update = function(arrayCategories) {

            // svuotare la tabella
            this.listcontainer.innerHTML = "";

            let self = this;
            let alert = self.alert;
            let container = self.listcontainer;

            printCategory(container,arrayCategories,alert);

        }
    }

    function ConfirmButton(_alert, button) {

        this.alert = _alert;
        this.button = button;

        this.reset = function() {
            this.button.style.visibility = "hidden";
            this.button.disabled = true;
        }

        this.show = function () {
            this.button.disabled = false;
            this.button.style.visibility = "visible";
        }

        this.registerEvents = function(orchestrator) {

            this.button.addEventListener("click", (e) => {

                let self = this;

                let json = sessionStorage.getItem("modList");
                let modifiedList = JSON.parse(json);

                if(modifiedList.length > 0) {

                    makeCallJson("POST", "UpdateCategory", json,
                        function (x) {

                            if(x.readyState == XMLHttpRequest.DONE) {

                                let message = x.responseText;
                                switch (x.status) {

                                    case 200:
                                        orchestrator.refresh();
                                        self.alert.textContent = message;
                                        break;

                                    case 403:
                                        window.location.href = req.getResponseHeader("Location");
                                        window.sessionStorage.removeItem("userinfo");
                                        break;

                                    case 500: // server error
                                        self.alert.textContent = message;
                                        break;

                                    default:
                                        // 400 bad request, 401 unauthorized
                                        orchestrator.refresh();
                                        self.alert.textContent = message;
                                        break;

                                }
                            }

                        });
                } else {
                    orchestrator.refresh();
                    self.alert.textContent = "Invalid operation!";
                }
            });
        }

    }


	function printCategory(container, categories, alert, indentationLevel = 0) {
	  if (!categories) {
	    return;
	  }
	
	  const ul = document.createElement("ul");
	
	  for (const category of categories) {
	    const li = document.createElement("li");
	    label = document.createElement("label");	
	    
		li.draggable = true;
		const categoryNum = document.createTextNode(category.id + " ");
		label.appendChild(categoryNum);
		
		const spanElement = document.createElement("span");
	    spanElement.className = "nome";
		spanElement.textContent = category.name;
		label.appendChild(spanElement);
		
		const inputElement = document.createElement("input");
		inputElement.type = "text";
		inputElement.className = "input-nome hidden";
		label.appendChild(inputElement);
	//click sulla categoria
		spanElement.addEventListener("click", function () {
		  spanElement.classList.add("hidden");
		  inputElement.classList.remove("hidden");
		  inputElement.focus();
	    });
	//blur sulla categoria	
		inputElement.addEventListener("blur", function () {
		  const nuovoNome = inputElement.value;
		  spanElement.textContent = nuovoNome;
		  inputElement.classList.add("hidden");
		  spanElement.classList.remove("hidden");
		
		  const categoryID = category.id;
		
		  if (categoryID) {
			console.log(categoryID);
		    const requestData = {
		      categoryID: categoryID,
		      newName: nuovoNome
		        
		    };
		      
		    makeCallJson("POST", "RenameCategory", JSON.stringify(requestData), function (response) {
		      if (response.readyState === XMLHttpRequest.DONE) {
		        if (response.status === 200) {
		          const responseData = response.responseTex;
		          console.log(responseData);
		        } else {
		          console.error("Errore nella richiesta al server:", response.status);
		        }
		      }
		    });
		  } else {
		    console.error("categoryID è null o vuoto");
		  }
		});
	
	    li.setAttribute("categoryid", category.id);
	    li.appendChild(label);
	    setListEvents(li, label, alert);
	
	    
	    ul.appendChild(li);
	  }
	  container.appendChild(ul);
	}




    function setListEvents(li,label,alert) {

        if(li.draggable) {
            li.addEventListener("dragstart",(e) => {
                dragStart(e,alert);
            });
        }
        li.addEventListener("dragover", (e) => {
            dragOver(e);
        });

        li.addEventListener("dragleave", (e) => {
            dragLeave(e);
        });
        
		li.addEventListener("drop", (e) => {
		            drop(e);
		});
		        
        

    }



    function CreationForm(_alert, formcontainer) {

        this.alert = _alert;

        this.registerEvents = function(orchestrator) {

            document.getElementById("id_formbutton").addEventListener(
                "click", (e) => {

                    e.preventDefault();
                    let form  = e.target.closest("form");
                    let self = this;

                    if(form.checkValidity()) {
                        makeCall("POST", "CreateCategory", e.target.closest("form"),
                            function (x) {
                                if(x.readyState == XMLHttpRequest.DONE) {
                                    let message = x.responseText;

                                    switch (x.status) {
                                        case 200:
                                            orchestrator.refresh();
                                            self.alert.textContent = message;
                                            break;

                                        case 403:
                                            window.location.href = req.getResponseHeader("Location");
                                            window.sessionStorage.removeItem('username');
                                            break;

                                        default:
                                            // 400 bad request, 401 unauthorized, 500 server error
                                            self.alert.textContent = message;
                                            break;

                                    }
                                }

                            });
                    } else {
                        form.reportValidity();
                    }
                }
            );

        }

        this.reset = function () {
            document.getElementById("id_formdiv").style.display = "none";
        }

        this.show = function() {

            let self = this;

            document.getElementById("id_formdiv").style.display = "block";

            makeCall("GET", "GetCategoryFormData", null, function(req) {

                if(req.readyState == 4) {
                    let message = req.responseText;

                    if(req.status == 200) {

                        let optionsToShow = JSON.parse(message);

                        if(optionsToShow.length == 0) {
                            self.alert.textContent = "There are no categories present!";
                            return;
                        }
                        self.update(optionsToShow);

                    } else if(req.status == 403) {
                        window.location.href = req.getResponseHeader("Location");
                        window.sessionStorage.removeItem("userinfo");

                    } else {
                        self.alert.textContent = message;
                    }
                }
            });
        }

        this.update = function(arrayOptions) {

            let selection = document.getElementById("id_selection");
            selection.innerHTML = "";

            arrayOptions.forEach((element) => {
               let opt = document.createElement("option");
                opt.value = element.id;
                opt.text = element.id;
                selection.appendChild(opt);
            })

        }


    }

    /* The dragstart event is fired when the user starts dragging an element (if it is draggable=True) */
    function dragStart(event,alert) {
        /* we need to save in a variable the row that provoked the event
         to then copy it to the new position */
        startUl = event.target.closest("ul");
        startElement = event.target.closest("li");

        alert.textContent = "";
    }


    /* The dragover event is fired when an element is being dragged over a valid drop target. */
    function dragOver(event) {
        // We need to use prevent default, otherwise the drop event is not called
        event.preventDefault();

        let dest = event.target.closest("li");

        // Mark  the current element as "selected", then with CSS we will put it in red
        dest.className = "selected";
    }


    /* The dragleave event is fired when a dragged element leaves a valid drop target. */
    function dragLeave(event) {
        // We need to select the row that triggered this event to marked as "notselected" so it's clear for the user
        let dest = event.target.closest("li");

        // Mark  the current element as "notselected", then with CSS we will put it in black
        dest.className = "notselected";
	    }
	
	    /* The drop event is fired when an element or text selection is dropped on a valid drop target. */
	    function drop(event) {
	    let destLi = event.target.closest("li");
	    let destUl = destLi.lastChild;
	
	    destLi.className = "notselected";
	    startElement.className = "notselected";
	
	    if (!startElement.contains(destUl) &&  !destLi.getAttribute("categoryid").startsWith(startElement.getAttribute("categoryid"))) {
	        // Clonare l'elemento anziché spostarlo
	        let clonedElement = startElement.cloneNode(true);
	        destUl.appendChild(clonedElement);
	
	        let fcatId = destLi.getAttribute("categoryid");
	        let cid = clonedElement.getAttribute("categoryid");
	
	        if (confirm("Do you want to confirm the copy?")) {
	            updateLocalList([fcatId, cid]);
	
	            confirmButton.show();
	            creationForm.reset();
	        }
	    } else {
	        alert("Invalid operation!");
	    }
	}


    function updateLocalList(newElement) {
        let modifiedList = JSON.parse(sessionStorage.getItem("modList"));
        modifiedList.push(newElement);
        sessionStorage.setItem("modList",JSON.stringify(modifiedList));

        console.table(modifiedList);
    }

    
})();