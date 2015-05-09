/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    WekaDemo example.
 *    Copyright (C) 2009 University of Waikato, Hamilton, New Zealand
 *    Modified for email classification by 10-601 team members for (Email Reply Assistant)
 *
 */

package weka.api;
import weka.core.converters.CSVLoader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.supervised.instance.Resample;
import java.io.File;
//import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.Instance;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.filters.Filter;
import java.util.Arrays;
import java.io.*;

class MyEntity{
	double p;
	int idx;
    public MyEntity(double p, int idx){
        this.p = p;
        this.idx = idx;
    }
}
class MyComparator implements Comparator<MyEntity>{
	  public int compare(MyEntity p1, MyEntity p2){
	        if (p1.p < p2.p) return 1;
	        if (p1.p > p2.p) return -1;
	        return 0;
	  }
}

public class HelloWeka {
  //public static final String INPUT_CSV = "/home/usert/Dropbox/ml/ResponseGenerator/ReduceThemes/Final.csv";
  //public static final String TEST_CSV = "/home/usert/Dropbox/ml/ResponseGenerator/ReduceThemes/FinalTest.csv";
  public static final String INPUT_CSV = "/home/usert/Dropbox/ml/ResponseGenerator/EmailExtractor/src/EmailData.csv";
  public static final String TEST_CSV = "/home/usert/Dropbox/ml/ResponseGenerator/EmailExtractor/src/TestData.csv";
  public static final String CLASSIFIER_FILE = "/home/usert/Dropbox/ml/ResponseGenerator/Task3/classifier.txt";
  public static final String OUTPUT_FILE = "results.txt";
  /** the classifier used internally */
  protected Classifier m_Classifier = null;

  /** the filter to use */
  protected Filter m_Filter = null;

  /** the training instances */
  protected Instances m_Training = null;
  /** the test instances */
  protected Instances m_Test = null;

  /** for evaluating the classifier */
  protected Evaluation m_Evaluation = null;
  protected Evaluation testEvaluation = null;
  /**
   * initializes the demo
   */
  public HelloWeka() {
    super();
  }

  /**
   * sets the classifier to use
   * 
   * @param name the classname of the classifier
   * @param options the options for the classifier
   */
  public void setClassifier(String name, String[] options) throws Exception {
    m_Classifier = Classifier.forName(name, options);
  }

  /**
   * sets the filter to use
   * 
   * @param name the classname of the filter
   * @param options the options for the filter
   */
  public void setFilter(Filter filter) throws Exception {
    m_Filter = filter;
  }

  /**
   * sets the Instance to use for training
   */
  public void setTraining(Instances trainingSet) throws Exception {
    m_Training = trainingSet;
  }

  /**
   * sets the Instance to use for training
   */
  public void setTest(Instances testSet) throws Exception {
    m_Test = testSet;
  }

  
  /**
   * runs 10fold CV over the training file
   */
  public double execute(int wantedIndex, PrintWriter writer, double[][] wordProb, int wordIndex) throws Exception {
    // run filter
    m_Filter.setInputFormat(m_Training);
    Instances filtered = Filter.useFilter(m_Training, m_Filter);
    Instances filteredTest = Filter.useFilter(m_Test, m_Filter);
    filtered.setClassIndex(filtered.numAttributes()-1);
    filteredTest.setClassIndex(filteredTest.numAttributes()-1);
	Resample resample = new Resample();                         // new instance of filter
	String[] options = new String[2];
	options[0] = "-B"; options[1] = "0.5";
	resample.setOptions(options);                           // set options
	resample.setInputFormat(filtered);                          // inform filter about dataset **AFTER** setting options
    Instances resampled = Filter.useFilter(filtered, resample);
	String name=resampled.attribute(resampled.numAttributes()-1).name();
	writer.println("Class name:" + name);
    // train classifier on complete file for tree
    m_Classifier.buildClassifier(resampled);

    // 10fold CV with seed=1
    //m_Evaluation = new Evaluation(filtered);
    //m_Evaluation.crossValidateModel(m_Classifier, filtered, 10,
    //  m_Training.getRandomNumberGenerator(1));
    testEvaluation = new Evaluation(resampled);
    testEvaluation.evaluateModel(m_Classifier, filteredTest);
    int numInst = filteredTest.numInstances();
    
    for (int i = 0; i < numInst; i++) {
    	Instance testInst = filteredTest.instance(i);
    	double distr[] = m_Classifier.distributionForInstance(testInst);
    	wordProb[i][wordIndex] = distr[1];
    }
    return testEvaluation.pctCorrect();
  }

