# Order Microservice

## Overview
The Order Microservice is a core component of our e-commerce platform, responsible for managing order creation, updates, cancellations, and interactions with other microservices (e.g., Coupon, Store, Payment, Shipping, and Notification). This service is built using Spring Boot and integrates with Kafka for event-driven communication in a distributed system.

## System Design Evolution

### Initial Design (Flowchart)
In the early stages, I started with a simple flowchart to outline the basic order processing workflow and compensation steps in case of failures.

![Order Processing Flowchart](https://github.com/Fawry-Intern/.github/blob/main/images/order_api_flow.png)

**Description:**
- The customer initiates a checkout.
- The Order Microservice:
  1. Validates the coupon with the Coupon Service.
  2. Checks inventory with the Store Microservice.
  3. Processes payment with the Bank Service.
  4. Initiates shipping with the Shipping Microservice.
  5. Sends a notification via the Notification Microservice.
- If the payment fails, the system:
  - Reverses the payment.
  - Cancels shipping.
  - Cancels the order.
  - Sends a cancellation notification.

**Limitations:**
- Lacked clarity on inter-service communication (e.g., REST vs. Kafka).
- Compensation steps were not detailed enough.
- No mention of database transactions or consistency mechanisms.

### Improved Design (Compensation Flow with Saga Pattern)
After researching best practices for distributed systems, I adopted the Choreographed Saga Pattern using Kafka to ensure better decoupling and consistency across services.

![Compensation Flow](https://github.com/Fawry-Intern/.github/blob/main/images/ordre_high_level_deign_v2.png)

**Description:**
- **Order Service**: Publishes an `order_created` event to Kafka and adds the order to its local database (TX 1).
- **Store Service**: Consumes the event, reduces inventory, and publishes a `store_updated` event (TX 2).
- **Payment Bank Service**: Processes the payment and publishes a `bank_updated` event (TX 3).
- **Shipping Service**: Ships the order and publishes a `shipping_updated` event (TX 2).
- **Compensation Flow**: If any step fails (e.g., payment fails):
  - Reverse inventory (Store Service).
  - Reverse payment (Payment Bank Service).
  - Cancel shipping (Shipping Service).

**Why This is Better:**
- Uses Kafka for event-driven communication, reducing coupling between services.
- Ensures consistency with local transactions for each service.
- Provides a clear compensation flow for handling failures.

### Order UML Class Digram
![Order UML Class](https://github.com/Fawry-Intern/.github/blob/main/images/Order%20Digram.drawio.png)

### API Flow (Alternative Approach)
I also explored an API-based approach for some interactions, which is useful for synchronous calls.

![API Flow](https://github.com/Fawry-Intern/.github/blob/main/images/order_digram.png)

**Description:**
- `order_products`: Fetch product details.
- `check_inventory`: Verify stock availability.
- `make_payment`: Process payment.
- `ship_order`: Initiate shipping.
- `send_notifications`: Notify customer and driver, with a reference notification.

**Limitations:**
- Lacks compensation details for failures.
- REST-based communication can be less reliable than Kafka for distributed transactions.

## Why the Saga Pattern?
The Choreographed Saga Pattern with Kafka was chosen as the best practice because:
- It decouples services, allowing them to operate independently.
- Kafka ensures reliable event delivery and supports scalability.
- Local transactions per service maintain consistency without requiring a global transaction coordinator.

## Future Improvements
- Add retry mechanisms for Kafka event publishing.
- Include the Notification Service in the compensation flow.
- Implement circuit breakers for REST-based interactions.

## Setup and Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/order-microservice.git
