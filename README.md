# aes
=====

Oficial repository of Web based alcohol intervention developed by CREPEIA | UFJF

**Current development branch**

homologation

**Dependencies**

You will need the following materials to study and deploy the project:

- [Netbeans IDE 12.0](https://netbeans.apache.org/download/index.html)
- [Java JDK - 8 u241](https://www.oracle.com/java/technologies/javase/javase8u211-later-archive-downloads.htm)
- [Glassfish Server 5.0.1](https://javaee.github.io/glassfish/download)
- [MySQL 5.7](https://dev.mysql.com/downloads/windows/installer/8.0.html)

*It’s very important to use the versions stated above, since otherwise the system may not work.*

**Notes**

- Branch *master*: Production branch. Used on website's project live version.
- Branch *homologation*: Work branch. Used to development.
- Branch *gh-pages*: Currently unused.

## Integração com o chatbot

O app solicita sugestões em `POST /aes/webresources/chatbotinteraction/generate`, com Bearer individual
e corpo `{"messagePacienteId":123}`. O backend verifica o consultor e seu vínculo com o paciente,
busca o contexto do chat e chama a API como serviço.

Configurar no processo GlassFish `CHATBOT_BASE_URL`, `CHATBOT_SERVICE_TOKEN` e, somente na transição HTTP,
`CHATBOT_ALLOW_HTTP=true`. Alternativamente, usar `aes.chatbot.baseUrl`, `aes.chatbot.serviceToken`
e `aes.chatbot.allowHttp`. O Java não lê `.env` automaticamente. O segredo deve coincidir com o da API
e nunca ser configurado no app. HTTP não oferece confidencialidade em trânsito.

Testes locais: `powershell -NoProfile -ExecutionPolicy Bypass -File aes/test/run-authentication-tests.ps1`.
O script permite ajustar os caminhos dos módulos GlassFish e da API de anotações Java.
