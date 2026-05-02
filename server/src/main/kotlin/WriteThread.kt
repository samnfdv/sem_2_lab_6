package org.example


import mechanicsOfCollection.CollectionManager
import org.apache.logging.log4j.LogManager
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel

class WriteThread(
    private val clientChannel: SocketChannel,
    private val key: SelectionKey,
    private val response: Response,
    val serverApp: ServerApp
) : Runnable {
    private val LOGGER: org.apache.logging.log4j.Logger = LogManager.getLogger(WriteThread::class.java)

    override fun run() {
        try {
            ByteArrayOutputStream().use { baos ->
                ObjectOutputStream(baos).use { oos ->
                    LOGGER.info("Отправка ответа в потоке ${Thread.currentThread().id} для ${serverApp.getRemoteAddress(clientChannel)}")
                    oos.writeObject(response)
                    oos.flush()
                    val data = baos.toByteArray()

                    val lengthBuf = ByteBuffer.allocate(4).putInt(data.size).apply { flip() }
                    while (lengthBuf.hasRemaining()) {
                        clientChannel.write(lengthBuf)
                    }

                    val dataBuf = ByteBuffer.wrap(data)
                    while (dataBuf.hasRemaining()) {
                        clientChannel.write(dataBuf)
                    }

                    LOGGER.info("Ответ отправлен ${serverApp.getRemoteAddress(clientChannel)}")
                    serverApp.finishWrite(key)
                }
            }
        } catch (e: IOException) {
            LOGGER.warn("Ошибка отправки ответа: ${e.message}")
            serverApp.closeClient(clientChannel, key)
        }
    }
}