  /**
   * outputs some data about the classifier
   */
  @Override
  public String toString() {
    StringBuffer result;

    result = new StringBuffer();
    result.append("Weka - Demo\n===========\n\n");

    result.append("Classifier...: " 
            + m_Classifier.getClass().getName() + " " 
            + Utils.joinOptions(m_Classifier.getOptions()) + "\n");
    if (m_Filter instanceof OptionHandler) {
      result.append("Filter.......: " + m_Filter.getClass().getName() + " "
        + Utils.joinOptions(((OptionHandler) m_Filter).getOptions()) + "\n");
    } else {
      result.append("Filter.......: " + m_Filter.getClass().getName() + "\n");
    }
    result.append("\n");
    result.append(m_Classifier.toString() + "\n");
    /*
    result.append("\n10fold Cross Validation Results\n======\n");
    result.append(m_Evaluation.toSummaryString() + "\n");
    try {
      result.append(m_Evaluation.toMatrixString() + "\n");
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      result.append(m_Evaluation.toClassDetailsString() + "\n");
    } catch (Exception e) {
      e.printStackTrace();
    }
    */
    result.append(testEvaluation.toSummaryString("\nTest Set Results\n======\n", false));
    try {
        result.append(testEvaluation.toMatrixString() + "\n");
      } catch (Exception e) {
        //e.printStackTrace();
      }
      try {
        result.append(testEvaluation.toClassDetailsString() + "\n");
      } catch (Exception e) {
        //e.printStackTrace();
      }
    
    return result.toString();
  }
  public static Filter makeFilter(Instances dataset, int startIndex, int wantedIndex) throws Exception {
	    // Set filter
		String[] options_rm = new String[2];
		options_rm[0] = "-R";                                    // "range"
	  	int endIndex = dataset.numAttributes() - 1;
	  	// Remove API is 1-indexed, not 0 indexed
	  	options_rm[1] = Integer.toString(startIndex-3+1) + "-" + Integer.toString(startIndex-1+1) + ",";
	  	if (wantedIndex == startIndex) {
	  		options_rm[1] += Integer.toString(startIndex+1+1) + "-" + Integer.toString(endIndex+1);
	  	}
	  	else if (wantedIndex == endIndex) {
	  		options_rm[1] += Integer.toString(startIndex+1) + "-" + Integer.toString(endIndex-1+1);
	  	}
	  	else {
	  		options_rm[1] += Integer.toString(startIndex+1)+"-"+Integer.toString(wantedIndex-1+1)+","+Integer.toString(wantedIndex+1+1)+"-"+Integer.toString(endIndex+1);
	  	}
		Remove remove = new Remove();                         // new instance of filter
		remove.setOptions(options_rm);                           // set options
		remove.setInputFormat(dataset);                          // inform filter about dataset **AFTER** setting options
	   return remove;
  }
  public static int getFirstThemeIndex(Instances dataset) {
	  int answer = -1;
	  for (int i = dataset.numAttributes()-1; i >= 0; i--) {
		  String name=dataset.attribute(i).name();
		  if (name.contains("(Reply theme) ")) {
			  answer = i;
		  }
	  }
	  return answer;
  }
  public static void score(double[][] wordProb, int firstThemeIndex, int numWords, int numTestInst, Instances testset, String classifier, PrintWriter writer) {
	    double pointAvg = 0;
	    int ignore = 0;
	    List<Double> awards = new ArrayList<Double>();
	    for (int i = 0; i < numTestInst; i++) {
	    	List<MyEntity> list = new ArrayList<MyEntity>();
	    	int total = 0;
	    	for (int j = 0; j < numWords; j++) {
	    		list.add(new MyEntity(wordProb[i][j],j));
	    		if (testset.instance(i).stringValue(j+firstThemeIndex).equals("True")||
	    				testset.instance(i).stringValue(j+firstThemeIndex).equals("true")) {
	    			total++;
	    		}
	    	}
	    	Collections.sort(list,new MyComparator());
	    	int k = 0;
	    	int point = 0;
	    	for (MyEntity m : list) {
	    		if (k > 10) break;
	    		int themeIndex = m.idx + firstThemeIndex;
	    		if (testset.instance(i).stringValue(themeIndex).equals("True")||
	    				testset.instance(i).stringValue(themeIndex).equals("true")) {
	    			point++;
	    		}
	    		k++;
	    	}
	    	double award = 0;
	    	if (0 == total) {
	    		ignore++;
	    	}
	    	else if (total < 10) {
	    		award = (double)point/total;
	    		awards.add(award);
	    	}
	    	else {
	    		award = (double)point/10;
	    		awards.add(award);
	    	}
	    	pointAvg += award;
	    }
	    pointAvg /= (numTestInst-ignore);
	    double pointVariance = 0;
	    for (Double x : awards) {
	    	pointVariance += (x-pointAvg)*(x-pointAvg);
	    }
	    pointVariance /= awards.size();
	    writer.println(classifier + " Point average:" + pointAvg + " Variance:" + pointVariance + " Num Test Cases:" + awards.size());
	    System.out.println(classifier + " Point average:" + pointAvg + " Variance:" + pointVariance + " Num Test Cases:" + awards.size() + " Num theme words:" + numWords);
  }
  public static void main(String[] args) throws Exception {
    HelloWeka demo;
	CSVLoader loader = new CSVLoader();
	loader.setSource(new File(INPUT_CSV));
	Instances dataset = loader.getDataSet();
	CSVLoader loaderTest = new CSVLoader();
	loaderTest.setSource(new File(TEST_CSV));
	Instances testset = loaderTest.getDataSet();
	// Set classifier
	PrintWriter writer = new PrintWriter(OUTPUT_FILE, "UTF-8");
	BufferedReader br = new BufferedReader(new FileReader(CLASSIFIER_FILE));
	    String line;
	    while ((line = br.readLine()) != null) {
	       // process the line.
			String[] classOptions = line.split("\\s+");
			String classifier = classOptions[0];
		    int firstThemeIndex = getFirstThemeIndex(dataset);
		    int numWords = dataset.numAttributes() -firstThemeIndex;
		    int numTestInst = testset.numInstances();
		    double[][] wordProb = new double[numTestInst][numWords];
			if (classifier.equals("RANDOM_CONTROL")) {
				Random generator = new Random();
				for (int i = 0; i < numTestInst; i++) {
					for (int j = 0; j < numWords; j++) {
						wordProb[i][j] = generator.nextDouble();
					}
				}
				score(wordProb, firstThemeIndex, numWords, numTestInst, testset, classifier, writer);
				continue;
			}
			String[] options = new String[classOptions.length - 1];
			for (int i = 0; i < (classOptions.length - 1); i++) {
				options[i] = classOptions[i+1];
			}
		    // run
		    demo = new HelloWeka();
		    demo.setClassifier(classifier, options);
		    double accuracy = 0;
		    int num = 0;
		    int wordIndex = 0;
		    for (int wantedIndex = firstThemeIndex; wantedIndex < dataset.numAttributes(); wantedIndex++) {
		    	Filter filter = makeFilter(dataset,firstThemeIndex,wantedIndex);
		    	demo.setFilter(filter);
		    	demo.setTraining(dataset);
		    	demo.setTest(testset);
		    	accuracy += demo.execute(wantedIndex,writer,wordProb, wordIndex);
		    	num++;
		    	writer.println(demo.toString());
		    	wordIndex++;
		    }
		    writer.println(classifier + " Accuracy:" + ((double)accuracy)/num);
		    score(wordProb, firstThemeIndex, numWords, numTestInst, testset, classifier, writer);
	    }
 
	/*String classifier = "weka.classifiers.trees.J48";
	String[] options = new String[4];
	options[0] = "-C"; options[1] = "0.11";
	options[2] = "-M"; options[3] = "3";*/

  }
}
