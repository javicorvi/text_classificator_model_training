package es.bsc.inb.limtox.services;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import edu.stanford.nlp.classify.Classifier;
import edu.stanford.nlp.classify.ColumnDataClassifier;
import edu.stanford.nlp.classify.LinearClassifier;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.objectbank.ObjectBank;
import edu.stanford.nlp.util.ErasureUtils;
import edu.stanford.nlp.util.Pair;
@PropertySource({ "classpath:hepatotoxicity_training.properties" })
@Service
class ClassifierServiceImpl implements ClassifierService {
	
	@Autowired
    private Environment env;
	
	static final Logger classifierLog = Logger.getLogger("classifierLog");
	
	public void trainAndTestClassifier() {
		try {
			classifierLog.info("Training with hepatotoxicity_training.properties " );
			ColumnDataClassifier cdc = new ColumnDataClassifier("/home/jcorvi/eclipse-workspace/standard_tokenization/src/main/resources/hepatotoxicity_training.properties");
			cdc.trainClassifier("examples/cheeseDisease.train");
			System.out.println("Testing predictions of ColumnDataClassifier");
			for (String line : ObjectBank.getLineIterator("examples/cheeseDisease.test", "utf-8")) {
				// instead of the method in the line below, if you have the individual elements
				// already you can use cdc.makeDatumFromStrings(String[])
				Datum<String,String> d = cdc.makeDatumFromLine(line);
				System.out.printf("%s  ==>  %s (%.4f)%n", line, cdc.classOf(d), cdc.scoresOf(d).getCount(cdc.classOf(d)));
		    }
			 
			System.out.println();
			System.out.println("Testing accuracy of ColumnDataClassifier");
			Pair<Double, Double> performance = cdc.testClassifier("examples/cheeseDisease.test");
			System.out.printf("Accuracy: %.3f; macro-F1: %.3f%n", performance.first(), performance.second());
			
			demonstrateSerialization();
			demonstrateSerializationColumnDataClassifier();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


  private static void demonstrateSerialization() throws IOException, ClassNotFoundException {
    System.out.println();
    System.out.println("Demonstrating working with a serialized classifier");
    ColumnDataClassifier cdc = new ColumnDataClassifier("examples/cheese2007.prop");
    Classifier<String,String> cl =
            cdc.makeClassifier(cdc.readTrainingExamples("examples/cheeseDisease.train"));

    // Exhibit serialization and deserialization working. Serialized to bytes in memory for simplicity
    System.out.println();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(cl);
    oos.close();

    byte[] object = baos.toByteArray();
    ByteArrayInputStream bais = new ByteArrayInputStream(object);
    ObjectInputStream ois = new ObjectInputStream(bais);
    LinearClassifier<String,String> lc = ErasureUtils.uncheckedCast(ois.readObject());
    ois.close();
    ColumnDataClassifier cdc2 = new ColumnDataClassifier("examples/cheese2007.prop");

    // We compare the output of the deserialized classifier lc versus the original one cl
    // For both we use a ColumnDataClassifier to convert text lines to examples
    System.out.println();
    System.out.println("Making predictions with both classifiers");
    for (String line : ObjectBank.getLineIterator("examples/cheeseDisease.test", "utf-8")) {
      Datum<String,String> d = cdc.makeDatumFromLine(line);
      Datum<String,String> d2 = cdc2.makeDatumFromLine(line);
      System.out.printf("%s  =origi=>  %s (%.4f)%n", line, cl.classOf(d), cl.scoresOf(d).getCount(cl.classOf(d)));
      System.out.printf("%s  =deser=>  %s (%.4f)%n", line, lc.classOf(d2), lc.scoresOf(d).getCount(lc.classOf(d)));
    }
  }

  private static void demonstrateSerializationColumnDataClassifier() throws IOException, ClassNotFoundException {
    System.out.println();
    System.out.println("Demonstrating working with a serialized classifier using serializeTo");
    ColumnDataClassifier cdc = new ColumnDataClassifier("examples/cheese2007.prop");
    cdc.trainClassifier("examples/cheeseDisease.train");

    // Exhibit serialization and deserialization working. Serialized to bytes in memory for simplicity
    System.out.println();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    cdc.serializeClassifier(oos);
    oos.close();

    byte[] object = baos.toByteArray();
    ByteArrayInputStream bais = new ByteArrayInputStream(object);
    ObjectInputStream ois = new ObjectInputStream(bais);
    ColumnDataClassifier cdc2 = ColumnDataClassifier.getClassifier(ois);
    ois.close();

    // We compare the output of the deserialized classifier cdc2 versus the original one cl
    // For both we use a ColumnDataClassifier to convert text lines to examples
    System.out.println("Making predictions with both classifiers");
    for (String line : ObjectBank.getLineIterator("examples/cheeseDisease.test", "utf-8")) {
      Datum<String,String> d = cdc.makeDatumFromLine(line);
      Datum<String,String> d2 = cdc2.makeDatumFromLine(line);
      System.out.printf("%s  =origi=>  %s (%.4f)%n", line, cdc.classOf(d), cdc.scoresOf(d).getCount(cdc.classOf(d)));
      System.out.printf("%s  =deser=>  %s (%.4f)%n", line, cdc2.classOf(d2), cdc2.scoresOf(d).getCount(cdc2.classOf(d)));
    }
  }
  
  /**
   * 
   */
  public static void execute_news_classification() {
	  System.out.println("Training ColumnDataClassifier");
	  String dev_train = "examples/20news-bydate-devtrain-stanford-classifier.txt";
	  try {
		 ColumnDataClassifier cdc = new ColumnDataClassifier("examples/news.prop");
		 cdc.trainClassifier(dev_train);
		 Pair<Double, Double> performance = cdc.testClassifier("examples/20news-bydate-devtest-stanford-classifier.txt");
		 System.out.printf("Accuracy: %.3f; macro-F1: %.3f%n", performance.first(), performance.second());
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
  
  

}
