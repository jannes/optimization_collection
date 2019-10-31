from gurobipy import *


def point_length(point1, point2):
    return math.sqrt((point1.x - point2.x)**2 + (point1.y - point2.y)**2)


def get_mip(facilities, customers):
    model = Model('FacLocIP')
    ys = []
    xs = {}
    # variables
    for f in facilities:
        ys.append(model.addVar(name=f'y_{f.index}', vtype=GRB.INTEGER))
        for c in customers:
            xs[(f, c)] = model.addVar(name=f'x_{f.index},{c.index}', vtype=GRB.INTEGER)
    # constraint 1
    for f in facilities:
        rhs = f.capacity
        lhs = 0
        for c in customers:
            lhs += xs[(f, c)]
        model.addConstr(lhs <= rhs)
    # constraint 2
    for c in customers:
        rhs = 1
        lhs = 0
        for f in facilities:
            lhs += xs[(f, c)]
        model.addConstr(lhs == rhs)
    # objective
    obj_term = 0
    for f in facilities:
        setup = ys[f.index] * f.setup_cost
        delivery_costs = 0
        for c in customers:
            delivery_costs += xs[(f, c)] * point_length(f.location, c.location)
        obj_term += (setup + delivery_costs)
    model.setObjective(obj_term, GRB.MINIMIZE)
    model.update()
    return xs, ys, model
