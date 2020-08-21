import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

def main():
    # data_frame = pd.read_csv("../../evolveChandigarhToLuebeck_k200Addm2moreTestsAllInfosPred.csv", sep=" ")
    data_frame = pd.read_csv("../../evolveChandigarhToLuebeck_k200Addm6moreTestsAllInfosPred.csv", sep=" ")
    print(data_frame.head())

    _, axes = plt.subplots(2, 1)

    ax1 = [axes[0].twinx(), axes[1].twinx()]

    data_frame.plot(kind="line", x="cycle", y="risk_ub", color=(0.2, 0.2, 0.2), ax=axes[0])
    data_frame.plot(kind="line", x="cycle", y="risk_add", color=(0.4, 0.4, 0.4), ax=axes[0])
    data_frame.plot(kind="line", x="cycle", y="risk_add_ub", color=(0.6, 0.6, 0.6), ax=axes[0])
    data_frame.plot(kind="line", x="cycle", y="risk_no_added_test", color=(0.8, 0.8, 0.8), ax=axes[0])
    data_frame.plot(kind="line", x="cycle", y="batchCntLuebeck", color="red", alpha=0.1, ax=ax1[0], legend=None)
    data_frame.plot(kind="line", x="cycle", y="batchCntChandigarh", color="blue", alpha=0.1, ax=ax1[0], legend=None)

    data_frame.plot(kind="line", marker="o", linestyle="None", x="cycle", y="nr_newTests_ub", color=(0.2, 0.2, 0.2), ax=axes[1])
    data_frame.plot(kind="line", marker="v", linestyle="None", x="cycle", y="nr_newTests_add", color=(0.4, 0.4, 0.4), ax=axes[1])
    data_frame.plot(kind="line", marker="D", linestyle="None", x="cycle", y="nr_newTests_add_ub", color=(0.6, 0.6, 0.6), ax=axes[1])
    data_frame.plot(kind="line", x="cycle", y="batchCntLuebeck", color="red", alpha=0.1, ax=ax1[1], legend=None)
    data_frame.plot(kind="line", x="cycle", y="batchCntChandigarh", color="blue", alpha=0.1, ax=ax1[1], legend=None)
    # axes[1].set_ylim([-1, 50])
    # axes[1].set_yticks(np.arange(-1, 50, 2))
    # axes[1].grid()
    axes[1].legend(loc="upper right", frameon=False)

    plt.figure()
    axis0 = plt.gca()
    axis1 = axis0.twinx()
    data_frame.plot(kind="line", x="cycle", y="risk_ub", color=(0.2, 0.2, 0.2), ax=axis0)
    data_frame.plot(kind="line", x="cycle", y="risk_add", color=(0.4, 0.4, 0.4), ax=axis0)
    data_frame.plot(kind="line", x="cycle", y="risk_add_ub", color=(0.6, 0.6, 0.6), ax=axis0)
    data_frame.plot(kind="line", x="cycle", y="risk_no_added_test", color=(0.8, 0.8, 0.8), ax=axis0)
    data_frame.plot(kind="line", x="cycle", y="batchCntLuebeck", color="red", alpha=0.1, ax=axis1, legend=None)
    data_frame.plot(kind="line", x="cycle", y="batchCntChandigarh", color="blue", alpha=0.1, ax=axis1, legend=None)

    plt.figure()
    axis0 = plt.gca()
    axis1 = axis0.twinx()
    data_frame.plot(kind="line", x="cycle", y="nr_allTestsUb", color=(0.2, 0.2, 0.2), ax=axis0)
    data_frame.plot(kind="line", x="cycle", y="nr_allTestsAdd", color=(0.4, 0.4, 0.4), ax=axis0)
    data_frame.plot(kind="line", x="cycle", y="nr_allTestsAddUb", color=(0.6, 0.6, 0.6), ax=axis0)
    data_frame.plot(kind="line", x="cycle", y="batchCntLuebeck", color="red", alpha=0.1, ax=axis1, legend=None)
    data_frame.plot(kind="line", x="cycle", y="batchCntChandigarh", color="blue", alpha=0.1, ax=axis1, legend=None)
    axis0.grid()


    plt.figure()
    axis0 = plt.gca()
    axis1 = axis0.twinx()
    data_frame.plot(kind="line", linestyle="None", marker=".", markeredgewidth=2.0, markerfacecolor=(0.2, 0.2, 0.2, 0.05),  markeredgecolor=(0.2, 0.2, 0.2, 0.2), linewidth=0.1, x="cycle", y="risk_diff_ub", color="green", alpha=0.3, ax=axis0)
    data_frame.plot(kind="line", linestyle="None", marker=".", markeredgewidth=2.0, markerfacecolor=(0.4, 0.4, 0.4, 0.05), markeredgecolor=(0.4, 0.4, 0.4, 0.2), linewidth=0.1, x="cycle", y="risk_diff_add", color="cyan", alpha=0.3, ax=axis0)
    data_frame.plot(kind="line", linestyle="None", marker=".", markeredgewidth=2.0, markerfacecolor=(0.6, 0.6, 0.6, 0.05), markeredgecolor=(0.6, 0.6, 0.6, 0.2), linewidth=0.1, x="cycle", y="risk_diff_add_ub", color="magenta", alpha=0.3, ax=axis0)
    data_frame.plot(kind="line", linestyle="None", marker=".", markeredgewidth=2.0, markerfacecolor=(0.8, 0.8, 0.8, 0.05), markeredgecolor=(0.8, 0.8, 0.8, 0.2), linewidth=0.1, x="cycle", y="risk_no_add_test", color="brown", alpha=0.3, ax=axis0)
    data_frame.plot(kind="line", x="cycle", y="batchCntLuebeck", color="red", alpha=0.1, ax=axis1, legend=None)
    data_frame.plot(kind="line", x="cycle", y="batchCntChandigarh", color="blue", alpha=0.1, ax=axis1, legend=None)
    axis0.legend(loc="upper right", frameon=False)

    plt.figure()
    axis0 = plt.gca()
    axis1 = axis0.twinx()
    data_frame.plot(kind="line", x="cycle", y="diff_p", color="blue", alpha=0.4, ax=axis0)
    data_frame.plot(kind="line", x="cycle", y="batchCntLuebeck", color="red", alpha=0.1, ax=axis1, legend=None)
    data_frame.plot(kind="line", x="cycle", y="batchCntChandigarh", color="blue", alpha=0.1, ax=axis1, legend=None)

    plt.show()

if __name__ == '__main__':
    main()
