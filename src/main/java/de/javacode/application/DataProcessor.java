package de.itdesign.application;

import de.itdesign.application.data.City;
import de.itdesign.application.data.Operation;
import de.itdesign.application.enums.Function;
import de.itdesign.application.enums.XMLNode;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DataProcessor {

    // In a maven project can use @Slf4j to print log messages
    private static final Logger LOGGER = Logger.getLogger(DataProcessor.class.getName());
    private static final String INPUT_ERROR_MSG = "XML can not be parsed correctly.";
    private static final String OUTPUT_ERROR_MSG = "XML can not be created successfully.";
    private static final String YES = "yes";

    private String data_file;
    private String operations_file;
    private String output_file;

    public DataProcessor(String data_file, String operations_file, String output_file) {
        this.data_file = data_file;
        this.operations_file = operations_file;
        this.output_file = output_file;
    }

    /**
     * Transfer the data from XML files to objects, calculate and output the result in another XML.
     */
    public void processInputAndOutputResult() {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            List<City> cityList = new ArrayList<>();
            List<Operation> operationList = new ArrayList<>();

            processDataXml(documentBuilder, data_file, cityList);
            processOperationsXml(documentBuilder, operations_file, operationList);
            calculateDataAndOutputResult(documentBuilder, cityList, operationList);
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, INPUT_ERROR_MSG, e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, INPUT_ERROR_MSG, e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, INPUT_ERROR_MSG, e);
        } catch (TransformerException e) {
            LOGGER.log(Level.SEVERE, OUTPUT_ERROR_MSG, e);
        }
    }

    /**
     * Getting all node information from data.xml, map them to java objects, then add them to a list.
     *
     * @param documentBuilder Document builder for executing the operations related to xml.
     * @param data_file The uri of the file which contains the data that will be processed.
     * @param cityList A list contains all the city objects based on the data.xml.
     * @throws IOException
     * @throws SAXException
     */
    private void processDataXml(DocumentBuilder documentBuilder, String data_file, List<City> cityList) throws IOException, SAXException {
        Document doc_data = documentBuilder.parse(data_file);
        NodeList cities = doc_data.getElementsByTagName(XMLNode.CITY.getNodeName());

        for (int i = 0; i < cities.getLength(); i++) {
            cityList.add(mapCityInformationToObject(cities.item(i)));
        }
    }

    /**
     * Mapping city information from data.xml to object.
     *
     * @param city Node in data.xml.
     * @return City Java object, which is one-to-one corresponding city node from data.xml.
     */
    private City mapCityInformationToObject(Node city) {
        NamedNodeMap cityAttributes = city.getAttributes();
        String name = cityAttributes.getNamedItem(XMLNode.NAME.getNodeName()).getNodeValue();
        long population = Long.valueOf(cityAttributes.getNamedItem(XMLNode.POPULATION.getNodeName()).getNodeValue());
        String area = city.getFirstChild().getNextSibling().getFirstChild().getNodeValue();

        return City.builder()
                .name(name)
                .population(population)
                .area(Double.parseDouble(area))
                .build();
    }

    /**
     * Get all node information from operations.xml, map them to java objects, then add them to a list.
     *
     * @param documentBuilder Document builder for executing the operations related to xml.
     * @param operations_file The uri of the file which contains the operations for processing data.
     * @param operationList A list contains all the operation objects based on the operations.xml.
     * @throws IOException
     * @throws SAXException
     */
    private void processOperationsXml(DocumentBuilder documentBuilder, String operations_file, List<Operation> operationList) throws IOException, SAXException {
        Document doc_data = documentBuilder.parse(operations_file);
        NodeList operations = doc_data.getElementsByTagName(XMLNode.OPERATION.getNodeName());

        for (int i = 0; i < operations.getLength(); i++) {
            operationList.add(mapOperationInformationToObject(operations.item(i)));
        }
    }

    /**
     * Mapping operation information from operations.xml to object.
     *
     * @param operation Node in operations.xml.
     * @return Operation Java object, which is one-to-one corresponding operation node from operations.xml.
     */
    private Operation mapOperationInformationToObject(Node operation) {
        NamedNodeMap operationAttributes = operation.getAttributes();
        String name = operationAttributes.getNamedItem(XMLNode.NAME.getNodeName()).getNodeValue();
        String type = operationAttributes.getNamedItem(XMLNode.TYPE.getNodeName()).getNodeValue();
        String function = operationAttributes.getNamedItem(XMLNode.FUNCTION.getNodeName()).getNodeValue();
        String attribute = operationAttributes.getNamedItem(XMLNode.ATTRIBUTE.getNodeName()).getNodeValue();
        String filter = operationAttributes.getNamedItem(XMLNode.FILTER.getNodeName()).getNodeValue();

        return Operation.builder()
                .name(name)
                .type(type)
                .function(function)
                .attribute(attribute)
                .filter(filter)
                .build();
    }

    /**
     * Calculate data based on the operations
     *
     * @param cityList A list contains all the city objects based on the data.xml.
     * @param operationList A list contains all the operation objects based on the operations.xml.
     */
    private void calculateDataAndOutputResult(DocumentBuilder documentBuilder, List<City> cityList, List<Operation> operationList) throws TransformerException {

        Document outputDocument = documentBuilder.newDocument();
        //create root node
        Element results = outputDocument.createElement(XMLNode.RESULTS.getNodeName());
        outputDocument.appendChild(results);

        for (Operation operation : operationList) {
            List<City> filteredCityList = cityList.stream()
                    .filter(city -> city.getName().matches(operation.getFilter()))
                    .collect(Collectors.toList());

            String calculatedResult;

            switch (Function.fromString(operation.getFunction())) {
                case AVERAGE:
                    calculatedResult = calculateAverage(filteredCityList, operation);
                    break;
                case SUM:
                    calculatedResult = calculateSum(filteredCityList, operation);
                    break;
                case MAX:
                    calculatedResult = calculateMax(filteredCityList, operation);
                    break;
                case MIN:
                    calculatedResult = calculateMin(filteredCityList, operation);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown function" + operation.getFunction());

            }
            prepareNodeInformationForOutput(outputDocument, results, operation.getName(), calculatedResult);
        }

        createOutputXml(outputDocument);
    }

    /**
     * Calculate the average of the values of population or area based on the attribute of operation.
     *
     * @param filteredCityList A list which contains all the city objects which are filtered by the filter defined in operation.
     * @param operation Operation object which contains the operations for processing data.
     * @return average as string
     */
    private String calculateAverage(List<City> filteredCityList, Operation operation) {
        if (XMLNode.AREA.getNodeName().equalsIgnoreCase(operation.getAttribute())) {
            return String.valueOf(filteredCityList.stream().mapToDouble(City::getArea).average().getAsDouble());
        } else {
            return String.valueOf(filteredCityList.stream().mapToLong(City::getPopulation).average().getAsDouble());
        }
    }

    /**
     * Calculate the sum of the values of population or area based on the attribute of operation.
     *
     * @param filteredCityList A list which contains all the city objects which are filtered by the filter defined in operation.
     * @param operation Operation object which contains the operations for processing data.
     * @return sum as string
     */
    private String calculateSum(List<City> filteredCityList, Operation operation) {
        if (XMLNode.POPULATION.getNodeName().equalsIgnoreCase(operation.getAttribute())) {
            return String.valueOf(filteredCityList.stream().mapToLong(City::getPopulation).sum());
        } else {
            return String.valueOf(filteredCityList.stream().mapToDouble(City::getArea).sum());
        }
    }

    /**
     * Calculate the maximum of the values of population or area based on the attribute of operation.
     *
     * @param filteredCityList A list which contains all the city objects which are filtered by the filter defined in operation.
     * @param operation Operation object which contains the operations for processing data.
     * @return maximum value as string
     */
    private String calculateMax(List<City> filteredCityList, Operation operation) {
        if (XMLNode.POPULATION.getNodeName().equalsIgnoreCase(operation.getAttribute())) {
            return String.valueOf(filteredCityList.stream().mapToLong(City::getPopulation).max().getAsLong());
        } else {
            return String.valueOf(filteredCityList.stream().mapToDouble(City::getArea).max().getAsDouble());
        }
    }

    /**
     * Calculate the minimum of the values of population or area based on the attribute of operation.
     *
     * @param filteredCityList A list which contains all the city objects which are filtered by the filter defined in operation.
     * @param operation Operation object which contains the operations for processing data.
     * @return minimum as string
     */
    private String calculateMin(List<City> filteredCityList, Operation operation) {
        if (XMLNode.POPULATION.getNodeName().equalsIgnoreCase(operation.getAttribute())) {
            return String.valueOf(filteredCityList.stream().mapToLong(City::getPopulation).min().getAsLong());
        } else {
            return String.valueOf(filteredCityList.stream().mapToDouble(City::getArea).min().getAsDouble());
        }
    }

    /**
     * Prepare the node information of result,
     *
     * @param outputDocument The output document.
     * @param results The root node in the output XML.
     * @param name Attribute name of result node.
     * @param calculatedResult calculated result.
     */
    private void prepareNodeInformationForOutput(Document outputDocument, Element results, String name, String calculatedResult) {
        //create result node
        Element result = outputDocument.createElement(XMLNode.RESULT.getNodeName());
        result.setAttribute(XMLNode.NAME.getNodeName(), name);
        //set precision of calculated result to be two decimal places
        result.appendChild(outputDocument.createTextNode(new BigDecimal(calculatedResult)
                .setScale(2, RoundingMode.HALF_UP).toString()));
        //add result node to root node
        results.appendChild(result);
    }

    /**
     * Create output XML.
     *
     * @param outputDocument The output document.
     * @throws TransformerException
     */
    private void createOutputXml(Document outputDocument) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        //Newline when outputting data
        transformer.setOutputProperty(OutputKeys.INDENT, YES);
        //Output the document without xml declaration
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, YES);
        transformer.transform(new DOMSource(outputDocument), new StreamResult(new File(output_file)));
    }
}
