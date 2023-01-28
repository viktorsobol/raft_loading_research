import os
from os import listdir
from os.path import isfile, join

from results_processor.calculate_pbs import calculate_pbs, save_percentiles_stats_and_plot_flat
import numpy as np

def retrieve_data_folders(data_path: str) -> set:
    if isfile(data_path):
        return set()

    if os.path.basename(os.path.normpath(data_path)) == 'data':
        return {data_path}

    folders = [f for f in listdir(data_path) if not isfile(join(data_path, f))]

    result = set()

    for folder in folders:
        result = set.union(result, retrieve_data_folders(join(data_path, folder)))

    return result


if __name__ == '__main__':
    data_folders = retrieve_data_folders('/Users/vsobol/personal_projects/PhD/Raft_Load_capacity_research/raft_loading_research/_results/')
   # data_folders = ['/Users/vsobol/personal_projects/PhD/Raft_Load_capacity_research/raft_loading_research/_results/3node/120-40write-80read/leader/experiment_1/data']
    for f in data_folders:
        if "leader" in f:
            continue
        print("Processing data from folder {f}".format(f=f))
        stats = calculate_pbs(f)
        all_stats = stats.values()
        flat_stats = [item for sublist in all_stats for item in sublist]
        save_percentiles_stats_and_plot_flat(flat_stats, join(f, "../"))
