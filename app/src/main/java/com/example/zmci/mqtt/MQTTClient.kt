package com.example.zmci.mqtt

import android.content.Context
import android.util.Log
import info.mqtt.android.service.Ack
import info.mqtt.android.service.MqttAndroidClient
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.io.BufferedInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.security.KeyStore
import java.security.Security
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory


class MQTTClient(context: Context,
                 serverURI: String,
                 clientID: String = "") {
    private var mqttClient = MqttAndroidClient(context, serverURI, clientID, Ack.AUTO_ACK)
    private val defaultCbConnect = object : IMqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken?) {
            Log.d(this.javaClass.name, "(Default) Connection success")
        }

        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
            Log.d(this.javaClass.name, "Connection failure: ${exception.toString()}")
        }
    }
    private val defaultCbClient = object : MqttCallback {
        override fun messageArrived(topic: String?, message: MqttMessage?) {
            Log.d(this.javaClass.name, "Receive message: ${message.toString()} from topic: $topic")
        }

        override fun connectionLost(cause: Throwable?) {
            Log.d(this.javaClass.name, "Connection lost ${cause.toString()}")
        }

        override fun deliveryComplete(token: IMqttDeliveryToken?) {
            Log.d(this.javaClass.name, "Delivery completed")
        }
    }
    private val defaultCbSubscribe = object : IMqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken?) {
            Log.d(this.javaClass.name, "Subscribed to topic")
        }

        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
            Log.d(this.javaClass.name, "Failed to subscribe topic")
        }
    }
    private val defaultCbUnsubscribe = object : IMqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken?) {
            Log.d(this.javaClass.name, "Unsubscribed to topic")
        }

        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
            Log.d(this.javaClass.name, "Failed to unsubscribe topic")
        }
    }
    private val defaultCbPublish = object : IMqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken?) {
            Log.d(this.javaClass.name, "Message published to topic")
        }

        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
            Log.d(this.javaClass.name, "Failed to publish message to topic")
        }
    }
    private val defaultCbDisconnect = object : IMqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken?) {
            Log.d(this.javaClass.name, "Disconnected")
        }

        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
            Log.d(this.javaClass.name, "Failed to disconnect")
        }
    }

    fun connect(options: MqttConnectOptions,
                cbConnect:  IMqttActionListener  = defaultCbConnect,
                cbClient:   MqttCallback         = defaultCbClient) {
        mqttClient.setCallback(cbClient)

//        val options = MqttConnectOptions()
        //val sslSocketFactory: SSLSocketFactory? = mqttClient.getSocketFactory(resources.openRawResource(R.raw.amazonrootca1),resources.openRawResource(R.raw.certificate_pem),resources.openRawResource(R.raw.private_pem),"")
        //                options.socketFactory = sslSocketFactory
//        options.userName = username
//        options.password = password.toCharArray()

        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }

        try {
            mqttClient.connect(options, null, cbConnect)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun isConnected(): Boolean {
        return mqttClient.isConnected
    }

    fun subscribe(topic:        String,
                  qos:          Int                 = 1,
                  cbSubscribe:  IMqttActionListener = defaultCbSubscribe) {
        try {
            mqttClient.subscribe(topic, qos, null, cbSubscribe)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun unsubscribe(topic:          String,
                    cbUnsubscribe:  IMqttActionListener = defaultCbUnsubscribe) {
        try {
            mqttClient.unsubscribe(topic, null, cbUnsubscribe)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun publish(topic:      String,
                msg:        String,
                qos:        Int                 = 1,
                retained:   Boolean             = false,
                cbPublish:  IMqttActionListener = defaultCbPublish) {
        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            message.qos = qos
            message.isRetained = retained
            mqttClient.publish(topic, message, null, cbPublish)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun disconnect(cbDisconnect: IMqttActionListener = defaultCbDisconnect ) {
        try {
            mqttClient.disconnect(null, cbDisconnect)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun getSocketFactory(
        caCrtFile: InputStream?, crtFile: InputStream?, keyFile: InputStream?,
        password: String
    ): SSLSocketFactory? {
        Security.addProvider(BouncyCastleProvider())

        // load CA certificate
        var caCert: X509Certificate? = null
        var bis = BufferedInputStream(caCrtFile)
        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
        while (bis.available() > 0) {
            caCert = cf.generateCertificate(bis) as X509Certificate
        }

        // load client certificate
        bis = BufferedInputStream(crtFile)
        var cert: X509Certificate? = null
        while (bis.available() > 0) {
            cert = cf.generateCertificate(bis) as X509Certificate
        }

        // load client private cert
        val pemParser = PEMParser(InputStreamReader(keyFile))
        val `object` = pemParser.readObject()
        val converter = JcaPEMKeyConverter()
        val key = converter.getKeyPair(`object` as PEMKeyPair)
        val caKs = KeyStore.getInstance(KeyStore.getDefaultType())
        caKs.load(null, null)
        caKs.setCertificateEntry("cert-certificate", caCert)
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(caKs)
        val ks = KeyStore.getInstance(KeyStore.getDefaultType())
        ks.load(null, null)
        ks.setCertificateEntry("certificate", cert)
        ks.setKeyEntry("private-cert", key.private, password.toCharArray(), arrayOf(cert))
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(ks, password.toCharArray())
        val context = SSLContext.getInstance("TLSv1.2")
        context.init(kmf.keyManagers, tmf.trustManagers, null)
        return context.socketFactory
    }

}