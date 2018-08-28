package es.bsc.inb.limtox.services;

public interface ClassifierService {

	public void crossValidation(String propertiesPath);

	public void generateClassificator(String properties_parameters_path);
	
}
