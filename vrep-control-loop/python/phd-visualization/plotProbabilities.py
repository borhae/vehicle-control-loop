import math


def main():
    T = 8
    p = [1.0]
    r_s = risk_t_self_defined(p, [1.0], T)
    print("risk: %5.2f\n" % r_s)

    r_d = risk_t_self_defined([0.3, 0.7], [0.3, 0.7], T)
    print("dual profile risk: %5.2f\n" % r_d)

    r_d = risk_t_self_defined([0.5, 0.5], [0.5, 0.5], T)
    print("dual profile risk: %5.2f\n" % r_d)

    r_d_j = risk_johannes([0.5, 0.5], T)
    print("dual profile risk johannes: %5.2f\n" % r_d_j)

    r_d_j = risk_johannes([0.3, 0.7], T)
    print("dual profile risk: %5.2f\n" % r_d_j)


def risk_t_self_defined(p, t_w, T):
    r = 0.0;
    print(p)
    print(t_w)
    for p_i, t_w_i in zip(p, t_w):
        t_i = t_w_i * T;
        print(t_i);
        r += p_i / (2 + t_i)
    return r


def risk_johannes(p, T):
    r = 0.0;
    print(p)
    sqrt_sum : float = 0.0;
    for p_i in p:
        sqrt_sum += math.sqrt(p_i)
    for p_i in p:
        t_i = ((math.sqrt(p_i) * (T + 2 * len(p))) / sqrt_sum) - 2
        print(t_i)
        r += p_i / (2 + t_i)
    return r


if __name__ == '__main__':
    main()
