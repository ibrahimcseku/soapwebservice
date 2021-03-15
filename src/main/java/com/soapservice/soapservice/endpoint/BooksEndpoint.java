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
