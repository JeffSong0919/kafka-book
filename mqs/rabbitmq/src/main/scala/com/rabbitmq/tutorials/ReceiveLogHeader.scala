package com.rabbitmq.tutorials

import java.util.HashMap

import com.rabbitmq.client._

object ReceiveLogHeader {

  private val EXCHANGE_NAME = "header_test"

  def main(argv: Array[String]) {
    if (argv.length < 1) {
      System.err.println("Usage: ReceiveLogsHeader queueName [headers]...")
      System.exit(1)
    }
    val factory = new ConnectionFactory()
    factory.setHost("localhost")
    val connection = factory.newConnection()
    val channel = connection.createChannel()
    channel.exchangeDeclare(EXCHANGE_NAME, "headers")
    val routingKeyFromUser = "ourTestRoutingKey"
    val queueInputName = argv(0)
    val headers = new HashMap[String, Object]()
    for (i <- 1 until argv.length by 2) {
      headers.put(argv(i), argv(i + 1))
      println("Binding header " + argv(i) + " and value " + argv(i + 1) +
        " to queue " +
        queueInputName)
    }
    val queueName = channel.queueDeclare(queueInputName, true, false, false, null).getQueue
    channel.queueBind(queueName, EXCHANGE_NAME, routingKeyFromUser, headers)
    println(" [*] Waiting for messages. To exit press CTRL+C")

    val consumer = new DefaultConsumer(channel) {
      override def handleDelivery(consumerTag: String,
                                  envelope: Envelope,
                                  properties: AMQP.BasicProperties,
                                  body: Array[Byte]) {
        val message = new String(body, "UTF-8")
        println(" [x] Received '" + envelope.getRoutingKey + "':'" + message +
          "'")
      }
    }
    channel.basicConsume(queueName, true, consumer)
  }
}