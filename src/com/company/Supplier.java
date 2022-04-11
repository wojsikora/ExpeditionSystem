package com.company;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Supplier {
    private static String supplierName;

    public static void main(String[] args) throws Exception{
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter supplier name: ");
        supplierName = br.readLine();

        String QUEUE_NAME = supplierName;
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.basicQos(1);

        String EXCHANGE_NAME = "team-supplier exchange";
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

        String EXCHANGE_NAME2 = "supplier-team exchange";
        channel.exchangeDeclare(EXCHANGE_NAME2, BuiltinExchangeType.TOPIC);

        String EXCHANGE_NAME3 = "admin exchange";
        channel.exchangeDeclare(EXCHANGE_NAME3, BuiltinExchangeType.TOPIC);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME3, "suppliers");
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME3, "both");


        Consumer consumer  = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, "UTF-8");
                System.out.println(supplierName + " received: " + msg);
                if(!msg.substring(0,5).equals("Admin")){
                    String teamName = msg.split(" ")[0];
                    String sentMsg  = teamName + "'s order for " + envelope.getRoutingKey() + " supplied by " + supplierName;
                    channel.basicPublish(EXCHANGE_NAME2, teamName, null, sentMsg.getBytes(StandardCharsets.UTF_8));
                    System.out.println(supplierName + " sent " + sentMsg);

                }
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        channel.basicConsume(QUEUE_NAME, false, consumer);

        System.out.println("Enter orders to be supplied by this supplier:");
        String[] orders = br.readLine().split(" ");

        for(String o: orders){

            channel.queueDeclare(o+"Queue", false, false, false, null);
            channel.queueBind(o+"Queue", EXCHANGE_NAME, o);
            channel.basicConsume(o+"Queue", false, consumer);
        }




    }
}
