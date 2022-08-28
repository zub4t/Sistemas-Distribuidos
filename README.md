# Trabalho Pratico de
## YouTubeLink https://youtu.be/6tU0XRJWxV4 and https://youtu.be/642eT2xISpM
### Build
```
mvn clean 
mvn install 
mvn package
```

# Sistemas Distribu ́ıdos

# – 2021/22 –

Usando os templates que lhe foram fornecidos nas aulas de laborat ́orio como ponto de
partida, implemente prot ́otipos de aplica ̧c ̃oes para os seguintes cen ́arios.

## 1. Algoritmo de “Token Ring”


1. cap ́ıtulo 6 de van Steen e Tanenbaum (“Token Ring Algorithm”);
2. crie um anel com 5 “peers” (p1ap5) como o representado na figura anterior;
3. note que cada “peer” deve estar numa m ́aquina (m1am5) no laborat ́orio FC6 008;
4. cada “peer” tem apenas o IP do seguinte (em5tem o IP de m1 para fechar o anel);


5. o cliente em cada “peer” tem uma shell que executa dois comandos poss ́ıveis:lock()
    eunlock();
6. um outro thread no “peer” executa um loop cont ́ınuo em que espera por uma men-
    sagem (que s ́o poder ́a vir do IP anterior) e envia-a para o n ́o que se segue no anel;
7. a mensagem (a “token”) ́e constitu ́ıda apenas por um inteiro;
8. o valor inicial da “token” ́e 0 e ́e incrementado de uma unidade por cada “peer” em
    que passa;
9. sempre que recebem a “token” os “peers” escrevem-na;
10. quando a shell executalock()esse processo ́e interrompido, ficando a “token” no n ́o
actual at ́e o cliente executarunlock().

## 2. Rede P2P com “Push”, “Pull” e “Push+Pull”


1. cap ́ıtulo 4 de van Steen e Tanenbaum (“Information Dissemination Models”);
2. crie uma rede com 6 “peers” (p1ap6) correndo em outras tantas m ́aquinas,m1a
    m6, no laborat ́orio FC6 008) como a topologia representada na figura anterior;
3. cada “peer” tem um cliente com uma shell que executa apenas 4 m ́etodos (ver abaixo);
4. a constru ̧c ̃ao da rede deve ser feita “peer” a “peer”, usando mensagens do tiporegis-
    ter()(ver abaixo);


5. cada “peer” mant ́em uma tabela com os IPs dos n ́os que se registaram com ele;
6. cada “peer” mant ́em tamb ́em um dicion ́ario de palavras que v ̃ao sendo geradas perio-
    dicamente (e.g., arranjem um dicion ́ario online e periodicamente retirem uma palavra
    `a sorte e insiram no vosso dicion ́ario - a estrutura de dados);
7. os comandos da shell (correspondentes `a API do servidor) s ̃ao (assumindo o cliente
    emip1):
       - register(ip2) - que regista o “peer” actual com o “peer” emip2 (se for bem
          sucedido a tabela deip1fica comip2, a tabela deip2fica comip1);
       - push(ip2)- o cliente envia paraip2o conteudo do seu dicion ́ario eip2actualiza
          as entradas em falta no seu dicion ́ario;
       - pull(ip2)- o cliente pede aip2o conte ́udo do seu dicion ́ario e actualiza as entradas
          em falta no seu dicion ́ario;
       - pushpull(ip2)- faz as duas opera ̧c ̃oes anteriores (os dicion ́arios dos dois “peers”
          ficam sincronizados temporariamente).

## 3. Algoritmo de “Reliable Totally Ordered Multicast”



1. cap ́ıtulo 6 de van Steen e Tanenbaum (“Lamport Clocks” e “Totally Ordered Multi-
    cast”);
2. crie uma rede com 4 “peers” (p1ap4) noutras tantas m ́aquinas do laborat ́orio FC
    008 (m1am4) com a topologia representada na figura anterior;


3. cada “peer” tem uma tabela com os IPs de todos os restantes;
4. cada cliente de um “peer” corre uma shell que apenas recebe uma linha de texto e a
    envia para todos os outros “peers”, tal e qual numa aplica ̧c ̃ao do tipo “chat”;
5. o servidor em cada “peer” recebe o texto de clientes e imprime-o no monitor com o
    seu “timestamp” (ver abaixo sobre isto);
6. note que o problema ́e garantir que todos os “peers” vˆem as mensagens pela mesma
    ordem;
7. implemente um Rel ́ogios de Lamport em cada “peer” para marcar as mensagens com
    “timestamps” relativos;
8. implemente o algoritmo de “Reliable Totally Ordered Multicast” (veja aqui uma
    descri ̧c ̃ao detalhada).

## Condi ̧c ̃oes Gerais

O trabalho pr ́atico deve ser realizado individualmente ou em grupo (2 pessoas no m ́aximo).
A constitui ̧c ̃ao do grupo, com 1 ou 2 pessoas, deve ser comunicada at ́e 12 de Novembro, via
e-mail (nome completo dos membros e os respectivos n ́umeros mecanogr ́aficos). Sugiro que
use Java para a implementa ̧c ̃ao. Se preferir outra linguagem fale comigo para eu validar.
Pode usar sockets ou gRPC (eventualmente as 2 em aplica ̧c ̃oes diferentes). Assumindo a
utiliza ̧c ̃ao do Java, o software produzido deve ser organizado em 3 packages:

- ds.trabalho.parte
- ds.trabalho.parte
- ds.trabalho.parte

O trabalho dever ́a ser entregue at ́e `a data limite de 7 de Janeiro de 2022. Na semana
seguinte ser ́a feita a sua apresenta ̧c ̃ao com a presen ̧ca obrigat ́oria de todos os elementos
do grupo. A entrega ́e feita enviando paralblopes@dcc.fc.up.ptum ficheiro.ZIPcom es-
tas packages e um ficheiroREADME.md (formato Markdown, verifiquem o output num
browser) que, para al ́em do nome completo dos membros do grupo e respectivo n ́umero
mecanogr ́afico, deve conter uma descri ̧c ̃ao de como compilar e executar cada uma das
aplica ̧c ̃oes.

```
Bom trabalho,
```
```
Lu ́ıs Lopes
```

