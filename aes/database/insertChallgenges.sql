INSERT INTO aes.tb_challenge
(id,base_value,description,modifier,title,type)
VALUES
('1', '5', 'Finalize o Cadastro no Site', '0', ' Cadastro', 'ONCE'),
('2', '10', 'Completar o plano de mudança', '0', 'Plano de Mudança', 'ONCE'),
('3', '1', 'Ler uma dica Recebida', '1', 'Ler Dicas', 'DAILY'),
('4', '20', 'Utilizar o chat para tirar dúvidas ou pedir ajuda', '0', 'Chat', 'ONCE'),
('5', '5', 'Informe o número de doses consumidas diariamente', '1', 'Informar Doses Diariamente', 'DAILY'),
('6', '5', 'Não consumir bebidas alcoólicas durante o dia', '2', 'Não beber', 'DAILY'),
('7', '10', 'Enviar uma sugestão pela primeira vez', '0', 'Enviar Sugestão', 'ONCE'),
('8', '10', 'Entre no Ranking pela primeira vez', '0', 'Entrar no Ranking', 'ONCE'),
('9', '10', 'Convide amigos para o aplicativo', '0', 'Convidar amigos', 'ACCUMULATIVE'),
('10', '10', 'Comparecer à reunião', '0', 'Entre no chat no momento de um agendamento', 'ACCUMULATIVE');
