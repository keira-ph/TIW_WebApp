/**
 * Login management
 */

(function() { // avoid variables ending up in the global scope

    document.getElementById("loginbutton").addEventListener('click', (e) => {

        e.preventDefault();
        let form = e.target.closest("form");

        // In questo caso checkValidity controlla se rispetta"required"
        if (form.checkValidity()) {

            // Crea una XMLHttpRequest
            makeCall("POST", 'CheckLogin', form, function(x) {

                if (x.readyState == XMLHttpRequest.DONE) {
					var message = x.responseText;

                    switch (x.status) {

                        case 200:
                            // Per poi mostrare il nome dell'utente nella home page
                            sessionStorage.setItem("userinfo", message);

                            // Reindirizzamento dell'utente
                            window.location.href = "HomePage.html";
                            break;

                        case 400: // bad request
                            document.getElementById("errormessage").textContent = message;
                            break;

                        case 401: // unauthorized
                            document.getElementById("errormessage").textContent = message;
                            break;

                        case 500: // server error
                            document.getElementById("errormessage").textContent = message;
                            break;
                    }
                }
            });

        } else {
            // Funzione automatica per il messaggio di errore (checkValidity fallito)
            form.reportValidity();
        }
    });

})();