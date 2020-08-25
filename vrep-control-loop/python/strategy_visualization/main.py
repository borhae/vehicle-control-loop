import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import scipy as sp
import scipy.signal as sg

def make_patch_spines_invisible(ax):
    ax.set_frame_on(True)
    ax.patch.set_visible(False)
    for sp in ax.spines.values():
        sp.set_visible(False)

def main():
    # input_name = "evolveLuebeckToChandigarh_k1000Addm10moreTests100.00scaledInit20000samplePerCycle10000CyclesAllInfosPred"
    # input_name = "evolveLuebeckToChandigarh_k1000Addm10moreTestsAllInfosPred"
    # input_name = "evolveLuebeckToChandigarh_k1000Addm10moreTests100.00scaledInit100samplePerCycle10000CyclesAllInfosPred"
    # input_name = "evolveLuebeckToChandigarh_k1000Addm10moreTests100.00scaledInit40000samplePerCycle10000CyclesAllInfosPred"
    # input_name = "evolveLuebeckToChandigarh_k200Addm200moreTestsAllInfosPred"
    # input_name = "evolveLuebeckToChandigarh_k200Addm10moreTestsAllInfosPred"
    # input_name = "evolveLuebeckToChandigarh_k200Addm10moreTests100.00scaledInit20000samplePerCycle10000CyclesAllInfosPred"
    # input_name = "evolveLuebeckToChandigarh_k200Addm10moreTests100.00scaledInit100samplePerCycle10000CyclesAllInfosPred"
    # input_name = "evolveLuebeckToChandigarh_k200Addm10moreTests100.00scaledInit40000samplePerCycle10000CyclesAllInfosPred"
    input_name = "evolveLuebeckToChandigarh_k200Addm6moreTests1.00scaledInit2000samplePerCycle10000CyclesAllInfosPred"
    data_frame = pd.read_csv("../../" + input_name + ".csv", sep=" ")    
    
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
    
    axes[0].set_ylabel("risk")
    ax1[0].set_ylabel("samples added")
    axes[1].set_ylabel("number of new tests")
    ax1[1].set_ylabel("samples added")
    # axes[1].set_ylim([-1, 50])
    # axes[1].set_yticks(np.arange(-1, 50, 2))
    # axes[1].grid()
    axes[1].legend(loc="upper right", frameon=False)
    plt.savefig(input_name + "risk_newtests.pdf", papertype="a4")

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
    axis0.set_ylabel("risk")
    axis1.set_ylabel("samples added")
    plt.savefig(input_name + "risk.pdf", papertype="a4")

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
    axis0.set_ylabel("number of all tests")
    axis1.set_ylabel("samples added")
    plt.savefig(input_name + "all_tests.pdf", papertype="a4")


    fig = plt.figure()
    fig.set_size_inches(11.69, 8.27)
    axis0 = plt.gca()
    axis1 = axis0.twinx()
    # -------------------------------------------
    # We create a 4-th order Butterworth low-pass filter.
    b, a = sg.butter(4, 2. / 365)
    # We apply this filter to the signal.
    filtered_risk_diff_ub = sg.filtfilt(b, a,  data_frame["risk_diff_ub"])
    filtered_risk_diff_add = sg.filtfilt(b, a,  data_frame["risk_diff_add"])
    filtered_risk_diff_add_ub = sg.filtfilt(b, a,  data_frame["risk_diff_add_ub"])
    filtered_risk_diff_no_add_test = sg.filtfilt(b, a,  data_frame["risk_no_add_test"])
    # -------------------------------------------

    data_frame.plot(kind="line", linestyle="None",  marker=".", markersize=3, markeredgewidth=.2, markerfacecolor=(0.2, 0.2, 0.2, 0.2),  markeredgecolor=(0.2, 0.2, 0.2, 0.2), x="cycle", y="risk_diff_ub", color="green", alpha=0.3, ax=axis0)
    data_frame.plot(kind="line", linestyle="None", marker=".", markersize=3, markeredgewidth=.2, markerfacecolor=(0.4, 0.4, 0.4, 0.2), markeredgecolor=(0.4, 0.4, 0.4, 0.2), x="cycle", y="risk_diff_add", color="cyan", alpha=0.3, ax=axis0)
    data_frame.plot(kind="line", linestyle="None", marker=".", markersize=3, markeredgewidth=.2, markerfacecolor=(0.6, 0.6, 0.6, 0.2), markeredgecolor=(0.6, 0.6, 0.6, 0.2), x="cycle", y="risk_diff_add_ub", color="magenta", alpha=0.3, ax=axis0)
    data_frame.plot(kind="line", linestyle="None", marker=".", markersize=3, markeredgewidth=.2, markerfacecolor=(0.8, 0.8, 0.8, 0.8), markeredgecolor=(0.8, 0.8, 0.8, 0.2), x="cycle", y="risk_no_add_test", color="brown", alpha=0.3, ax=axis0)
    data_frame.plot(kind="line", x="cycle", y="batchCntLuebeck", color="red", alpha=0.1, ax=axis1, legend=None)
    data_frame.plot(kind="line", x="cycle", y="batchCntChandigarh", color="blue", alpha=0.1, ax=axis1, legend=None)

    axis0.plot(filtered_risk_diff_ub, linewidth=3, color=(1.0, 1.0, 1.0))
    axis0.plot(filtered_risk_diff_ub, linewidth=2, linestyle=(0, (10 ,10)), color=((0.2, 0.2, 0.2)), label="risk_diff_ub filtered")

    axis0.plot(filtered_risk_diff_add, linewidth=3, color=(1.0, 1.0, 1.0))
    axis0.plot(filtered_risk_diff_add, linewidth=2, linestyle=(0, (10, 3, 3, 4)), color=((0.2, 0.2, 0.2)), label="risk_diff_add filtered")

    axis0.plot(filtered_risk_diff_add_ub, linewidth=3, color=(1.0, 1.0, 1.0))
    axis0.plot(filtered_risk_diff_add_ub, linewidth=2, linestyle=(0, (8, 2, 2,8)), color=((0.2, 0.2, 0.2)), label="risk_diff_add_ub filtered")

    axis0.plot(filtered_risk_diff_no_add_test, linewidth=3, color=(1.0, 1.0, 1.0,))
    axis0.plot(filtered_risk_diff_no_add_test, linewidth=2, linestyle=(0, (4, 6, 4, 6)), color=((0.2, 0.2, 0.2)), label="risk_diff_no_add_test filtered")

    axis0.legend(loc='upper center', bbox_to_anchor=(0.5, 1.05), ncol=3, fancybox=True, shadow=True, facecolor=(1.0, 1.0, 1.0), handlelength=5)
    axis0.set_ylabel(r'$\Delta_R(c) = \sum_{i \in I} \frac{p_{c, i} - p_{c - 1, i}}{2 + t_{c - 1, i}}$')
    axis1.set_ylabel("samples added")
    plt.savefig(input_name + "risk_diff.pdf", papertype="a4")

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
    axis0.set_xlabel("cycle")
    
    axis0.tick_params(axis='y', colors="blue", size=4, width=1.5)
    axis0.set_ylabel("delta p")
    
    axis1.tick_params(axis='y', colors="purple", size=4, width=1.5)
    axis1.set_ylabel("samples added")
    
    axis2.tick_params(axis='y', colors=(0.5, 0.5, 0.5), size=4, width=1.5)
    axis2.set_ylabel("number of new tests")

    axis0.legend(loc="upper left", frameon=False)
    axis2.legend(loc="center left", frameon=False)
    fig.savefig(input_name + "diff_p_new_tests.pdf", papertype="a4")

    plt.show()

if __name__ == '__main__':
    main()
