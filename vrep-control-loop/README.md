# vehicle-control-loop

This eclipse project was used to create the data for the paper 
[Collective Risk Minimization via a Bayesian Model for Statistical Software Testing](https://arxiv.org/abs/2005.07460)

## Prerequisites
In order to run this you need the **coppelia-sim simulator** installed on your system ([home site](https://www.coppeliarobotics.com/), 
[download](https://www.coppeliarobotics.com/downloads), the product was formerly known as v-rep simulator).
After installation you need to start the simulator once, and after shutdown of 
the simulator there should be a file usrset.txt in the folder where coppelia-sim is installed. In this file you have to alter the value dynamicActivityRange to 
100000 to extend the simulation area to 100 Kilometers. 

Due to the large simulation area only the ODE physics engine seems to work (suggestions for a fix to make bullet run the simulation are welcome).

## Troubleshooting
* I sometimes had the issue of V-Rep running super slow. After a while I figured that it wasn't using the graphics card any more. It speeded up to normal when I attached graphical run settings from the NVIDIA Control Panel
1. Start application NVIDIA Control Panel
1. In Treeview (select a Task...) expand "3D Settings"
1. select "Manage 3D settings" 
1. In tab "Program Settings"
1. Add CoppeliaSim from the file System in "Select a program to customize"
1. apply (go with default settings)
1. fixed the slow down