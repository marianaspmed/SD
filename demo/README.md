# Guião de Demonstração

## 1. Preparação do sistema

Para testar o sistema e todos os seus componentes, é necessário preparar um ambiente com dados para proceder à verificação dos testes.
###### 1.1. Lançar o registry

Para lançar o ZooKeeper, ir à pasta zookeeper/bin e correr o comando
./zkServer.sh start (Linux) ou zkServer.cmd (Windows).

É possível também lançar a consola de interação com o ZooKeeper, novamente na pasta zookeeper/bin e correr ./zkCli.sh (Linux) ou zkCli.cmd (Windows).
###### 1.2. Compilar o projeto

Primeiramente, é necessário compilar e instalar todos os módulos e suas dependências -- rec, hub, app, etc. Para isso, basta ir à pasta root do projeto e correr o seguinte comando:

$ mvn clean install -DskipTests

###### 1.3. Lançar e testar o rec

Para proceder aos testes, é preciso em primeiro lugar lançar o servidor rec . Para isso basta ir à pasta rec e executar:

$ mvn compile exec:java "-Dexec.args=localhost 2181 localhost 8091 1"

Este comando vai colocar o rec no endereço localhost e na porta 8091.

Para confirmar o funcionamento do servidor com um ping, fazer:

$ cd rec-tester  
$ mvn compile exec:java

Para executar toda a bateria de testes de integração, fazer:

$ mvn verify

Todos os testes devem ser executados sem erros.

###### 1.4. Lançar e testar o hub

Para lançar e testar o hub é necessário que o servidor rec esteja a correr.

Para proceder aos testes, é preciso em primeiro lugar lançar o servidor hub . 
Os ficheiros users.csv e stations.csv com informação sobre os utilizadores e estações encontram-se na pasta do hub.
Para isso basta ir à pasta hub e executar:

$ mvn exec:java -Dexec.args="localhost 2181 localhost 8081 1 users.csv stations.csv initRec"

Este comando vai colocar o hub no endereço localhost e na porta 8081 e vai-se ligar ao rec que está a correr na porta 8091 através do zookeeper
O comando vai também consumir os ficheiros users e stations e guardar a informação tanto no hub como no rec.

Para confirmar o funcionamento do servidor com um ping, fazer:

$ cd hub-tester  
$ mvn compile exec:java

Para executar toda a bateria de testes de integração, fazer:

$ mvn verify

Todos os testes devem ser executados sem erros.

###### 1.5. Lançar a App

Iniciar a aplicação com a utilizadora alice:

$ mvn compile exec:java "-Dexec.args=localhost 2181 alice +35191102030 38.730 -9.3000"

Nota: Para poder correr o script app diretamente é necessário fazer mvn install e adicionar ao PATH ou utilizar diretamente os executáveis gerados na pasta target/appassembler/bin/.

Depois de lançar todos os componentes, tal como descrito acima, já temos o que é necessário para usar o sistema através dos comandos.

## 2. Teste dos comandos

Nesta secção vamos correr os comandos necessários para testar todas as operações do sistema. Cada subsecção é respetiva a cada operação presente no hub.  
###### 2.1. balance

&gt; balance  
alice 0 BIC

&gt; balance 1  
alice 0 BIC

###### 2.2 top-up

&gt; top-up 10  
alice 100 BIC

&gt; top-up 25  
alice 100 BIC

###### 2.3 tag

&gt; tag 38.7376 -9.3031 loc1  
OK

&gt; tag abc abc loc2  
Inputs inválidos

###### 2.3 at

&gt; at  
alice em https://www.google.com/maps/place/38.73, -9.30

###### 2.4 scan

&gt; scan 2  
istt, lat 38.7372, -9.3023 long, 20 docas,4BIC prémio, 12 bicicletas, a 82.0 metros
stao, lat 38.6867, -9.3124 long, 30 docas,3BIC prémio, 20 bicicletas, a 5717.0 metros

###### 2.5 move

&gt; move loc1  
alice em https://www.google.com/maps/place/38.7376,-9.3031

&gt; move a  
Tag não existente

###### 2.6 bike-up  

&gt; tag 38.7633 -9.0950 loc2  
OK

&gt; move loc2  
alice em https://www.google.com/maps/place/38.7633, -9.0950

&gt; bike-up istt  
ERRO fora de alcance

&gt; bike-up ocea  
OK

&gt; bike-up istt  
ERRO utilizador ja se encontra com uma bicicleta

###### 2.7 bike-down

&gt; bike-down istt  
ERRO fora de alcance

&gt; bike-down ocea  
OK

&gt; bike-down ocea  
ERRO utilizador nao levantou uma bicicleta

###### 2.8 info

&gt; info istt  
IST Taguspark, lat 38.7372, -9.3023 long, 20 docas, 4 BIC prémio, 12 bicicletas, 22 levantamentos, 7 devoluções, https://www.google.com/maps/place/38.7372,-9.3023

&gt; info abc  
Estação inexistente

###### 2.9 ping

&gt; ping  
UP

###### 2.10 fechar App

&gt; exit

## 3. Réplicas

Lançar duas réplicas do servidor rec:

$ mvn compile exec:java "-Dexec.args=localhost 2181 localhost 8092 2"

Este comando vai colocar o rec no endereço localhost e na porta 8092.

$ mvn compile exec:java "-Dexec.args=localhost 2181 localhost 8093 3"

Este comando vai colocar o rec no endereço localhost e na porta 8093.

Para confirmar o funcionamento do servidor com um ping, fazer:

$ cd rec-tester  
$ mvn compile exec:java

Para executar toda a bateria de testes de integração, fazer:

$ mvn verify

Depois de lançar as réplicas será necessário correr novamente o hub. Para tal desliga-se o hub que se encontra de momento a correr
e lança-se novo hub com o mesmo comando na pasta hub:

$ mvn exec:java -Dexec.args="localhost 2181 localhost 8081 1 users.csv stations.csv initRec"

Este comando vai colocar o hub no endereço localhost e na porta 8081 e vai-se ligar aos recs que está a correr na porta 8091, 8092 e 8093 através do zookeeper
O comando vai também consumir os ficheiros users e stations e guardar a informação tanto no hub como nos recs.

Confirmar no hub as mensagens que indicam a que recs se ligou.

Para confirmar o funcionamento do servidor com um ping, fazer:

$ cd hub-tester  
$ mvn compile exec:java

Para executar toda a bateria de testes de integração, fazer:

$ mvn verify



## 4. Considerações Finais

Estes testes não cobrem tudo, pelo que devem ter sempre em conta os testes de integração e o código.