package de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.anglesimple;

import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class AngleHash
{
    private double _elementLength;
    private int _numOfElems;
    public static final char[] ALPHABET = 
        {
                '0', //1
                '1', //2
                '2', //3 
                '3', //4
                '4', //5
                '5', //6
                '6', //7
                '7', //8
                '8', //9
                '9', //10
                'a', //11
                'b', //12
                'c', //13
                'd', //14
                'e', //15
                'f', //16
                'g', //17
                'h', //18
                'i', //19
                'j', //20
                'k', //21 
                'l', //22
                'm', //23
                'o', //24
                'p', //25
                'q', //26
                'r', //27
                's', //28 
                't', //29
                'u', //30
                'v', //31
                'w', //32
                'x', //33
        };

    public AngleHash(double elementLength, int numOfElems)
    {
        _elementLength = elementLength;
        _numOfElems = numOfElems;
    }

    public String hash(List<TrajectoryElement> trajectory)
    {
        StringBuilder result = new StringBuilder();
        for (int idx = 0; idx < trajectory.size() - 1; idx++)
        {
            TrajectoryElement e1 = trajectory.get(idx);
            TrajectoryElement e2 = trajectory.get(idx + 1);
            Vector2D v1 = e1.getVector();
            Vector2D v2 = e2.getVector();
            double angleDiff = Vector2D.computeAngle(v1, v2);
            double velocityDiff = e2.getVelocity() - e1.getVelocity();
            double side = v1.side(v2.getTip());
            Character hashDigit = hashDigit(angleDiff, velocityDiff, side);
            result.append(hashDigit);
        }
        return result.toString();
    }

    private Character hashDigitFavorAngle(double angleDiff, double velocityDiff, double side)
    {
        int anglePart = (int)((angleDiff * side + 180.0) / 36); //should be between 0 and 9
        int velocityPart = velocityDiff < 0.01 ? 0 : (velocityDiff > 0.01 ? 2 : 1);
        int numericalResult = anglePart; // * 3 + velocityPart; 
        numericalResult = numericalResult > ALPHABET.length ? ALPHABET.length : numericalResult;
        numericalResult = numericalResult < 0 ? 0 : numericalResult;
        return ALPHABET[numericalResult];
    }

    private Character hashDigit(double angleDiff, double velocityDiff, double side)
    {
        int anglePart = (int)((angleDiff * side + 180.0) / 36); //should be between 0 and 9
        int velocityPart = velocityDiff < 0.01 ? 0 : (velocityDiff > 0.01 ? 2 : 1);
        int numericalResult = anglePart;  
        numericalResult = numericalResult > ALPHABET.length ? ALPHABET.length : numericalResult;
        numericalResult = numericalResult < 0 ? 0 : numericalResult;
        return ALPHABET[numericalResult];
    }

    private Character hashDigitFavorAcceleration(double angleDiff, double velocityDiff, double side)
    {
        int anglePart = (int)((angleDiff * side + 180.0) / 36); //should be between 0 and 9
        int velocityPart = velocityDiff < 0.01 ? 0 : (velocityDiff > 0.01 ? 2 : 1);
        int numericalResult = anglePart + 10 * velocityPart; 
        numericalResult = numericalResult > 29 ? 29 : numericalResult;
        numericalResult = numericalResult < 0 ? 0 : numericalResult;
        return ALPHABET[numericalResult];
    }
}
