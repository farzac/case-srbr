											Case-SRBR



O serviço vai receber dados provindos de integração REST e efetuar a autenticação do usuário em um servidor LDAP.



Passos para testes:



Deve fazer o checkout desse projeto (Case-srbr) e seguir o passo abaixo:

git checkout https://github.com/farzac/case-srbr.git
cd case-srbr
sudo chmod 777 mvnw
./mvnw spring-boot:run




Após checkout do projeto no github, será preciso pegar imagem do docher que contém o servidor LDAP

docker run -i -t --net="host" zaccantte/ubuntu-16.04-case-srbr
/etc/init.d/slapd start



Para testes, será preciso utilizar o Postman

https://www.getpostman.com/downloads/




Feito o download do Postan, informar o seguinte endereço:

http://127.0.0.1:8181/login?user=user1&passwd=1234




Em caso de informar um usuario diferente de user1 e senha 1234, será consultado e confirmado que este usuário
não existe na base do LDAP e retornado atraves do seriço Rest a mensagem de Usuário inválido e se informar 
o user1 e senha 1234, o mesmo será encontrado na base do LDAP e a aplicação vai retornar usuário valido.




Para o controle de tempo de sessão, foi feito para que guarde a sessão por 60 segundos, enquanto a
sessão estiver ativa, não é validado o usuário no LDAP sendo apenas retornado que o usuario é valido. Após
sessão ficar inativa, a consulta volta a ser feito para o usuário na base do LDAP.




