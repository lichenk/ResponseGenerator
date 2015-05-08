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
 *    Modified for email classification
 *
 */

package weka.api;
import weka.core.converters.CSVLoader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;
import weka.filters.unsupervised.attribute.Remove;
import java.io.File;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.filters.Filter;


public class HelloWeka {
  public static final String INPUT_CSV = "/home/usert/Dropbox/ml/ResponseGenerator/ReduceThemes/Final.csv";
  /** the classifier used internally */
  protected Classifier m_Classifier = null;

  /** the filter to use */
  protected Filter m_Filter = null;

  /** the training instances */
  protected Instances m_Training = null;

  /** for evaluating the classifier */
  protected Evaluation m_Evaluation = null;

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
    m_Classifier = AbstractClassifier.forName(name, options);
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
   * sets the file to use for training
   */
  public void setTraining(Instances trainingSet) throws Exception {
    m_Training = trainingSet;
  }

  /**
   * runs 10fold CV over the training file
   */
  public void execute(int wantedIndex) throws Exception {
    // run filter
    m_Filter.setInputFormat(m_Training);
    Instances filtered = Filter.useFilter(m_Training, m_Filter);
	String name=filtered.attribute(filtered.numAttributes()-1).name();
	System.out.println("Class name:" + name);
    filtered.setClassIndex(filtered.numAttributes()-1);
    // train classifier on complete file for tree
    m_Classifier.buildClassifier(filtered);

    // 10fold CV with seed=1
    m_Evaluation = new Evaluation(filtered);
    m_Evaluation.crossValidateModel(m_Classifier, filtered, 10,
      m_Training.getRandomNumberGenerator(1));
  }

  /**
   * outputs some data about the classifier
   */
  @Override
  public String toString() {
    StringBuffer result;

    result = new StringBuffer();
    result.append("Weka - Demo\n===========\n\n");

    result.append("Classifier...: " + Utils.toCommandLine(m_Classifier) + "\n");
    if (m_Filter instanceof OptionHandler) {
      result.append("Filter.......: " + m_Filter.getClass().getName() + " "
        + Utils.joinOptions(((OptionHandler) m_Filter).getOptions()) + "\n");
    } else {
      result.append("Filter.......: " + m_Filter.getClass().getName() + "\n");
    }
    result.append("\n");

    result.append(m_Classifier.toString() + "\n");
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

    return result.toString();
  }
  public static Filter makeFilter(Instances dataset, int startIndex, int wantedIndex) throws Exception {
	    // Set filter
		String[] options_rm = new String[2];
		options_rm[0] = "-R";                                    // "range"
	  	int endIndex = dataset.numAttributes() - 1;
	  	// Remove API is 1-indexed, not 0 indexed
	  	if (wantedIndex == startIndex) {
	  		options_rm[1] = Integer.toString(startIndex+1+1) + "-" + Integer.toString(endIndex+1);
	  	}
	  	else if (wantedIndex == endIndex) {
	  		options_rm[1] = Integer.toString(startIndex+1) + "-" + Integer.toString(endIndex-1+1);
	  	}
	  	else {
	  		options_rm[1] = Integer.toString(startIndex+1)+"-"+Integer.toString(wantedIndex-1+1)+","+Integer.toString(wantedIndex+1+1)+"-"+Integer.toString(endIndex+1);
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
  public static void main(String[] args) throws Exception {
    HelloWeka demo;
	CSVLoader loader = new CSVLoader();
	loader.setSource(new File(INPUT_CSV));
	Instances dataset = loader.getDataSet();
	// Set classifier
	String classifier = "weka.classifiers.trees.J48";
	String[] options = new String[4];
	options[0] = "-C"; options[1] = "0.11";
	options[2] = "-M"; options[3] = "3";
    // run
    demo = new HelloWeka();
    demo.setClassifier(classifier, options);
    int firstThemeIndex = getFirstThemeIndex(dataset);
    for (int wantedIndex = firstThemeIndex; wantedIndex < dataset.numAttributes(); wantedIndex++) {
    	Filter filter = makeFilter(dataset,firstThemeIndex,wantedIndex);
    	demo.setFilter(filter);
    	demo.setTraining(dataset);
    	demo.execute(wantedIndex);
    	System.out.println(demo.toString());
    }
  }
}
