<h1>Greenfield</h1>
<h2>Introduction</h2>
In this repository is contained all of the code for the project of Distributed and Pervasive Systems by Professor Ardagna and Professor Bettini at Università degli Studi di Milano, the project was presented in September 2023. In the README file is contained the analysis of the work I did as well as some informations regarding things that I would like to do in the future to make it better.
<br>
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
The `Comm Pair` class is a simple toy I came up with, it's nothing more than a pair containing a `ManagedChannel` and a `GRPCStub`. The idea behind it is simply to keep on recycling open stubs and channels; thus saving the resources necessary to open a new one each and every time a communication has to take place. Since the project is required to rely heavily on GRPC communication.
CommPairs are clustered into `ThreadSafeHashMaps`, each of the pairs is associated to the hash of the `BotIdentity` the `ManagedChannel` connects to.
<h3>Maintenance process</h3>
<h3>Removal</h3>
<h2>Pollution sensor</h2>
<h2>FIX and QUIT commands</h2>
<h2>Final thoughts</h2>
<h2>Future upgrades</h2>
