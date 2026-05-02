

package org.example

import mechanicsOfCollection.CollectionManager
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.*
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.*
import java.util.concurrent.*

/**
 * Класс для запуска и управления сервером приложений.
 * Обеспечивает приём клиентских подключений, обработку запросов и отправку ответов.
 *
 * @property collectionManager менеджер коллекции, отвечающий за хранение и сохранение объектов.
 * @property dispatcher диспетчер команд, обрабатывающий входящие запросы клиентов.
 */
class ServerApp(
    val collectionManager: CollectionManager,
    val dispatcher: CommandDispatcher,
) {

    private val PORT = 1488
    private var running = true
    private val MAX_DATA_LENGTH: Int = 1024 * 1024

    private lateinit var selector: Selector
    private lateinit var serverChannel: ServerSocketChannel

    private var connectionProblem = false

    private val LOGGER: Logger = LogManager.getLogger(ServerApp::class.java)

    private val writePool: ExecutorService = Executors.newCachedThreadPool()
    private val commandPool: ExecutorService = Executors.newFixedThreadPool(4)
    private object WritingMarker

    // ----- Админ-приглашение -----
    private val ADMIN_PROMPT = "[server-admin] $ "
    private fun showAdminPrompt() {
        print(ADMIN_PROMPT)
        System.out.flush()
    }

    /**
     * Класс для хранения состояния клиента во время чтения данных.
     */
    class ClientState {
        var lengthBuffer: ByteBuffer = ByteBuffer.allocate(4)
        var dataBuffer: ByteBuffer? = null
        var dataLength = -1
    }

    /**
     * Устанавливает соединение и запускает серверный канал.
     * @throws IOException при ошибке открытия сокета или селектора.
     */
    @Throws(IOException::class)
    private fun connection() {
        serverChannel = ServerSocketChannel.open()
        serverChannel.configureBlocking(false)
        serverChannel.bind(InetSocketAddress("localhost", PORT))
        selector = Selector.open()
        serverChannel.register(selector, SelectionKey.OP_ACCEPT)
        LOGGER.info("Сервер запущен на порту {}", PORT)
        connectionProblem = false
        showAdminPrompt()
    }

    /**
     * Принимает подключение нового клиента.
     * @param key ключ, представляющий событие готовности к подключению.
     */
    @Throws(IOException::class)
    fun acceptClient(key: SelectionKey) {
        val server = key.channel() as ServerSocketChannel
        val clientChannel = server.accept()
        if (clientChannel == null) {
            LOGGER.warn("Соединение не было принято")
            return
        }
        clientChannel.configureBlocking(false)
        val clientKey = clientChannel.register(selector, SelectionKey.OP_READ)
        clientKey.attach(ClientState())
        LOGGER.info("Подключён клиент: {}", getRemoteAddress(clientChannel))
        showAdminPrompt()
    }

    /**
     * Закрывает соединение с клиентом.
     * @param clientChannel канал клиента.
     * @param key ключ события селектора.
     */
    fun closeClient(clientChannel: SocketChannel, key: SelectionKey) {
        try {
            clientChannel.close()
            key.cancel()
            LOGGER.info("Соединение с клиентом {} закрыто", getRemoteAddress(clientChannel))
        } catch (e: IOException) {
            LOGGER.error("Ошибка при закрытии клиентского соединения: {}", e.message)
        } finally {
            showAdminPrompt()
        }
    }

    fun finishWrite(key: SelectionKey) {
        synchronized(key) {
            key.attach(ClientState())
            key.interestOps(SelectionKey.OP_READ)
        }

        try {
            selector.wakeup()
        } catch (e: Exception) {
            LOGGER.warn("Не удалось разбудить селектор после finishWrite: {}", e.message)
        }
    }

    /**
     * Возвращает удалённый адрес клиента.
     * @param channel канал клиента.
     * @return строковое представление адреса или "unknown" при ошибке.
     */
    fun getRemoteAddress(channel: SocketChannel): String {
        return try {
            channel.remoteAddress.toString()
        } catch (e: IOException) {
            "unknown"
        }
    }

    /**
     * Читает запрос от клиента.
     * @param key ключ события селектора.
     */
    @Throws(IOException::class)
    fun readRequest(key: SelectionKey) {
        if (!key.isValid) {
            LOGGER.warn("Чтение из невалидного ключа")
            showAdminPrompt()
            return
        }
        val clientChannel = key.channel() as SocketChannel
        val state = key.attachment() as? ClientState
        if (state == null) {
            LOGGER.error("State не инициализирован для {}", getRemoteAddress(clientChannel))
            closeClient(clientChannel, key)
            return
        }

        val buffer = (state.dataBuffer ?: state.lengthBuffer)
        val bytesRead = clientChannel.read(buffer)
        if (bytesRead == -1) {
            LOGGER.info("Клиент {} отключился", getRemoteAddress(clientChannel))
            try {

                LOGGER.info("Коллекция сохранена после отключения клиента")
            } catch (e: Exception) {
                LOGGER.error("Ошибка сохранения коллекции после отключения: {}", e.message)
            }
            closeClient(clientChannel, key)
            return
        }

        if (buffer.hasRemaining()) {
            LOGGER.warn("Частичное чтение от {}", getRemoteAddress(clientChannel))
            showAdminPrompt()
            return
        }

        if (state.dataBuffer == null) {
            state.lengthBuffer.flip()
            state.dataLength = state.lengthBuffer.int
            state.lengthBuffer.clear()
            if (state.dataLength <= 0 || state.dataLength > MAX_DATA_LENGTH) {
                LOGGER.error("Неверный размер данных {} от {}", state.dataLength, getRemoteAddress(clientChannel))
                closeClient(clientChannel, key)
                return
            }
            state.dataBuffer = ByteBuffer.allocate(state.dataLength)
            LOGGER.info("Ожидаем данные длиной {} от {}", state.dataLength, getRemoteAddress(clientChannel))
            showAdminPrompt()
        } else {
            state.dataBuffer!!.flip()
            val bytes = ByteArray(state.dataLength)
            state.dataBuffer!!.get(bytes)
            state.dataBuffer = null
            state.dataLength = -1

            val readTask = Runnable {
                try {
                    ObjectInputStream(ByteArrayInputStream(bytes)).use { ois ->
                        val obj = ois.readObject()
                        if (obj !is Request) {
                            LOGGER.warn("Некорректный объект от клиента {}", getRemoteAddress(clientChannel))
                            key.attach(Response(false, "Неверный запрос"))
                            key.interestOps(SelectionKey.OP_WRITE)
                            return@Runnable
                        }

                        LOGGER.info("Запрос {} от {} в потоке {}", obj.type, getRemoteAddress(clientChannel), Thread.currentThread().id)

                        val commandTask = Callable<Response> {
                            LOGGER.info("Обработка команды {} в потоке {}", obj.type, Thread.currentThread().id)
                            try {
                                val response = dispatcher.dispatch(obj)
                                LOGGER.info("Команда {} успешно обработана", obj.type)
                                response
                            } catch (e: Exception) {
                                LOGGER.error("Ошибка обработки команды {}: {}", obj.type, e.message)
                                Response(false, "Ошибка обработки: ${e.message}")
                            }
                        }

                        val future = commandPool.submit(commandTask)
                        val response = future.get()

                        key.attach(response)
                        key.interestOps(SelectionKey.OP_WRITE)
                    }
                } catch (e: Exception) {
                    LOGGER.warn("Ошибка десериализации или обработки запроса: {}", e.message)
                    key.attach(Response(false, "Ошибка обработки данных: ${e.message}"))
                    key.interestOps(SelectionKey.OP_WRITE)
                }
            }


            commandPool.submit(readTask)

        }
    }

    /**
     * Отправляет ответ клиенту.
     * @param key ключ события селектора.
     *
     *
     *
     *
     *
     */

    @Throws(IOException::class)
    fun writeResponse(key: SelectionKey) {
        if (!key.isValid) {
            LOGGER.warn("Попытка записи в невалидный ключ")
            showAdminPrompt()
            return
        }

        val clientChannel = key.channel() as SocketChannel
        val attachment = key.attachment()
        if (attachment !is Response) {
            LOGGER.error("Нет ответа для клиента {}", getRemoteAddress(clientChannel))
            closeClient(clientChannel, key)
            return
        }

        synchronized(key) {
            val current = key.attachment()
            if (current === WritingMarker) {
                LOGGER.info("Запись для {} уже выполняется, пропускаем повторный запуск", getRemoteAddress(clientChannel))
                return
            }

            key.attach(WritingMarker)

            try {
                key.interestOps(key.interestOps() and SelectionKey.OP_WRITE.inv())
            } catch (e: Exception) {
                LOGGER.warn("Не удалось убрать OP_WRITE: {}", e.message)
            }
        }


        try {
            selector.wakeup()
        } catch (e: Exception) {
            LOGGER.warn("Не удалось разбудить селектор: {}", e.message)
        }


        val response = attachment as Response
        writePool.execute(WriteThread(clientChannel, key, response, this))
    }


    /**
     * Закрывает ресурсы сервера (каналы и селектор).
     */
    fun closeResources() {
        try {
            if (this::serverChannel.isInitialized && serverChannel.isOpen) {
                serverChannel.close()
            }
            if (this::selector.isInitialized && selector.isOpen) {
                selector.close()
            }
            commandPool.shutdown()
            try {
                if (!commandPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    commandPool.shutdownNow()
                    LOGGER.warn("Принудительное завершение commandPool")
                }
            } catch (e: InterruptedException) {
                commandPool.shutdownNow()
                Thread.currentThread().interrupt()
                LOGGER.error("Прерывание при завершении commandPool: {}", e.message)
            }
            writePool.shutdown()
            try {
                if (!writePool.awaitTermination(5, TimeUnit.SECONDS)) {
                    writePool.shutdownNow()
                    LOGGER.warn("Принудительное завершение writePool")
                }
            } catch (e: InterruptedException) {
                writePool.shutdownNow()
                Thread.currentThread().interrupt()
                LOGGER.error("Прерывание при завершении writePool: {}", e.message)
            }
            LOGGER.info("Ресурсы сервера закрыты")
        } catch (e: IOException) {
            LOGGER.error("Ошибка при закрытии ресурсов: {}", e.message)
        }
        connectionProblem = true
    }

    /**
     * Обрабатывает команды, введённые администратором в консоль.
     */
    fun processConsoleInput() {
        try {
            val scanner = Scanner(System.`in`)
            if (System.`in`.available() > 0) {
                showAdminPrompt()
                val line = scanner.nextLine().trim()
                if (line.isBlank()) {
                    print(ADMIN_PROMPT)
                    return
                }
                when {
                    line.equals("save", ignoreCase = true) -> {

                        LOGGER.info("Коллекция сохранена по админ-команде save")
                        println("Коллекция сохранена по админ-команде save")
                        showAdminPrompt()
                    }

                    line.equals("exit", ignoreCase = true) -> {

                        LOGGER.info("Коллекция сохранена по админ-команде exit — сервер завершается")
                        println("Коллекция сохранена по админ-команде exit — сервер завершается")
                        closeResources()
                        running = false
                    }

                    else -> {
                        if (line.isBlank()) print(ADMIN_PROMPT)
                        else {
                            LOGGER.warn("Неизвестная админ-команда: {}", line)
                            println("Неизвестная админ-команда: ${line}")
                            showAdminPrompt()
                        }
                    }
                }
            }
        } catch (e: IOException) {
            LOGGER.error("Ошибка чтения админ-команды: {}", e.message)
            showAdminPrompt()
        }
    }

    /**
     * Запускает основной цикл работы сервера.
     */
    fun run() {
        Runtime.getRuntime().addShutdownHook(Thread {
            try {

                LOGGER.info("Коллекция сохранена при завершении работы")
            } catch (e: Exception) {
                LOGGER.error("Коллекция не сохранена при завершении: {}", e.message)
            }
        })

        try {
            connection()
            while (running) {
                try {
                    val readyChannels = selector.select(500)
                    if (readyChannels > 0) {
                        val keys: MutableIterator<SelectionKey> = selector.selectedKeys().iterator()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            keys.remove()
                            when {
                                key.isAcceptable -> acceptClient(key)
                                key.isReadable -> readRequest(key)
                                key.isWritable -> writeResponse(key)
                            }
                        }
                    }
                    processConsoleInput()
                } catch (e: IOException) {
                    LOGGER.error("Ошибка в работе сервера: {}", e.message)
                    showAdminPrompt()
                } catch (e: ClassNotFoundException) {
                    LOGGER.error("Ошибка десериализации объекта: {}", e.message)
                    showAdminPrompt()
                }
            }
            closeResources()
            System.exit(0)
        } catch (e: IOException) {
            LOGGER.error("Ошибка инициализации сервера: {}", e.message)
        }
    }
}
