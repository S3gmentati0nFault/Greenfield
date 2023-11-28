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
<h3>Join</h3>
<h3>Maintenance process</h3>
<h3>Removal</h3>
<h2>Pollution sensor</h2>
<h2>FIX and QUIT commands</h2>
<h2>Final thoughts</h2>
<h2>Future upgrades</h2>
