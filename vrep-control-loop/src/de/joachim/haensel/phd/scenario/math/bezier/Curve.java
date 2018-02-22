package de.joachim.haensel.phd.scenario.math.bezier;

public class Curve
{
    public static final Cubic[] calcCurve(int n, float[] axis)
    {
        float[] gamma = new float[n + 1];
        float[] delta = new float[n + 1];
        float[] d = new float[n + 1];
        Cubic[] c = new Cubic[n + 0];

        // gamma
        gamma[0] = 0.5f;
        for (int i = 1; i < n; i++)
            gamma[i] = 1.0f / (4.0f - gamma[i - 1]);
        gamma[n] = 1.0f / (2.0f - gamma[n - 1]);

        // delta
        delta[0] = 3.0f * (axis[1] - axis[0]) * gamma[0];
        for (int i = 1; i < n; i++)
            delta[i] = (3.0f * (axis[i + 1] - axis[i - 1]) - delta[i - 1]) * gamma[i];
        delta[n] = (3.0f * (axis[n] - axis[n - 1]) - delta[n - 1]) * gamma[n];

        // d
        d[n] = delta[n];
        for (int i = n - 1; i >= 0; i--)
            d[i] = delta[i] - gamma[i] * d[i + 1];

        // c
        for (int i = 0; i < n; i++)
        {
            float x0 = axis[i + 0];
            float x1 = axis[i + 1];
            float d0 = d[i + 0];
            float d1 = d[i + 1];
            c[i] = new Cubic(x0, d0, 3.0f * (x1 - x0) - 2.0f * d0 - d1, 2.0f * (x0 - x1) + d0 + d1);
        }
        return c;
    }
}