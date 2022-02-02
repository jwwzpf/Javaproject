import de.itdesign.application.DataProcessor;
import de.itdesign.application.enums.XMLNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DataProcessorTest {

    private DataProcessor dataProcessor;
    private static final String DATA_XML = "input_city.xml";
    private static final String OPERATIONS_XML = "input_operations.xml";
    private static final String OUTPUT_XML = "src/test/java/de.javacode.application/output/output_test.xml";

    @BeforeEach
    public void setUp() throws IOException {
        this.dataProcessor = new DataProcessor(DATA_XML, OPERATIONS_XML, OUTPUT_XML);

        //if output XML already exists before test, delete it
        if (Files.exists(Path.of(OUTPUT_XML))) {
            Files.delete(Path.of(OUTPUT_XML));
        }
    }

    /**
     * Test, with input XML files: data.xml and operations.xml, whether the data can be correctly processed, the output
     * XML can be generated successfully and the results are also correct.
     *
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    @Test
    public void testOutputXMLWithCorrectDataGeneratedSuccessfully() throws IOException, SAXException, ParserConfigurationException {
        dataProcessor.processInputAndOutputResult();

        List<String> resultValues = parseOutputXMLAndGetResultValues();

        Assertions.assertTrue(Files.exists(Path.of(OUTPUT_XML)));
        Assertions.assertFalse(resultValues.isEmpty());
        Assertions.assertEquals(4, resultValues.size());
        Assertions.assertEquals("4030418.67", resultValues.get(0));
    }

    /**
     * Read all result node values from generated output xml and add them to a list for test.
     *
     * @return A list contains all the result values for test.
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    private List<String> parseOutputXMLAndGetResultValues() throws ParserConfigurationException, IOException, SAXException {
        List<String> resultValueList = new ArrayList<>();
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc_data = documentBuilder.parse(OUTPUT_XML);
        NodeList resultNodes = doc_data.getElementsByTagName(XMLNode.RESULT.getNodeName());

        for (int i = 0; i < resultNodes.getLength(); i++) {
            Node result = resultNodes.item(i);
            resultValueList.add(result.getFirstChild().getNodeValue());
        }
        return resultValueList;
    }
}
