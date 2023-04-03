#!/usr/bin/env python3
import argparse
import csv
import pprint
import pathlib

import xml.etree.ElementTree as ET

def perc_of(a,b):
    return a/(b/100) if b > 0 else 0.0

def parse(input, allowed_prefixes, blocked_prefixes, no_filter):
    root = ET.parse(input)
    ins_total = 0
    ins_total_covered = 0
    line_total = 0
    line_total_covered = 0
    meth_total = 0
    meth_total_covered = 0

    forbidden_classes = ["EnhancerBySpring", "HibernateProxy", "FastClassBySpring", "_Accessor_"]
    allowed_classes = ["org/isf"]
    
    for package in root.findall("package"):
        for clazz in package.findall("class"):
            clazz_name = clazz.get("name")
            if any(substring in clazz_name for substring in forbidden_classes):
                continue
            if len(allowed_classes) > 0 and not any(substring in clazz_name for substring in allowed_classes):
                continue
            for method in clazz.findall("method"):
                method_name = method.get("name")

                class_meth_missed = 0
                class_meth_covered = 0
                class_line_missed = 0
                class_ins_missed = 0
                class_ins_covered = 0

                for counter in method.findall("counter"):
                    type = counter.get("type")
                    if type == "METHOD":
                        class_meth_missed = int(counter.get("missed"))
                        class_meth_covered = int(counter.get("covered"))
                    elif type == "LINE":
                        class_line_missed = int(counter.get("missed"))
                        class_line_covered = int(counter.get("covered"))
                    elif type == "INSTRUCTION":
                        class_ins_missed = int(counter.get("missed"))
                        class_ins_covered = int(counter.get("covered"))
                if class_meth_missed > 0:
                    print(method_name)
                class_meth_total = class_meth_missed + class_meth_covered
                class_line_total = class_line_missed + class_line_covered
                class_ins_total = class_ins_missed + class_ins_covered
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
    parser.add_argument('--no-filter', action='store_true', default=False)
    args = parser.parse_args()
    pprint.pprint(args)
    parse(args.input, args.keep, args.block, args.no_filter)

if __name__ == '__main__':
    main()

