import argparse

parser = argparse.ArgumentParser()
parser.add_argument("-v", nargs="+", action="append")
args = parser.parse_args()

if(args.v):
    print(args.v)

bla = args.v
print(bla[0])