SOAP Web Service with Spring Boot:
This article will show you how to create a SOAP web service with spring boot. You can create a spring project within a minutes by using Spring Initializr (https://start.spring.io/). In Spring Initializr landing page I am choosing below items:
	Project: Maven Project
	Language: Java
	Spring Boot: 2.4.1
	Project Metadata: 
		Group:com.soapservice
		Artifact:soapservice
		Name:soapservice
		Description:Demo SOAP service with Spring Boot
		Package name:com.soapservice.soapservice
		Packaging:Jar
		Java:8
		Dependencies:spring-boot-starter-web, spring-boot-starter-web-services

After choosing the above items then click one Generate button. It will give you a spring boot project skeleton. I have import this project in eclipse as a maven project. After importing if you found any error like Project configuration is not up-to-date with pom.xml. Then select the project and right click on it then Maven->Update Project.

Now add the Spring-WS dependency in pom.xml file:

	<dependency>
		<groupId>wsdl4j</groupId>
		<artifactId>wsdl4j</artifactId>
	</dependency>

There are two possible approaches to creating a web service - Contract-Last and Contract-First. In Contract-Last we start with the Java code and then generate WSDL. In Contract-First we start with the WSDL contract and from which we generate the Java classes. Here we are following Contract-First approach. 

So, Create an XSD file at ..src/main/resources/books.xsd which will return a book’s name, author, isbn and price. Here is the XSD file which I create for this article:
	
	<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:tns="https://localhost:8080/ws" 
		targetNamespace="https://localhost:8080/ws" elementFormDefault="qualified">

	<xs:element name="getBookRequest">
		<xs:complexType>
			<xs:sequence>
				<xs:element name="name" type="xs:string" />
			</xs:sequence>
		</xs:complexType>
	</xs:element>

	<xs:element name="getBookResponse">
		<xs:complexType>
			<xs:sequence>
				<xs:element name="book" type="tns:book" />
			</xs:sequence>
		</xs:complexType>
	</xs:element>

	<xs:complexType name="book">
		<xs:sequence>
			<xs:element name="name" type="xs:string" />
			<xs:element name="author" type="xs:string" />
			<xs:element name="isbn" type="xs:string" />
			<xs:element name="price" type="xs:int" />
		</xs:sequence>
	</xs:complexType>
</xs:schema>

Now as we say we are using Contract-First approach, So it's time to generate the java Classes based on an XML Schema. To do this thing need to add a maven plugin in pom.xml file. The following listing shows the necessary plugin configuration for Maven:

	<plugin>
		<groupId>org.codehaus.mojo</groupId>
		<artifactId>jaxb2-maven-plugin</artifactId>
		<version>2.5.0</version>
		<executions>
			<execution>
				<id>xjc</id>
				<goals>
					<goal>xjc</goal>
				</goals>
			</execution>
		</executions>
		<configuration>
			<sources>
				<source>${project.basedir}/src/main/resources/books.xsd</source>
			</sources>
		</configuration>
	</plugin>
	
Generated classes are placed in the target/generated-sources/jaxb/ directory and I am copying all file and place it in a newly created pacakge - com.soapservice.soapservice.entity

At this stage we will create BookRepository class (..src/main/java/com/soapservice/soapservice/entity/BookRepository.java) with some hardcoded data:
	
	package com.soapservice.soapservice.entity;
	import java.util.HashMap;
	import java.util.Map;
	import javax.annotation.PostConstruct;
	import org.springframework.stereotype.Component;
	import org.springframework.util.Assert;

	@Component
	public class BookRepository {
		
		private static final Map<String, Book> books = new HashMap<>();

		@PostConstruct
		public void initData() {
			Book java = new Book();
			java.setName("J2EE");
			java.setAuthor("Goncalves");
			java.setIsbn("I-1234");
			java.setPrice(500);
			
			books.put(java.getName(), java);

			Book python = new Book();
			python.setName("Python");
			python.setAuthor("David Ascher");
			python.setIsbn("I-12345");
			python.setPrice(500);
			
			books.put(python.getName(), python);

			Book c = new Book();
			c.setName("C++");
			c.setAuthor("Bjarne Stroustrup");
			c.setIsbn("I-123456");
			c.setPrice(500);
			
			books.put(c.getName(), c);
		}

		public Book findCountry(String name) {
			Assert.notNull(name, "The book's name must not be null");
			return books.get(name);
		}
	}

Now we are ready to create service endpoint. Let create a BooksEndpoint.java (..src/main/java/com/soapservice/soapservice/endpoint/BooksEndpoint.java):

    package com.soapservice.soapservice.endpoint;
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.ws.server.endpoint.annotation.Endpoint;
	import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
	import org.springframework.ws.server.endpoint.annotation.RequestPayload;
	import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
	import com.soapservice.soapservice.entity.BookRepository;
	import com.soapservice.soapservice.entity.GetBookRequest;
	import com.soapservice.soapservice.entity.GetBookResponse;

	@Endpoint
	public class BooksEndpoint {

		private static final String NAMESPACE_URI = "https://localhost:8080/ws";

		@Autowired
		private BookRepository bookRepository;

		@PayloadRoot(namespace = NAMESPACE_URI, localPart = "getBookRequest")
		@ResponsePayload
		public GetBookResponse getBooks(@RequestPayload GetBookRequest request) {
			GetBookResponse response = new GetBookResponse();
			response.setBook(bookRepository.findCountry(request.getName()));

			return response;
		}

	}


Now create and configure the WS bean class (..src/main/java/com/soapservice/soapservice/WebServiceConfig.java):
	
	package com.soapservice.soapservice;
	import org.springframework.boot.web.servlet.ServletRegistrationBean;
	import org.springframework.context.ApplicationContext;
	import org.springframework.context.annotation.Bean;
	import org.springframework.context.annotation.Configuration;
	import org.springframework.core.io.ClassPathResource;
	import org.springframework.ws.config.annotation.EnableWs;
	import org.springframework.ws.transport.http.MessageDispatcherServlet;
	import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
	import org.springframework.xml.xsd.SimpleXsdSchema;
	import org.springframework.xml.xsd.XsdSchema;

	@EnableWs
	@Configuration
	public class WebServiceConfig {
		@Bean
		public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
			MessageDispatcherServlet servlet = new MessageDispatcherServlet();
			servlet.setApplicationContext(applicationContext);
			servlet.setTransformWsdlLocations(true);
			return new ServletRegistrationBean<>(servlet, "/ws/*");
		}

		@Bean(name = "books")
		public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema countriesSchema) {
			DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
			wsdl11Definition.setPortTypeName("BooksPort");
			wsdl11Definition.setLocationUri("/ws");
			wsdl11Definition.setTargetNamespace("https://localhost:8080/ws");
			wsdl11Definition.setSchema(countriesSchema);
			return wsdl11Definition;
		}

		@Bean
		public XsdSchema booksSchema() {
			return new SimpleXsdSchema(new ClassPathResource("books.xsd"));
		}
	}

	
