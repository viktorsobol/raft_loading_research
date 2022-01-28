import os
from os import listdir
from os.path import isfile, join

from results_processor.calculate_pbs import calculate_pbs
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

    for f in data_folders:
        stats = calculate_pbs(f)
        all_stats = stats.values()
        flat_stats = [item for sublist in all_stats for item in sublist]
        p_50 = np.percentile(flat_stats, 50)
        p_75 = np.percentile(flat_stats, 75)
        p_90 = np.percentile(flat_stats, 90)
        p_95 = np.percentile(flat_stats, 95)
        p_97 = np.percentile(flat_stats, 97)
        p_99 = np.percentile(flat_stats, 99)

        percentile_file = open(join(f, "../percentiles.txt"), "a")
        percentile_file.write("p50 - {p}\n".format(p=p_50))
        percentile_file.write("p75 - {p}\n".format(p=p_75))
        percentile_file.write("p90 - {p}\n".format(p=p_90))
        percentile_file.write("p95 - {p}\n".format(p=p_95))
        percentile_file.write("p97 - {p}\n".format(p=p_97))
        percentile_file.write("p99 - {p}\n".format(p=p_99))
        percentile_file.close()
