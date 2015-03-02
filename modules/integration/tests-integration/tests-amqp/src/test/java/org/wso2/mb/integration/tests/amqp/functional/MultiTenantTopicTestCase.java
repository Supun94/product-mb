/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.mb.integration.tests.amqp.functional;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.mb.integration.common.clients.AndesClient;
import org.wso2.mb.integration.common.clients.operations.utils.AndesClientUtils;
import org.wso2.mb.integration.common.utils.backend.MBIntegrationBaseTest;

/**
 * Testing for multi tenant - Topic specific test case
 *
 * Test case 1
 * 1. Start a 3 subscribers in same tenant(Normal tenant) who listens to the same topic
 * 2. Send 200 messages to the topic
 * 3. All 3 subscribers should receive all 200 messages
 *
 * Test case 2
 * 1. Start 2 subscribers from different tenant for the same topic
 * 2. Start 2 publishers from different tenant for the same topic
 * 3. Subscribers should receive the message from their tenant only
 */
public class MultiTenantTopicTestCase extends MBIntegrationBaseTest {
    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);
        AndesClientUtils.sleepForInterval(15000);
    }

    /**
     * Test case 1
     * 1. Start a 3 subscribers in same tenant(Normal tenant) who listens to the same topic
     * 2. Send 200 messages to the topic
     * 3. All 3 subscribers should receive all 200 messages
     */
    @Test(groups = "wso2.mb", description = "Single Tenant with multiple Users Test")
    public void performSingleTenantMultipleUserTopicTestCase() {
        int sendMessageCount = 200;
        int runTime = 40;
        int expectedMessageCount = 200;

        // Start receiving clients (admin, user1, user2)

        AndesClient adminReceivingClient = new AndesClient("receive", "127.0.0.1:5672", "topic:topictenant1.com/tenantTopic",
                "100", "false", Integer.toString(runTime), Integer.toString(expectedMessageCount),
                "1", "listener=true,ackMode=1,delayBetweenMsg=0,stopAfter=" + expectedMessageCount, "",
                "admin!topictenant1.com", "admin");
        adminReceivingClient.startWorking();

        AndesClient tenant1ReceivingClient1 = new AndesClient("receive", "127.0.0.1:5672", "topic:topictenant1.com/tenantTopic",
                "100", "false", Integer.toString(runTime), Integer.toString(expectedMessageCount),
                "1", "listener=true,ackMode=1,delayBetweenMsg=0,stopAfter=" + expectedMessageCount, "",
                "topictenantuser1!topictenant1.com", "topictenantuser1");
        tenant1ReceivingClient1.startWorking();

        AndesClient tenant1ReceivingClient2 = new AndesClient("receive", "127.0.0.1:5672", "topic:topictenant1.com/tenantTopic",
                "100", "false", Integer.toString(runTime), Integer.toString(expectedMessageCount),
                "1", "listener=true,ackMode=1,delayBetweenMsg=0,stopAfter=" + expectedMessageCount, "",
                "topictenantuser2!topictenant1.com", "topictenantuser2");
        tenant1ReceivingClient2.startWorking();

        // Start sending client (tenant1)
        AndesClient tenant1SendingClient = new AndesClient("send", "127.0.0.1:5672", "topic:topictenant1.com/tenantTopic",
                "100", "false", Integer.toString(runTime), Integer.toString(sendMessageCount), "1",
                "ackMode=1,delayBetweenMsg=0,stopAfter=" + sendMessageCount, "", "topictenantuser1!topictenant1.com", "topictenantuser1");

        tenant1SendingClient.startWorking();


        boolean tenant1ReceiveSuccess1 = AndesClientUtils.waitUntilMessagesAreReceived(tenant1ReceivingClient1,
                expectedMessageCount, runTime);
        boolean tenant1ReceiveSuccess2 = AndesClientUtils.waitUntilMessagesAreReceived(tenant1ReceivingClient2,
                expectedMessageCount, runTime);
        boolean adminReceiveSuccess = AndesClientUtils.waitUntilMessagesAreReceived(adminReceivingClient,
                expectedMessageCount, runTime);

        boolean tenant1SendSuccess = AndesClientUtils.getIfSenderIsSuccess(tenant1SendingClient, sendMessageCount);

        Assert.assertTrue(tenant1SendSuccess, "Sending failed for tenant 1 user 1.");
        Assert.assertTrue(tenant1ReceiveSuccess1, "Message receiving failed for tenant 1 user 1. Received message count : "+tenant1ReceivingClient1.getReceivedTopicMessagecount());
        Assert.assertTrue(tenant1ReceiveSuccess2, "Message receiving failed for tenant 1 user 2. Received message count : " + tenant1ReceivingClient2.getReceivedTopicMessagecount());
        Assert.assertEquals(adminReceivingClient.getReceivedTopicMessagecount(), expectedMessageCount);
        Assert.assertTrue(adminReceiveSuccess, "Message receiving failed for admin of tenant 1. Received message count : " + adminReceivingClient.getReceivedTopicMessagecount());

    }

    /**
     *
     * Test case 2
     * 1. Start 2 subscribers from different tenant for the same topic
     * 2. Start 2 publishers from different tenant for the same topic
     * 3. Subscribers should receive the message from their tenant only
     */
    @Test(groups = "wso2.mb", description = "Multiple Tenant Single Users Test")
    public void performMultipleTenantTopicTestCase() {
        int sendMessageCount1 = 120;
        int sendMessageCount2 = 80;
        int runTime = 20;
        int expectedMessageCount = 200;

        // Start receiving clients (tenant1, tenant2)
        AndesClient tenant1ReceivingClient = new AndesClient("receive", "127.0.0.1:5672", "topic:topictenant1.com/multitenantTopic",
                "100", "false", Integer.toString(runTime), Integer.toString(expectedMessageCount),
                "1", "listener=true,ackMode=1,delayBetweenMsg=0,stopAfter=" + expectedMessageCount, "",
                "topictenantuser1!topictenant1.com", "topictenantuser1");
        tenant1ReceivingClient.startWorking();

        AndesClient tenant2ReceivingClient = new AndesClient("receive", "127.0.0.1:5672", "topic:topictenant2.com/multitenantTopic",
                "100", "false", Integer.toString(runTime), Integer.toString(expectedMessageCount),
                "1", "listener=true,ackMode=1,delayBetweenMsg=0,stopAfter=" + expectedMessageCount, "",
                "topictenantuser1!topictenant2.com", "topictenantuser1");
        tenant2ReceivingClient.startWorking();

        // Start sending clients (tenant1, tenant2)
        AndesClient tenant1SendingClient = new AndesClient("send", "127.0.0.1:5672", "topic:topictenant1.com/multitenantTopic",
                "100", "false", Integer.toString(runTime), Integer.toString(sendMessageCount1), "1",
                "ackMode=1,delayBetweenMsg=0,stopAfter=" + sendMessageCount1, "",
                "topictenantuser1!topictenant1.com", "topictenantuser1");

        tenant1SendingClient.startWorking();

        AndesClient tenant2SendingClient = new AndesClient("send", "127.0.0.1:5672", "topic:topictenant2.com/multitenantTopic",
                "100", "false", Integer.toString(runTime), Integer.toString(sendMessageCount2), "1",
                "ackMode=1,delayBetweenMsg=0,stopAfter=" + sendMessageCount2, "",
                "topictenantuser2!topictenant2.com", "topictenantuser2");
        tenant2SendingClient.startWorking();

        AndesClientUtils.waitUntilMessagesAreReceived(tenant1ReceivingClient,
                expectedMessageCount, runTime);
        AndesClientUtils.waitUntilMessagesAreReceived(tenant2ReceivingClient,
                expectedMessageCount, runTime);

        boolean tenant1SendSuccess = AndesClientUtils.getIfSenderIsSuccess(tenant1SendingClient, sendMessageCount1);
        boolean tenant2SendSuccess = AndesClientUtils.getIfSenderIsSuccess(tenant2SendingClient, sendMessageCount2);

        Assert.assertTrue(tenant1SendSuccess, "Sending failed for tenant 1 user 1.");
        Assert.assertTrue(tenant2SendSuccess, "Sending failed for tenant 2 user 1.");
        Assert.assertEquals(tenant1ReceivingClient.getReceivedTopicMessagecount(), sendMessageCount1, "Tenant 1 client received incorrect number of message count. Received message count : " + tenant1ReceivingClient.getReceivedTopicMessagecount());
        Assert.assertEquals(tenant2ReceivingClient.getReceivedTopicMessagecount(), sendMessageCount2, "Tenant 2 client received incorrect number of message count. Received message count : " + tenant2ReceivingClient.getReceivedTopicMessagecount());


    }
}