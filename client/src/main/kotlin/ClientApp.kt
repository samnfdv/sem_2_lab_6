//package org.example
//
//import AcountsManager
//import commands.CommandExecutor
//import org.apache.logging.log4j.LogManager
//import org.apache.logging.log4j.Logger
//import outerLayer.InputManager
//import outerLayer.OutputManager
//import java.io.*
//import java.net.InetSocketAddress
//import java.nio.ByteBuffer
//import java.nio.channels.SelectionKey
//import java.nio.channels.Selector
//import java.nio.channels.SocketChannel
//import java.util.*
//import java.util.concurrent.ConcurrentLinkedQueue
//import kotlin.concurrent.Volatile
//
//
//
///**
// * Класс клиентского приложения.
// * Обеспечивает подключение к серверу, обработку пользовательского ввода и обмен данными по протоколу NIO.
// *
// * @property console объект для вывода сообщений пользователю.
// * @property inputManager обработчик ввода команд и данных.
// * @property reader ридер для получения данных при создании объектов.
// */
//class ClientApp(
//    private val console: OutputManager,
//    private val inputManager: InputManager,
//    private val reader: Reader,
//) {
//    private val HOST = "localhost"
//    private val PORT = 1488
//    private val BUFFER_SIZE = 18192
//    private val RECONNECT_DELAY_MS = 1000
//    private val PROMPT = "[client] $ "
//
//    companion object IsWaitingForInput {
//        @Volatile
//        protected var isWaitingForInput = false
//
//        /**
//         * Проверяет, ожидает ли клиент пользовательского ввода.
//         * @return true если клиент в режиме ожидания ввода.
//         */
//        fun getIWFI(): Boolean {
//            return isWaitingForInput
//        }
//    }
//
//    private val LOGGER: Logger = LogManager.getLogger(ClientApp::class.java)
//
//    private val vehicleAdder: VehicleAdder = VehicleAdder(console, inputManager, reader)
//    private val acountsManager = AcountsManager(inputManager, console, reader)
//    //    private val builder: RequestBuilder = RequestBuilder(vehicleAdder, inputManager, console)
//    private val executor: CommandExecutor = CommandExecutor(vehicleAdder, console, inputManager, acountsManager)
//    private var socketChannel: SocketChannel? = null
//    private var selector: Selector? = null
//    private var isConnected = false
//    private var isWaitingForResponse = false
//    private var running = true
//    private var connectionProblem = true
//    private val requestQueue: Queue<Request> = ConcurrentLinkedQueue()
//    private val lengthBuffer: ByteBuffer = ByteBuffer.allocate(4)
//    private var dataBuffer: ByteBuffer? = null
//    private var dataLength = -1
//
//    /** Отображает клиентское приглашение */
//    private fun showPrompt() {
//        print(PROMPT)
//        System.out.flush()
//    }
//
//    /**
//     * Подключается к серверу.
//     * @throws IOException при ошибке соединения.
//     */
//    @Throws(IOException::class)
//    fun connect() {
//        socketChannel = SocketChannel.open()
//        socketChannel!!.configureBlocking(false)
//        socketChannel!!.connect(InetSocketAddress(HOST, PORT))
//
//        selector = Selector.open()
//        socketChannel!!.register(selector, SelectionKey.OP_CONNECT)
//        console.println("\"Попытка подключения к серверу $HOST:$PORT\"")
//        LOGGER.info("Попытка подключения к серверу {}:{}", HOST, PORT)
//    }
//
//    /**
//     * Запускает основной цикл клиентского приложения.
//     */
//    fun run() {
//        if (connectionProblem && !isConnected) {
//            try {
//                connect()
//            } catch (e: IOException) {
//                try {
//                    noConnectionHandler()
//                } catch (ex: IOException) {
//                    console.println("Ошибка подключения: ${ex.message}")
//                    LOGGER.error("Ошибка подключения: {}", ex.message)
//                    return
//                }
//            }
//        }
//
//        while (running) {
//            try {
//                val readyChannels = selector!!.select(100)
//                if (readyChannels == 0 && !socketChannel!!.isConnected) {
//                    noConnectionHandler()
//                }
//
//                if (readyChannels > 0) {
//                    val keys = selector!!.selectedKeys().iterator()
//                    while (keys.hasNext()) {
//                        val key = keys.next()
//                        keys.remove()
//                        when {
//                            key.isConnectable -> successConnect(key)
//                            key.isReadable -> read(key)
//                            key.isWritable -> write(key)
//                        }
//                    }
//                }
//                processConsoleInput()
//            } catch (e: IOException) {
//                try {
//                    noConnectionHandler()
//                } catch (ex: IOException) {
//                    console.println("Ошибка подключения: ${ex.message}")
//                    LOGGER.error("Ошибка подключения: {}", ex.message)
//                }
//            } catch (e: ClassNotFoundException) {
//                console.println("Ошибка десериализации ответа: ${e.message}")
//                LOGGER.error("Ошибка десериализации ответа: {}", e.message)
//            }
//        }
//        LOGGER.info("Клиент завершил работу")
//        console.println("Клиент завершил работу")
//        System.exit(0)
//    }
//
//    /**
//     * Обрабатывает ситуацию отсутствия соединения с сервером.
//     * @throws IOException при ошибке повторного подключения.
//     */
//    @Throws(IOException::class)
//    private fun noConnectionHandler() {
//        closeResources()
//        try {
//            console.println("Сервер недоступен. Повторная попытка через $RECONNECT_DELAY_MS мс...")
//            LOGGER.warn("Сервер недоступен. Повторная попытка через {} мс...", RECONNECT_DELAY_MS)
//            Thread.sleep(RECONNECT_DELAY_MS.toLong())
//            connect()
//            connectionProblem = true
//        } catch (ie: InterruptedException) {
//            Thread.currentThread().interrupt()
//        }
//    }
//
//    /**
//     * Обрабатывает успешное подключение клиента.
//     * @param key ключ события селектора.
//     */
//    @Throws(IOException::class)
//    private fun successConnect(key: SelectionKey) {
//        val channel = key.channel() as SocketChannel
//        if (channel.finishConnect()) {
//            channel.register(selector, SelectionKey.OP_READ)
//            console.println("Подключено к серверу: $HOST:$PORT")
//            LOGGER.info("Подключено к серверу: {}:{}", HOST, PORT)
//            isConnected = true
//            connectionProblem = false
//            showPrompt()
//        }
//    }
//
//    /**
//     * Читает ответ от сервера.
//     * @param key ключ события селектора.
//     */
//    @Throws(IOException::class, ClassNotFoundException::class)
//    private fun read(key: SelectionKey) {
//        val channel = key.channel() as SocketChannel
//        val buffer = dataBuffer ?: lengthBuffer
//        val bytesRead = channel.read(buffer)
//
//        if (bytesRead == -1) {
//            console.println("Соединение с сервером потеряно")
//            LOGGER.warn("Соединение с сервером потеряно")
//            noConnectionHandler()
//            isConnected = false
//            return
//        }
//
//        if (buffer.hasRemaining()) {
//            return
//        }
//
//        if (dataBuffer == null) {
//            lengthBuffer.flip()
//            dataLength = lengthBuffer.int
//            lengthBuffer.clear()
//            if (dataLength <= 0 || dataLength > BUFFER_SIZE) {
//                console.println("Некорректный размер ответа: $dataLength")
//                LOGGER.error("Некорректный размер ответа: {}", dataLength)
//                closeResources()
//                return
//            }
//            dataBuffer = ByteBuffer.allocate(dataLength)
//        } else {
//            dataBuffer!!.flip()
//            val data = ByteArray(dataLength)
//            dataBuffer!!.get(data)
//            dataBuffer = null
//            dataLength = -1
//
//            ByteArrayInputStream(data).use { bais ->
//                ObjectInputStream(bais).use { ois ->
//                    val response = ois.readObject() as Response
//
//                    response.message?.let {
//                        LOGGER.info("Ответ сервера: {}", it)
//                        console.println("Ответ сервера: ${it}")
//                    }
//
//                    if (response.message?.startsWith("Найден") == true) {
//                        isWaitingForInput = true
//                        val id = response.data as Long
//                        console.println("Сервер запросил обновление данных объекта с id=${id}")
//                        LOGGER.info("Сервер запросил обновление данных объекта с id={}", id)
//                        val v = vehicleAdder.vehiclePack()
//
//                        requestQueue.offer(Request(CommandType.UPDATE, arrayOf<Any>(id, v)))
//                        key.interestOps(SelectionKey.OP_WRITE)
//
//                        isWaitingForInput = false
//                    }
//                }
//            }
//
//            isWaitingForResponse = false
//            showPrompt()
//            key.interestOps(SelectionKey.OP_WRITE)
//        }
//    }
//
//    /**
//     * Отправляет запрос серверу.
//     * @param key ключ события селектора.
//     */
//    @Throws(IOException::class)
//    private fun write(key: SelectionKey) {
//        if (requestQueue.isEmpty()) {
//            key.interestOps(SelectionKey.OP_READ)
//            return
//        }
//
//        val channel = key.channel() as SocketChannel
//        val request = requestQueue.peek()
//        ByteArrayOutputStream().use { baos ->
//            ObjectOutputStream(baos).use { oos ->
//                oos.writeObject(request)
//                oos.flush()
//                val data = baos.toByteArray()
//
//                val lengthBuf = ByteBuffer.allocate(4).putInt(data.size)
//                lengthBuf.flip()
//                channel.write(lengthBuf)
//
//                val dataBuf = ByteBuffer.wrap(data)
//                channel.write(dataBuf)
//
//                requestQueue.poll()
//                isWaitingForResponse = true
//                console.println("Отправлен запрос: ${request?.type}")
//                LOGGER.info("Отправлен запрос: {}", request?.type)
//                key.interestOps(SelectionKey.OP_READ)
//            }
//        }
//    }
//
//    /**
//     * Обрабатывает пользовательский ввод в консоли клиента.
//     */
//    private fun processConsoleInput() {
//        if (isConnected && !isWaitingForResponse && !isWaitingForInput) {
//            try {
//                val scanner = Scanner(System.`in`)
//                if (System.`in`.available() > 0) {
//                    showPrompt()
////                    val line = scanner.nextLine().trim()
//                    val line = inputManager.read()
//                    if (line.isEmpty()) {
//                        return
//                    }
//
//                    if (line.equals("exit", ignoreCase = true)) {
//                        console.print("Клиент завершает работу по команде exit")
//                        LOGGER.info("Клиент завершает работу по команде exit")
//                        closeResources()
//                        running = false
//                        return
//                    }
//
//                    if (line.startsWith("save")) {
//                        console.println("Команда save не поддерживается клиентом")
//                        LOGGER.warn("Команда save не поддерживается клиентом")
//                        showPrompt()
//                        return
//                    }
//
//                    val req = executor.executeCommand(line) as Request?
//                    if (req != null) {
//                        console.println("Сформирован запрос: ${req.type}")
//                        LOGGER.info("Сформирован запрос: {}", req.type)
//                        requestQueue.offer(req)
//                        val key = socketChannel!!.keyFor(selector)
//                        if (key != null) key.interestOps(SelectionKey.OP_WRITE)
//                    } else {
//                        return
//                    }
//                }
//            } catch (e: IOException) {
//                console.println("Ошибка ввода: ${e.message}")
//                LOGGER.error("Ошибка ввода: {}", e.message)
//                showPrompt()
//            }catch (e:  exceptions.InvalidInputException) {
//                LOGGER.error("Ошибка ввода: {}", e.message)
//                inputManager.finishScriptRead()
//            }
//        }
//    }
//
//    /**
//     * Закрывает все ресурсы клиента (сокет и селектор).
//     */
//    private fun closeResources() {
//        try {
//            if (socketChannel != null && socketChannel!!.isOpen) {
//                socketChannel!!.close()
//            }
//            if (selector != null && selector!!.isOpen) {
//                selector!!.close()
//            }
//            console.println("Ресурсы клиента закрыты")
//            LOGGER.info("Ресурсы клиента закрыты")
//        } catch (e: IOException) {
//            console.println("Ошибка при закрытии ресурсов: ${e.message}")
//            LOGGER.error("Ошибка при закрытии ресурсов: {}", e.message)
//        }
//        isConnected = false
//        connectionProblem = true
//        requestQueue.clear()
//    }
//}
package org.example

