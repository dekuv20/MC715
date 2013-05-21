Andr� Vasconcellos 080664
Guilherme Lanna    083597
Felipe Rosa 	   083499

---------- Marido Apressado ------------

- Compila��o

Para a compila��o � necess�rio o arquivo .jar do zookeeper

javac -cp ../zookeeper-3.4.5.jar:../lib/*:../conf:. -Xlint:deprecation *.java

- Execu��o do sistema utilizando Barreira Simples

java -cp zookeeper-3.4.5.jar:lib/*:conf:. casamento.TesteBarreiraSimplesMulher <host_cluster>:<port> <parametro> <porta_desejada>

Ex.
java -cp zookeeper-3.4.5.jar:lib/*:conf:. casamento.TesteBarreiraSimplesMulher cluster1.lab.ic.unicamp.br:35858 0 40040
java -cp zookeeper-3.4.5.jar:lib/*:conf:. casamento.TesteBarreiraSimplesMarido cluster1.lab.ic.unicamp.br:35858 start 40040

- Execu��o do sistema utilizando Barreira Dupla

java -cp zookeeper-3.4.5.jar:lib/*:conf:. casamento.TesteBarreiraSimplesMulher <host_cluster>:<port> <parametro> <porta_desejada>

Ex.
java -cp zookeeper-3.4.5.jar:lib/*:conf:. casamento.TesteBarreiraDuplaMulher cluster1.lab.ic.unicamp.br:35858 4 40050
java -cp zookeeper-3.4.5.jar:lib/*:conf:. casamento.TesteBarreiraDuplaMarido cluster1.lab.ic.unicamp.br:35858 4 40050
java -cp zookeeper-3.4.5.jar:lib/*:conf:. casamento.TesteBarreiraDuplaMarido cluster1.lab.ic.unicamp.br:35858 4 40060
java -cp zookeeper-3.4.5.jar:lib/*:conf:. casamento.TesteBarreiraDuplaMulher cluster1.lab.ic.unicamp.br:35858 4 40060

- Organiza��o dos arquivos do diret�rio

-- BarreiraDupla.java
Arquivo com a implementa��o das primitivas de barreira dupla

-- BarreiraSimples.java
Arquivo com a implementa��o das primitivas de barreira simples

-- Mulher.java
Arquivo que realiza as altera��es de uma pessoa (Mulher) que ir� casar e receber o sobrenome do marido

-- Pessoa.java
Defini��o dos m�todos e atributos de uma pessoa que ir� casar
: casar()
: getPort()
: setEstadoCivilCasado()
: setEstadoCivilSolteiro()
: setEstadoCivilViuvo()
: getEstadoCivil()
: getNome()
: getSobrenome()
: setNome()
: setSobrenome()

-- SyncPrimitive.java
Arquivo que realiza a sincroiza��o das primitivas de barreira

-- TesteBarreiraDuplaMarido.java
-- TesteBarreiraDuplaMulher.java
-- TesteBarreiraSimplesMarido.java
-- TesteBarreiraSimplesMulher.java

Arquivos para teste de execu��o