package io.reed.ezstore.weather;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@SpringCloudApplication
@RestController
public class WeatherReportApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeatherReportApplication.class, args);
	}
	
	@Autowired
	RestTemplateBuilder restTemplateBuilder;
	
	@GetMapping("/{locationName}")
	void fetchWeather(@PathVariable String locationName) {
	
		RestTemplate restTemplate = restTemplateBuilder.build();
		String xml = restTemplate.getForObject("http://opendata.cwb.gov.tw/opendata/DIV2/O-A0001-001.xml", String.class);				
		
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		try {
			InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document xmlDocument = builder.parse(is);
			XPath xPath = XPathFactory.newInstance().newXPath();
			String query = "/cwbopendata/location[locationName='" + locationName + "']";
			Node location = (Node) xPath.compile(query).evaluate(xmlDocument, XPathConstants.NODE);
			
			Node weather = (Node) xPath.compile("//weatherElement[elementName='TEMP']/elementValue/value").evaluate(location, XPathConstants.NODE);
			for (int j = 0; j < weather.getChildNodes().getLength(); j++) {
				Node node = weather.getChildNodes().item(j);
				System.out.println(node.getNodeName());
				System.out.println(node.getNodeValue());
			}
			
			/*
			for (int i = 0; i < location.getChildNodes().getLength(); i++) {
				Node node = location.getChildNodes().item(i);
				String nodeName = node.getNodeName();
				if (nodeName.equals("weatherElement"))
					for (int j = 0; j < node.getChildNodes().getLength(); j++) {
						Node weatherNode = node.getChildNodes().item(i);
						weatherNode.
					}
						
				System.out.println(nodeName);
			}
			*/				
		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
