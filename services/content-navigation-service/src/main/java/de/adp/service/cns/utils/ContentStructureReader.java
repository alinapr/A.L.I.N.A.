package de.adp.service.cns.utils;

import de.adp.service.cns.model.ContentStructure;

/**
 * Created by glenn on 23.09.15.
 */
public class ContentStructureReader {


    /**
     * this method reads a content structure (json or bpmn or BT) from file and
     * creates, returns a contentStructure-Instance
     *
     * content structure files are somehow stored in file system, files are referenced by
     * their processId
     *
     * @param processId
     * @return
     */
    public static ContentStructure getContentStructureFromFile( String processId ) {

        ContentStructure contentStructure = new ContentStructure( processId );

        if (processId.indexOf("DummytextProduktionsanlage") != -1) {
            contentStructure.appendContentNode("DummytextProduktionsanlage",
                    "Aufbau der Produktionsanlage");
        }
        else if (processId.indexOf("DummytextAnlagenoperator") != -1) {
            contentStructure.appendContentNode("DummytextAnlagenoperator",
                    "Aufgaben eines Anlagenoperators");
        }
        else if (processId.indexOf("DummytextPruefstation") != -1) {
            contentStructure.appendContentNode("DummytextPruefstation", "Zweck der Prüfstation");
        }
        {
            contentStructure.appendContentNode("GenercicStepId", "Generic Learning Object");
        }

        return contentStructure;
    }

}
