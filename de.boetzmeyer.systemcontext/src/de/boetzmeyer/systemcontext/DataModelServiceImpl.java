package de.boetzmeyer.systemcontext;

import java.util.ArrayList;
import java.util.List;

import de.boetzmeyer.systemmodel.ApplicationConfig;
import de.boetzmeyer.systemmodel.ApplicationDataModel;
import de.boetzmeyer.systemmodel.DataModel;
import de.boetzmeyer.systemmodel.IServer;
import de.boetzmeyer.systemmodel.ModelAttribute;
import de.boetzmeyer.systemmodel.ModelDataType;
import de.boetzmeyer.systemmodel.ModelEntity;
import de.boetzmeyer.systemmodel.ModelReference;
import de.boetzmeyer.systemmodel.SystemModel;

final class DataModelServiceImpl extends SystemContextService implements DataModelService {

	public DataModelServiceImpl(final IServer inSystemAccess) {
		super(inSystemAccess);
	}

	@Override
	public DataModel addDataModel(DataModel inDataModel) {
		if ((inDataModel != null) && inDataModel.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addDataModel(inDataModel);
			model.save();
			return inDataModel;
		}
		return null;
	}

	@Override
	public ModelAttribute addModelAttribute(ModelAttribute inModelAttribute) {
		if ((inModelAttribute != null) && inModelAttribute.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addModelAttribute(inModelAttribute);
			model.save();
			return inModelAttribute;
		}
		return null;
	}

	@Override
	public ModelReference addModelReference(ModelReference inModelReference) {
		if ((inModelReference != null) && inModelReference.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addModelReference(inModelReference);
			model.save();
			return inModelReference;
		}
		return null;
	}

	@Override
	public ModelDataType addModelDataType(ModelDataType inModelDataType) {
		if ((inModelDataType != null) && inModelDataType.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addModelDataType(inModelDataType);
			model.save();
			return inModelDataType;
		}
		return null;
	}

	@Override
	public ModelEntity addModelEntity(ModelEntity inModelEntity) {
		if ((inModelEntity != null) && inModelEntity.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addModelEntity(inModelEntity);
			model.save();
			return inModelEntity;
		}
		return null;
	}

	@Override
	public List<DataModel> getDataModels(ApplicationConfig inApp) {
		final List<DataModel> dataModels = new ArrayList<DataModel>();
		if (inApp != null) {
			final List<ApplicationDataModel> appModels = systemAccess.referencesApplicationDataModelByApplicationConfig(inApp.getPrimaryKey());
			for (ApplicationDataModel appModel : appModels) {
				if (appModel != null) {
					final DataModel dataModel = appModel.getDataModelRef();
					if (dataModel != null) {
						dataModels.add(dataModel);
					}
				}
			}
		}
		return dataModels;
	}

	@Override
	public SystemModel getDataModel(DataModel inDataModel) {
		final SystemModel systemModel = SystemModel.createEmpty();
		if (inDataModel != null) {
			final DataModel dataModel = systemModel.findByIDDataModel(inDataModel.getPrimaryKey());
			if (dataModel != null) {
				systemModel.addDataModel(dataModel);
				final List<ModelEntity> modelEntities = systemModel.referencesModelEntityByDataModel(dataModel.getPrimaryKey());
				systemModel.addAllModelEntity(modelEntities);
				for (ModelEntity modelEntity : modelEntities) {
					if (modelEntity != null) {
						final List<ModelAttribute> modelAttributes = systemAccess.referencesModelAttributeByModelEntity(modelEntity.getPrimaryKey());
						final SystemModel attributesModel = systemAccess.exportModelAttribute(modelAttributes);
						systemModel.add(attributesModel);
						final List<ModelReference> modelReferencesIn = systemAccess.referencesModelReferenceBySource(modelEntity.getPrimaryKey());
						final SystemModel inReferencesModel = systemAccess.exportModelReference(modelReferencesIn);
						systemModel.add(inReferencesModel);
						final List<ModelReference> modelReferencesOut = systemAccess.referencesModelReferenceByDestination(modelEntity.getPrimaryKey());
						final SystemModel outReferencesModel = systemAccess.exportModelReference(modelReferencesOut);
						systemModel.add(outReferencesModel);
					}
				}
			}
		}
		return systemModel;
	}

}
