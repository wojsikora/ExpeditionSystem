package com.company;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Team {
    private static String teamName;

    public static void main(String[] args) throws Exception{

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter team name: ");
        teamName = br.readLine();

        String QUEUE_NAME = teamName;
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        String EXCHANGE_NAME = "team-supplier exchange";
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

        String EXCHANGE_NAME2 = "supplier-team exchange";
        channel.exchangeDeclare(EXCHANGE_NAME2, BuiltinExchangeType.TOPIC);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME2, teamName);

        String EXCHANGE_NAME3 = "admin exchange";
        channel.exchangeDeclare(EXCHANGE_NAME3, BuiltinExchangeType.TOPIC);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME3, "teams");
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME3, "both");


        Consumer consumer  = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, "UTF-8");
                System.out.println(teamName + " received: " + msg);
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        channel.basicConsume(QUEUE_NAME, false, consumer);

        while(true){
            System.out.println("Enter order's series:");
            String[] orders = br.readLine().split(" ");

            if("stop".equals(orders[0])){
                break;
            }
            for(String o: orders){
                String sentMsg = teamName + " ordered " + o;
                channel.basicPublish(EXCHANGE_NAME, o, null, sentMsg.getBytes(StandardCharsets.UTF_8));
                System.out.println(teamName + " sent: " + sentMsg);
            }

        }








    }

}
