# Description
Facility location problem in the plane with euclidian distances.  
Given n facilties and m customers make an assignment of facilities to customers,
s.t all customer demand is met and the cost of opening and operating facitlites is minimized.

# Requirements
local installation of Gurobi including the python module

# Running the solver 
modify the timeout of the solver in the solver.py script  
run with python solver.py [input file]

# Data format
## input
first line: "amount of faculties (n)" "amount of customers (m)"  
n lines: "setup cost" "capacity" "x coordinate" "y coordinate"  
m lines: "demand" "x coordinate" "y coordinate"  

see the examples

## outpt
first line: objective value and optimality indicator  
second line: faculty of each customer
