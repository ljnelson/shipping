package io.helidon.examples.sockshop.shipping.mongo;

import io.helidon.examples.sockshop.shipping.ShipmentRepository;
import io.helidon.examples.sockshop.shipping.ShipmentRepositoryTest;

import static io.helidon.examples.sockshop.shipping.mongo.MongoProducers.client;
import static io.helidon.examples.sockshop.shipping.mongo.MongoProducers.db;
import static io.helidon.examples.sockshop.shipping.mongo.MongoProducers.shipments;

/**
 * Integration tests for {@link io.helidon.examples.sockshop.shipping.mongo.MongoShipmentRepository}.
 */
class MongoShipmentRepositoryIT extends ShipmentRepositoryTest {
    public ShipmentRepository getShipmentRepository() {
        String host = System.getProperty("db.host","localhost");
        int    port = Integer.parseInt(System.getProperty("db.port","27017"));

        return new MongoShipmentRepository(shipments(db(client(host, port))));
    }

    @Override
    protected void clearRepository(ShipmentRepository repository) {
        ((MongoShipmentRepository) repository).clear();
    }
}
