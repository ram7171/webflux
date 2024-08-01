package com.example.webflux.demo.sec01;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("reactive")
public class ReactiveWebController {
    private static final Logger log = LoggerFactory.getLogger(RestController.class);
    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:7070")
            .build();

    @GetMapping("products")
    public Flux<Product> getProducts() {
        return this.webClient.get()
                .uri("/demo01/products")
                .retrieve()
                .bodyToFlux(Product.class)
                .doOnNext(p -> log.info("recieved product {}", p));
    }

    /*
    * if the rest api if failed, partial response will be received in reactive.
     */
    @GetMapping("productsNotorious")
    public Flux<Product> getProductsNotorious() {
        return this.webClient.get()
                .uri("/demo01/products/notorious")
                .retrieve()
                .bodyToFlux(Product.class)
                .onErrorComplete() // serialize the partial response properly in case of error in middle
                .doOnNext(p -> log.info("recieved product {}", p));
    }
//[{"id":1,"description":"product-1","price":1},{"id":2,"description":"product-2","price":2},
// {"id":3,"description":"product-3","price":3},{"id":4,"description":"product-4","price":4}]
    // this method chaining is reactive pipeline.
    /*
    * 1. when browser send request to controller.
    *   a. its subscribe with controller.
    *   b. controller will publish response in non-blocking fashion
    *   c. whenever the browser cancel request, controller detect and propogate to the following rest api
    *  to cancel it , it will not happen when traditional programming.
    * */
    @GetMapping(value = "products/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Product> getProductsStream() {
        return this.webClient.get()
                .uri("/demo01/products")
                .retrieve()
                .bodyToFlux(Product.class)
                .doOnNext(p -> log.info("recieved product {}", p));

    }
}
