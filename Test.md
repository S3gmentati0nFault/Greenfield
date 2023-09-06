<h1>Testing condotto</h1>

Questi sono alcuni dei processi di test che sono stati condotti durante lo 
sviluppo. <br>
Da questo punto in poi, con rimozione incontrollata si intenderà la rimozione di un 
robot dal sistema utilizzando il pulsante di stop dell'editor, con rimozione controllata
si intende invece la rimozione di un robot dal sistema usando l'apposito comando di 
QUIT.

<h3>Test di eliminazione multipla con controllo su redirezione MQTT</h3>
    
    1) Inizializzazione di un processo AdminServer
    2) Inizializzazione di un processo AdminClient
    3) Inizializzazione di 4 processi CleaningBot
    4) Si lasci passare un po' di tempo (e.g. 1 minuto) controllando un po' di log
    5) Rimozione incontrollata di due robot
        5a) Controllare che la rimozione abbia provocato la stabilizzazione della struttura 
        dati
        5b) Controllare che la redirezione di MQTT sia avvenuta nel modo corretto
    6) Rimozione incontrollata di un altro robot (dopo che la stabilizzazione è avvenuta)
        6a) Controllare che la distribuzione sia ancora uniforme
    7) Rimozione controllata dell'ultimo robot
        7a) Controllare tramite l'AdminClient che la rete sia effettivamente vuota

<h3>Test di aggiunta con rumore</h3>

    1) Settare Variables.BOT_THREAD_DEBUGGING = true
    2) Inizializzazione di un processo AdminServer
    3) Inizializzazione di un processo AdminClient
    4) Inizializzare due processi CleaningBot
        4a) Quando si palesa la stampa "DO SOMETHING NOW" inizializzare altri due processi 
        CleaningBot
        4b) Lasciare che le cose facciano il loro corso e controllare che non vi siano errori
    5) Rimozione incontrollata di 3 robot
        5a) Controllare che tutto sia in ordine sia lato admin che lato rete
    6) Inizializzare un processo CleaningBot
        6a) Quando si palesa la stampa "DO SOMETHING NOW" eseguire la rimozione incontrollata 
        dell'ultimo robot presente in precedenza
        6b) Inizializzare un processo CleaningBot
        6c) Lasciare che le cose facciano il loro corso
    7) Inizializzare un ultimo processo CleaningBot
        7a) Quando si palesa la stampa "DO SOMETHING NOW" rimuovere tutti i robot presenti in 
        precedenza e lasciare che le cose facciano il loro corso
    8) Rimozione controllata dell'ultimo robot
        8a) Controllare tramite l'AdminClient che la rete sia effettivamente vuota

<h3>Test specifico di aggiunta post rimozione</h3>
Test utile a controllare che la distribuzione venga stabilizzata anche in fase di aggiunta di nuovi 
robot (dopo l'eliminazione di alcune entità)

    1) Settare Variables.BOT_THREAD_DEBUGGING = true
    2) Inizializzazione di un processo AdminServer
    3) Inizializzazione di un processo AdminClient
    4) Inizializzazione di due processi CleaningBot
      4a) Consentire ai processi di stabilizzarsi
    5) Rimozione incontrollata di entrambi i processi
      5a) A questo punto i processi non sono più presenti all'interno della rete, 
          però le loro informazioni sono ancora contenute nell'AdminServer, 
          visto che nessuno lo ha notificato della loro morte
    6) Inizializzare altri due processi CleaningBot, uno leggermente dopo l'altro
      6a) Consentire ai processi di stabilizzarsi e controllare che la rete sia 
          in uno stato coerente

<h3>Test specifico stile proposto in esame</h3>
Test simile a quello proposto in esame

    1) Inizializzazione di un processo AdminServer
    2) Inizializzazione di un processo AdminClient
    3) Inizializzazione di tre processi CleaningBot
        3a) Consentirne l'avviamento e controllare che il processo AdminClient contenga le informazioni corrette
    4) Utilizzare il comando FIX per mandare tutti i processi in manutenzione
        4a) Controllare i log per dimostrare che i processi non si pestano i piedi tra di loro
        4b) Rimozione controllata di uno dei processi CleaningBot
    5) Rimozione incontrollata di uno dei processi CleaningBot
        5a) Mostrare che la distribuzione è ancora stabile e che la rimozione è stata propagata

In alternativa i passi (5) e (4b) possono essere fusi insieme per fare un test leggermente diverso e un po' più aggressivo

<h3>Test di morte in fase di rimozione</h3>
In questo test si possono utilizzare, sia la rimozione incontrollata che la rimozione controllata (in quanto quest'ultima 
ha un effetto simile al processo di rimozione incontrollata se il robot non è in manutenzione).

    1) Settare Variables.BOT_THREAD_DEBUGGING = true
    2) Inizializzazione di un processo AdminServer
    3) Inizializzazione di un processo AdminClient
    4) Inizializzazione di sei processi CleaningBot
        4a) Dare il tempo ai processi di stabilizzarsi, controllare i log
    5) Eseguire rimozione di un bot tra i primi del gruppo, questo perchè, per come è costruita la distribuzione, questa sarà maggiormente instabile quando si eliminano i robot che vengono prima
    6) Identificare i robot che stanno modificando la propria posizione all'interno della rete
        6a) Rimuoverne almeno uno dopo che ha notificato il cambiamento della propria posizione ad almeno un paio di robot
    7) Controllare lo stato dei log.

<h3>Boss test di eliminazione e stabilizzazione</h3>
Test abbastanza critico di eliminazione e stabilizzazione della distribuzione
    
    1) Inizializzazione di un processo AdminServer
    2) Inizializzazione di un processo AdminClient
    3) Inizializzazione di quattro processi CleaningBot
    4) Rimozione incontrollata dei processi CleaningBot
    5) Aggiunta di cinque processi CleaningBot