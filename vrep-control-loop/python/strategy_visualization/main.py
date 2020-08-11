import matplotlib.pyplot as plt
import pandas as pd

def main():
    data = pd.read_csv("../../evolveChandigarhToLuebeck_k200Addm6moreTests.csv")
    print(data.head())

    plt.figure()
    data.plot()

if __name__ == '__main__':
    main()
