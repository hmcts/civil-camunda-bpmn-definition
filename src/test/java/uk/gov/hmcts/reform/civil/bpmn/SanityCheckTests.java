package uk.gov.hmcts.reform.civil.bpmn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * These tests are intended to prevent easily made mistakes that could threaten production. These tests have nothing
 * to do with requirements and everything to do with stopping developers from mistakenly releasing code that
 * may break random parts of the application.
 * Each test has an explanation of what it does and the rationale behind it.
 */
class SanityCheckTests {

    DocumentBuilder documentBuilder;

    @BeforeEach
    void prepareDomParser() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        documentBuilder = dbf.newDocumentBuilder();
    }

    /**
     * <p>Often times when we create new Camunda files we do it by copy/pasting other files and modifying some key
     * portions. An easy mistake to make however is to forget modifying the "Process ID" for the file, as it's not
     * meant to affect the application's behaviour and it's done by clicking in an empty area of the Camunda Modeler.
     * </p>
     * <p>One such mistake can result in the "legit" process being overwritten by the new one, resulting in the manual
     * testing from the developer succeeding while leaving other areas of the application broken in the process.
     * This mistake is also easy to miss during code review, so some automation around it to prevent slipping through
     * is needed.</p>
     *
     * @throws Exception Unimportant
     */
    @Test
    void ensureProcessIdUniqueness() throws Exception {
        // Given: all the Camunda files
        PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resourcePatternResolver.getResources("classpath:camunda/**.bpmn");

        // When: I scan them and extract the process id
        Map<String, List<URL>> processIds = new HashMap<>();
        for (Resource resource : resources) {
            String processId = extractProcessId(resource);
            List<URL> urls = processIds.computeIfAbsent(processId, k -> new ArrayList<>());
            urls.add(resource.getURL());
        }

        // Then: no two different files can have the same process id
        processIds.entrySet().stream().filter(entry -> entry.getValue().size() > 1)
            .forEach(entry -> {
                entry.getValue().forEach(url -> System.out.println(entry.getKey() + " --> " + url.toString()));
            });
        assertThat(processIds.entrySet().stream().noneMatch(entry -> entry.getValue().size() > 1))
            .withFailMessage("Some duplicate values for <bpmn:process id> were found. This will cause"
                                 + " failures in Camunda. Please check the affected files and make sure that none"
                                 + " has duplicate process id values.")
            .isTrue();
    }

    private String extractProcessId(Resource resource) throws Exception {
        Document doc = documentBuilder.parse(resource.getInputStream());
        NodeList nodes = doc.getElementsByTagName("bpmn:process");
        Node node = nodes.item(0);
        NamedNodeMap attributes = node.getAttributes();
        return attributes.getNamedItem("id").getNodeValue();
    }

}