import AcountsManager
import org.example.User
import commands.CommandExecutor
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import outerLayer.InputManager
import outerLayer.OutputManager
import java.io.*
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.Volatile

/**
 * Класс клиентского приложения.
 * Обеспечивает подключение к серверу, обработку пользовательского ввода и обмен данными по протоколу NIO.
 *
 * @property console объект для вывода сообщений пользователю.
 * @property inputManager обработчик ввода команд и данных.
 * @property reader ридер для получения данных при создании объектов.
 */
class ClientApp(
    private val console: OutputManager,
    private val inputManager: InputManager,
    private val reader: Reader,
) {
    private val HOST = "localhost"
    private val PORT = 1488
    private val BUFFER_SIZE = 18192
    private val RECONNECT_DELAY_MS = 1000
    private val PROMPT = "[client] $ "

    companion object IsWaitingForInput {
        @Volatile
        protected var isWaitingForInput = false
        var user: User = User() // Хранение текущего пользователя

        /**
         * Проверяет, ожидает ли клиент пользовательского ввода.
         * @return true если клиент в режиме ожидания ввода.
         */
        fun getIWFI(): Boolean {
            return isWaitingForInput
        }
    }

    private val LOGGER: Logger = LogManager.getLogger(ClientApp::class.java)

    private val vehicleAdder: VehicleAdder = VehicleAdder(console, inputManager, reader)
    private val acountsManager = AcountsManager(inputManager, console, reader)
    private val executor: CommandExecutor = CommandExecutor(vehicleAdder, console, inputManager, acountsManager)
    private var socketChannel: SocketChannel? = null
    private var selector: Selector? = null
    private var isConnected = false
    private var isWaitingForResponse = false
    private var running = true
    private var connectionProblem = true
    private val requestQueue: Queue<Request> = ConcurrentLinkedQueue()
    private val lengthBuffer: ByteBuffer = ByteBuffer.allocate(4)
    private var dataBuffer: ByteBuffer? = null
    private var dataLength = -1

    /** Отображает клиентское приглашение */
    private fun showPrompt() {
        print(PROMPT)
        System.out.flush()
    }

    /**
     * Выводит подсказку с доступными командами авторизации.
     */
    private fun advice() {
        val advice = """
            ===================================================
                       Вы не вошли в аккаунт
            ===================================================
            login <login> <password> : войти в аккаунт
            logout : выйти из аккаунта
            registration <login> <password> : создать аккаунт
            ===================================================
        """.trimIndent()
        console.println(advice)
        showPrompt()
    }

    /**
     * Подключается к серверу.
     * @throws IOException при ошибке соединения.
     */
    @Throws(IOException::class)
    fun connect() {
        socketChannel = SocketChannel.open()
        socketChannel!!.configureBlocking(false)
        socketChannel!!.connect(InetSocketAddress(HOST, PORT))

        selector = Selector.open()
        socketChannel!!.register(selector, SelectionKey.OP_CONNECT)
        console.println("Попытка подключения к серверу $HOST:$PORT")
        LOGGER.info("Попытка подключения к серверу {}:{}", HOST, PORT)
    }

    /**
     * Запускает основной цикл клиентского приложения.
     */
    fun run() {
        if (connectionProblem && !isConnected) {
            try {
                connect()
            } catch (e: IOException) {
                try {
                    noConnectionHandler()
                } catch (ex: IOException) {
                    console.println("Ошибка подключения: ${ex.message}")
                    LOGGER.error("Ошибка подключения: {}", ex.message)
                    return
                }
            }
        }

        while (running) {
            try {
                val readyChannels = selector!!.select(100)
                if (readyChannels == 0 && !socketChannel!!.isConnected) {
                    noConnectionHandler()
                }

                if (readyChannels > 0) {
                    val keys = selector!!.selectedKeys().iterator()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        keys.remove()
                        when {
                            key.isConnectable -> successConnect(key)
                            key.isReadable -> read(key)
                            key.isWritable -> write(key)
                        }
                    }
                }
                processConsoleInput()
            } catch (e: IOException) {
                try {
                    noConnectionHandler()
                } catch (ex: IOException) {
                    console.println("Ошибка подключения: ${ex.message}")
                    LOGGER.error("Ошибка подключения: {}", ex.message)
                }
            } catch (e: ClassNotFoundException) {
                console.println("Ошибка десериализации ответа: ${e.message}")
                LOGGER.error("Ошибка десериализации ответа: {}", e.message)
            }
        }
        LOGGER.info("Клиент завершил работу")
        console.println("Клиент завершил работу")
        System.exit(0)
    }

    /**
     * Обрабатывает ситуацию отсутствия соединения с сервером.
     * @throws IOException при ошибке повторного подключения.
     */
    @Throws(IOException::class)
    private fun noConnectionHandler() {
        closeResources()
        try {
            console.println("Сервер недоступен. Повторная попытка через $RECONNECT_DELAY_MS мс...")
            LOGGER.warn("Сервер недоступен. Повторная попытка через {} мс...", RECONNECT_DELAY_MS)
            Thread.sleep(RECONNECT_DELAY_MS.toLong())
            connect()
            connectionProblem = true
        } catch (ie: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    /**
     * Обрабатывает успешное подключение клиента.
     * @param key ключ события селектора.
     */
    @Throws(IOException::class)
    private fun successConnect(key: SelectionKey) {
        val channel = key.channel() as SocketChannel
        if (channel.finishConnect()) {
            channel.register(selector, SelectionKey.OP_READ)
            console.println("Подключено к серверу: $HOST:$PORT")
            LOGGER.info("Подключено к серверу: {}:{}", HOST, PORT)
            isConnected = true
            connectionProblem = false
            if (user.login == null) {
                advice()
            } else {
                console.println("С возвращением ${user.login}!")
            }
            showPrompt()
        }
    }

    /**
     * Читает ответ от сервера.
     * @param key ключ события селектора.
     */
    @Throws(IOException::class, ClassNotFoundException::class)
    private fun read(key: SelectionKey) {
        val channel = key.channel() as SocketChannel
        val buffer = dataBuffer ?: lengthBuffer
        val bytesRead = channel.read(buffer)

        if (bytesRead == -1) {
            console.println("Соединение с сервером потеряно")
            LOGGER.warn("Соединение с сервером потеряно")
            noConnectionHandler()
            isConnected = false
            return
        }

        if (buffer.hasRemaining()) {
            return
        }

        if (dataBuffer == null) {
            lengthBuffer.flip()
            dataLength = lengthBuffer.int
            lengthBuffer.clear()
            if (dataLength <= 0 || dataLength > BUFFER_SIZE) {
                console.println("Некорректный размер ответа: $dataLength")
                LOGGER.error("Некорректный размер ответа: {}", dataLength)
                closeResources()
                return
            }
            dataBuffer = ByteBuffer.allocate(dataLength)
        } else {
            dataBuffer!!.flip()
            val data = ByteArray(dataLength)
            dataBuffer!!.get(data)
            dataBuffer = null
            dataLength = -1

            ByteArrayInputStream(data).use { bais ->
                ObjectInputStream(bais).use { ois ->
                    val response = ois.readObject() as Response

                    response.message?.let {
                        LOGGER.info("Ответ сервера: {}", it)
                        console.println("Ответ сервера: ${it}")
                    }

                    // Обработка ответа login
                    if (response.message?.startsWith("Приветствую") == true) {
                        val login = response.data as String?
                        if (login != null) {
                            user.login = login
//                            println(login)
                            user.isExists = true
                            console.println("Успешно вошли как $login")
                            LOGGER.info("Успешно вошли как {}", login)
                        }
                    }

                    // Обработка команды update
                    if (response.message?.startsWith("Найден") == true) {
                        isWaitingForInput = true
//                        if (response.data !is Long) {
//                            console.println("Ошибка неверный аргумент")
//                            return
//                        }
                        val inner = response.data as Array<*>
                        val id = inner[0] as Long
                        console.println("Сервер запросил обновление данных объекта с id=$id")
                        LOGGER.info("Сервер запросил обновление данных объекта с id={}", id)
                        val v = vehicleAdder.vehiclePack()

                        requestQueue.offer(Request(CommandType.UPDATE, arrayOf<Any>(id,  v)))
                        key.interestOps(SelectionKey.OP_WRITE)

                        isWaitingForInput = false
                    }
                }
            }

            isWaitingForResponse = false
            showPrompt()
            key.interestOps(SelectionKey.OP_WRITE)
        }
    }

    /**
     * Отправляет запрос серверу.
     * @param key ключ события селектора.
     */
    @Throws(IOException::class)
    private fun write(key: SelectionKey) {
        if (requestQueue.isEmpty()) {
            key.interestOps(SelectionKey.OP_READ)
            return
        }

        val channel = key.channel() as SocketChannel
        val request = requestQueue.peek()
        ByteArrayOutputStream().use { baos ->
            ObjectOutputStream(baos).use { oos ->
                oos.writeObject(request)
                oos.flush()
                val data = baos.toByteArray()

                val lengthBuf = ByteBuffer.allocate(4).putInt(data.size)
                lengthBuf.flip()
                channel.write(lengthBuf)

                val dataBuf = ByteBuffer.wrap(data)
                channel.write(dataBuf)

                requestQueue.poll()
                isWaitingForResponse = true
                console.println("Отправлен запрос: ${request?.type}")
                LOGGER.info("Отправлен запрос: {}", request?.type)
                key.interestOps(SelectionKey.OP_READ)
            }
        }
    }

    /**
     * Обрабатывает пользовательский ввод в консоли клиента.
     */
    private fun processConsoleInput() {
        if (isConnected && !isWaitingForResponse && !isWaitingForInput) {
            try {
                val scanner = Scanner(System.`in`)
                if (System.`in`.available() > 0) {
                    showPrompt()
                    val line = inputManager.read()
                    if (line.isEmpty()) {
                        return
                    }

                    if (line.equals("exit", ignoreCase = true)) {
                        console.println("Клиент завершает работу по команде exit")
                        LOGGER.info("Клиент завершает работу по команде exit")
                        closeResources()
                        running = false
                        return
                    }

                    if (line.equals("logout", ignoreCase = true)) {
                        if (user.login != null) {
                            user.login = null
                            user.password = null
                            user.isExists = false
                            console.println("Вы покинули аккаунт.")
                            LOGGER.info("Пользователь вышел из аккаунта")
                            advice()
                        } else {
                            console.println("Вы еще не вошли в аккаунт, чтобы его покидать.")
                            LOGGER.warn("Попытка выхода без авторизации")
                        }
                        return
                    }

                    if (line.startsWith("registration")) {
                        val parts = line.split("\\s+".toRegex())
//                        if (parts.size != 3) {
//                            console.println("Нужно: registration <login> <password>")
//                            LOGGER.warn("Неверный формат команды registration: {}", line)
//                            showPrompt()
//                            return
//                        }
                        val us = acountsManager.registration()
                        val login = us[0]
                        val password = us[1]
                        val req = Request(CommandType.REGISTRATION, us)
                        console.println("Сформирован запрос: ${req.type}")
                        LOGGER.info("Сформирован запрос: {}", req.type)
                        requestQueue.offer(req)
                        val key = socketChannel!!.keyFor(selector)
                        if (key != null) key.interestOps(SelectionKey.OP_WRITE)
                        return
                    }

                    val req = executor.executeCommand(line) as Request?
                    if (req != null) {
                        console.println("Сформирован запрос: ${req.type}")
                        LOGGER.info("Сформирован запрос: {}", req.type)
                        requestQueue.offer(req)
                        val key = socketChannel!!.keyFor(selector)
                        if (key != null) key.interestOps(SelectionKey.OP_WRITE)
                    } else {
                        return
                    }
                }
            } catch (e: IOException) {
                console.println("Ошибка ввода: ${e.message}")
                LOGGER.error("Ошибка ввода: {}", e.message)
                showPrompt()
            } catch (e: exceptions.InvalidInputException) {
                console.println("Ошибка ввода: ${e.message}")
                LOGGER.error("Ошибка ввода: {}", e.message)
                inputManager.finishScriptRead()
            }
        }
    }

    /**
     * Закрывает все ресурсы клиента (сокет и селектор).
     */
    private fun closeResources() {
        try {
            if (socketChannel != null && socketChannel!!.isOpen) {
                socketChannel!!.close()
            }
            if (selector != null && selector!!.isOpen) {
                selector!!.close()
            }
            console.println("Ресурсы клиента закрыты")
            LOGGER.info("Ресурсы клиента закрыты")
        } catch (e: IOException) {
            console.println("Ошибка при закрытии ресурсов: ${e.message}")
            LOGGER.error("Ошибка при закрытии ресурсов: {}", e.message)
        }
        isConnected = false
        connectionProblem = true
        requestQueue.clear()
    }
}