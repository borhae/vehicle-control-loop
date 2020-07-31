# vehicle-control-loop

This eclipse project was used to create the data for the paper 
[Collective Risk Minimization via a Bayesian Model for Statistical Software Testing](https://arxiv.org/abs/2005.07460)

## Prerequisites
### Simulator
In order to run this you need the **coppelia-sim simulator** installed on your system ([home site](https://www.coppeliarobotics.com/), 
[download](https://www.coppeliarobotics.com/downloads), the product was formerly known as v-rep simulator).
After installation you need to start the simulator once, and after shutdown of 
the simulator there should be a file usrset.txt in the folder where coppelia-sim is installed. In this file you have to alter the value dynamicActivityRange to 
100000 to extend the simulation area to 100 Kilometers. 

Due to the large simulation area only the ODE physics engine seems to work (suggestions for a fix to make bullet run the simulation are welcome).

### Code
The java part is an eclipse project (the one you're just reading the readme of). It requires java 11 or greater. Currently I develop with jdk 14. 
## Importing Data
Once an experiment has been run, all collected data goes into their respective .json files:
* configuration: Co(luebeck|chandigarh)_*.json
* observation: Ob(luebeck|chandigarh)_*.json
* plan: Plan(luebeck|chandigarh)_*.json
* 
For the evaluation and proper persistence these data should be imported into a database. This is done by class "BatchImportData" from package "de.joachim.haensel.phd.scenario.experiment.evaluation.database". This works for configuration and observation data only at the moment.


## Existing Data
All Data collected during the simulations are stored in a Mongo-Database. The final dataset assessed is named "data_available_at_2020-01-18" in database "indexed_trajectories". It encompasses the individual runs captured in:
* car.sim_Chandigarh-20190709_183_max_scattered_targets
* car.sim_Chandigarh-20190717_200_random_targets_4096Seed
* car.sim_Chandigarh-20190721_200_random_targets_5098Seed
* car.sim_Chandigarh-20190723_200_random_targets_5555Seed
* car.sim_Chandigarh-20190725_200_random_targets_3453Seed
* car.sim_Chandigarh-20190730_200_random_targets_7304Seed
* car.sim_Chandigarh-20191008_200_random_targets_923704Seed
* car.sim_Luebeck-20190705_183_max_scattered_targets
* car.sim_Luebeck-20190716_200_random_targets_4096Seed
* car.sim_Luebeck-20190719_200_random_targets_5098Seed
* car.sim_Luebeck-20190719_200_random_targets_5555Seed
* car.sim_Luebeck-20190727_200_random_targets_7304Seed
* car.sim_Luebeck-20190731_200_random_targets_923704Seed

This process is done by class "de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.TrajectoryIndexer". Whenever this is run it will collect any data in the "car" database into a new dataset in the "indexed_trajectories" database.

## Data Evaluation
Classes for data evaluations are in package "de.joachim.haensel.phd.scenario.experiment.evaluation" and its sub-packages. In addition there is classes in "de.joachim.haensel.phd.scenario.experiment.recipe" and sub-packages.

## Troubleshooting
* I sometimes had the issue of V-Rep running super slow. After a while I figured that it wasn't using the graphics card any more. It speeded up to normal when I attached graphical run settings from the NVIDIA Control Panel
1. Start application NVIDIA Control Panel
1. In Treeview (select a Task...) expand "3D Settings"
1. select "Manage 3D settings" 
1. In tab "Program Settings"
1. Add CoppeliaSim from the file System in "Select a program to customize"
1. apply (go with default settings)
1. fixed the slow down