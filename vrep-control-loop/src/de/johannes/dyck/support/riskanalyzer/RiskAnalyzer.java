package de.johannes.dyck.support.riskanalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RiskAnalyzer {

	public static Double[] pArray;
	public static Double[] tArray;
	public static Double[] tArrayRounded;
	public static Double[] tArrayRoundedC;
		
	private static String filePath;
	
	public static void main (String[] args) {
		//String fileName = "histogrampvalues.txt";
		//String fileName = "histogrampvalues1.txt";
		String fileName = "Lu-histogram.txt";
		//String fileName = "old/shortEx.txt";
		
		//double risk = 0.1;
		double risk = 0.01;
		minimizeTestSum(risk, fileName);
		
		System.out.println();
		//System.out.println();
		
		double testSum = 50000;
		minimizeRisk(testSum, fileName);
		
		System.out.println();
		
		//Stream.of(tArray).forEach(d -> System.out.println(d));
		//Stream.of(tArrayRoundedC).forEach(d -> System.out.println(d));
		
	}
	
	public static void minimizeTestSum(Double risk, String fileName) {
		readInPArray(fileName);		
		Stream<Double> stream = findOptimalTsForRisk(Stream.of(pArray), Stream.of(pArray), risk);
		tArray = (Double[]) stream.toArray(Double[]::new);
		//Iterator<Double> tIter = Stream.of(tArray).iterator();		
		//Iterator<Double> pIter = Stream.of(pArray).iterator();
		
		double riskSumReal = computeRisk(pArray, tArray);
		double testSumReal = Stream.of(tArray).collect(Collectors.summingDouble(d -> d));
		
		System.out.println("R = " + riskSumReal + ", real-valued");
		System.out.println("T = " + testSumReal + ", real-valued (lower bound)");
		System.out.println("==================");
		
		tArrayRounded = (Double[]) round(Stream.of(tArray)).toArray(Double[]::new);
		tArrayRoundedC = roundUpDown(tArray, false);
		
		double riskSumIntRounded = computeRisk(pArray, tArrayRounded);
		double testSumIntRounded = Stream.of(tArrayRounded).collect(Collectors.summingDouble(d -> d));
		System.out.println("R = " + riskSumIntRounded + ", integer, rounded up");
		System.out.println("T = " + testSumIntRounded + ", integer, rounded up");
		System.out.println("DiffT: " + (testSumIntRounded - testSumReal) + ", ratioT: " + ((testSumIntRounded - testSumReal) * 100 / testSumReal) + "%, rounded up");
		System.out.println("DiffR: " + (riskSumReal - riskSumIntRounded) + ", ratioR: " + ((riskSumReal - riskSumIntRounded) * 100 / riskSumReal) + "%, rounded up");
		System.out.println("==================");
		
		double riskSumIntRoundedC = computeRisk(pArray, tArrayRoundedC);
		double testSumIntRoundedC = Stream.of(tArrayRoundedC).collect(Collectors.summingDouble(d -> d));
		System.out.println("R = " + riskSumIntRoundedC + ", integer, rounded up and down");
		System.out.println("T = " + testSumIntRoundedC + ", integer, rounded up and down");
		System.out.println("DiffT: " + (testSumIntRoundedC - testSumReal) + ", ratioT: " + ((testSumIntRoundedC - testSumReal) * 100 / testSumReal) + "%, rounded up and down");
		System.out.println("DiffR: " + (riskSumReal - riskSumIntRoundedC) + ", ratioR: " + ((riskSumReal - riskSumIntRoundedC) * 100 / riskSumReal) + "%, rounded up");
		System.out.println("==================");
		
		Map<Double, Long> valuesEncountered = new HashMap<Double, Long>();
		for (Double d : tArrayRoundedC) {			
			if (valuesEncountered.get(d) != null) {
				valuesEncountered.put(d, valuesEncountered.get(d) + 1);
			} else {
				valuesEncountered.put(d, Long.valueOf(1));
			}
		}
		for (Map.Entry<Double, Long> entry : valuesEncountered.entrySet()) {
			//System.out.println(entry);
		}
		//System.out.println();
		valuesEncountered = new HashMap<Double, Long>();
		for (Double d : tArray) {			
			if (valuesEncountered.get(d) != null) {
				valuesEncountered.put(d, valuesEncountered.get(d) + 1);
			} else {
				valuesEncountered.put(d, Long.valueOf(1));
			}
		}
		
		//output
		List<String> tValues = Stream.of(tArray).map(value -> Double.toString(value)).collect(Collectors.toList());
		List<String> tValuesRoundedC = Stream.of(tArrayRoundedC).map(value -> Double.toString(value)).collect(Collectors.toList());
		
		try {
			Files.write(Paths.get(filePath + "minTtValuesReal.txt"), tValues, Charset.defaultCharset());
			Files.write(Paths.get(filePath + "minTtValuesInt.txt"), tValuesRoundedC, Charset.defaultCharset());
		} catch (IOException e) {
			System.out.println(e);
		}
		
	}
	
	public static void minimizeRisk(Double testSum, String fileName) {
		readInPArray(fileName);
		Stream<Double> stream = findOptimalTsForTestSum(Stream.of(pArray), Stream.of(pArray), testSum);
		tArray = (Double[]) stream.toArray(Double[]::new);
		//Iterator<Double> tIter = Stream.of(tArray).iterator();		
		//Iterator<Double> pIter = Stream.of(pArray).iterator();
		
		double riskSumReal = computeRisk(pArray, tArray);
		double testSumReal = Stream.of(tArray).collect(Collectors.summingDouble(d -> d));
		
		System.out.println("R = " + riskSumReal + ", real-valued");
		System.out.println("T = " + testSumReal + ", real-valued (lower bound)");
		System.out.println("==================");
		
		//tArrayRounded = (Double[]) round(Stream.of(tArray)).toArray(Double[]::new);
		tArrayRoundedC = roundUpDown(tArray, true);
		
		double riskSumIntRoundedC = computeRisk(pArray, tArrayRoundedC);
		double testSumIntRoundedC = Stream.of(tArrayRoundedC).collect(Collectors.summingDouble(d -> d));
		System.out.println("R = " + riskSumIntRoundedC + ", integer, rounded up");
		System.out.println("T = " + testSumIntRoundedC + ", integer, rounded up");
		System.out.println("DiffT: " + (testSumIntRoundedC - testSumReal) + ", ratioT: " + ((testSumIntRoundedC - testSumReal) * 100 / testSumReal) + "%, rounded");
		System.out.println("DiffR: " + (riskSumIntRoundedC - riskSumReal) + ", ratioR: " + ((riskSumIntRoundedC - riskSumReal) * 100 / riskSumReal) + "%, rounded");
		System.out.println("==================");
		
		/*
		double riskSumIntRoundedC = computeRisk(pArray, tArrayRoundedC);
		double testSumIntRoundedC = Stream.of(tArrayRoundedC).collect(Collectors.summingDouble(d -> d));
		System.out.println("R = " + riskSumIntRoundedC + ", integer, rounded up and down");
		System.out.println("T = " + testSumIntRoundedC + ", integer, rounded up and down");
		System.out.println("DiffT: " + (testSumIntRoundedC - testSumReal) + ", ratioT: " + ((testSumIntRoundedC - testSumReal) * 100 / testSumReal) + "%, rounded up and down");
		System.out.println("DiffR: " + (riskSumReal - riskSumIntRoundedC) + ", ratioR: " + ((riskSumReal - riskSumIntRoundedC) * 100 / riskSumReal) + "%, rounded up");
		System.out.println("==================");
		*/
		List<String> tValues = Stream.of(tArray).map(value -> Double.toString(value)).collect(Collectors.toList());
		List<String> tValuesRoundedC = Stream.of(tArrayRoundedC).map(value -> Double.toString(value)).collect(Collectors.toList());
		
		try {
			Files.write(Paths.get(filePath + "minRtValuesReal.txt"), tValues, Charset.defaultCharset());
			Files.write(Paths.get(filePath + "minRtValuesInt.txt"), tValuesRoundedC, Charset.defaultCharset());
		} catch (IOException e) {
			System.out.println(e);
		}
		
	}
	
	public static void readInPArray(String fileName) {		
		try {
			File file = new File("./res/" + fileName);
			filePath = file.getAbsolutePath();
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
						
			
			
			String current = br.readLine();
			int count = 0;
			while (current != null) {
				count++;								
				current = br.readLine();
			}
			pArray = new Double[count];
			tArray = new Double[count];
			
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			
			current = br.readLine();
			count = 0;
			while (current != null) {				
				Double value = Double.valueOf(current);
				pArray[count] = value;
				current = br.readLine();
				count++;
			}
			br.close();
		} catch (IOException e) {
			System.out.println(e);
		}
		
		double sum = Stream.of(pArray).collect(Collectors.summingDouble(d -> d));
		if (sum != 1.0) {
			System.out.println("not adding up to 1: " + sum);
		} else {
			System.out.println("adding up to 1: " + sum);
		}
		
		
	}
	
	public static double computeRisk(Iterator<Double> probIter, Iterator<Double> testIter) {		
		double result = 0;
		while (probIter.hasNext()) {
			Double p = probIter.next();
			Double t = testIter.next();
			result = result + p / (2 + t);	
		}
		return result;		
	}
	
	public static double computeRisk(boolean rounded) {
		double result = 0;
		for (int i = 0; i <= tArray.length - 1; i++) {
			result = result + pArray[i] / (2 + (rounded ? tArrayRounded[i] : tArray[i]));
		}		
		return result;		
	}
	
	public static double computeRisk(Double[] p, Double[] t) {
		double result = 0;
		for (int i = 0; i <= t.length - 1; i++) {
			result = result + p[i] / (2 + t[i]);
		}		
		return result;		
	}
	
	public static double computeRootSum(Stream<Double> stream) {
		return stream.collect(Collectors.summingDouble(d -> Math.sqrt(d)));/*
		double result = 0;
		while (iter.hasNext()) {
			Double d = iter.next();
			result = result + Math.sqrt(d);	
		}		
		return result;		*/
	}
	
	
	
	public static Stream<Double> round(Stream<Double> stream) {
		return stream.map(a -> a < 0 ? 0 : Math.ceil(a));
	}
	
	public static Double[] roundUpDown(Double[] array, boolean fixedSum) {
		Double[] result = new Double[array.length];
		
		if (!fixedSum) {
			double riskBuffer = 0;
			for (int i = 0; i <= array.length - 1; i++) {
				if (array[i] <= 0) {
					//System.out.println("sdfdfs");
					result[i] = 0.0;
					riskBuffer = riskBuffer + pArray[i] / (2 + array[i]) - pArray[i] / 2;  
				} else {
					double floor = Math.floor(array[i]);
					double lostRisk = pArray[i] / (2 + floor) - pArray[i] / (2 + array[i]);
					if (lostRisk <= riskBuffer) {
						result[i] = floor;
						riskBuffer = riskBuffer - lostRisk;
					} else {
						double ceiling = Math.ceil(array[i]);
						result[i] = ceiling;
						riskBuffer = riskBuffer - pArray[i] / (2 + ceiling) + pArray[i] / (2 + array[i]);
					}
				}
			}
		} else {
			double testBuffer = 0;
			for (int i = 0; i <= array.length - 1; i++) {
				if (i == array.length - 1 && testBuffer > 0) {
					result[i] = Math.floor(array[i]);
					testBuffer = testBuffer - result[i];
				} else if (array[i] <= 0) {
					result[i] = 0.0;
					testBuffer = testBuffer - array[i];  
				} else {
					double floor = Math.floor(array[i]);
					double lostTests = array[i] - floor;
					if (lostTests <= testBuffer) {
						result[i] = floor;
						testBuffer = testBuffer - lostTests;
					} else {
						double ceiling = Math.ceil(array[i]);
						result[i] = ceiling;
						testBuffer = testBuffer + ceiling - array[i];
					} 
				}
			}
		}
				
		return result;
	}
	
	public static Stream<Double> findOptimalTsForRisk (Stream<Double> stream1, Stream<Double> stream2, double upperBound) {
		Double imd = computeRootSum(stream2);
		return stream1.map(a -> Math.sqrt(a) / upperBound * imd - 2);
	}
	
	public static Stream<Double> findOptimalTsForTestSum (Stream<Double> stream1, Stream<Double> stream2, double testSum) {
		Double imd = computeRootSum(stream2);
		return stream1.map(a -> Math.sqrt(a) * (testSum + 2 * pArray.length) / imd - 2);
	}
	
	
}
