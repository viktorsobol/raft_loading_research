import numpy as np
from numpy import random

if __name__ == '__main__':
    s = np.random.exponential(0.1, 10000)
    s = [e + 10 for e in s]
    s = []
    for i in range(0, 10000):
        s.append(0.1 + np.random.exponential(0.4) + np.random.lognormal(3, 1))

    print(np.percentile(s, 50))
    print(np.percentile(s, 75))
    print(np.percentile(s, 90))
    print(np.percentile(s, 99))

    import matplotlib.pyplot as plt

    plt.hist(s, bins=np.arange(min(s), max(s) + 1, 1), density=True, align='mid', )
    plt.xlim([0, 300])
    # plt.vlines(x=40, ymin=0, ymax = 0.05,colors='green', ls=':', lw=2)

    # plt.axis('tight')
    plt.show()
