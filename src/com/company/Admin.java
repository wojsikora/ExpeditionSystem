package com.company;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Admin {

    public static void main(String[] args) throws Exception {

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();


        String QUEUE_NAME = "adminQueue";
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        String EXCHANGE_NAME = "admin exchange";
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "*");

        String EXCHANGE_NAME2 = "team-supplier exchange";
        channel.exchangeDeclare(EXCHANGE_NAME2, BuiltinExchangeType.TOPIC);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME2, "*");

        String EXCHANGE_NAME3 = "supplier-team exchange";
        channel.exchangeDeclare(EXCHANGE_NAME3, BuiltinExchangeType.TOPIC);
        channel.queueBind(QUEUE_NAME,EXCHANGE_NAME3, "*");

        Consumer consumer  = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Admin received: " + message);
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        channel.basicConsume(QUEUE_NAME, false, consumer);

        while (true) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Select admin mode (T - all teams, S - all suppliers, B - both teams and suppliers");
            String option = br.readLine();
            System.out.println("Enter your message");
            String msg = br.readLine();
            String key="";

            switch(option){
                case "T":
                    key = "teams";
                    break;
                case "S":
                    key = "suppliers";
                    break;
                case "B":
                    key = "both";
                    break;
                default:
                    System.out.println("Invalid option");
                    break;

            }
            String sentMsg = "Admin message: " + msg;
            channel.basicPublish(EXCHANGE_NAME, key, null, sentMsg.getBytes(StandardCharsets.UTF_8));
            System.out.println("Admin sent: " + sentMsg);

        }
    }
}