We need to add the following class (..src/main/java/com/soapservice/soapservice/SoapserviceApplication.java) to make the application executable:

	@SpringBootApplication
	public class SoapserviceApplication {

		public static void main(String[] args) {
			SpringApplication.run(SoapserviceApplication.class, args);
		}

	}
	
Now build and run the project. After runnging the project you can find the wsdl file by hitting below url:
	http://localhost:8080/ws/books.wsdl

You can check the service by using soap client like SOAP UI or boomerangapi or etc. Here is the resuest and response sample:

Request:
	<x:Envelope
		xmlns:x="http://schemas.xmlsoap.org/soap/envelope/"
		xmlns:ws="https://localhost:8080/ws">
		<x:Header/>
		<x:Body>
			<ws:getBookRequest>
				<ws:name>J2EE</ws:name>
			</ws:getBookRequest>
		</x:Body>
	</x:Envelope>

Response:
	<SOAP-ENV:Envelope
		xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
		<SOAP-ENV:Header/>
		<SOAP-ENV:Body>
			<ns2:getBookResponse
				xmlns:ns2="https://localhost:8080/ws">
				<ns2:book>
					<ns2:name>J2EE</ns2:name>
					<ns2:author>Goncalves</ns2:author>
					<ns2:isbn>I-1234</ns2:isbn>
					<ns2:price>500</ns2:price>
				</ns2:book>
			</ns2:getBookResponse>
		</SOAP-ENV:Body>
	</SOAP-ENV:Envelope>	

Note:
@Endpoint annotation registers the class with Spring WS as a potential candidate for processing incoming SOAP messages.
@PayloadRoot annotation is then used by Spring WS to pick the handler method, based on the message’s namespace and localPart.
@RequestPayload annotation indicates that the incoming message will be mapped to the method’s request parameter.
@ResponsePayload annotation makes Spring WS map the returned value to the response payload.	
@EnableWs enables SOAP Web Service features in this Spring Boot application.	

See Also:
	1. X.509 Authentication in Spring Security	
	
	
	


