import os
from os.path import join

import numpy as np

from results_processor.calculate_pbs import save_percentiles_flat, plot_stats, save_plot_stats, \
    save_percentiles_stats_and_plot_flat


def run(total_number_of_iterations: int,
        total_number_of_nodes: int,
        IW,
        IA,
        CR,
        CA
        ):

    delays = []

    for i in range(total_number_of_iterations):
        replica_pairs = []
        for replica in range(total_number_of_nodes - 1):
            iw = IW()
            ia = IA()
            replica_pairs.append((iw, ia))

        replica_pairs.sort(key=lambda k: k[0] + k[1])
        max_replication_delay_pair = replica_pairs[int(total_number_of_nodes/2) - 1]

        # Delay for successful replication(replicated to N / 2 + 1 nodes) One is leader
        max_replication_delay = max_replication_delay_pair[0] + max_replication_delay_pair[1]

        # max delay for leader contacting the follower node
        replica_pairs.sort(key=lambda k: k[0])
        max_follower_replication_delay = replica_pairs[len(replica_pairs) - 1][0]

        ca = CA()
        cr = CR()

        delay = max_follower_replication_delay - max_replication_delay - ca - cr

        delays.append(delay)

    return delays

if __name__ == '__main__':


    distributions = {
        '1': {
            'IW': lambda: (0.1 + np.random.exponential(0.4) + np.random.lognormal(3, 1)),
            'IA': lambda: 0.05 + np.random.exponential(0.2),
            'CA': lambda: 10 + np.random.exponential(40),
            'CR': lambda: 10 + np.random.exponential(40)
        }
        ,
        '5': {
            'IW': lambda: (np.random.exponential(0.4) + np.random.lognormal(2, 1)),
            'IA': lambda: 0.05 + np.random.exponential(0.2),
            'CA': lambda: 10 + np.random.exponential(40),
            'CR': lambda: 10 + np.random.exponential(40)
        }
        ,
        '6': {
            'IW': lambda: (0.1 + np.random.exponential(0.4) + np.random.lognormal(3, 1)),
            'IA': lambda: 0.05 + np.random.exponential(0.5),
            'CA': lambda: 10 + np.random.exponential(40),
            'CR': lambda: 10 + np.random.exponential(40)
        }
        ,
        '7': {
            'IW': lambda: (0.1 + np.random.exponential(0.4) + np.random.lognormal(3, 1)),
            'IA': lambda: 0.05 + np.random.exponential(0.2),
            'CA': lambda: 10 + np.random.exponential(20),
            'CR': lambda: 10 + np.random.exponential(50)
        }
        ,
        '8': {
            # No
            'IW': lambda: (0.1 + np.random.exponential(0.4) + np.random.normal(5, 7)),
            'IA': lambda: 0.05 + np.random.exponential(0.2),
            'CA': lambda: 10 + np.random.exponential(40),
            'CR': lambda: 10 + np.random.exponential(40)
        },
         '9': {
            'IW': lambda: (0.1 + np.random.exponential(0.4) + np.random.lognormal(3, 1)),
            'IA': lambda: 0.05 + np.random.exponential(0.4),
            'CA': lambda: 10 + np.random.exponential(40),
            'CR': lambda: 10 + np.random.exponential(40)
        },
         '10': {
            'IW': lambda: (0.1 + np.random.exponential(0.4) + np.random.lognormal(3, 1)),
            'IA': lambda: 0.05 + np.random.exponential(0.2),
            'CA': lambda: np.random.gamma(6.72, 7.29),
            'CR': lambda: np.random.gamma(6.72, 7.29)
        },
         '11': {
             # No
            'IW': lambda: (0.1 + np.random.exponential(0.4) + np.random.gamma(3, 5)),
            'IA': lambda: 0.05 + np.random.exponential(0.2),
            'CA': lambda: 10 + np.random.gamma(6.72, 7.29),
            'CR': lambda: 10 + np.random.gamma(6.72, 7.29)
        },
         '12': {
             # No
            'IW': lambda: (0.1 + np.random.exponential(0.4) + np.random.gamma(2, 5)),
            'IA': lambda: 0.05 + np.random.exponential(0.2),
            'CA': lambda: 10 + np.random.gamma(6.72, 7.29),
            'CR': lambda: 10 + np.random.gamma(6.72, 7.29)
        },
         '13': {
             # No
            'IW': lambda: (0.1 + np.random.exponential(0.4) + np.random.gamma(2, 6)),
            'IA': lambda: 0.05 + np.random.exponential(0.2),
            'CA': lambda: 10 + np.random.gamma(6.72, 7.29),
            'CR': lambda: 10 + np.random.gamma(6.72, 7.29)
        },
         '14': {
             # No
            'IW': lambda: (0.1 + np.random.exponential(0.4) + np.random.gamma(2, 6)),
            'IA': lambda: 0.05 + np.random.exponential(0.2),
            'CA': lambda: np.random.gamma(6.72, 7.29),
            'CR': lambda: np.random.gamma(6.72, 7.29)
        }
        ,
        '15': {
            # No
            'IW': lambda: (np.random.gamma(2, 6)),
            'IA': lambda: 0.05 + np.random.exponential(0.2),
            'CA': lambda: np.random.gamma(6.72, 7.29),
            'CR': lambda: np.random.gamma(6.72, 7.29)
        },
        '16': {
            # No
            'IW': lambda: (np.random.gamma(2, 6)),
            'IA': lambda: 0.05 + np.random.exponential(0.2),
            'CA': lambda:  np.random.exponential(30),
            'CR': lambda: np.random.gamma(6.72, 7.29)
        },
        '17': {
            # No
            'IW': lambda: (np.random.exponential(0.4) + np.random.gamma(2, 6)),
            'IA': lambda: 0.05 + np.random.exponential(0.2),
            'CA': lambda:  np.random.exponential(30),
            'CR': lambda: np.random.gamma(6.72, 7.29)
        },
        '18': {
            # No
            'IW': lambda: (np.random.exponential(0.4) + np.random.gamma(2, 6)),
            'IA': lambda: np.random.gamma(1, 2),
            'CA': lambda:  np.random.exponential(30),
            'CR': lambda: np.random.gamma(6.72, 7.29)
        },
        '19': {
            # No
            'IW': lambda: (np.random.gamma(1, 3) + np.random.gamma(2, 6)),
            'IA': lambda: np.random.gamma(1, 2),
            'CA': lambda: np.random.exponential(30),
            'CR': lambda: np.random.gamma(6.72, 7.29)
        },
        '20': {
            # No
            'IW': lambda: (np.random.gamma(1, 3) + np.random.gamma(2, 7)),
            'IA': lambda: np.random.gamma(1, 2),
            'CA': lambda: np.random.gamma(3, 7.29),
            'CR': lambda: np.random.gamma(6.72, 7.29)
        },
        '21': {
            # No
            'IW': lambda: (np.random.gamma(3, 9)),
            'IA': lambda: np.random.gamma(2, 3),
            'CA': lambda: np.random.gamma(3, 7.29),
            'CR': lambda: np.random.gamma(6.72, 7.29)
        },
        '22': {
            'IW': lambda: (np.random.lognormal(2.5, 1)),
            'IA': lambda: np.random.exponential(0.5),
            'CA': lambda: np.random.exponential(40),
            'CR': lambda: np.random.lognormal(3.5, 1)
        },
        '23': {
            # No
            'IW': lambda: (np.random.lognormal(2.5, 1)),
            'IA': lambda: np.random.exponential(0.5),
            'CA': lambda: np.random.exponential(40),
            'CR': lambda: np.random.exponential(40)
        }
    }

    for dist_name, dist in distributions.items():
        path = join("/Users/vsobol/personal_projects/PhD/Raft_Load_capacity_research/raft_loading_research/_results/imitational_results/", dist_name)
        delays = run(
            total_number_of_nodes=3,
            total_number_of_iterations=50000,
            IW=dist['IW'],
            IA=dist['IA'],
            CA=dist['CA'],
            CR=dist['CR']
        )
        if not os.path.exists(path):
            os.makedirs(path)
        results = save_percentiles_flat(delays, path)
        save_plot_stats({1:delays}, path)
        save_percentiles_stats_and_plot_flat(delays, path)

        for p, v in results.items():
            print('{p}: {v}'.format(p=p, v=v))



