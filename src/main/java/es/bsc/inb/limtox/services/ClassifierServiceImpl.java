package es.bsc.inb.limtox.services;


import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import edu.stanford.nlp.classify.ColumnDataClassifier;
import edu.stanford.nlp.classify.GeneralDataset;
import edu.stanford.nlp.objectbank.ObjectBank;
import edu.stanford.nlp.util.Pair;
@Service
class ClassifierServiceImpl implements ClassifierService {

	static final Logger classifierLog = Logger.getLogger("classifierLog");
	
	public void crossValidation(String properitesParametersPath) {
		try {
			classifierLog.info("Training model with properties :  " +  properitesParametersPath);
			Properties propertiesParameters = this.loadPropertiesParameters(properitesParametersPath);
			classifierLog.info("The data set for training : " + propertiesParameters.getProperty("trainFile"));
			classifierLog.info("Cross Validation with k-folds : " + propertiesParameters.getProperty("crossValidationFolds"));
			
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
			
		    //cdc.serializeClassifier(propertiesParameters.getProperty("serializeTo")+".gz");
		    
		    /*FileOutputStream fout2 = new FileOutputStream(propertiesParameters.getProperty("serializeTo")+".ser2");
		    ObjectOutputStream oos2 = new ObjectOutputStream(fout2);
		    oos2.writeObject(cl);
		    oos2.close();*/
		    /*
		    FileOutputStream fout = new FileOutputStream(propertiesParameters.getProperty("serializeTo"));
		    ObjectOutputStream oos = new ObjectOutputStream(fout);
		    oos.writeObject(cl);
		    oos.close();*/
		    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void generateMulticlassEntityMentionTokens(String properitesParametersPath) {
		Properties propertiesParameters = this.loadPropertiesParameters(properitesParametersPath);
		classifierLog.info("The top entity mentions will contain the words from the features : " +  propertiesParameters.getProperty("printTo"));
		classifierLog.info("Entity Mentions Rules output : " +  propertiesParameters.getProperty("entityMentionsTokensRules"));
		FileOutputStream fos;
		File inputDirectory = new File(propertiesParameters.getProperty("printTo"));
		try { 
			fos = new FileOutputStream(propertiesParameters.getProperty("entityMentionsTokensRules"));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			for (String line : ObjectBank.getLineIterator(inputDirectory.getAbsolutePath(), "utf-8")) {
				//word
				if(line.contains("-SW#-")) {
					int i = line.lastIndexOf("-SW#-");
					String t = line.substring(i+5);
					i = t.lastIndexOf(")");
					t = t.substring(0, i);
					String[] data = t.split(",");
					bw.write(data[0]+"\t" + data[1] + "\n");		
					bw.flush();
				}
			}
			bw.close();
			fos.close();
		} catch (FileNotFoundException e) {
				classifierLog.error(" File not Found " + inputDirectory.getAbsolutePath(), e);
				
		} catch (IOException e) {
				classifierLog.error(" IOException " + inputDirectory.getAbsolutePath(), e);
		}
	}
	
	/**
	 * 
	 */
	public void generateClassificator(String properitesParametersPath) {
		try {
			Properties propertiesParameters = this.loadPropertiesParameters(properitesParametersPath);
			classifierLog.info("After cross validation results the model is generate with all the training dataset");
			classifierLog.info("The model will be stored in : " + propertiesParameters.getProperty("serializeTo"));
			classifierLog.info("The top : " + propertiesParameters.getProperty("printClassifierParam") + " features will be stored in : " +  propertiesParameters.getProperty("printTo"));
			ColumnDataClassifier cdc = new ColumnDataClassifier(properitesParametersPath);
			propertiesParameters.setProperty("crossValidationFolds", "0");
			cdc.trainClassifier(propertiesParameters.getProperty("trainFile"));			
			classifierLog.info("The model was generated.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Un ultimo test con otro data set distinto al del cross validation
	 */
	public void testClassificator(String properitesParametersPath) {
		try {
			Properties propertiesParameters = this.loadPropertiesParameters(properitesParametersPath);
			classifierLog.info("A test with a diferent dataset of the cross validation and training model to generate de classifier");
			classifierLog.info("The model is : " + propertiesParameters.getProperty("serializeTo"));
			classifierLog.info("The test data set is  : " + propertiesParameters.getProperty("testFile"));
			
			//Levantar ColumnDataClassifier
			ByteArrayInputStream bais = new ByteArrayInputStream(FileUtils.readFileToByteArray(new File(propertiesParameters.getProperty("serializeTo"))));
		    ObjectInputStream ois = new ObjectInputStream(bais);
		    ColumnDataClassifier cdc = ColumnDataClassifier.getClassifier(ois);
		    ois.close();
			
			Pair<Double, Double> performance = cdc.testClassifier(propertiesParameters.getProperty("testFile"));
			System.out.printf("Accuracy: %.3f; macro-F1: %.3f%n", performance.first(), performance.second());		
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
