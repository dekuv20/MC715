Andr� Vasconcellos 080664
Guilherme Lanna    083597
Felipe Rosa 	   083499

Barreira Dupla Reutiliz�vel
--------------------------------------------

	Para garantir a reutiliza��o da barreira um processo checa se existe um Node "ready", caso exista o processo
cria um "watcher" que o avisa quando o Node deixar de existir somente entao o processo pode entrar na barreira.
No final de cada execucao, apos ter deletado todos os nodes representantes de cada Node cada node antes de sair tenta
deletar o Node "ready" caso ele ainda exista.
	As bibliotecas de terceiros necess�rias j� est�o inclu�das no reposit�rio.


---------- Marido Apressado ------------

- Compila��o

Para a compila��o � necess�rio o arquivo .jar as pastas /lib e /conf do zookeeper

javac -cp ../zookeeper-3.4.5.jar:../lib/*:../conf:. -Xlint:deprecation *.java

- Execu��o do teste utilizando Barreira Dupla Reutiliz�vel

java -cp zookeeper-3.4.5.jar:lib/*:conf:. casamento.TesteBarreiraSimplesMulher <host_cluster>:<porta> <tamanho_barreira> <porta_socket> <#_iteracoes>

- Para o teste simples:

java -cp zookeeper-3.4.5.jar:lib/*:conf:. casamento.SimpleTest <host_cluster>:<porta> <tamanho_barreira> 

Ex.
java -cp zookeeper-3.4.5.jar:lib/*:conf:. casamento.TesteBarreiraDuplaMulher cluster1.lab.ic.unicamp.br:35858 4 40050 4
java -cp zookeeper-3.4.5.jar:lib/*:conf:. casamento.TesteBarreiraDuplaMarido cluster1.lab.ic.unicamp.br:35858 4 40050 4
java -cp zookeeper-3.4.5.jar:lib/*:conf:. casamento.TesteBarreiraDuplaMarido cluster1.lab.ic.unicamp.br:35858 4 40060 4
java -cp zookeeper-3.4.5.jar:lib/*:conf:. casamento.TesteBarreiraDuplaMulher cluster1.lab.ic.unicamp.br:35858 4 40060 4

- Organiza��o dos arquivos do diret�rio

-- SyncPrimitive.java
Arquivo que implementa as primitivas de sincroniza��o que ser�o usadas pela barreira.

-- BarreiraDupla.java
Arquivo com a implementa��o da classe barreira dupla.

-- Pessoa.java
Arquivo com a implementa��o da classe "Pessoa" que representa uma pessoa que ser� utilizada para os testes.

-- Mulher.java
Arquivo a implementacao de uma classe filha da classe "Pessoa" que representa uma mulher que ir� casar e receber o 
sobrenome do marido nos testes.
   
-- TesteBarreiraDuplaMarido.java
Cria um processo representando um Marido.

-- TesteBarreiraDuplaMulher.java
Cria um processo representando um Marido.

-- SimpleTest.java
Teste simples contendo opera��es irrelevantes apenas para testar o funcionamento da barreira.

Arquivos para teste de execu��o

