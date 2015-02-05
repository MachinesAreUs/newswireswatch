package com.gex.cms.newswires.newswireswatch

import org.apache.camel.*
import org.apache.camel.impl.*
import org.apache.camel.builder.*
import org.apache.camel.impl.JndiRegistry
import org.apache.camel.model.dataformat.*

import com.mongodb.Mongo
import com.mongodb.DBObject
import com.mongodb.util.JSON

def BASE_DATA_PATH =  "/Users/agus/gex/newswireswatch_data/data"
def RABBITMQ_EXCHANGE = "newswires"

def camelContext = new DefaultCamelContext()

def mongoDb = new Mongo("localhost", 27017)
camelContext.registry.registry.bind("mongoDb", mongoDb)

def xmlJsonDataFormat = new XmlJsonDataFormat();

camelContext.addRoutes(new RouteBuilder() {
    def void configure() {
        from("file://${BASE_DATA_PATH}/src")
            .to("log://camelLogger?level=INFO")
            .to("file://${BASE_DATA_PATH}/dest")
            .to("rabbitmq://localhost:5672/${RABBITMQ_EXCHANGE}?username=guest&password=guest&autoDelete=false")

        from("rabbitmq://localhost:5672/${RABBITMQ_EXCHANGE}?username=guest&password=guest&autoDelete=false&queue=afpnews")
        	.to("log://camelLogger?level=WARN")
        	.marshal(xmlJsonDataFormat)
        	.convertBodyTo(String.class)        	
        	.to("mongodb:mongoDb?database=news&collection=afpnews&operation=insert")

    }
})
camelContext.start()

addShutdownHook{ camelContext.stop() }
synchronized(this){ this.wait() }
