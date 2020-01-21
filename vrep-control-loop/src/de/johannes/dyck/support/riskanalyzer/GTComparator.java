package de.johannes.dyck.support.riskanalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GTComparator {

	public static Double[] pArray;
	public static Double[] tArray;
	public static Double[] tArrayRounded;
	public static Double[] tArrayRoundedC;
		
	private static String filePath;
	
	public static void main (String[] args) {
		String city = "./res/Lu";
		String tOrR = "T";		
		
		String gtHashes = city + "-gt-hashValues.txt";
		String gtPValues = city + "-gt-histogram.txt";		
		
		String hashes = city + "-hashValues.txt";
		String pValues = city + "-histogram.txt";
		
		String tValuesReal = city + "-min" + tOrR + "tValuesReal.txt";
		String tValuesInt = city + "-min" + tOrR + "tValuesInt.txt";
		
		System.out.println(city + tOrR);
		Set<String> hashesSet = new HashSet<String>(); 
		readInSet(hashes, hashesSet);
		
		Set<String> hashesSetGT = new HashSet<String>(); 
		readInSet(gtHashes, hashesSetGT);
		
		System.out.println("hashesGT in hashes: " + setInSet(hashesSetGT, hashesSet));
		System.out.println("hashes in hashesGT: " + setInSet(hashesSet, hashesSetGT));
		
		System.out.println("hashesGT not in hashes: " + setNotInSet(hashesSetGT, hashesSet));
		System.out.println("hashes not in hashesGT: " + setNotInSet(hashesSet, hashesSetGT));
		
		//Stream.of(tArray).forEach(d -> System.out.println(d));
		//Stream.of(tArrayRoundedC).forEach(d -> System.out.println(d));
		
	}
	
	private static int setInSet(Set<String> set1, Set<String> set2) {
		int count = 0;
		for (String s : set1) {
			if (set2.contains(s)) {
				count++;
			}
		}
		return count;
	}
	
	private static int setNotInSet(Set<String> set1, Set<String> set2) {
		int count = 0;
		for (String s : set1) {
			if (!set2.contains(s)) {
				count++;
			}
		}
		return count;
	}
	
	
	public static void readInSet(String fileName, Set<String> set) {		
		try {
			File file = new File(fileName);
			filePath = file.getAbsolutePath();
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			
			String current = br.readLine();
			int count = 0;
			while (current != null) {
				count++;								
				current = br.readLine();
				set.add(current);
			}
			br.close();
		} catch (IOException e) {
			System.out.println(e);
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
