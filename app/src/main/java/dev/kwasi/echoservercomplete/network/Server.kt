@file:OptIn(ExperimentalEncodingApi::class)

package dev.kwasi.echoservercomplete.network

import android.util.Log
import com.google.gson.Gson
import dev.kwasi.echoservercomplete.models.ContentModel
import java.io.BufferedWriter
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.Buffer
import kotlin.Exception
import kotlin.concurrent.thread
import java.security.MessageDigest
import kotlin.text.Charsets.UTF_8
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.SecretKey
import javax.crypto.Cipher
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

fun ByteArray.toHex() = joinToString(separator = ""){ byte: Byte -> "%02x".format(byte) }

fun getFirstNChars(str: String, n: Int) = str.substring(0,n)

fun hashStrSha256(str: String): String{
    val algorithm = "SHA-256"
    val hashedString = MessageDigest.getInstance(algorithm).digest(str.toByteArray(UTF_8))
    return hashedString.toHex();
}

fun generateAESKey(seed: String): SecretKeySpec {
    val first32Chars = getFirstNChars(seed,32)
    val secretKey = SecretKeySpec(first32Chars.toByteArray(), "AES")
    return secretKey
}

@OptIn(ExperimentalEncodingApi::class)
fun encryptMessage(plaintext: String, aesKey:SecretKey, aesIv: IvParameterSpec):String{
    val plainTextByteArr = plaintext.toByteArray()

    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    cipher.init(Cipher.ENCRYPT_MODE, aesKey, aesIv)

    val encrypt = cipher.doFinal(plainTextByteArr)
    return Base64.Default.encode(encrypt)
}

fun decryptMessage(encryptedText: String, aesKey:SecretKey, aesIv: IvParameterSpec):String{
    val textToDecrypt = Base64.Default.decode(encryptedText)

    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")

    cipher.init(Cipher.DECRYPT_MODE, aesKey,aesIv)

    val decrypt = cipher.doFinal(textToDecrypt)
    return String(decrypt)

}

/// The [Server] class has all the functionality that is responsible for the 'server' connection.
/// This is implemented using TCP. This Server class is intended to be run on the GO.

class Server(private val iFaceImpl:NetworkMessageInterface) {
    companion object {
        const val PORT: Int = 9999

    }

    private val svrSocket: ServerSocket = ServerSocket(PORT, 0, InetAddress.getByName("192.168.49.1"))
    private val clientMap: HashMap<String, Socket> = HashMap()
//    private val serverContent: ContentModel
//    private lateinit var writer: BufferedWriter
    private val studentIds = arrayOf("816117992","816035550")
    private var tempStudent  = ""

    init {
        thread{
            while(true){
                try{
                    val clientConnectionSocket = svrSocket.accept()
                    Log.e("SERVER", "The server has accepted a connection: ")
                    handleSocket(clientConnectionSocket)
//                    sendMessage(content: ContentModel)

                }catch (e: Exception){
                    Log.e("SERVER", "An error has occurred in the server!")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun handleSocket(socket: Socket){
        socket.inetAddress.hostAddress?.let {
            clientMap[it] = socket
            Log.e("SERVER", "A new connection has been detected!")
            thread {
                val clientReader = socket.inputStream.bufferedReader()
                val clientWriter = socket.outputStream.bufferedWriter()
                var receivedJson: String?

                while(socket.isConnected){
                    try{
                        receivedJson = clientReader.readLine()
                            if (receivedJson!= null){
                                Log.e("SERVER", "Received a message from client $it")
                                val clientContent = Gson().fromJson(receivedJson, ContentModel::class.java)
                                if(clientContent.message == "I am here"){
                                    val clientNonce = (0..1000000).random()
                                    val challengeMsg = ContentModel(clientNonce.toString(), "192.168.49.1")
                                    val chalStr = Gson().toJson(challengeMsg)

                                    clientWriter.write("$chalStr\n")
                                    clientWriter.flush()

                                    // To show the correct alignment of the items (on the server), I'd swap the IP that it came from the client
                                    // This is some OP hax that gets the job done but is not the best way of getting it done.
                                    val tmpIp = clientContent.senderIp
                                    clientContent.senderIp = challengeMsg.senderIp
                                    challengeMsg.senderIp = tmpIp

                                    iFaceImpl.onContent(clientContent)
                                    iFaceImpl.onContent(challengeMsg)

                                    for (student in studentIds){
                                        val studentHash = hashStrSha256(student)
                                        val studentKey = generateAESKey(studentHash)
                                        val studentIv = IvParameterSpec(studentHash.toByteArray())
                                        val challengeNonce = challengeMsg.message.toString()
                                        val checkMsg = decryptMessage(clientContent.message, studentKey, studentIv)

                                        if(challengeNonce == checkMsg){
                                            tempStudent = student
                                            val displayMsg = ContentModel(checkMsg, "192.168.49.1")
                                        }else{
                                            break
                                        }

                                        if (hashStrSha256(student) == clientContent.message){
                                            tempStudent = student
                                            break
                                        }
                                    }
                                }
                            }
                    } catch (e: Exception){
                        Log.e("SERVER", "An error has occurred with the client $it")
                        e.printStackTrace()
                    }
                }
            }
        }
    }


    fun close(){
        svrSocket.close()
        clientMap.clear()
    }

    fun sendMessage(content: ContentModel) {
        thread {
            val connection = svrSocket.accept()
            if(!connection.isConnected){
                throw Exception("We aren't currently connected to the server!")
            }
            val writer = connection.outputStream.bufferedWriter()
            val contentAsStr: String? = Gson().toJson(content)
            writer.write("$contentAsStr\n")
            writer.flush()

            val clientContent = Gson().fromJson(tempStudent, ContentModel::class.java)
            val moddedContent = ContentModel(clientContent.message, "192.168.49.1")


            val tmpIp = clientContent.senderIp
            clientContent.senderIp = moddedContent.senderIp
            moddedContent.senderIp = tmpIp

            iFaceImpl.onContent(clientContent)
            iFaceImpl.onContent(moddedContent)
        }
    }

}