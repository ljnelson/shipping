package io.helidon.examples.sockshop.shipping;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.opentracing.Traced;

/**
 * Simple in-memory implementation of {@link io.helidon.examples.sockshop.shipping.ShipmentRepository}
 * that can be used for demos and testing.
 * <p/>
 * This implementation is obviously not suitable for production use, because it is not
 * persistent and it doesn't scale, but it is trivial to write and very useful for the
 * API testing and quick demos.
 */
@ApplicationScoped
@Traced
public class DefaultShipmentRepository implements ShipmentRepository {

    private Map<String, Shipment> shipments;

    /**
     * Construct {@code DefaultShipmentRepository} with an empty storage map.
     */
    public DefaultShipmentRepository() {
        this(new ConcurrentHashMap<>());
    }

    /**
     * Construct {@code DefaultShipmentRepository} with the specified storage map.
     *
     * @param shipments the storage map to use
     */
    protected DefaultShipmentRepository(Map<String, Shipment> shipments) {
        this.shipments = shipments;
    }

    @Override
    public Shipment getShipment(String orderId) {
        return shipments.get(orderId);
    }

    @Override
    public void saveShipment(Shipment shipment) {
        shipments.put(shipment.getOrderId(), shipment);
    }

    // ---- helpers ---------------------------------------------------------

    /**
     * Helper to clear this repository for testing.
     */
    public void clear() {
        shipments.clear();
    }
}
