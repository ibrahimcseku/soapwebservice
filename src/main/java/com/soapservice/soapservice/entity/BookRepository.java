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
