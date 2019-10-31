from collections import namedtuple
from model import *
from gurobipy import *

# set timelimit for mip solver
TIMELIMIT = 100.0

Point = namedtuple("Point", ['x', 'y'])
Facility = namedtuple("Facility", ['index', 'setup_cost', 'capacity', 'location'])
Customer = namedtuple("Customer", ['index', 'demand', 'location'])


def solve(input_data):
    # Modify this code to run your optimization algorithm

    # parse the input
    lines = input_data.split('\n')

    parts = lines[0].split()
    facility_count = int(parts[0])
    customer_count = int(parts[1])
    
    facilities = []
    for i in range(1, facility_count+1):
        parts = lines[i].split()
        facilities.append(Facility(i-1, float(parts[0]), int(parts[1]), Point(float(parts[2]), float(parts[3])) ))

    customers = []
    for i in range(facility_count+1, facility_count+1+customer_count):
        parts = lines[i].split()
        customers.append(Customer(i-1-facility_count, int(parts[0]), Point(float(parts[1]), float(parts[2]))))

    # solve MIP
    xs, ys, ip = get_mip(facilities, customers)
    ip.setParam(GRB.Param.TimeLimit, TIMELIMIT)
    ip.optimize()
    if ip.getAttr(GRB.attr.Status) == GRB.OPTIMAL:
        s = "optimal"
    else:
        s = "not optimal"
    # construct output
    first_line = f'objective: {ip.getObjective().getValue()} {s}\n'
    second_line = ''
    for c in customers:
        for f in facilities:
            if int(round(xs[(f, c)].x)) == 1:
                second_line += f'{f.index} '

    return first_line + second_line


if __name__ == '__main__':
    import sys
    if len(sys.argv) > 1:
        file_location = sys.argv[1].strip()
        with open(file_location, 'r') as input_data_file:
            input_data = input_data_file.read()
        print(solve(input_data))
    else:
        print('missing input file')

