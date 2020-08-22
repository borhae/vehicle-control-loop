import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

def make_patch_spines_invisible(ax):
    ax.set_frame_on(True)
    ax.patch.set_visible(False)
    for sp in ax.spines.values():
        sp.set_visible(False)

def main():
    # data_frame = pd.read_csv("../../evolveChandigarhToLuebeck_k200Addm2moreTestsAllInfosPred.csv", sep=" ")
    # data_frame = pd.read_csv("../../evolveChandigarhToLuebeck_k200Addm6moreTestsAllInfosPred.csv", sep=" ")
    # data_frame = pd.read_csv("../../evolveLuebeckToChandigarh_k200Addm6moreTestsAllInfosPred.csv", sep=" ")
    data_frame = pd.read_csv("../../evolveLuebeckToChandigarh_k200Addm200moreTestsAllInfosPred.csv", sep=" ")
    
    
    fig, axes = plt.subplots(2, 1)
    fig.set_size_inches(11.69, 8.27)

    ax1 = [axes[0].twinx(), axes[1].twinx()]

    data_frame.plot(kind="line", x="cycle", y="risk_ub", color=(0.2, 0.2, 0.2), ax=axes[0])
    data_frame.plot(kind="line", x="cycle", y="risk_add", color=(0.4, 0.4, 0.4), ax=axes[0])
    data_frame.plot(kind="line", x="cycle", y="risk_add_ub", color=(0.6, 0.6, 0.6), ax=axes[0])
    data_frame.plot(kind="line", x="cycle", y="risk_no_added_test", color=(0.8, 0.8, 0.8), ax=axes[0])
    data_frame.plot(kind="line", x="cycle", y="batchCntLuebeck", color="red", alpha=0.1, ax=ax1[0], legend=None)
    data_frame.plot(kind="line", x="cycle", y="batchCntChandigarh", color="blue", alpha=0.1, ax=ax1[0], legend=None)

    data_frame.plot(kind="line", markersize=1, marker="o", linestyle="None", x="cycle", y="nr_newTests_ub", color=(0.2, 0.2, 0.2), ax=axes[1])
    data_frame.plot(kind="line", markersize=1, marker="v", linestyle="None", x="cycle", y="nr_newTests_add", color=(0.4, 0.4, 0.4), ax=axes[1])
    data_frame.plot(kind="line", markersize=1, marker="D", linestyle="None", x="cycle", y="nr_newTests_add_ub", color=(0.6, 0.6, 0.6), ax=axes[1])
    data_frame.plot(kind="line", x="cycle", y="batchCntLuebeck", color="red", alpha=0.1, ax=ax1[1], legend=None)
    data_frame.plot(kind="line", x="cycle", y="batchCntChandigarh", color="blue", alpha=0.1, ax=ax1[1], legend=None)
    # axes[1].set_ylim([-1, 50])
    # axes[1].set_yticks(np.arange(-1, 50, 2))
    # axes[1].grid()
    axes[1].legend(loc="upper right", frameon=False)
    plt.savefig("risk_newtests.pdf", papertype="a4")

    fig = plt.figure()
    fig.set_size_inches(11.69, 8.27)

    axis0 = plt.gca()
    axis1 = axis0.twinx()
    data_frame.plot(kind="line", x="cycle", y="risk_ub", color=(0.2, 0.2, 0.2), ax=axis0)
    data_frame.plot(kind="line", x="cycle", y="risk_add", color=(0.4, 0.4, 0.4), ax=axis0)
    data_frame.plot(kind="line", x="cycle", y="risk_add_ub", color=(0.6, 0.6, 0.6), ax=axis0)
    data_frame.plot(kind="line", x="cycle", y="risk_no_added_test", color=(0.8, 0.8, 0.8), ax=axis0)
    data_frame.plot(kind="line", x="cycle", y="batchCntLuebeck", color="red", alpha=0.1, ax=axis1, legend=None)
    data_frame.plot(kind="line", x="cycle", y="batchCntChandigarh", color="blue", alpha=0.1, ax=axis1, legend=None)
    plt.savefig("risk.pdf", papertype="a4")

    fig = plt.figure()
    fig.set_size_inches(11.69, 8.27)
    axis0 = plt.gca()
    axis1 = axis0.twinx()
    data_frame.plot(kind="line", x="cycle", y="nr_allTestsUb", color=(0.2, 0.2, 0.2), ax=axis0)
    data_frame.plot(kind="line", x="cycle", y="nr_allTestsAdd", color=(0.4, 0.4, 0.4), ax=axis0)
    data_frame.plot(kind="line", x="cycle", y="nr_allTestsAddUb", color=(0.6, 0.6, 0.6), ax=axis0)
    data_frame.plot(kind="line", x="cycle", y="batchCntLuebeck", color="red", alpha=0.1, ax=axis1, legend=None)
    data_frame.plot(kind="line", x="cycle", y="batchCntChandigarh", color="blue", alpha=0.1, ax=axis1, legend=None)
    axis0.grid()
    plt.savefig("all_tests.pdf", papertype="a4")


    fig = plt.figure()
    fig.set_size_inches(11.69, 8.27)
    axis0 = plt.gca()
    axis1 = axis0.twinx()
    data_frame.plot(kind="line", linestyle="None", marker=".", markeredgewidth=2.0, markerfacecolor=(0.2, 0.2, 0.2, 0.05),  markeredgecolor=(0.2, 0.2, 0.2, 0.2), linewidth=0.1, x="cycle", y="risk_diff_ub", color="green", alpha=0.3, ax=axis0)
    data_frame.plot(kind="line", linestyle="None", marker=".", markeredgewidth=2.0, markerfacecolor=(0.4, 0.4, 0.4, 0.05), markeredgecolor=(0.4, 0.4, 0.4, 0.2), linewidth=0.1, x="cycle", y="risk_diff_add", color="cyan", alpha=0.3, ax=axis0)
    data_frame.plot(kind="line", linestyle="None", marker=".", markeredgewidth=2.0, markerfacecolor=(0.6, 0.6, 0.6, 0.05), markeredgecolor=(0.6, 0.6, 0.6, 0.2), linewidth=0.1, x="cycle", y="risk_diff_add_ub", color="magenta", alpha=0.3, ax=axis0)
    data_frame.plot(kind="line", linestyle="None", marker=".", markeredgewidth=2.0, markerfacecolor=(0.8, 0.8, 0.8, 0.05), markeredgecolor=(0.8, 0.8, 0.8, 0.2), linewidth=0.1, x="cycle", y="risk_no_add_test", color="brown", alpha=0.3, ax=axis0)
    data_frame.plot(kind="line", x="cycle", y="batchCntLuebeck", color="red", alpha=0.1, ax=axis1, legend=None)
    data_frame.plot(kind="line", x="cycle", y="batchCntChandigarh", color="blue", alpha=0.1, ax=axis1, legend=None)
    axis0.legend(loc="upper right", frameon=False)
    plt.savefig("risk_diff.pdf", papertype="a4")

    fig, axis0 = plt.subplots()
    fig.set_size_inches(11.69, 8.27)
    fig.subplots_adjust(right=0.75)
    axis1 = axis0.twinx()
    axis2 = axis0.twinx()

    axis2.spines["right"].set_position(("axes", 1.2))
    make_patch_spines_invisible(axis2)
    axis2.spines["right"].set_visible(True)

    data_frame.plot(kind="line", x="cycle", y="diff_p", color="blue", alpha=0.4, ax=axis0, legend=None)
    data_frame.plot(kind="line", x="cycle", y="batchCntLuebeck", color="red", alpha=0.1, ax=axis1, legend=None)
    data_frame.plot(kind="line", x="cycle", y="batchCntChandigarh", color="blue", alpha=0.1, ax=axis1, legend=None)
    data_frame.plot(kind="line", markersize=1, marker="o", linestyle="None", x="cycle", y="nr_newTests_ub", color=(0.2, 0.2, 0.2), ax=axis2, legend=None)
    data_frame.plot(kind="line", markersize=1, marker="v", linestyle="None", x="cycle", y="nr_newTests_add", color=(0.4, 0.4, 0.4), ax=axis2, legend=None)
    data_frame.plot(kind="line", markersize=1, marker="D", linestyle="None", x="cycle", y="nr_newTests_add_ub", color=(0.6, 0.6, 0.6), ax=axis2, legend=None)
    axis0.set_xlabel("cycles")
    
    axis0.tick_params(axis='y', colors="blue", size=4, width=1.5)
    axis0.set_ylabel("delta p")
    
    axis1.tick_params(axis='y', colors="purple", size=4, width=1.5)
    axis1.set_ylabel("source sampled")
    
    axis2.tick_params(axis='y', colors=(0.5, 0.5, 0.5), size=4, width=1.5)
    axis2.set_ylabel("number of new tests")

    axis0.legend(loc="upper left", frameon=False)
    axis2.legend(loc="center left", frameon=False)
    fig.savefig("diff_p_new_tests.pdf", papertype="a4")
    # plt.savefig("diff_p_new_tests", papertype="a4")

    # plt.show()

if __name__ == '__main__':
    main()
