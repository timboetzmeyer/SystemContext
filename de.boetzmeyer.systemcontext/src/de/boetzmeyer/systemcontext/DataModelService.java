package de.boetzmeyer.systemcontext;

import java.util.List;

import de.boetzmeyer.systemmodel.ApplicationConfig;
import de.boetzmeyer.systemmodel.DataModel;
import de.boetzmeyer.systemmodel.ModelAttribute;
import de.boetzmeyer.systemmodel.ModelDataType;
import de.boetzmeyer.systemmodel.ModelEntity;
import de.boetzmeyer.systemmodel.ModelReference;
import de.boetzmeyer.systemmodel.SystemModel;

public interface DataModelService {

	DataModel addDataModel(DataModel inDataModel);

	ModelAttribute addModelAttribute(ModelAttribute inModelAttribute);

	ModelReference addModelReference(ModelReference inModelReference);

	ModelDataType addModelDataType(ModelDataType inModelDataType);

	ModelEntity addModelEntity(ModelEntity inModelEntity);
	
	List<DataModel> getDataModels(ApplicationConfig inApp);

	SystemModel getDataModel(DataModel inDataModel);

}
