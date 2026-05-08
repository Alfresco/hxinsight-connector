/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2026 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.hxi_connector.e2e_test.reliability.harness;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.DeliveryMode;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

import lombok.SneakyThrows;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * One-shot {@code TextMessage} publisher onto an ActiveMQ queue, used by reliability tests that need to inject synthetic payloads (e.g. an unprocessable event in {@link BulkIngesterDeadLetterReliabilityIT}) without going through the bulk-ingester service.
 *
 * <p>
 * Mirror of {@link DirectTopicPublisher}; the only difference is the destination type ({@code Queue} vs {@code Topic}). Connects directly to the broker's host-mapped OpenWire port (see {@link ReliabilityEnvironment#activemqDirectBrokerUrl()}) so the test path is not affected by the Toxiproxy that sits in front of the live-ingester's consumer.
 *
 * <p>
 * Persistent delivery is used so the broker treats the synthetic message exactly the same as a real bulk-ingester event for retry / DLQ purposes.
 */
public final class DirectQueuePublisher
{
    private DirectQueuePublisher()
    {}

    @SneakyThrows
    @SuppressWarnings("PMD.CloseResource")
    public static void publishTextMessage(String brokerUrl, String queueName, String body)
    {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = connectionFactory.createConnection();
        try
        {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            try
            {
                Queue queue = session.createQueue(queueName);
                MessageProducer producer = session.createProducer(queue);
                try
                {
                    producer.setDeliveryMode(DeliveryMode.PERSISTENT);
                    TextMessage message = session.createTextMessage(body);
                    producer.send(message);
                }
                finally
                {
                    producer.close();
                }
            }
            finally
            {
                session.close();
            }
        }
        finally
        {
            connection.close();
        }
    }
}
