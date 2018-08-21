package es.bsc.inb.limtox.services;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import edu.stanford.nlp.classify.Classifier;
import edu.stanford.nlp.classify.ColumnDataClassifier;
import edu.stanford.nlp.classify.GeneralDataset;
import edu.stanford.nlp.classify.LinearClassifier;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.util.Pair;
@Service
class ClassifierServiceImpl implements ClassifierService {

	static final Logger classifierLog = Logger.getLogger("classifierLog");
	
	public void trainAndTestClassifier(String properitesParametersPath) {
		try {
			
			classifierLog.info("Training model with properties :  " +  properitesParametersPath);
			Properties propertiesParameters = this.loadPropertiesParameters(properitesParametersPath);
			classifierLog.info("The data set for training : " + propertiesParameters.getProperty("trainFile"));
			classifierLog.info("Cross Validation with k-folds : " + propertiesParameters.getProperty("crossValidationFolds"));
			classifierLog.info("The model will be stored in : " + propertiesParameters.getProperty("serializeTo"));
			classifierLog.info("The top : " + propertiesParameters.getProperty("printClassifierParam") + " features will be stored in : " +  propertiesParameters.getProperty("printTo"));
			
			ColumnDataClassifier cdc = new ColumnDataClassifier(properitesParametersPath);
			//tiene que sacarlo del archivo de properties o sino pasarlo tb como parametro ...
			
			//cdc.trainClassifier("/home/jcorvi/text_tox_test/pubmed/classificator/dataset.txt");
			
			/* Cross Validation */
			Pair<GeneralDataset<String,String>, List<String[]>> dataInfo = cdc.readAndReturnTrainingExamples(propertiesParameters.getProperty("trainFile"));
		    GeneralDataset<String,String> train = dataInfo.first();
		    List<String[]> lineInfos = dataInfo.second();
			Pair<Double,Double> results = cdc.crossValidate(train, lineInfos);
		    /*Cross Validation Results*/
			classifierLog.info("Cross Validation Results");
			classifierLog.info("Average accuracy/micro-averaged F1: " + results.first);
		    classifierLog.info("Average macro-averaged F1: " + results.second);
			
		    // build the classifier and print the features again but will all the train dataset
		    Classifier<String,String> cl = cdc.makeClassifier(train);
		    printClassifier(cl, propertiesParameters.getProperty("printClassifier"), new Integer(propertiesParameters.getProperty("printClassifierParam")), propertiesParameters.getProperty("printTo"),null);
		    
		    //cdc.serializeClassifier(propertiesParameters.getProperty("serializeTo")+".gz");
		    
		    /*FileOutputStream fout2 = new FileOutputStream(propertiesParameters.getProperty("serializeTo")+".ser2");
		    ObjectOutputStream oos2 = new ObjectOutputStream(fout2);
		    oos2.writeObject(cl);
		    oos2.close();*/
		    
		    FileOutputStream fout = new FileOutputStream(propertiesParameters.getProperty("serializeTo"));
		    ObjectOutputStream oos = new ObjectOutputStream(fout);
		    oos.writeObject(cl);
		    oos.close();
		    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	 private void printClassifier(Classifier classifier, String printClassifier, int printClassifierParam, String printTo, String enconding ) {
		    String classString;
		    if (classifier instanceof LinearClassifier<?,?>) {
		      classString = ((LinearClassifier<?,?>)classifier).toString(printClassifier, printClassifierParam);
		    } else {
		      classString = classifier.toString();
		    }
		    if (printTo != null) {
		      PrintWriter fw = null;
		      try {
		        fw = IOUtils.getPrintWriter(printTo, enconding);
		        fw.write(classString);
		        fw.println();
		      } catch (IOException ioe) {
		    	  classifierLog.warn(ioe);
		      } finally {
		        IOUtils.closeIgnoringExceptions(fw);
		      }
		      classifierLog.info("Built classifier described in file " + printTo);
		    } else {
		    classifierLog.info("Built this classifier: " + classString);
		    }
		  }

	 
	 /**
	  * Load Properties
	  * @param properitesParametersPath
	  */
	 public Properties loadPropertiesParameters(String properitesParametersPath) {
		 Properties prop = new Properties();
		 InputStream input = null;
		 try {
			 input = new FileInputStream(properitesParametersPath);
			 // load a properties file
			 prop.load(input);
			 return prop;
		 } catch (IOException ex) {
			 ex.printStackTrace();
		 } finally {
			 if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			 }
		}
		return null;
	 }
	 
}
