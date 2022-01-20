from os import listdir
from os.path import isfile, join
import json
import sys
import numpy as np


def calculate_basic_stats(data_path: str):

    onlyfiles = [f for f in listdir(data_path) if isfile(join(data_path, f))]

    total_read_records = 0
    total_write_records = 0

    total_read_threads = 0
    total_write_threads = 0
    for file in onlyfiles:
        results = json.load(open(data_path + file))
        if results['type'] == 'READ':
            total_read_records += results['dataSize']
            total_read_threads += 1
        else:
            total_write_records += results['dataSize']
            total_write_threads += 1

    print('WRITE STATS')
    print('Records saved in total - {total}'.format(total=total_write_records))
    print('Total threads - {total}'.format(total=total_write_threads))
    print('Average per thread - {avg}'.format(avg=(total_write_records/total_write_threads)))

    print('READ STATS')
    print('Records read in total - {total}'.format(total=total_read_records))
    print('Total threads - {total}'.format(total=total_read_threads))
    print('Average per thread - {avg}'.format(avg=(total_read_records/total_read_threads)))

if __name__ == "__main__":
    data_path = mypath=sys.argv[1]
    # data_path = '/Users/vsobol/personal_projects/PhD/' \
    #             'Raft_Load_capacity_research/raft_loading_research/' \
    #             'experiment_results/experiment_1/data/'
    calculate_basic_stats(data_path)
