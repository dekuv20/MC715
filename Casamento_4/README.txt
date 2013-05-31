André Vasconcellos 080664
Guilherme Lanna    083597
Felipe Rosa 	   083499

Barreira Dupla Reutilizável e Restrita
--------------------------------------------

	Para garantir a reutilização da barreira um processo checa se existe um Node "ready", caso exista o processo
cria um "watcher" que o avisa quando o Node deixar de existir somente entao o processo pode entrar na barreira.
No final de cada execucao, apos ter deletado todos os nodes representantes de cada Node cada node antes de sair tenta
deletar o Node "ready" caso ele ainda exista.
	Para garantir que a barreira seja restrita existe um Node "queue" onde cada processo quando quer entrar na 
barreira cria um Node filho nele com um número sequencial que sera seu ID na fila e só consegue seguir para a próxima 
parte da barreira caso seu ID seja um dos N primeiros da fila, onde N é o tamanho da barreira. No final da execução
o processo deleta seu node na fila logo após deletar seu node que representa o processo garantindo assim que a fila
anda.
	Para testar a restritividade basta rodar um número de processos maior que o tamanho da barreira.
	As bibliotecas de terceiros necessárias já estão incluídas no repositório.

---------- Marido Apressado ------------

- Compilação

Para a compilação é necessário o arquivo .jar as pastas /lib e /conf do zookeeper

javac -cp ../zookeeper-3.4.5.jar:../lib/*:../conf:. -Xlint:deprecation *.java

- Execução do teste utilizando Barreira Dupla Reutilizável

java -cp zookeeper-3.4.5.jar:lib/*:conf:. casamento.TesteBarreiraSimplesMulher <host_cluster>:<porta> <tamanho_barreira> <porta_socket> <#_iteracoes>

- Para o teste simples:

java -cp zookeeper-3.4.5.jar:lib/*:conf:. casamento.SimpleTest <host_cluster>:<porta> <tamanho_barreira> 

Ex.
java -cp zookeeper-3.4.5.jar:lib/*:conf:. casamento.TesteBarreiraDuplaMulher cluster1.lab.ic.unicamp.br:35858 4 40050 4
java -cp zookeeper-3.4.5.jar:lib/*:conf:. casamento.TesteBarreiraDuplaMarido cluster1.lab.ic.unicamp.br:35858 4 40050 4
java -cp zookeeper-3.4.5.jar:lib/*:conf:. casamento.TesteBarreiraDuplaMarido cluster1.lab.ic.unicamp.br:35858 4 40060 4
java -cp zookeeper-3.4.5.jar:lib/*:conf:. casamento.TesteBarreiraDuplaMulher cluster1.lab.ic.unicamp.br:35858 4 40060 4

- Organização dos arquivos do diretório

-- SyncPrimitive.java
Arquivo que implementa as primitivas de sincronização que serão usadas pela barreira.

-- BarreiraDupla.java
Arquivo com a implementação da classe barreira dupla.

-- Pessoa.java
Arquivo com a implementação da classe "Pessoa" que representa uma pessoa que será utilizada para os testes.

-- Mulher.java
Arquivo a implementacao de uma classe filha da classe "Pessoa" que representa uma mulher que irá casar e receber o 
sobrenome do marido nos testes.
   
-- TesteBarreiraDuplaMarido.java
Cria um processo representando um Marido.

-- TesteBarreiraDuplaMulher.java
Cria um processo representando um Marido.

-- SimpleTest.java
Teste simples contendo operações irrelevantes apenas para testar o funcionamento da barreira.

Arquivos para teste de execução

