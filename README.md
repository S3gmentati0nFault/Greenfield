<h1>Greenfield</h1>
<h2>Introduction</h2>
In this repository is contained all of the code for the project of Distributed and Pervasive Systems by Professor Ardagna and Professor Bettini at Università degli Studi di Milano, the project was presented in September 2023. In the README file is contained the analysis of the work I did as well as some informations regarding things that I would like to do in the future to make it better.<br>
Before beginning the explanation I would like to point out that throughout this file I will generally talk about robots and processes really freely, they are the same thing, threads are of course light-processes that are sons of the <code>Main</code> class that is associated to any Robot.
In the project we were required to simulate a smart city's cleaning system, we had a set of processes roaming around and collecting pollution data that were autonomously sent to the server, each of the robots had a 10 percent probability of having problems and needing to go to a mechanic for repairs. The mechanic was able to handle one repair process at a time thus the project needed to handle different requests arriving at the mechanic in parallel. The full project description can be found in ./project_assignement.
<h2>Structure of the solution</h2>
The following is the list of all the classes that I chose to implement for this project

- Main
  - Admin Client
  - Admin Server
    - Server Rest Interface
    - MQTT Subscriber
    - Bot Positions
    - Average List
  - Cleaning Bot
    - Bot Services
    - Service Comparator
    - Waiting Thread
    - Bot Thread
    - Eliminator Thread
    - Fix Helper Thread
    - GRPC Services Thread (GRPC server)
    - Input Thread
    - Maintenance Thread
    - Measurement Gathering Thread
    - Mutual Exclusion Thread
    - Pollution Sensor Thread
    - Quit Helper Thread
    - Bot Identity Comparator
    - Bot Utilities
    - Comm Pair
    - Measurement Buffer
    - Position
  - Extra
    - Atomic Counter
    - Atomic Flag
    - Logger
    - Thread Safe data structure wrapper
  - Simulator Code
  - Variables

My implementation is quite thread heavy, but I wanted to really experiment as much as possible with it, to get to know and understand how threads work and how to control them while accessing information together.
<h2>Operations</h2>
In the following I will discuss my implementation for the different operations.
<h3>Join</h3>
When a new process is created it starts the initiator thread, which is simply a thread that instanciates and starts all of the necessary services (e.g. the GRPC Services Thread). in the meanwhile it requests the server to join the network, the join operation currently consists of two parts:

  - Addition of the new robot to the Admin Server's local structure
  - Communication of personal data (i.e. an instance of the BotIdentity class) to all of the robots in the network

The GRPC communication is asynchronous, and I decided to do two different HTTP requests (check ID availability, communicate my existence to the network) to highlight the difference between the two tasks.
The position in the city is chosen by the Admin Server simply by checking in the system which one is the district with the lowest current density. Originally it was supposed to pick a random district uniformly, while the solution would tend to a stable distribution (if enough processes were initialized), it would not be stable for the majority of the low density cases.
<h4>Comm Pair</h4>
The <code>CommPair</code> class is a simple toy I came up with, it's nothing more than a pair containing a <code>ManagedChannel</code> and a <code>GRPCStub</code>. The idea behind it is simply to keep on recycling open stubs and channels; thus saving the resources necessary to open a new one each and every time a communication has to take place. Since the project is required to rely heavily on GRPC communication.
Comm Pairs are clustered into <code>ThreadSafeHashMaps</code>, each of the pairs is associated to the hash of the <code>BotIdentity</code> the <code>ManagedChannel</code> connects to.
<h3>Maintenance process</h3>
The maintenance process i s handled by two different threads, one deals with checking whether the robot should go under maintenance or not, the other built and initialized when the robot undergoes maintenance and handles communication and the maintenance procedure.<br>
The maintenance process uses a new thread to carry out the function, something that I didn't mention above was that, to handle potential concurrent removals and insertions, any operation involving the network to its full extent would be done by taking a snapshot of the network.<br>
The GRPC is asynchronous and carried out in parallel, any removals necessary are postponed until the time for maintenance is up, thus the removal and stabilization (which do not come cheap) are done at most once per process.
<h4>Pollution measurements</h4>
Pollution measurements (more about this later) stop during maintenance, originally they would still be sent, but I decided against it becasue it would not make much sense.
<h3>Removal</h3>
The solution I implemented to handle removals is overengineered and overkill for the job and the setup of the project.<br>
The reason behind it is that it was originally thought to straighten even the worst distributions (e.g. 1-6, 2-0, 3-0, 4-0), keep in mind that I originally placed robots in the city randomly.<br>
The elimination works this way:

  - I remove mentions of the dead robot from the local machine
  - I contact the Admin Server to let him know that there are dead robots in the city
  - Contact the other robots in the network to remove the references to dead robots from their systems
  - Stabilize the data structure

It's important to mention a couple of details
<h4>GRPC calls</h4>
For the elimination procedure, two different GRPC procedures have been called:

  - <code>moveRequest</code> -> which is used by the robot dealing with the elimination process to tell another robot to move away from a district
  - <code>positionModificationRequest</code> -> which is more like a notification, when a robot moves to another district computes a random position inside that district and lets every robot in the network know that its position is changed.

Whenever a robot receives a <code>moveRequest</code> the thread waits until the position is changed and the robots in the network have been notified to perform the <code>onComplete</code> and <code>onNext</code> operations.
<h4>How robots are chosen</h4>
During the elimination procedure it's important to make sure that everyone is on the same page, to be sure about that I created a setup phase before stabilization where an auxiliary data structure is built.<br>
I decided to use a list of Priority Queues with a specific Comparator that allows me to do sorting on the robots by ID. The stabilization is later carried out just by moving around the top of the queues.
<h2>Pollution sensor</h2>
The pollution sensor is composed by four different entities:

  - Simulator
  - Measurement Buffer
  - Measurement Gathering Thread
  - Pollution Sensor Thread

The Measurement Buffer implements a read/write cycle depending on the available size of the buffer. The cycle works unless the robot is on maintenance.<br>
The Measurement Gathering Thread keeps averages in memory until the Pollution Sensor Thread comes and takes them from him. This Thread, the same way the other did, stays on hold until the maintenance process is done.<br>
The Pollution Sensor Thread is the timer of the pipeline and handles MQTT broadcasting. Each MQTT broadcast will be at most 25 seconds apart from the one before, that is because I wanted to simulate the fact that the robot being broken down. To conclude the Admin Server will receive an MQTT message every: $15$ seconds + $min($
<code>REMAINING_MAINTENANCE_TIME</code>
$, 10)$
<h2>FIX and QUIT commands</h2>
Both FIX and QUIT commands are being handled through dedicated threads, created once the commands are typed. To be fair the existence of a specialized thread just to send a process to the mechanic is really useless and could be avoided altogether. The QUIT command is a bit more complicated and having a thread in that case is a bit more useful, because if it has to wait doesn't lock the entirety of the input pipeline, but could still be removed in favour of a synchronized function.
<h2>Final thoughts and future upgrades</h2>
The solution I found is far from simple and still a bit rough around the edges. A couple of steps that I will take in the near future to make the solution better are:

  - [ ] Cleaning the repository
  - [ ] Lower the project complexity, find ways to merge functionalities together and lower the amount of threads per process
  - [ ] Do code refactoring to better define responsibilities inside the network
  - [ ] Change a couple of asynchronous GRPCs to better suited synchronous remote procedure calls
  - [ ] Create a GUI to better understand what is going on
  - [ ] Write good code documentation
