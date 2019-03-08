package de.joachim.haensel.phd.scenario.experiment.setup;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.random.MersenneTwister;

public class PointTupleDistanceDistributionOptimization
{
    public static void main(String[] args)
    {
        try (Stream<String> stream = Files.lines(Paths.get("./res/roadnetworks/Chandigarhpoints.txt"))) 
        {  
            MersenneTwister randomGenerator = new MersenneTwister(4009l);
            List<Position2D> mapPositions = stream.map(curLine -> new Position2D(curLine)).collect(Collectors.toList());
            double fitness = computeMinDistanceFollowupPositions(mapPositions);
            int mapPositionsSize = mapPositions.size();
            int iterationCnt = 0;
            int successfulSwaps = 0;
            int maxIterations = 10000000;
            int maxSwaps = 10;
            double nrOfSwaps = maxSwaps;
            double steps = (1.0 / (double)maxIterations) * (double)maxSwaps;
            while(fitness < 6000.0 && iterationCnt < maxIterations)
            {
                List<Position2D> mutant = mutate(mapPositions, randomGenerator, mapPositionsSize, (int)nrOfSwaps + 1);
                double mutantFitness = computeMinDistanceFollowupPositions(mutant);
                if(mutantFitness > fitness)
                {
                    fitness = mutantFitness;
                    mapPositions = mutant;
                    successfulSwaps++;
                    System.out.println((int)nrOfSwaps + 1);
                }
                iterationCnt++;
                nrOfSwaps -= steps;
            }
            List<String> outContent = mapPositions.stream().map(curPos -> String.format(Locale.ENGLISH, "%.2f, %.2f", curPos.getX(), curPos.getY())).collect(Collectors.toList());
            Files.write(new File("./res/roadnetworks/Chandigarhpoints_spread.txt").toPath(), outContent, Charset.defaultCharset());
            System.out.println(steps);
            System.out.println(String.format("final fitness: %f, amount of iterations: %d, successfull mutations: %d", fitness, iterationCnt, successfulSwaps));
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }

    private static ArrayList<Position2D> mutate(List<Position2D> mapPositions, MersenneTwister randomGenerator, int mapPositionsSize, int nrOfSwaps)
    {
        ArrayList<Position2D> copy = new ArrayList<Position2D>(mapPositions);
        for(int cnt = 0; cnt < nrOfSwaps; cnt++)
        {
            int randomIdx1 = randomGenerator.nextInt(mapPositionsSize);
            int randomIdx2 = randomGenerator.nextInt(mapPositionsSize);
            if(randomIdx1 != randomIdx2)
            {
                Position2D tmp = copy.get(randomIdx1);
                copy.set(randomIdx1, copy.get(randomIdx2));
                copy.set(randomIdx2, tmp);
            }
        }
        return copy;
    }

    private static double computeMinDistanceFollowupPositions(List<Position2D> mapPositions)
    {
        double minDist = Double.POSITIVE_INFINITY;
        for(int idx = 0; idx < mapPositions.size() - 1; idx++)
        {
            Position2D pos1 = mapPositions.get(idx);
            Position2D pos2 = mapPositions.get(idx + 1);
            double curDistance = Position2D.distance(pos1, pos2);
            if(curDistance < minDist)
            {
                minDist = curDistance;
            }
        }
        return minDist;
    }
}
