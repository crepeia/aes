/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

let has_completed_recaptcha = false;
const numberList = [];

function validateRecaptcha(number) {
    if(has_completed_recaptcha) {
        // ReCAPTCHA preenchido.
        return true; // Permite o envio do formulário.
    } else {
        // ReCAPTCHA não preenchido.
        // Exibe a mensagem de erro
        if(!numberList.includes(number)) {
            const recaptchaErrorContainer = document.getElementById("recaptcha-error-container-" + number);
            recaptchaErrorContainer.classList.add("ui-message");
            recaptchaErrorContainer.classList.add("ui-message-error");
            recaptchaErrorContainer.classList.add("ui-widget");
            recaptchaErrorContainer.classList.add("ui-corner-all");
            const recaptchaErrorIcon = document.createElement("span");
            recaptchaErrorIcon.classList.add("ui-message-error-icon");
            const recaptchaErrorMessage = document.createElement("span");
            recaptchaErrorMessage.classList.add("ui-message-error-detail");
            recaptchaErrorMessage.innerText = "Por favor, clique em 'Não sou um robô' para completar o reCAPTCHA";
            recaptchaErrorContainer.appendChild(recaptchaErrorIcon);
            recaptchaErrorContainer.appendChild(recaptchaErrorMessage);
            numberList.push(number);
        }
        return false; // Impede o envio do formulário.
    }
}

function onRecaptchaSuccess(token) {
    has_completed_recaptcha = true;
}


