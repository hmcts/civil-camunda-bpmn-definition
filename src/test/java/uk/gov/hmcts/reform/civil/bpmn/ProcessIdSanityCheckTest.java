package uk.gov.hmcts.reform.civil.bpmn;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessIdSanityCheckTest {

    DocumentBuilder documentBuilder;

    @BeforeEach
    void prepareDomParser() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        documentBuilder = dbf.newDocumentBuilder();
    }

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
