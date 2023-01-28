import os
from os import listdir
from os.path import isfile, join

import matplotlib.pyplot as plt
import numpy as np
import seaborn as sns
from sklearn.metrics import mean_squared_error


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


def retreive_percentile_stats(path: str, all_path = False)-> dict:
    if isfile(path) and os.path.basename(path) == 'percentiles.txt':
        file = open(path)
        lines = file.readlines()
        exp_name = os.path.dirname(path).split("/")[-1] if not all_path else path
        res = {}
        for l in lines:
            arr = l.split(":")
            if arr[0] == '100.0':
                continue
            res[float(arr[0])] = float(arr[1])

        return {exp_name: res}

    if isfile(path) and not os.path.basename(path) == 'percentile.txt':
        return {}

    folders = listdir(path)

    result = dict()

    for folder in folders:
        result = result |  retreive_percentile_stats(join(path, folder), all_path)

    return result

def calculate_percentiles(flat_stats: list):
    percentiles = {}
    flat_stats = [e if e > 0 else 0 for e in flat_stats]

    percentile = 1
    while percentile <= 100:
        percentiles[percentile] = np.percentile(flat_stats, percentile)
        percentile += 0.1
    return percentiles

def plot_errors(stats: dict):
    for k, stat in stats.items():
          sns.distplot(stat, hist = False, kde = True,
                 kde_kws = {'shade': True, 'linewidth': 1},
                 label = k)

    # plt.legend(prop={'size': 16}, title = 'PBS', bbox_to_anchor=(1.15, 1.2), loc='upper left')
    plt.xlim([0, 3000])
    plt.title('RMSE')
    plt.xlabel('Error')
    plt.ylabel('Density')
    plt.tight_layout()
    plt.show()
    # plt.savefig("results.svg")


if __name__ == '__main__':
    data_folders = retrieve_data_folders('/Users/vsobol/personal_projects/PhD/Raft_Load_capacity_research/raft_loading_research/_results/')
    real_data_folders = retreive_percentile_stats('/Users/vsobol/personal_projects/PhD/Raft_Load_capacity_research/raft_loading_research/_results/', True)
    imitation_data_folders = retreive_percentile_stats('/Users/vsobol/personal_projects/PhD/Raft_Load_capacity_research/raft_loading_research/imitational_results')
   # data_folders = ['/Users/vsobol/personal_projects/PhD/Raft_Load_capacity_research/raft_loading_research/_results/3node/120-40write-80read/leader/experiment_1/data']


    errors = {}
    for exp_n, p_data in imitation_data_folders.items():
        errors[exp_n] = []
        for real_n, real_data in real_data_folders.items():
            print("Processing data from folder {f} for exp - {e}".format(f=real_n, e = exp_n))
            error = mean_squared_error(list(real_data.values()), list(p_data.values()), squared=False)
            errors[exp_n].append(error)

    # Have no idea if it makes sense.
    errors = dict(sorted(errors.items(), key=lambda item: np.mean(item[1])))
    for k, err in errors.items():
        min = np.min(err)
        p25 = np.percentile(err, 25)
        p50 = np.percentile(err, 50)
        p90 = np.percentile(err, 90)
        p99 = np.percentile(err, 90)
        print(f"{k} - {min}")
        print(f"{k} - {p25}")
        print(f"{k} - {p50}")
        print(f"{k} - {p90}")
        print(f"{k} - {p99}")
