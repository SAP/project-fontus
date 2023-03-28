#!/usr/bin/env python3
import argparse
import csv
import pprint
import pathlib

allowed_package_prefixes = ['org.broadleafcommerce']

def perc_of(a,b):
    return a/(b/100)

def parse(input, allowed_prefixes, blocked_prefixes):
    with open(input, newline='') as csvfile:
        reader = csv.DictReader(csvfile)
        ins_total = 0
        ins_total_covered = 0
        line_total = 0
        line_total_covered = 0
        meth_total = 0
        meth_total_covered = 0
        for row in reader:
            keep = False
            for allowed_prefix in allowed_prefixes:
                package = row['PACKAGE']
                class_name = row['CLASS']
                class_name = f'{package}.{class_name}'
                if class_name.startswith(allowed_prefix):
                    blocked = False
                    for blocked_prefix in blocked_prefixes:
                        if class_name.startswith(blocked_prefix):
                            blocked = True
                    if not blocked:
                        keep = True
            if keep:
                #print(row['PACKAGE'] + '.' + row['CLASS'])
                class_ins_missed = int(row['INSTRUCTION_MISSED'])
                class_ins_covered = int(row['INSTRUCTION_COVERED'])
                class_ins_total = class_ins_missed + class_ins_covered
                class_line_missed = int(row['LINE_MISSED'])
                class_line_covered = int(row['LINE_COVERED'])
                class_line_total = class_line_missed + class_line_covered
                class_meth_missed = int(row['METHOD_MISSED'])
                class_meth_covered = int(row['METHOD_COVERED'])
                class_meth_total = class_meth_missed + class_meth_covered
                ins_total = ins_total + class_ins_total
                ins_total_covered = ins_total_covered + class_ins_covered
                line_total = line_total + class_line_total
                line_total_covered = line_total_covered + class_line_covered
                meth_total = meth_total + class_meth_total
                meth_total_covered = meth_total_covered + class_meth_covered
        print(f'{ins_total_covered}/{ins_total} = {perc_of(ins_total_covered,ins_total)}% instruction coverage')
        print(f'{line_total_covered}/{line_total} = {perc_of(line_total_covered,line_total)}% line coverage')
        print(f'{meth_total_covered}/{meth_total} = {perc_of(meth_total_covered,meth_total)}% method coverage')

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--keep', default=[], action="extend", nargs="+", type=str)
    parser.add_argument('--block', default=[], action="extend", nargs="+", type=str)
    parser.add_argument('--input', type=pathlib.Path, required=True)
    args = parser.parse_args()
    pprint.pprint(args)
    parse(args.input, args.keep, args.block)

if __name__ == '__main__':
    main()

