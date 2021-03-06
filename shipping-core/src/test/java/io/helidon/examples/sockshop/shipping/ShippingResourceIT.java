package io.helidon.examples.sockshop.shipping;

import java.time.LocalDate;

import io.helidon.microprofile.server.Server;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.jboss.weld.proxy.WeldClientProxy;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.helidon.examples.sockshop.shipping.TestDataFactory.shipment;
import static io.helidon.examples.sockshop.shipping.TestDataFactory.shippingRequest;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.is;

/**
 * Integration tests for {@link io.helidon.examples.sockshop.shipping.ShippingResource}.
 */
public class ShippingResourceIT {
    protected static Server SERVER;

    /**
     * This will start the application on ephemeral port to avoid port conflicts.
     * We can discover the actual port by calling {@link io.helidon.microprofile.server.Server#port()} method afterwards.
     */
    @BeforeAll
    static void startServer() {
        // disable global tracing so we can start server in multiple test suites
        System.setProperty("tracing.global", "false");
        SERVER = Server.builder().port(0).build().start();
    }

    /**
     * Stop the server, as we cannot have multiple servers started at the same time.
     */
    @AfterAll
    static void stopServer() {
        SERVER.stop();
    }

    private ShipmentRepository shipments;

    @BeforeEach
    void setup() throws Exception {
        // Configure RestAssured to run tests against our application
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = SERVER.port();

        shipments = SERVER.cdiContainer().select(ShipmentRepository.class).get();

        // oh, boy... not pretty, but probably the best we can do
        // without adding clear() to public interface
        WeldClientProxy proxy = (WeldClientProxy) shipments;
        Object o = proxy.getMetadata().getContextualInstance();
        o.getClass().getMethod("clear").invoke(o);
    }

    @Test
    void testFedEx() {
        given().
                body(shippingRequest("A123", 1)).
                contentType(ContentType.JSON).
                accept(ContentType.JSON).
        when().
                post("/shipping").
        then().
                statusCode(OK.getStatusCode()).
                body("carrier", is("FEDEX"),
                     "deliveryDate", is(LocalDate.now().plusDays(1).toString())
                );
    }

    @Test
    void testUPS() {
        given().
                body(shippingRequest("A456", 3)).
                contentType(ContentType.JSON).
                accept(ContentType.JSON).
        when().
                post("/shipping").
        then().
                statusCode(OK.getStatusCode()).
                body("carrier", is("UPS"),
                     "deliveryDate", is(LocalDate.now().plusDays(3).toString())
                );
    }

    @Test
    void testUSPS() {
        given().
                body(shippingRequest("A789", 10)).
                contentType(ContentType.JSON).
                accept(ContentType.JSON).
        when().
                post("/shipping").
        then().
                statusCode(OK.getStatusCode()).
                body("carrier", is("USPS"),
                     "deliveryDate", is(LocalDate.now().plusDays(5).toString())
                );
    }

    @Test
    void testGetShipmentByOrder() {
        LocalDate deliveryDate = LocalDate.now().plusDays(2);
        shipments.saveShipment(shipment("A123", "UPS", "1Z999AA10123456784", deliveryDate));

        when().
                get("/shipping/{orderId}", "A123").
        then().
                statusCode(OK.getStatusCode()).
                body("carrier", is("UPS"),
                     "trackingNumber", is("1Z999AA10123456784"),
                     "deliveryDate", is(deliveryDate.toString())
                );

        when().
                get("/shipments/{orderId}", "B456").
        then().
                statusCode(NOT_FOUND.getStatusCode());
    }
}
